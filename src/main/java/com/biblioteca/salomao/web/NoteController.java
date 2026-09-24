package com.biblioteca.salomao.web;

import com.biblioteca.salomao.domain.Note;
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

import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/anotacoes")
public class NoteController extends BaseController {

    private final LibraryService library;

    public NoteController(LibraryService library) {
        this.library = library;
    }

    @GetMapping
    public String listar(@AuthenticationPrincipal CustomUserDetails principal,
                         @RequestParam(required = false) String q,
                         @RequestParam(required = false) Boolean arquivadas,
                         @RequestParam(defaultValue = "0") int pagina,
                         @RequestParam(defaultValue = "grade") String visao,
                         Model model) {
        var page = library.listNotes(userId(principal), q, arquivadas,
                PageRequest.of(pagina, 12, Sort.by("updatedAt").descending()));
        model.addAttribute("itens", page);
        model.addAttribute("q", q);
        model.addAttribute("arquivadas", arquivadas);
        model.addAttribute("visao", visao);
        model.addAttribute("titulo", "Anotações");
        return "anotacoes/lista";
    }

    @GetMapping("/nova")
    public String nova(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        model.addAttribute("nota", new Note());
        model.addAttribute("categorias", library.noteCategories(userId(principal)));
        model.addAttribute("titulo", "Nova anotação");
        return "anotacoes/form";
    }

    @PostMapping
    public String criar(@AuthenticationPrincipal CustomUserDetails principal,
                        @RequestParam String title,
                        @RequestParam String content,
                        @RequestParam(required = false) String category,
                        @RequestParam(required = false) String tags,
                        RedirectAttributes redirect) {
        Note n = library.saveNote(userId(principal), null, title, content, category, tags,
                false, false);
        redirect.addFlashAttribute("sucesso", "Anotação salva.");
        return "redirect:/anotacoes/" + n.getId();
    }

    @GetMapping("/{id}")
    public String ver(@AuthenticationPrincipal CustomUserDetails principal,
                      @PathVariable UUID id, Model model) {
        var uid = userId(principal);
        model.addAttribute("nota", library.getNote(uid, id));
        model.addAttribute("categorias", library.noteCategories(uid));
        model.addAttribute("titulo", "Anotação");
        return "anotacoes/form";
    }

    @PostMapping("/{id}")
    public String atualizar(@AuthenticationPrincipal CustomUserDetails principal,
                            @PathVariable UUID id,
                            @RequestParam String title,
                            @RequestParam String content,
                            @RequestParam(required = false) String category,
                            @RequestParam(required = false) String tags,
                            @RequestParam(defaultValue = "false") boolean favorite,
                             @RequestParam(defaultValue = "false") boolean archived,
                             RedirectAttributes redirect) {
        // Nota: 404 de isolamento (recurso alheio) NAO e capturado aqui — sobe intacto.
        library.saveNote(userId(principal), id, title, content, category, tags, favorite, archived);
        redirect.addFlashAttribute("sucesso", "Alterações salvas.");
        return "redirect:/anotacoes/" + id;
    }

    /** Autosave (debounce no frontend): retorna estado para o indicador visual. */
    @PutMapping("/{id}/autosave")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> autosave(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        try {
            Note atual = library.getNote(userId(principal), id);
            library.saveNote(userId(principal), id,
                    body.getOrDefault("title", atual.getTitle()),
                    body.getOrDefault("content", atual.getContent()),
                    body.getOrDefault("category", atual.getCategory()),
                    body.getOrDefault("tags", atual.getTagsCsv()),
                    atual.isFavorite(), atual.isArchived());
            return ResponseEntity.ok(Map.of("ok", true, "quando", java.time.Instant.now().toString()));
        } catch (org.springframework.web.server.ResponseStatusException e) {
            throw e; // 404 de isolamento e 401 passam intactos (nunca mascarados como 400)
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "erro",
                    "Não foi possível salvar suas alterações. Tente novamente."));
        }
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@AuthenticationPrincipal CustomUserDetails principal,
                          @PathVariable UUID id, RedirectAttributes redirect) {
        library.deleteNote(userId(principal), id);
        redirect.addFlashAttribute("sucesso", "Anotação excluída.");
        return "redirect:/anotacoes";
    }

    @PostMapping("/{id}/favoritar")
    public String favoritar(@AuthenticationPrincipal CustomUserDetails principal,
                            @PathVariable UUID id, RedirectAttributes redirect) {
        library.toggleFavoriteNote(userId(principal), id);
        return "redirect:/anotacoes/" + id;
    }
}
