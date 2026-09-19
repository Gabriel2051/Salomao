package com.biblioteca.salomao.web;

import com.biblioteca.salomao.domain.Story;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/historias")
public class StoryController extends BaseController {

    private final LibraryService library;

    public StoryController(LibraryService library) {
        this.library = library;
    }

    @GetMapping
    public String listar(@AuthenticationPrincipal CustomUserDetails principal,
                         @RequestParam(required = false) String q,
                         @RequestParam(required = false) Boolean arquivadas,
                         @RequestParam(defaultValue = "0") int pagina,
                         @RequestParam(defaultValue = "grade") String visao,
                         Model model) {
        var page = library.listStories(userId(principal), q, arquivadas,
                PageRequest.of(pagina, 12, Sort.by("updatedAt").descending()));
        model.addAttribute("itens", page);
        model.addAttribute("q", q);
        model.addAttribute("arquivadas", arquivadas);
        model.addAttribute("visao", visao);
        model.addAttribute("titulo", "Histórias");
        return "historias/lista";
    }

    @GetMapping("/nova")
    public String nova(Model model) {
        model.addAttribute("historia", new Story());
        model.addAttribute("titulo", "Nova história");
        return "historias/editor";
    }

    @PostMapping
    public String criar(@AuthenticationPrincipal CustomUserDetails principal,
                        @RequestParam String title,
                        @RequestParam(required = false) String subtitle,
                        @RequestParam String content,
                        @RequestParam(required = false) String tags,
                        @RequestParam(required = false, defaultValue = "RASCUNHO") String status,
                        @RequestParam(required = false) List<UUID> mencoes,
                        RedirectAttributes redirect) {
        Story s = library.saveStory(userId(principal), null, title, subtitle, content, tags,
                status, false, false, mencoes);
        redirect.addFlashAttribute("sucesso", "História salva.");
        return "redirect:/historias/" + s.getId();
    }

    @GetMapping("/{id}")
    public String ver(@AuthenticationPrincipal CustomUserDetails principal,
                      @PathVariable UUID id, Model model) {
        model.addAttribute("historia", library.getStory(userId(principal), id));
        model.addAttribute("statusValores", Story.Status.values());
        model.addAttribute("titulo", "História");
        return "historias/editor";
    }

    @PostMapping("/{id}")
    public String atualizar(@AuthenticationPrincipal CustomUserDetails principal,
                            @PathVariable UUID id,
                            @RequestParam String title,
                            @RequestParam(required = false) String subtitle,
                            @RequestParam String content,
                            @RequestParam(required = false) String tags,
                            @RequestParam(required = false, defaultValue = "RASCUNHO") String status,
                            @RequestParam(defaultValue = "false") boolean favorite,
                            @RequestParam(defaultValue = "false") boolean archived,
                            @RequestParam(required = false) List<UUID> mencoes,
                            RedirectAttributes redirect) {
        library.saveStory(userId(principal), id, title, subtitle, content, tags, status,
                favorite, archived, mencoes);
        redirect.addFlashAttribute("sucesso", "Alterações salvas.");
        return "redirect:/historias/" + id;
    }

    @PutMapping("/{id}/autosave")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> autosave(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id,
            @RequestBody Map<String, Object> body) {
        try {
            Story atual = library.getStory(userId(principal), id);
            @SuppressWarnings("unchecked")
            List<String> mids = (List<String>) body.getOrDefault("mencoes", List.of());
            List<UUID> mentionIds = new ArrayList<>();
            for (String m : mids) {
                try { mentionIds.add(UUID.fromString(m)); } catch (Exception ignored) {}
            }
            library.saveStory(userId(principal), id,
                    str(body.get("title"), atual.getTitle()),
                    str(body.get("subtitle"), atual.getSubtitle()),
                    str(body.get("content"), atual.getContent()),
                    str(body.get("tags"), atual.getTagsCsv()),
                    str(body.get("status"), atual.getStatus().name()),
                    atual.isFavorite(), atual.isArchived(), mentionIds);
            return ResponseEntity.ok(Map.of("ok", true, "quando", java.time.Instant.now().toString()));
        } catch (org.springframework.web.server.ResponseStatusException e) {
            throw e; // 404 de isolamento e 401 passam intactos
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("ok", false,
                    "erro", "Não foi possível salvar suas alterações. Tente novamente."));
        }
    }

    private String str(Object v, String fallback) {
        return v == null ? fallback : String.valueOf(v);
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@AuthenticationPrincipal CustomUserDetails principal,
                          @PathVariable UUID id, RedirectAttributes redirect) {
        library.deleteStory(userId(principal), id);
        redirect.addFlashAttribute("sucesso", "História excluída.");
        return "redirect:/historias";
    }
}
