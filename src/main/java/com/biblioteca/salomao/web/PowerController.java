package com.biblioteca.salomao.web;

import com.biblioteca.salomao.domain.Power;
import com.biblioteca.salomao.security.CustomUserDetails;
import com.biblioteca.salomao.service.LibraryService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/poderes")
public class PowerController extends BaseController {

    private final LibraryService library;

    public PowerController(LibraryService library) {
        this.library = library;
    }

    @GetMapping
    public String listar(@AuthenticationPrincipal CustomUserDetails principal,
                         @RequestParam(required = false) String q,
                         @RequestParam(defaultValue = "0") int pagina,
                         Model model) {
        var page = library.listPowers(userId(principal), q,
                PageRequest.of(pagina, 18, Sort.by("name").ascending()));
        model.addAttribute("itens", page);
        model.addAttribute("q", q);
        model.addAttribute("titulo", "Poderes");
        return "poderes/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("poder", new Power());
        model.addAttribute("sugestoesCategoria", Suggestions.CATEGORIAS_PODER);
        model.addAttribute("sugestoesNivel", Suggestions.NIVEIS_PODER);
        model.addAttribute("titulo", "Novo poder");
        return "poderes/form";
    }

    @PostMapping
    public String criar(@AuthenticationPrincipal CustomUserDetails principal,
                        @RequestParam String name,
                        @RequestParam(required = false) String description,
                        @RequestParam(required = false) String category,
                        @RequestParam(required = false) String level,
                        @RequestParam(required = false) String limitations,
                        @RequestParam(required = false) String weaknesses,
                        @RequestParam(required = false) String origin,
                        @RequestParam(required = false) String observations,
                        RedirectAttributes redirect) {
        Power p = library.savePower(userId(principal), null, name, description, category, level,
                limitations, weaknesses, origin, observations);
        redirect.addFlashAttribute("sucesso", "Poder criado.");
        return "redirect:/poderes/" + p.getId();
    }

    @GetMapping("/{id}")
    public String ver(@AuthenticationPrincipal CustomUserDetails principal,
                      @PathVariable UUID id, Model model) {
        var uid = userId(principal);
        model.addAttribute("poder", library.getPower(uid, id));
        model.addAttribute("personagens", library.charactersOfPower(uid, id));
        model.addAttribute("titulo", "Poder");
        return "poderes/detalhe";
    }

    @GetMapping("/{id}/editar")
    public String editar(@AuthenticationPrincipal CustomUserDetails principal,
                         @PathVariable UUID id, Model model) {
        model.addAttribute("poder", library.getPower(userId(principal), id));
        model.addAttribute("sugestoesCategoria", Suggestions.CATEGORIAS_PODER);
        model.addAttribute("sugestoesNivel", Suggestions.NIVEIS_PODER);
        model.addAttribute("titulo", "Editar poder");
        return "poderes/form";
    }

    @PostMapping("/{id}")
    public String atualizar(@AuthenticationPrincipal CustomUserDetails principal,
                            @PathVariable UUID id,
                            @RequestParam String name,
                            @RequestParam(required = false) String description,
                            @RequestParam(required = false) String category,
                            @RequestParam(required = false) String level,
                            @RequestParam(required = false) String limitations,
                            @RequestParam(required = false) String weaknesses,
                            @RequestParam(required = false) String origin,
                            @RequestParam(required = false) String observations,
                            RedirectAttributes redirect) {
        library.savePower(userId(principal), id, name, description, category, level,
                limitations, weaknesses, origin, observations);
        redirect.addFlashAttribute("sucesso", "Alterações salvas.");
        return "redirect:/poderes/" + id;
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@AuthenticationPrincipal CustomUserDetails principal,
                          @PathVariable UUID id, RedirectAttributes redirect) {
        library.deletePower(userId(principal), id);
        redirect.addFlashAttribute("sucesso", "Poder excluído.");
        return "redirect:/poderes";
    }

    /** Detalhe em JSON para o modal/painel lateral dos tokens de poder. */
    @GetMapping("/api/{id}")
    @ResponseBody
    public Map<String, Object> detalheJson(@AuthenticationPrincipal CustomUserDetails principal,
                                           @PathVariable UUID id) {
        var uid = userId(principal);
        Power p = library.getPower(uid, id);
        List<Map<String, String>> chars = library.charactersOfPower(uid, id).stream()
                .map(c -> Map.of("id", c.getId().toString(), "nome", c.getFullName()))
                .collect(Collectors.toList());
        return Map.of(
                "id", p.getId().toString(),
                "nome", p.getName(),
                "descricao", p.getDescription() == null ? "" : p.getDescription(),
                "categoria", p.getCategory() == null ? "" : p.getCategory(),
                "nivel", p.getLevel() == null ? "" : p.getLevel(),
                "personagens", chars,
                "url", "/poderes/" + p.getId());
    }

    @GetMapping("/api/busca")
    @ResponseBody
    public List<Map<String, String>> buscar(@AuthenticationPrincipal CustomUserDetails principal,
                                            @RequestParam String q) {
        var page = library.listPowers(userId(principal), q, PageRequest.of(0, 8));
        return page.getContent().stream()
                .map(p -> Map.of("id", p.getId().toString(), "nome", p.getName()))
                .collect(Collectors.toList());
    }
}
