package com.biblioteca.salomao.web;

import com.biblioteca.salomao.domain.CatalogItem;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Catálogo do universo: armas, trajes, feitiços, materiais, artefatos…
 * Itens "físicos/conceituais" que podem ganhar aparência, história, efeitos
 * e ser vinculados a um personagem portador.
 */
@Controller
@RequestMapping("/catalogo")
public class CatalogController extends BaseController {

    private final LibraryService library;

    public CatalogController(LibraryService library) {
        this.library = library;
    }

    @GetMapping
    public String listar(@AuthenticationPrincipal CustomUserDetails principal,
                         @RequestParam(required = false) String q,
                         @RequestParam(required = false) String tipo,
                         @RequestParam(required = false) Boolean arquivados,
                         @RequestParam(defaultValue = "0") int pagina,
                         Model model) {
        var uid = userId(principal);
        var page = library.listCatalog(uid, q, tipo, arquivados,
                PageRequest.of(pagina, 18, Sort.by("updatedAt").descending()));
        model.addAttribute("itens", page);
        model.addAttribute("q", q);
        model.addAttribute("tipo", tipo);
        model.addAttribute("arquivados", arquivados);
        model.addAttribute("tipos", library.catalogKinds(uid));
        model.addAttribute("titulo", "Catálogo");
        return "catalogo/lista";
    }

    @GetMapping("/novo")
    public String novo(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        prepararForm(principal, model, new CatalogItem(), "Novo item");
        return "catalogo/form";
    }

    @PostMapping
    public String criar(@AuthenticationPrincipal CustomUserDetails principal,
                        @RequestParam String name,
                        @RequestParam(required = false) String kind,
                        @RequestParam(required = false) String summary,
                        @RequestParam(required = false) String appearance,
                        @RequestParam(required = false) String lore,
                        @RequestParam(required = false) String effects,
                        @RequestParam(required = false) String material,
                        @RequestParam(required = false) String rarity,
                        @RequestParam(required = false) UUID ownerId,
                        @RequestParam(required = false) String tags,
                        RedirectAttributes redirect) {
        try {
            CatalogItem i = library.saveCatalogItem(userId(principal), null, name, kind, summary,
                    appearance, lore, effects, material, rarity, ownerId, tags, false, false);
            redirect.addFlashAttribute("sucesso", "Item adicionado ao catálogo.");
            return "redirect:/catalogo/" + i.getId();
        } catch (org.springframework.web.server.ResponseStatusException e) {
            throw e; // 404 de isolamento passa intacto
        } catch (Exception e) {
            redirect.addFlashAttribute("erro", "Não foi possível salvar o item. Revise os campos.");
            return "redirect:/catalogo/novo";
        }
    }

    @GetMapping("/{id}")
    public String ver(@AuthenticationPrincipal CustomUserDetails principal,
                      @PathVariable UUID id, Model model) {
        model.addAttribute("item", library.getCatalogItem(userId(principal), id));
        model.addAttribute("titulo", "Item");
        return "catalogo/detalhe";
    }

    @GetMapping("/{id}/editar")
    public String editar(@AuthenticationPrincipal CustomUserDetails principal,
                         @PathVariable UUID id, Model model) {
        prepararForm(principal, model, library.getCatalogItem(userId(principal), id), "Editar item");
        return "catalogo/form";
    }

    @PostMapping("/{id}")
    public String atualizar(@AuthenticationPrincipal CustomUserDetails principal,
                            @PathVariable UUID id,
                            @RequestParam String name,
                            @RequestParam(required = false) String kind,
                            @RequestParam(required = false) String summary,
                            @RequestParam(required = false) String appearance,
                            @RequestParam(required = false) String lore,
                            @RequestParam(required = false) String effects,
                            @RequestParam(required = false) String material,
                            @RequestParam(required = false) String rarity,
                            @RequestParam(required = false) UUID ownerId,
                            @RequestParam(required = false) String tags,
                            @RequestParam(defaultValue = "false") boolean favorite,
                            @RequestParam(defaultValue = "false") boolean archived,
                            RedirectAttributes redirect) {
        try {
            library.saveCatalogItem(userId(principal), id, name, kind, summary, appearance,
                    lore, effects, material, rarity, ownerId, tags, favorite, archived);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            throw e; // 404 de isolamento passa intacto
        } catch (Exception e) {
            redirect.addFlashAttribute("erro", "Não foi possível salvar o item. Revise os campos.");
            return "redirect:/catalogo/" + id + "/editar";
        }
        redirect.addFlashAttribute("sucesso", "Alterações salvas.");
        return "redirect:/catalogo/" + id;
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@AuthenticationPrincipal CustomUserDetails principal,
                          @PathVariable UUID id, RedirectAttributes redirect) {
        library.deleteCatalogItem(userId(principal), id);
        redirect.addFlashAttribute("sucesso", "Item excluído.");
        return "redirect:/catalogo";
    }

    /** Autocomplete para vinculos (mapa mental, futuros modulos). */
    @GetMapping("/api/busca")
    @ResponseBody
    public List<Map<String, String>> buscar(@AuthenticationPrincipal CustomUserDetails principal,
                                            @RequestParam String q) {
        var page = library.listCatalog(userId(principal), q, null, null, PageRequest.of(0, 8));
        return page.getContent().stream()
                .map(i -> Map.of("id", i.getId().toString(), "nome", i.getName(),
                        "tipo", i.getKind() == null ? "" : i.getKind()))
                .collect(Collectors.toList());
    }

    private void prepararForm(CustomUserDetails principal, Model model, CatalogItem item, String titulo) {
        var uid = userId(principal);
        model.addAttribute("item", item);
        model.addAttribute("personagens", library.allCharacters(uid));
        model.addAttribute("sugestoesTipos", Suggestions.TIPOS_ITEM);
        model.addAttribute("sugestoesRaridade", Suggestions.RARIDADES);
        model.addAttribute("tiposExistentes", library.catalogKinds(uid));
        model.addAttribute("titulo", titulo);
    }
}
