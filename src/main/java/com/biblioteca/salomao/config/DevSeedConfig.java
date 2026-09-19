package com.biblioteca.salomao.config;

import com.biblioteca.salomao.domain.Character;
import com.biblioteca.salomao.domain.MentalMap;
import com.biblioteca.salomao.domain.User;
import com.biblioteca.salomao.repository.UserRepository;
import com.biblioteca.salomao.service.LibraryService;
import com.biblioteca.salomao.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.UUID;

/**
 * Seed OPCIONAL de desenvolvimento. Ativo somente com o profile "seed":
 *   ./gradlew bootRun --args='--spring.profiles.active=seed'
 * Nunca executado em producao. Credenciais apenas demonstrativas.
 */
@Configuration
@Profile("seed")
public class DevSeedConfig {

    private static final Logger log = LoggerFactory.getLogger(DevSeedConfig.class);

    @Bean
    CommandLineRunner seed(UserService users, UserRepository userRepository, LibraryService library) {
        return args -> {
            if (userRepository.existsByUsernameIgnoreCase("demo")) {
                log.info("[seed] usuário demo já existe; nada a fazer.");
                return;
            }
            User demo = users.register(new UserService.Registration(
                    "demo", "demo@salomao.local", "Demo1234", "Demo1234"));
            var uid = demo.getId();

            var poder = library.savePower(uid, null, "Manipulação Ígnea",
                    "<p>Controle sobre chamas e brasas.</p>", "Elemental", "Avançado",
                    "<p>Exige concentração; inútil sob chuva intensa.</p>",
                    "<p>Água e vácuo.</p>", "<p>Forja antiga do sul.</p>", "");

            Character f = new Character();
            f.setFullName("Arthur Black");
            f.setNickname("O Retornado");
            f.setSpecies("Humano");
            f.setOccupation("Estrategista");
            f.setBackstory("<p>Sobrevivente da queda do Reino Cinzento.</p>");
            f.setSkills("<p>Combate corpo a corpo<br>Estratégia</p>");
            Character arthur = library.saveCharacter(uid, f, null);
            library.attachPower(uid, arthur.getId(), poder.getId());

            library.saveStory(uid, null, "O Retorno do Rei", "Prólogo",
                    "<p>Arthur Black caminhou sobre as cinzas.</p>", "epico",
                    "RASCUNHO", false, false, List.of(arthur.getId()));
            library.saveNote(uid, null, "Ideias para capítulo 4",
                    "<p>O mapa revela a passagem secreta.</p>", "Roteiro", "cap4,ideias", false, false);
            MentalMap mapa = library.saveMap(uid, null, "Conflito dos Reinos", "Visão geral da campanha");
            var n1 = new LibraryService.NodeDto(UUID.randomUUID(), "CATEGORIA", "MUNDO", "", null, null, 100, 100, "", false);
            var n2 = new LibraryService.NodeDto(UUID.randomUUID(), "PERSONAGEM", "Arthur Black", "", "CHARACTER", arthur.getId(), 320, 220, "", false);
            library.saveGraph(uid, mapa.getId(), List.of(n1, n2),
                    List.of(new LibraryService.EdgeDto(null, n1.id(), n2.id(), "protagonista")));

            log.info("[seed] dados demo criados. Login: demo / Demo1234");
        };
    }
}
