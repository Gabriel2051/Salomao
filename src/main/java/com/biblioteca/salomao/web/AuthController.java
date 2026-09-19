package com.biblioteca.salomao.web;

import com.biblioteca.salomao.service.UserService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService users;

    public AuthController(UserService users) {
        this.users = users;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/registro")
    public String registroForm() {
        return "registro";
    }

    @PostMapping("/registro")
    public String registrar(
            @RequestParam @NotBlank String username,
            @RequestParam @NotBlank @Email String email,
            @RequestParam @Size(min = 8) String password,
            @RequestParam String confirm,
            RedirectAttributes redirect,
            Model model) {
        try {
            users.register(new UserService.Registration(username, email, password, confirm));
        } catch (IllegalArgumentException e) {
            model.addAttribute("erro", e.getMessage());
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            return "registro";
        }
        redirect.addFlashAttribute("sucesso", "Conta criada com sucesso. Entre para acessar sua biblioteca.");
        return "redirect:/login?criada=1";
    }

    @GetMapping("/erro/403")
    public String forbidden(Model model) {
        model.addAttribute("mensagem", "Você não tem permissão para acessar este recurso.");
        model.addAttribute("codigo", 403);
        return "erro";
    }
}
