package com.biblioteca.salomao.web;

import com.biblioteca.salomao.domain.Character;
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
@RequestMapping("/personagens")
public class CharacterController extends BaseController {

    private final LibraryService library;

    public CharacterController(LibraryService library) {
        this.library = library;
    }

    @GetMapping
    public String listar(@AuthenticationPrincipal CustomUserDetails principal,
                         @RequestParam(required = false) String q,
                         @RequestParam(required = false) Boolean arquivadas,
                         @RequestParam(defaultValue = "0") int pagina,
                         @RequestParam(defaultValue = "grade") String visao,
                         Model model) {
        var page = library.listCharacters(userId(principal), q, arquivadas,
                PageRequest.of(pagina, 12, Sort.by("updatedAt").descending()));
        model.addAttribute("itens", page);
        model.addAttribute("q", q);
        model.addAttribute("arquivadas", arquivadas);
        model.addAttribute("visao", visao);
        model.addAttribute("titulo", "Personagens");
        return "personagens/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("p", new Character());
        model.addAttribute("titulo", "Novo personagem");
        return "personagens/form";
    }

    @PostMapping
    public String criar(@AuthenticationPrincipal CustomUserDetails principal,
                        @ModelAttribute("p") Character form,
                        RedirectAttributes redirect) {
        Character c = library.saveCharacter(userId(principal), form, null);
        redirect.addFlashAttribute("sucesso", "Personagem criado.");
        return "redirect:/personagens/" + c.getId();
    }

    @GetMapping("/{id}")
    public String ver(@AuthenticationPrincipal CustomUserDetails principal,
                      @PathVariable UUID id, Model model) {
        var uid = userId(principal);
        Character c = library.getCharacter(uid, id);
        model.addAttribute("p", c);
        model.addAttribute("poderesDisponiveis", library.allPowers(uid));
        // Consultas dedicadas por dono (evitam carregar 50 historias e tocar
        // colecoes LAZY fora de transacao — LazyInitializationException).
        model.addAttribute("historiasRelacionadas", library.relatedStories(uid, id));
        model.addAttribute("mapasRelacionados", library.relatedMaps(uid, id));
        model.addAttribute("titulo", c.getFullName());
        return "personagens/detalhe";
    }

    @GetMapping("/{id}/editar")
    public String editar(@AuthenticationPrincipal CustomUserDetails principal,
                         @PathVariable UUID id, Model model) {
        model.addAttribute("p", library.getCharacter(userId(principal), id));
        model.addAttribute("titulo", "Editar personagem");
        return "personagens/form";
    }

    @PostMapping("/{id}")
    public String atualizar(@AuthenticationPrincipal CustomUserDetails principal,
                            @PathVariable UUID id,
                            @ModelAttribute("p") Character form,
                            RedirectAttributes redirect) {
        library.saveCharacter(userId(principal), form, id);
        redirect.addFlashAttribute("sucesso", "Alterações salvas.");
        return "redirect:/personagens/" + id;
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@AuthenticationPrincipal CustomUserDetails principal,
                          @PathVariable UUID id, RedirectAttributes redirect) {
        library.deleteCharacter(userId(principal), id);
        redirect.addFlashAttribute("sucesso", "Personagem excluído.");
        return "redirect:/personagens";
    }

    @PostMapping("/{id}/poderes")
    public String associarPoder(@AuthenticationPrincipal CustomUserDetails principal,
                                @PathVariable UUID id,
                                @RequestParam UUID poderId,
                                RedirectAttributes redirect) {
        try {
            library.attachPower(userId(principal), id, poderId);
            redirect.addFlashAttribute("sucesso", "Poder associado.");
        } catch (Exception e) {
            redirect.addFlashAttribute("erro", "Não foi possível associar o poder. Ele precisa pertencer à sua biblioteca.");
        }
        return "redirect:/personagens/" + id;
    }

    @PostMapping("/{id}/poderes/{poderId}/remover")
    public String removerPoder(@AuthenticationPrincipal CustomUserDetails principal,
                               @PathVariable UUID id, @PathVariable UUID poderId,
                               RedirectAttributes redirect) {
        library.detachPower(userId(principal), id, poderId);
        redirect.addFlashAttribute("sucesso", "Associação removida.");
        return "redirect:/personagens/" + id;
    }

    /** Autocomplete do editor de historias (@Nome) e do mapa mental. */
    @GetMapping("/api/busca")
    @ResponseBody
    public List<Map<String, String>> autocomplete(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam String q) {
        return library.autocompleteCharacters(userId(principal), q).stream()
                .map(c -> Map.of("id", c.getId().toString(), "nome", c.getFullName()))
                .collect(Collectors.toList());
    }
}
