package com.biblioteca.salomao.web;

import com.biblioteca.salomao.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.UUID;

/** Disponibiliza usuario autenticado e injeta userId a partir da sessao validada. */
public abstract class BaseController {

    protected UUID userId(CustomUserDetails principal) {
        if (principal == null) throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.UNAUTHORIZED, "Sessão expirada. Entre novamente.");
        return principal.getId();
    }

    @ModelAttribute("currentUser")
    public CustomUserDetails currentUser(@AuthenticationPrincipal CustomUserDetails principal) {
        return principal;
    }
}
