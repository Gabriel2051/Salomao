package com.biblioteca.salomao;

import com.biblioteca.salomao.domain.Character;
import com.biblioteca.salomao.domain.MentalMap;
import com.biblioteca.salomao.domain.User;
import com.biblioteca.salomao.security.CustomUserDetails;
import com.biblioteca.salomao.service.LibraryService;
import com.biblioteca.salomao.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CHECKPOINTS 03–14 — todas as paginas renderizam (200) e os fluxos web funcionam.
 * Garante que nenhum template Thymeleaf possui expressao quebrada.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class WebSmokeTest {

    @Autowired MockMvc mvc;
    @Autowired UserService users;
    @Autowired LibraryService library;

    private UUID uid;

    @BeforeEach
    void auth() {
        User u = users.register(new UserService.Registration(
                "smoke", "smoke@exemplo.com", "Forte123", "Forte123"));
        uid = u.getId();
        CustomUserDetails p = new CustomUserDetails(u);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(p, null, p.getAuthorities()));
    }

    @Test
    void paginasPublicas() throws Exception {
        SecurityContextHolder.clearContext();
        mvc.perform(get("/login")).andExpect(status().isOk());
        mvc.perform(get("/registro")).andExpect(status().isOk());
        // area privada sem login redireciona para o login
        mvc.perform(get("/dashboard")).andExpect(status().is3xxRedirection());
    }

    @Test
    void registroViaWebCriaConta() throws Exception {
        SecurityContextHolder.clearContext();
        mvc.perform(post("/registro").with(csrf())
                        .param("username", "novo.web")
                        .param("email", "novo.web@exemplo.com")
                        .param("password", "Forte123")
                        .param("confirm", "Forte123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?criada=1"));
    }

    @Test
    void todasAsListasRenderizam() throws Exception {
        for (String url : List.of("/dashboard", "/anotacoes", "/anotacoes/nova",
                "/personagens", "/personagens/novo", "/poderes", "/poderes/novo",
                "/historias", "/historias/nova", "/mapas", "/pesquisa?q=guerra",
                "/configuracoes")) {
            mvc.perform(get(url)).andExpect(status().isOk());
        }
    }

    @Test
    void paginasDeDetalheRenderizamComDados() throws Exception {
        var nota = library.saveNote(uid, null, "Nota detalhe", "<p>corpo</p>", "Cat", "t1", false, false);
        Character f = new Character();
        f.setFullName("Heroi Detalhe");
        var c = library.saveCharacter(uid, f, null);
        var p = library.savePower(uid, null, "Poder Detalhe", "<p>d</p>", "Cat", "Nv", "", "", "", "");
        library.attachPower(uid, c.getId(), p.getId());
        var s = library.saveStory(uid, null, "Historia Detalhe", "", "<p>era uma vez</p>",
                "", "RASCUNHO", false, false, List.of(c.getId()));
        MentalMap m = library.saveMap(uid, null, "Mapa Detalhe", "");

        mvc.perform(get("/anotacoes/" + nota.getId())).andExpect(status().isOk());
        mvc.perform(get("/personagens/" + c.getId())).andExpect(status().isOk());
        mvc.perform(get("/personagens/" + c.getId() + "/editar")).andExpect(status().isOk());
        mvc.perform(get("/poderes/" + p.getId())).andExpect(status().isOk());
        mvc.perform(get("/poderes/" + p.getId() + "/editar")).andExpect(status().isOk());
        mvc.perform(get("/historias/" + s.getId())).andExpect(status().isOk());
        mvc.perform(get("/mapas/" + m.getId())).andExpect(status().isOk());
        // recurso alheio via web: 404 (nao vaza existencia)
        mvc.perform(get("/anotacoes/" + UUID.randomUUID())).andExpect(status().isNotFound());
    }

    @Test
    void apisRespeitamIsolamento() throws Exception {
        mvc.perform(get("/api/pesquisa").param("q", "xyznada"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens").isArray());
        mvc.perform(get("/personagens/api/busca").param("q", "Heroi"))
                .andExpect(status().isOk());
        mvc.perform(get("/mapas/" + UUID.randomUUID() + "/grafo"))
                .andExpect(status().isNotFound());
    }
}
