package com.biblioteca.salomao.web;

import com.biblioteca.salomao.domain.MentalMap;
import com.biblioteca.salomao.security.CustomUserDetails;
import com.biblioteca.salomao.service.LibraryService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/mapas")
public class MindMapController extends BaseController {

    private final LibraryService library;

    public MindMapController(LibraryService library) {
        this.library = library;
    }

    @GetMapping
    public String listar(@AuthenticationPrincipal CustomUserDetails principal,
                         @RequestParam(required = false) String q,
                         @RequestParam(defaultValue = "0") int pagina,
                         Model model) {
        var page = library.listMaps(userId(principal), q,
                PageRequest.of(pagina, 12, Sort.by("updatedAt").descending()));
        model.addAttribute("itens", page);
        model.addAttribute("q", q);
        model.addAttribute("titulo", "Mapas mentais");
        return "mapas/lista";
    }

    @PostMapping
    public String criar(@AuthenticationPrincipal CustomUserDetails principal,
                        @RequestParam String title,
                        @RequestParam(required = false) String description,
                        RedirectAttributes redirect) {
        MentalMap m = library.saveMap(userId(principal), null, title, description);
        return "redirect:/mapas/" + m.getId();
    }

    @GetMapping("/{id}")
    public String editor(@AuthenticationPrincipal CustomUserDetails principal,
                         @PathVariable UUID id, Model model) {
        var uid = userId(principal);
        MentalMap m = library.getMap(uid, id);
        model.addAttribute("mapa", m);
        // opcoes (id + nome) para vincular nos a entidades reais da biblioteca
        model.addAttribute("refs", library.mapRefs(uid));
        model.addAttribute("titulo", m.getTitle());
        return "mapas/editor";
    }

    @PostMapping("/{id}")
    public String renomear(@AuthenticationPrincipal CustomUserDetails principal,
                           @PathVariable UUID id,
                           @RequestParam String title,
                           @RequestParam(required = false) String description,
                           RedirectAttributes redirect) {
        library.saveMap(userId(principal), id, title, description);
        redirect.addFlashAttribute("sucesso", "Mapa atualizado.");
        return "redirect:/mapas/" + id;
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@AuthenticationPrincipal CustomUserDetails principal,
                          @PathVariable UUID id, RedirectAttributes redirect) {
        library.deleteMap(userId(principal), id);
        redirect.addFlashAttribute("sucesso", "Mapa excluído.");
        return "redirect:/mapas";
    }

    /** Grafo atual (nós + arestas) para o editor interativo. */
    @GetMapping("/{id}/grafo")
    @ResponseBody
    public Map<String, Object> grafo(@AuthenticationPrincipal CustomUserDetails principal,
                                     @PathVariable UUID id) {
        MentalMap m = library.getMap(userId(principal), id);
        List<Map<String, Object>> nos = new ArrayList<>();
        m.getNodes().forEach(n -> {
            Map<String, Object> o = new LinkedHashMap<>();
            o.put("id", n.getId().toString());
            o.put("nodeType", n.getNodeType().name());
            o.put("label", n.getLabel());
            o.put("content", n.getContent());
            o.put("refType", n.getRefType());
            o.put("refId", n.getRefId() == null ? null : n.getRefId().toString());
            o.put("x", n.getPosX());
            o.put("y", n.getPosY());
            o.put("color", n.getColor());
            o.put("collapsed", n.isCollapsed());
            nos.add(o);
        });
        List<Map<String, Object>> arestas = new ArrayList<>();
        m.getEdges().forEach(e -> {
            Map<String, Object> o = new LinkedHashMap<>();
            o.put("id", e.getId().toString());
            o.put("sourceId", e.getSource().getId().toString());
            o.put("targetId", e.getTarget().getId().toString());
            o.put("label", e.getLabel());
            arestas.add(o);
        });
        return Map.of("nos", nos, "arestas", arestas);
    }

    /** Salvamento do grafo (chamado pelo autosave do editor). */
    @PutMapping("/{id}/grafo")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> salvarGrafo(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id,
            @RequestBody Map<String, Object> body) {
        try {
            List<LibraryService.NodeDto> nos = parseNos(body.get("nos"));
            List<LibraryService.EdgeDto> arestas = parseArestas(body.get("arestas"));
            library.saveGraph(userId(principal), id, nos, arestas);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (org.springframework.web.server.ResponseStatusException e) {
            throw e; // 404 de isolamento e 401 passam intactos
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("ok", false,
                    "erro", "Não foi possível salvar o mapa. Tente novamente."));
        }
    }

    @SuppressWarnings("unchecked")
    private List<LibraryService.NodeDto> parseNos(Object raw) {
        List<LibraryService.NodeDto> out = new ArrayList<>();
        if (!(raw instanceof List<?> list)) return out;
        for (Object o : list) {
            if (!(o instanceof Map<?, ?> m)) continue;
            Map<String, Object> mm = (Map<String, Object>) m;
            out.add(new LibraryService.NodeDto(
                    uuid(mm.get("id")), str(mm.get("nodeType"), "TEXTO"),
                    str(mm.get("label"), "Nó"), str(mm.get("content"), ""),
                    strOrNull(mm.get("refType")), uuidOrNull(mm.get("refId")),
                    num(mm.get("x")), num(mm.get("y")),
                    str(mm.get("color"), ""), Boolean.TRUE.equals(mm.get("collapsed"))));
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private List<LibraryService.EdgeDto> parseArestas(Object raw) {
        List<LibraryService.EdgeDto> out = new ArrayList<>();
        if (!(raw instanceof List<?> list)) return out;
        for (Object o : list) {
            if (!(o instanceof Map<?, ?> m)) continue;
            Map<String, Object> mm = (Map<String, Object>) m;
            UUID s = uuidOrNull(mm.get("sourceId"));
            UUID t = uuidOrNull(mm.get("targetId"));
            if (s == null || t == null) continue;
            out.add(new LibraryService.EdgeDto(uuid(mm.get("id")), s, t, str(mm.get("label"), "")));
        }
        return out;
    }

    private String str(Object v, String fb) { return v == null ? fb : String.valueOf(v); }
    private String strOrNull(Object v) {
        if (v == null) return null;
        String s = String.valueOf(v);
        return s.isBlank() ? null : s;
    }
    private double num(Object v) {
        if (v instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(String.valueOf(v)); } catch (Exception e) { return 0; }
    }
    private UUID uuid(Object v) {
        UUID u = uuidOrNull(v);
        return u == null ? UUID.randomUUID() : u;
    }
    private UUID uuidOrNull(Object v) {
        if (v == null) return null;
        try { return UUID.fromString(String.valueOf(v)); } catch (Exception e) { return null; }
    }
}
