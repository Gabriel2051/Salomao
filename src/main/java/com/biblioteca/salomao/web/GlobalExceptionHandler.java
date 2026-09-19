package com.biblioteca.salomao.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String badRequest(IllegalArgumentException e, Model model) {
        model.addAttribute("mensagem", e.getMessage());
        model.addAttribute("codigo", 400);
        return "erro";
    }

    @ExceptionHandler(ResponseStatusException.class)
    public String status(ResponseStatusException e, Model model,
                         jakarta.servlet.http.HttpServletResponse response) {
        // Detalhes tecnicos vao para o log, nunca expostos na interface publica
        log.warn("Erro {}: {}", e.getStatusCode(), e.getReason());
        // O status HTTP precisa refletir o erro (APIs e testes dependem dele)
        response.setStatus(e.getStatusCode().value());
        if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
            model.addAttribute("mensagem", "O recurso solicitado não foi encontrado.");
            model.addAttribute("codigo", 404);
            return "erro";
        }
        if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            return "redirect:/login?expirada=1";
        }
        model.addAttribute("mensagem", "Não foi possível concluir a operação. Tente novamente.");
        model.addAttribute("codigo", e.getStatusCode().value());
        return "erro";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String generic(Exception e, Model model) {
        log.error("Erro interno", e);
        model.addAttribute("mensagem", "Não foi possível concluir a operação. Tente novamente.");
        model.addAttribute("codigo", 500);
        return "erro";
    }
}
