package com.biblioteca.salomao.web;

import com.biblioteca.salomao.security.CustomUserDetails;
import com.biblioteca.salomao.service.ActivityService;
import com.biblioteca.salomao.service.LibraryService;
import com.biblioteca.salomao.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class DashboardController extends BaseController {

    private final LibraryService library;
    private final UserService users;

    public DashboardController(LibraryService library, UserService users, ActivityService activity) {
        this.library = library;
        this.users = users;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        var dash = library.dashboard(userId(principal));
        model.addAttribute("dash", dash);
        model.addAttribute("titulo", "Dashboard");
        return "dashboard";
    }

    @GetMapping("/pesquisa")
    public String pesquisa(@AuthenticationPrincipal CustomUserDetails principal,
                           @RequestParam(required = false) String q, Model model) {
        if (q != null && q.trim().length() >= 2) {
            model.addAttribute("resultado", library.globalSearch(userId(principal), q.trim()));
            model.addAttribute("q", q.trim());
        }
        model.addAttribute("titulo", "Pesquisa");
        return "pesquisa";
    }

    /** API da paleta global (Ctrl+K): somente dados do usuario autenticado. */
    @GetMapping("/api/pesquisa")
    @ResponseBody
    public Map<String, Object> apiPesquisa(@AuthenticationPrincipal CustomUserDetails principal,
                                           @RequestParam String q) {
        var r = library.globalSearch(userId(principal), q == null ? "" : q.trim());
        List<Map<String, String>> itens = new ArrayList<>();
        r.characters().forEach(c -> itens.add(item("personagem", c.getFullName(), "/personagens/" + c.getId())));
        r.powers().forEach(p -> itens.add(item("poder", p.getName(), "/poderes/" + p.getId())));
        r.stories().forEach(s -> itens.add(item("história", s.getTitle(), "/historias/" + s.getId())));
        r.notes().forEach(n -> itens.add(item("anotação", n.getTitle(), "/anotacoes/" + n.getId())));
        r.maps().forEach(m -> itens.add(item("mapa", m.getTitle(), "/mapas/" + m.getId())));
        r.items().forEach(i -> itens.add(item("item", i.getName(), "/catalogo/" + i.getId())));
        return Map.of("itens", itens);
    }

    private Map<String, String> item(String tipo, String nome, String url) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("tipo", tipo);
        m.put("nome", nome);
        m.put("url", url);
        return m;
    }

    @GetMapping("/configuracoes")
    public String configuracoes(Model model) {
        model.addAttribute("titulo", "Configurações");
        return "configuracoes";
    }

    @PostMapping("/configuracoes/perfil")
    public String salvarPerfil(@AuthenticationPrincipal CustomUserDetails principal,
                               @RequestParam String displayName,
                               RedirectAttributes redirect) {
        users.updateProfile(userId(principal), displayName);
        redirect.addFlashAttribute("sucesso", "Perfil atualizado.");
        return "redirect:/configuracoes";
    }

    @PostMapping("/configuracoes/senha")
    public String trocarSenha(@AuthenticationPrincipal CustomUserDetails principal,
                              @RequestParam String atual,
                              @RequestParam String nova,
                              @RequestParam String confirmacao,
                              RedirectAttributes redirect) {
        try {
            users.changePassword(userId(principal), atual, nova, confirmacao);
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
            return "redirect:/configuracoes";
        }
        redirect.addFlashAttribute("sucesso", "Senha alterada com sucesso.");
        return "redirect:/configuracoes";
    }
}
