package com.biblioteca.salomao;

import com.biblioteca.salomao.domain.Character;
import com.biblioteca.salomao.domain.MentalMap;
import com.biblioteca.salomao.domain.Note;
import com.biblioteca.salomao.domain.Power;
import com.biblioteca.salomao.domain.Story;
import com.biblioteca.salomao.domain.User;
import com.biblioteca.salomao.repository.UserRepository;
import com.biblioteca.salomao.service.LibraryService;
import com.biblioteca.salomao.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * CHECKPOINTS 01/02/12 — autenticacao, banco e isolamento entre usuarios.
 * Regra: Usuario A JAMAIS acessa recurso de Usuario B (GET/PUT/DELETE/pesquisa/associacoes).
 */
@SpringBootTest
@Transactional
class SecurityIsolationTest {

    @Autowired UserService users;
    @Autowired LibraryService library;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder encoder;

    private User novoUsuario(String base) {
        return users.register(new UserService.Registration(
                base, base + "@exemplo.com", "Forte123", "Forte123"));
    }

    @Test
    void registroValidaESenhaNuncaFicaEmTextoPuro() {
        User u = novoUsuario("alice.iso");
        assertThat(u.getId()).isNotNull();
        assertThat(u.getPasswordHash()).doesNotContain("Forte123");
        assertThat(encoder.matches("Forte123", u.getPasswordHash())).isTrue();

        // duplicados bloqueados
        assertThatThrownBy(() -> novoUsuario("alice.iso")).isInstanceOf(IllegalArgumentException.class);
        // senha fraca bloqueada
        assertThatThrownBy(() -> users.register(
                new UserService.Registration("fraco", "fraco@exemplo.com", "123", "123")))
                .isInstanceOf(IllegalArgumentException.class);
        // email invalido bloqueado
        assertThatThrownBy(() -> users.register(
                new UserService.Registration("bad", "nao-email", "Forte123", "Forte123")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void usuarioBNaoAcessaRecursosDoUsuarioA() {
        User a = novoUsuario("usuario_a");
        User b = novoUsuario("usuario_b");

        Note nota = library.saveNote(a.getId(), null, "Secreta", "<p>conteudo</p>", "", "", false, false);
        Character c = character(a.getId(), "Arthas Secreto");
        Power p = library.savePower(a.getId(), null, "Poder Secreto", "", "", "", "", "", "", "");
        Story s = library.saveStory(a.getId(), null, "Historia Secreta", "", "<p>x</p>", "", "RASCUNHO", false, false, null);
        MentalMap m = library.saveMap(a.getId(), null, "Mapa Secreto", "");

        // leitura direta bloqueada (404 — nao revela existencia)
        assertThatThrownBy(() -> library.getNote(b.getId(), nota.getId())).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> library.getCharacter(b.getId(), c.getId())).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> library.getPower(b.getId(), p.getId())).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> library.getStory(b.getId(), s.getId())).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> library.getMap(b.getId(), m.getId())).isInstanceOf(ResponseStatusException.class);

        // escrita/exclusao bloqueada
        assertThatThrownBy(() -> library.deleteNote(b.getId(), nota.getId())).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> library.saveNote(b.getId(), nota.getId(), "hack", "", "", "", false, false))
                .isInstanceOf(ResponseStatusException.class);

        // associacao cruzada bloqueada: poder de A no personagem de B
        Character cb = character(b.getId(), "Personagem de B");
        assertThatThrownBy(() -> library.attachPower(b.getId(), cb.getId(), p.getId()))
                .isInstanceOf(ResponseStatusException.class);

        // mencao cruzada ignorada: personagem de B nao entra na historia de A
        Story s2 = library.saveStory(a.getId(), s.getId(), s.getTitle(), "", s.getContent(), "",
                "RASCUNHO", false, false, List.of(cb.getId()));
        assertThat(library.getStory(a.getId(), s2.getId()).getMentions()).isEmpty();

        // pesquisa global nao vaza entre usuarios
        assertThat(library.globalSearch(b.getId(), "Secreta").notes()).isEmpty();
        assertThat(library.globalSearch(b.getId(), "Secreto").characters()).isEmpty();
        assertThat(library.globalSearch(a.getId(), "Secreta").notes()).hasSize(1);
    }

    @Test
    void dashboardContaApenasDadosProprios() {
        User a = novoUsuario("dash_a");
        User b = novoUsuario("dash_b");
        library.saveNote(a.getId(), null, "N1", "", "", "", false, false);
        character(a.getId(), "C1");
        var dashA = library.dashboard(a.getId());
        var dashB = library.dashboard(b.getId());
        assertThat(dashA.notes()).isEqualTo(1);
        assertThat(dashA.characters()).isEqualTo(1);
        assertThat(dashB.notes()).isZero();
        assertThat(dashB.characters()).isZero();
        assertThat(dashB.activities()).isEmpty();
    }

    private Character character(UUID uid, String nome) {
        Character form = new Character();
        form.setFullName(nome);
        return library.saveCharacter(uid, form, null);
    }
}
