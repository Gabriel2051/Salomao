package com.biblioteca.salomao;

import com.biblioteca.salomao.domain.Character;
import com.biblioteca.salomao.domain.MentalMap;
import com.biblioteca.salomao.domain.User;
import com.biblioteca.salomao.repository.UserRepository;
import com.biblioteca.salomao.security.CustomUserDetails;
import com.biblioteca.salomao.service.LibraryService;
import com.biblioteca.salomao.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Regressao em condicoes de producao: SEM transacao compartilhada com o teste
 * (cada service roda na propria transacao, como no runtime com open-in-view=false).
 * A autenticacao e injetada POR REQUISICAO (asUser) — deterministico e imune a
 * sessao/thread-local entre perform()s. Captura LazyInitializationException em
 * listas/detalhes com dados reais, cobre os PUTs de autosave e o lockout.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class ProductionLikeWebTest {

    @Autowired MockMvc mvc;
    @Autowired UserService users;
    @Autowired LibraryService library;
    @Autowired UserRepository userRepository;

    private record Sessao(UUID uid, RequestPostProcessor auth) {}

    private Sessao novaSessao(String base) {
        String tag = base + "." + UUID.randomUUID().toString().substring(0, 8);
        User u = users.register(new UserService.Registration(tag, tag + "@exemplo.com", "Forte123", "Forte123"));
        CustomUserDetails p = new CustomUserDetails(u);
        Authentication a = new UsernamePasswordAuthenticationToken(p, null, p.getAuthorities());
        return new Sessao(u.getId(), SecurityMockMvcRequestPostProcessors.authentication(a));
    }

    @Test
    void listasEDetalhesComDadosRenderizam() throws Exception {
        Sessao s = novaSessao("prod");
        UUID uid = s.uid();

        Character f = new Character();
        f.setFullName("Lázaro Prod");
        var c = library.saveCharacter(uid, f, null);
        var p = library.savePower(uid, null, "Poder Prod", "", "", "", "", "", "", "");
        library.attachPower(uid, c.getId(), p.getId());
        // associar 2x nao pode quebrar (guarda de duplo-clique)
        library.attachPower(uid, c.getId(), p.getId());
        library.saveStory(uid, null, "Historia Prod", "", "<p>x</p>", "", "RASCUNHO", false, false, List.of(c.getId()));
        library.saveNote(uid, null, "Nota Prod", "<p>y</p>", "", "", false, false);
        MentalMap m = library.saveMap(uid, null, "Mapa Prod", "");
        library.saveGraph(uid, m.getId(),
                List.of(new LibraryService.NodeDto(UUID.randomUUID(), "PERSONAGEM", "Lázaro", "",
                        "CHARACTER", c.getId(), 50, 50, "", false)),
                List.of());

        for (String url : List.of("/dashboard", "/anotacoes", "/anotacoes?visao=lista",
                "/personagens", "/personagens?visao=lista", "/poderes",
                "/historias", "/historias?visao=lista", "/mapas", "/pesquisa?q=Prod")) {
            mvc.perform(get(url).with(s.auth())).andExpect(status().isOk());
        }
        mvc.perform(get("/personagens/" + c.getId()).with(s.auth()))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Mapas mentais relacionados")));
        mvc.perform(get("/mapas/" + m.getId()).with(s.auth())).andExpect(status().isOk());
        // outro usuario nao ve nada (isolamento na camada web)
        Sessao outro = novaSessao("alheio");
        mvc.perform(get("/personagens/" + c.getId()).with(outro.auth())).andExpect(status().isNotFound());
        mvc.perform(get("/mapas/" + m.getId()).with(outro.auth())).andExpect(status().isNotFound());
    }

    @Test
    void autosaveEndpointsRespondemJson() throws Exception {
        Sessao s = novaSessao("save");
        UUID uid = s.uid();
        var nota = library.saveNote(uid, null, "N", "<p>a</p>", "", "", false, false);
        var hist = library.saveStory(uid, null, "H", "", "<p>a</p>", "", "RASCUNHO", false, false, null);
        var mapa = library.saveMap(uid, null, "M", "");

        mvc.perform(put("/anotacoes/" + nota.getId() + "/autosave").with(s.auth()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"N2\",\"content\":\"<p>b</p>\",\"category\":\"\",\"tags\":\"x\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

        mvc.perform(put("/historias/" + hist.getId() + "/autosave").with(s.auth()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"H2\",\"content\":\"<p>b</p>\",\"mencoes\":[]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

        String noId = UUID.randomUUID().toString();
        mvc.perform(put("/mapas/" + mapa.getId() + "/grafo").with(s.auth()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nos\":[{\"id\":\"" + noId + "\",\"nodeType\":\"TEXTO\",\"label\":\"Ideia\",\"content\":\"\",\"refType\":null,\"refId\":null,\"x\":10,\"y\":20,\"color\":\"\",\"collapsed\":false}],\"arestas\":[]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

        // recurso alheio via PUT tambem bloqueia
        Sessao outro = novaSessao("outro");
        assertThat(outro.uid()).isNotEqualTo(uid);
        mvc.perform(put("/anotacoes/" + nota.getId() + "/autosave").with(outro.auth()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"hack\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void cincoFalhasBloqueiamConta() throws Exception {
        String tag = "lock." + UUID.randomUUID().toString().substring(0, 8);
        users.register(new UserService.Registration(tag, tag + "@exemplo.com", "Forte123", "Forte123"));
        SecurityContextHolder.clearContext();

        for (int i = 1; i <= 4; i++) {
            mvc.perform(post("/login").with(csrf())
                            .param("login", tag).param("password", "Errada123"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/login?erro=1"));
        }
        // 5a falha: bloqueia
        mvc.perform(post("/login").with(csrf())
                        .param("login", tag).param("password", "Errada123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?bloqueada=1"));
        // mesmo a senha correta nao entra enquanto bloqueada
        mvc.perform(post("/login").with(csrf())
                        .param("login", tag).param("password", "Forte123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?bloqueada=1"));

        var u = userRepository.findByUsernameIgnoreCase(tag).orElseThrow();
        assertThat(u.isLocked()).isTrue();
    }

    @Test
    void logoutEncerraSessao() throws Exception {
        Sessao s = novaSessao("sair");
        mvc.perform(post("/sair").with(s.auth()).with(csrf()))
                .andExpect(status().is3xxRedirection());
    }
}
