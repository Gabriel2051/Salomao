package com.biblioteca.salomao.security;
import com.biblioteca.salomao.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.UUID;
/** Barreira central anti-IDOR: todo acesso a recurso valida user_id no backend. */
@Service
public class OwnershipService {
    private final NoteRepository notes;
    private final CharacterRepository characters;
    private final PowerRepository powers;
    private final StoryRepository stories;
    private final MentalMapRepository maps;
    private final CatalogItemRepository catalog;
    public OwnershipService(NoteRepository n, CharacterRepository c, PowerRepository p, StoryRepository s, MentalMapRepository m, CatalogItemRepository ci) {
        this.notes = n; this.characters = c; this.powers = p; this.stories = s; this.maps = m; this.catalog = ci;
    }
    @Transactional(readOnly = true)
    public void requireNote(UUID id, UUID uid) {
        if (!notes.existsByIdAndUserId(id, uid)) throw notFound("Anotacao");
    }
    @Transactional(readOnly = true)
    public void requireCharacter(UUID id, UUID uid) {
        if (!characters.existsByIdAndUserId(id, uid)) throw notFound("Personagem");
    }
    @Transactional(readOnly = true)
    public void requirePower(UUID id, UUID uid) {
        if (!powers.existsByIdAndUserId(id, uid)) throw notFound("Poder");
    }
    @Transactional(readOnly = true)
    public void requireStory(UUID id, UUID uid) {
        if (!stories.existsByIdAndUserId(id, uid)) throw notFound("Historia");
    }
    @Transactional(readOnly = true)
    public void requireMap(UUID id, UUID uid) {
        if (!maps.existsByIdAndUserId(id, uid)) throw notFound("Mapa mental");
    }
    @Transactional(readOnly = true)
    public void requireCatalogItem(UUID id, UUID uid) {
        if (!catalog.existsByIdAndUserId(id, uid)) throw notFound("Item do catalogo");
    }
    private ResponseStatusException notFound(String what) {
        // 404 proposital: nao revela existencia de recurso alheio
        return new ResponseStatusException(HttpStatus.NOT_FOUND, what + " nao encontrado(a).");
    }
}
