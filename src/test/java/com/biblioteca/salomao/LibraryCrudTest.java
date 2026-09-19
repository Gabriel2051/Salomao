package com.biblioteca.salomao;

import com.biblioteca.salomao.domain.Character;
import com.biblioteca.salomao.domain.MentalMap;
import com.biblioteca.salomao.domain.Power;
import com.biblioteca.salomao.domain.Story;
import com.biblioteca.salomao.domain.User;
import com.biblioteca.salomao.service.LibraryService;
import com.biblioteca.salomao.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * CHECKPOINTS 04–11 — CRUD dos modulos, relacionamentos, mencoes, mapa e XSS.
 */
@SpringBootTest
@Transactional
class LibraryCrudTest {

    @Autowired UserService users;
    @Autowired LibraryService library;

    private UUID uid;

    private UUID usuario() {
        if (uid == null) {
            User u = users.register(new UserService.Registration(
                    "cronista", "cronista@exemplo.com", "Forte123", "Forte123"));
            uid = u.getId();
        }
        return uid;
    }

    private Character personagem(String nome) {
        Character f = new Character();
        f.setFullName(nome);
        f.setSpecies("Humano");
        return library.saveCharacter(usuario(), f, null);
    }

    @Test
    void personagemPoderMuitosParaMuitos() {
        Character c1 = personagem("Arthur Black");
        Character c2 = personagem("Kael");
        Power p = library.savePower(usuario(), null, "Manipulação Sombria", "<p>desc</p>",
                "Elemental", "Lendário", "", "", "", "");

        library.attachPower(usuario(), c1.getId(), p.getId());
        library.attachPower(usuario(), c2.getId(), p.getId());

        assertThat(library.getCharacter(usuario(), c1.getId()).getPowers())
                .extracting(Power::getName).contains("Manipulação Sombria");
        // mesmo poder em varios personagens
        assertThat(library.charactersOfPower(usuario(), p.getId())).hasSize(2);

        library.detachPower(usuario(), c1.getId(), p.getId());
        assertThat(library.getCharacter(usuario(), c1.getId()).getPowers()).isEmpty();
        assertThat(library.charactersOfPower(usuario(), p.getId())).hasSize(1);
    }

    @Test
    void historiaComMencoesEstruturadas() {
        Character c = personagem("Arthena");
        Story s = library.saveStory(usuario(), null, "O Retorno", "Capítulo 1",
                "<p>Arthena caminhou.</p>", "epico", "RASCUNHO", false, false, List.of(c.getId()));
        Story lida = library.getStory(usuario(), s.getId());
        assertThat(lida.getMentions()).hasSize(1);
        assertThat(lida.getMentions().get(0).getCharacter().getFullName()).isEqualTo("Arthena");
    }

    @Test
    void mapaMentalPersisteGrafo() {
        MentalMap m = library.saveMap(usuario(), null, "Conflito dos Reinos", "");
        var n1 = new LibraryService.NodeDto(UUID.randomUUID(), "CATEGORIA", "MUNDO", "", null, null, 100, 100, "", false);
        var n2 = new LibraryService.NodeDto(UUID.randomUUID(), "PERSONAGEM", "Herói", "", null, null, 300, 200, "", false);
        library.saveGraph(usuario(), m.getId(), List.of(n1, n2),
                List.of(new LibraryService.EdgeDto(null, n1.id(), n2.id(), "habita")));

        MentalMap relido = library.getMap(usuario(), m.getId());
        assertThat(relido.getNodes()).hasSize(2);
        assertThat(relido.getEdges()).hasSize(1);

        // reabrir e salvar novamente mantem consistencia
        library.saveGraph(usuario(), m.getId(), List.of(n1),
                List.of());
        assertThat(library.getMap(usuario(), m.getId()).getNodes()).hasSize(1);
        assertThat(library.getMap(usuario(), m.getId()).getEdges()).isEmpty();
    }

    @Test
    void htmlMaliciosoESanitizado() {
        var nota = library.saveNote(usuario(), null, "XSS",
                "<p>ok</p><script>alert(1)</script><a href=\"javascript:alert(2)\">x</a>", "", "", false, false);
        assertThat(nota.getContent()).doesNotContain("<script>", "javascript:");
        assertThat(nota.getContent()).contains("ok");
    }

    @Test
    void pesquisaGlobalAgrupaModulos() {
        personagem("Sombra de Kael");
        library.savePower(usuario(), null, "Lâmina da Sombra", "", "", "", "", "", "", "");
        var r = library.globalSearch(usuario(), "Sombra");
        assertThat(r.characters()).isNotEmpty();
        assertThat(r.powers()).isNotEmpty();
        // termo curto demais retorna vazio (protege o banco)
        assertThat(library.globalSearch(usuario(), "x").characters()).isEmpty();
    }

    @Test
    void anotacaoCicloCompleto() {
        var n = library.saveNote(usuario(), null, "Ideias cap. 4", "<p>rascunho</p>", "Roteiro", "cap4,ideias", false, false);
        assertThat(n.getTagsCsv()).contains("cap4");
        var pagina = library.listNotes(usuario(), "cap. 4", null, PageRequest.of(0, 10));
        assertThat(pagina.getTotalElements()).isEqualTo(1);
        library.deleteNote(usuario(), n.getId());
        assertThat(library.listNotes(usuario(), null, null, PageRequest.of(0, 10)).getTotalElements()).isZero();
    }
}
