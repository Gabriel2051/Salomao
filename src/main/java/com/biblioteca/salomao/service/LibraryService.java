package com.biblioteca.salomao.service;

import com.biblioteca.salomao.domain.*;
import com.biblioteca.salomao.domain.Character;
import com.biblioteca.salomao.repository.*;
import com.biblioteca.salomao.security.OwnershipService;
import com.biblioteca.salomao.util.Sanitizer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

/**
 * Casos de uso da biblioteca. TODOS recebem o userId autenticado (sessao)
 * e jamais aceitam user_id vindo do frontend. Cada leitura/escrita valida
 * a propriedade do recurso (anti-IDOR horizontal).
 */
@Service
public class LibraryService {

    private final UserRepository users;
    private final NoteRepository notes;
    private final CharacterRepository characters;
    private final PowerRepository powers;
    private final StoryRepository stories;
    private final StoryMentionRepository mentions;
    private final MentalMapRepository maps;
    private final MindMapNodeRepository nodes;
    private final MindMapEdgeRepository edges;
    private final OwnershipService ownership;
    private final ActivityService activity;

    public LibraryService(UserRepository users, NoteRepository notes, CharacterRepository characters,
                          PowerRepository powers, StoryRepository stories, StoryMentionRepository mentions,
                          MentalMapRepository maps, MindMapNodeRepository nodes, MindMapEdgeRepository edges,
                          OwnershipService ownership, ActivityService activity) {
        this.users = users; this.notes = notes; this.characters = characters; this.powers = powers;
        this.stories = stories; this.mentions = mentions; this.maps = maps; this.nodes = nodes;
        this.edges = edges; this.ownership = ownership; this.activity = activity;
    }

    private User ref(UUID userId) { return users.getReferenceById(userId); }

    private ResponseStatusException nf(String w) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, w + " não encontrado(a).");
    }

    // ==================== ANOTAÇÕES ====================

    @Transactional(readOnly = true)
    public Page<Note> listNotes(UUID uid, String q, Boolean archived, Pageable p) {
        if (q != null && !q.isBlank()) return notes.search(uid, q.trim(), p);
        if (archived != null) return notes.findByUserIdAndArchived(uid, archived, p);
        return notes.findByUserId(uid, p);
    }

    @Transactional(readOnly = true)
    public Note getNote(UUID uid, UUID id) {
        return notes.findByIdAndUserId(id, uid).orElseThrow(() -> nf("Anotação"));
    }

    @Transactional
    public Note saveNote(UUID uid, UUID id, String title, String content, String category,
                         String tags, boolean favorite, boolean archived) {
        Note n = (id == null) ? new Note()
                : notes.findByIdAndUserId(id, uid).orElseThrow(() -> nf("Anotação"));
        if (n.getUser() == null || n.getUser().getId() == null) n.setUser(ref(uid));
        n.setTitle(clip(Sanitizer.textOrEmpty(title), 200));
        if (n.getTitle().isBlank()) n.setTitle("Sem título");
        n.setContent(Sanitizer.richHtml(content));
        n.setCategory(clip(Sanitizer.textOrEmpty(category), 80));
        n.setTagsCsv(Sanitizer.tags(tags));
        n.setFavorite(favorite);
        n.setArchived(archived);
        Note saved = notes.save(n);
        activity.record(uid, id == null ? "CRIOU" : "EDITOU", "NOTE", saved.getId(),
                (id == null ? "Anotação criada: " : "Anotação atualizada: ") + saved.getTitle());
        return saved;
    }

    @Transactional
    public void deleteNote(UUID uid, UUID id) {
        Note n = notes.findByIdAndUserId(id, uid).orElseThrow(() -> nf("Anotação"));
        notes.delete(n);
        activity.record(uid, "EXCLUIU", "NOTE", null, "Anotação excluída: " + n.getTitle());
    }

    // ==================== PERSONAGENS ====================

    @Transactional(readOnly = true)
    public Page<Character> listCharacters(UUID uid, String q, Boolean archived, Pageable p) {
        Page<Character> page;
        if (q != null && !q.isBlank()) page = characters.search(uid, q.trim(), p);
        else if (archived != null) page = characters.findByUserIdAndArchived(uid, archived, p);
        else page = characters.findByUserId(uid, p);
        // Inicializa colecoes LAZY dentro da transacao: a view (open-in-view=false)
        // acessa c.powers; sem isso, LazyInitializationException com lista nao-vazia.
        page.getContent().forEach(c -> c.getPowers().size());
        return page;
    }

    @Transactional(readOnly = true)
    public Character getCharacter(UUID uid, UUID id) {
        Character c = characters.findByIdAndUserId(id, uid).orElseThrow(() -> nf("Personagem"));
        c.getPowers().size();
        return c;
    }

    @Transactional
    public Character saveCharacter(UUID uid, Character form, UUID id) {
        Character c = (id == null) ? new Character()
                : characters.findByIdAndUserId(id, uid).orElseThrow(() -> nf("Personagem"));
        if (c.getUser() == null || c.getUser().getId() == null) c.setUser(ref(uid));
        c.setFullName(orUntitled(clip(Sanitizer.textOrEmpty(form.getFullName()), 160)));
        c.setAlias(clip(Sanitizer.textOrEmpty(form.getAlias()), 160));
        c.setNickname(clip(Sanitizer.textOrEmpty(form.getNickname()), 80));
        c.setAge(form.getAge());
        c.setBirthDate(clip(Sanitizer.textOrEmpty(form.getBirthDate()), 40));
        c.setGender(clip(Sanitizer.textOrEmpty(form.getGender()), 40));
        c.setSpecies(clip(Sanitizer.textOrEmpty(form.getSpecies()), 80));
        c.setNationality(clip(Sanitizer.textOrEmpty(form.getNationality()), 80));
        c.setOccupation(clip(Sanitizer.textOrEmpty(form.getOccupation()), 120));
        c.setStatus(clip(Sanitizer.textOrEmpty(form.getStatus()), 40));
        c.setPhysicalDesc(Sanitizer.richHtml(form.getPhysicalDesc()));
        c.setHeight(clip(Sanitizer.textOrEmpty(form.getHeight()), 20));
        c.setWeight(clip(Sanitizer.textOrEmpty(form.getWeight()), 20));
        c.setEyeColor(clip(Sanitizer.textOrEmpty(form.getEyeColor()), 40));
        c.setHairColor(clip(Sanitizer.textOrEmpty(form.getHairColor()), 40));
        c.setSpecialTraits(Sanitizer.richHtml(form.getSpecialTraits()));
        c.setClothing(Sanitizer.richHtml(form.getClothing()));
        c.setMarks(Sanitizer.richHtml(form.getMarks()));
        c.setScars(Sanitizer.richHtml(form.getScars()));
        c.setOtherDetails(Sanitizer.richHtml(form.getOtherDetails()));
        c.setPersonality(Sanitizer.richHtml(form.getPersonality()));
        c.setLikes(Sanitizer.richHtml(form.getLikes()));
        c.setDislikes(Sanitizer.richHtml(form.getDislikes()));
        c.setFears(Sanitizer.richHtml(form.getFears()));
        c.setGoals(Sanitizer.richHtml(form.getGoals()));
        c.setMotivations(Sanitizer.richHtml(form.getMotivations()));
        c.setWeaknesses(Sanitizer.richHtml(form.getWeaknesses()));
        c.setVirtues(Sanitizer.richHtml(form.getVirtues()));
        c.setFlaws(Sanitizer.richHtml(form.getFlaws()));
        c.setBackstory(Sanitizer.richHtml(form.getBackstory()));
        c.setSkills(Sanitizer.richHtml(form.getSkills()));
        c.setTagsCsv(Sanitizer.tags(form.getTagsCsv()));
        c.setFavorite(form.isFavorite());
        c.setArchived(form.isArchived());
        Character saved = characters.save(c);
        activity.record(uid, id == null ? "CRIOU" : "EDITOU", "CHARACTER", saved.getId(),
                (id == null ? "Personagem criado: " : "Personagem atualizado: ") + saved.getFullName());
        return saved;
    }

    @Transactional
    public void deleteCharacter(UUID uid, UUID id) {
        Character c = characters.findByIdAndUserId(id, uid).orElseThrow(() -> nf("Personagem"));
        // remove mencoes que apontam para ele (integridade)
        mentions.findByCharacterIdAndCharacterUserId(id, uid)
                .forEach(m -> mentions.delete(m));
        characters.delete(c);
        activity.record(uid, "EXCLUIU", "CHARACTER", null, "Personagem excluído: " + c.getFullName());
    }

    /** Associa poder ao personagem. Ambos PRECISAM pertencer ao mesmo usuario. */
    @Transactional
    public void attachPower(UUID uid, UUID characterId, UUID powerId) {
        Character c = characters.findByIdAndUserId(characterId, uid).orElseThrow(() -> nf("Personagem"));
        Power p = powers.findByIdAndUserId(powerId, uid).orElseThrow(() -> nf("Poder"));
        // Guarda contra duplo-clique: Set por identidade poderia gerar INSERT duplicado
        // (violacao de unique) em sessoes distintas; sem isso, 500 ao associar 2x.
        boolean jaTem = c.getPowers().stream().anyMatch(x -> x.getId() != null && x.getId().equals(powerId));
        if (jaTem) return;
        c.getPowers().add(p);
        characters.save(c);
        activity.record(uid, "ASSOCIOU", "CHARACTER", c.getId(),
                "Poder '" + p.getName() + "' associado a " + c.getFullName());
    }

    @Transactional
    public void detachPower(UUID uid, UUID characterId, UUID powerId) {
        Character c = characters.findByIdAndUserId(characterId, uid).orElseThrow(() -> nf("Personagem"));
        c.getPowers().removeIf(p -> p.getId().equals(powerId));
        characters.save(c);
    }

    // ==================== PODERES ====================

    @Transactional(readOnly = true)
    public Page<Power> listPowers(UUID uid, String q, Pageable p) {
        if (q != null && !q.isBlank()) return powers.search(uid, q.trim(), p);
        return powers.findByUserId(uid, p);
    }

    @Transactional(readOnly = true)
    public List<Power> allPowers(UUID uid) {
        return powers.findByUserIdOrderByNameAsc(uid);
    }

    @Transactional(readOnly = true)
    public Power getPower(UUID uid, UUID id) {
        return powers.findByIdAndUserId(id, uid).orElseThrow(() -> nf("Poder"));
    }

    @Transactional(readOnly = true)
    public List<Character> charactersOfPower(UUID uid, UUID powerId) {
        ownership.requirePower(powerId, uid);
        return powers.findCharactersOfPower(powerId, uid);
    }

    @Transactional
    public Power savePower(UUID uid, UUID id, String name, String description, String category,
                           String level, String limitations, String weaknesses, String origin, String observations) {
        Power p = (id == null) ? new Power()
                : powers.findByIdAndUserId(id, uid).orElseThrow(() -> nf("Poder"));
        if (p.getUser() == null || p.getUser().getId() == null) p.setUser(ref(uid));
        p.setName(orUntitled(clip(Sanitizer.textOrEmpty(name), 160)));
        p.setDescription(Sanitizer.richHtml(description));
        p.setCategory(clip(Sanitizer.textOrEmpty(category), 80));
        p.setLevel(clip(Sanitizer.textOrEmpty(level), 40));
        p.setLimitations(Sanitizer.richHtml(limitations));
        p.setWeaknesses(Sanitizer.richHtml(weaknesses));
        p.setOrigin(Sanitizer.richHtml(origin));
        p.setObservations(Sanitizer.richHtml(observations));
        Power saved = powers.save(p);
        activity.record(uid, id == null ? "CRIOU" : "EDITOU", "POWER", saved.getId(),
                (id == null ? "Poder criado: " : "Poder atualizado: ") + saved.getName());
        return saved;
    }

    @Transactional
    public void deletePower(UUID uid, UUID id) {
        Power p = powers.findByIdAndUserId(id, uid).orElseThrow(() -> nf("Poder"));
        powers.delete(p);
        activity.record(uid, "EXCLUIU", "POWER", null, "Poder excluído: " + p.getName());
    }

    // ==================== HISTÓRIAS ====================

    @Transactional(readOnly = true)
    public Page<Story> listStories(UUID uid, String q, Boolean archived, Pageable p) {
        if (q != null && !q.isBlank()) return stories.search(uid, q.trim(), p);
        if (archived != null) return stories.findByUserIdAndArchived(uid, archived, p);
        return stories.findByUserId(uid, p);
    }

    @Transactional(readOnly = true)
    public Story getStory(UUID uid, UUID id) {
        Story s = stories.findByIdAndUserId(id, uid).orElseThrow(() -> nf("História"));
        // Inicializa mencoes E os personagens referenciados (proxies LAZY):
        // a view lista m.character.fullName fora da transacao.
        s.getMentions().forEach(mn -> mn.getCharacter().getFullName());
        return s;
    }

    @Transactional
    public Story saveStory(UUID uid, UUID id, String title, String subtitle, String content,
                           String tags, String status, boolean favorite, boolean archived,
                           List<UUID> mentionIds) {
        Story s = (id == null) ? new Story()
                : stories.findByIdAndUserId(id, uid).orElseThrow(() -> nf("História"));
        if (s.getUser() == null || s.getUser().getId() == null) s.setUser(ref(uid));
        s.setTitle(orUntitled(clip(Sanitizer.textOrEmpty(title), 220)));
        s.setSubtitle(clip(Sanitizer.textOrEmpty(subtitle), 300));
        s.setContent(Sanitizer.richHtml(content));
        s.setTagsCsv(Sanitizer.tags(tags));
        try {
            s.setStatus(status == null ? Story.Status.RASCUNHO : Story.Status.valueOf(status));
        } catch (IllegalArgumentException e) {
            s.setStatus(Story.Status.RASCUNHO);
        }
        s.setFavorite(favorite);
        s.setArchived(archived);
        Story saved = stories.save(s);
        // reconstroi mencoes: somente personagens do proprio usuario sao aceitos.
        // A colecao do lado pai e mantida em sincronia (evita leitura stale e
        // remocoes indevidas pelo orphanRemoval dentro da mesma transacao).
        if (mentionIds != null) {
            // limpa existentes e reinsere validadas
            List<StoryMention> current = mentions.findByStoryIdAndStoryUserId(saved.getId(), uid);
            current.forEach(mentions::delete);
            saved.getMentions().clear();
            for (UUID cid : mentionIds) {
                if (cid == null) continue;
                characters.findByIdAndUserId(cid, uid).ifPresent(c -> {
                    boolean dup = saved.getMentions().stream()
                            .anyMatch(x -> x.getCharacter() != null && cid.equals(x.getCharacter().getId()));
                    if (!dup) {
                        StoryMention m = new StoryMention();
                        m.setStory(saved);
                        m.setCharacter(c);
                        mentions.save(m);
                        saved.getMentions().add(m);
                    }
                });
            }
        }
        activity.record(uid, id == null ? "CRIOU" : "EDITOU", "STORY", saved.getId(),
                (id == null ? "História criada: " : "História atualizada: ") + saved.getTitle());
        return saved;
    }

    @Transactional
    public void deleteStory(UUID uid, UUID id) {
        Story s = stories.findByIdAndUserId(id, uid).orElseThrow(() -> nf("História"));
        stories.delete(s);
        activity.record(uid, "EXCLUIU", "STORY", null, "História excluída: " + s.getTitle());
    }

    @Transactional(readOnly = true)
    public List<Character> autocompleteCharacters(UUID uid, String q) {
        String term = q == null ? "" : q.trim();
        if (term.length() < 1) return List.of();
        return characters.searchByName(uid, term,
                org.springframework.data.domain.PageRequest.of(0, 8));
    }

    /** Historias que mencionam o personagem (via story_character_mentions, escopo do dono). */
    @Transactional(readOnly = true)
    public List<Story> relatedStories(UUID uid, UUID characterId) {
        ownership.requireCharacter(characterId, uid);
        return mentions.findByCharacterIdAndCharacterUserId(characterId, uid).stream()
                .map(StoryMention::getStory)
                .peek(st -> st.getTitle()) // story chega como proxy LAZY
                .distinct()
                .toList();
    }

    /** Mapas com nos que referenciam o personagem (refType=CHARACTER, escopo do dono). */
    @Transactional(readOnly = true)
    public List<MentalMap> relatedMaps(UUID uid, UUID characterId) {
        ownership.requireCharacter(characterId, uid);
        return nodes.findByMapUserIdAndRefTypeAndRefId(uid, "CHARACTER", characterId).stream()
                .map(MindMapNode::getMap)
                .peek(mp -> mp.getTitle()) // map chega como proxy LAZY
                .distinct()
                .toList();
    }

    // ==================== MAPAS MENTAIS ====================

    @Transactional(readOnly = true)
    public Page<MentalMap> listMaps(UUID uid, String q, Pageable p) {
        Page<MentalMap> page;
        if (q != null && !q.isBlank()) page = maps.search(uid, q.trim(), p);
        else page = maps.findByUserId(uid, p);
        // A view acessa m.nodes.size(); inicializa dentro da transacao.
        page.getContent().forEach(m -> m.getNodes().size());
        return page;
    }

    @Transactional(readOnly = true)
    public MentalMap getMap(UUID uid, UUID id) {
        MentalMap m = maps.findByIdAndUserId(id, uid).orElseThrow(() -> nf("Mapa mental"));
        m.getNodes().size();
        m.getEdges().forEach(e -> {
            // Extremidades sao proxies LAZY: o JSON do grafo le source/target fora da tx.
            e.getSource().getId();
            e.getTarget().getId();
        });
        return m;
    }

    @Transactional
    public MentalMap saveMap(UUID uid, UUID id, String title, String description) {
        MentalMap m = (id == null) ? new MentalMap()
                : maps.findByIdAndUserId(id, uid).orElseThrow(() -> nf("Mapa mental"));
        if (m.getUser() == null || m.getUser().getId() == null) m.setUser(ref(uid));
        m.setTitle(orUntitled(clip(Sanitizer.textOrEmpty(title), 160)));
        m.setDescription(Sanitizer.textOrEmpty(description));
        MentalMap saved = maps.save(m);
        activity.record(uid, id == null ? "CRIOU" : "EDITOU", "MAP", saved.getId(),
                (id == null ? "Mapa criado: " : "Mapa atualizado: ") + saved.getTitle());
        return saved;
    }

    @Transactional
    public void deleteMap(UUID uid, UUID id) {
        MentalMap m = maps.findByIdAndUserId(id, uid).orElseThrow(() -> nf("Mapa mental"));
        maps.delete(m);
        activity.record(uid, "EXCLUIU", "MAP", null, "Mapa excluído: " + m.getTitle());
    }

    public record NodeDto(UUID id, String nodeType, String label, String content,
                          String refType, UUID refId, double x, double y, String color, boolean collapsed) {}
    public record EdgeDto(UUID id, UUID sourceId, UUID targetId, String label) {}

    /** Persistencia completa do grafo (autosave do editor). Tudo validado por usuario. */
    @Transactional
    public void saveGraph(UUID uid, UUID mapId, List<NodeDto> nodeDtos, List<EdgeDto> edgeDtos) {
        MentalMap m = maps.findByIdAndUserId(mapId, uid).orElseThrow(() -> nf("Mapa mental"));
        // valida referencias a entidades: somente as do proprio usuario
        if (nodeDtos != null) {
            for (NodeDto d : nodeDtos) {
                if (d.refType() != null && d.refId() != null) {
                    boolean ok = switch (d.refType()) {
                        case "CHARACTER" -> characters.existsByIdAndUserId(d.refId(), uid);
                        case "POWER" -> powers.existsByIdAndUserId(d.refId(), uid);
                        case "STORY" -> stories.existsByIdAndUserId(d.refId(), uid);
                        case "NOTE" -> notes.existsByIdAndUserId(d.refId(), uid);
                        default -> false;
                    };
                    if (!ok) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Referência inválida em nó do mapa.");
                }
            }
        }
        edges.deleteByMapIdAndMapUserId(mapId, uid);
        nodes.deleteByMapIdAndMapUserId(mapId, uid);
        // Sincroniza as colecoes gerenciadas do pai: as remocoes acima sao bulk
        // (bypassam o contexto de persistencia), entao limpamos explicitamente
        // para o orphanRemoval nao apagar os novos nos ao salvar o mapa.
        m.getEdges().clear();
        m.getNodes().clear();
        java.util.Map<UUID, MindMapNode> byClientId = new java.util.HashMap<>();
        if (nodeDtos != null) {
            for (NodeDto d : nodeDtos) {
                MindMapNode n = new MindMapNode();
                n.setMap(m);
                try {
                    n.setNodeType(MindMapNode.NodeType.valueOf(d.nodeType()));
                } catch (Exception e) {
                    n.setNodeType(MindMapNode.NodeType.TEXTO);
                }
                n.setLabel(clip(Sanitizer.textOrEmpty(d.label()), 200));
                if (n.getLabel().isBlank()) n.setLabel("Nó");
                n.setContent(Sanitizer.textOrEmpty(d.content()));
                n.setRefType(d.refType());
                n.setRefId(d.refId());
                n.setPosX(d.x());
                n.setPosY(d.y());
                n.setColor(clip(d.color() == null ? "" : d.color(), 20));
                n.setCollapsed(d.collapsed());
                nodes.save(n);
                m.getNodes().add(n);
                if (d.id() != null) byClientId.put(d.id(), n);
            }
        }
        if (edgeDtos != null) {
            for (EdgeDto d : edgeDtos) {
                MindMapNode s = d.sourceId() == null ? null : byClientId.get(d.sourceId());
                MindMapNode t = d.targetId() == null ? null : byClientId.get(d.targetId());
                if (s == null || t == null) continue;
                MindMapEdge e = new MindMapEdge();
                e.setMap(m);
                e.setSource(s);
                e.setTarget(t);
                e.setLabel(clip(Sanitizer.textOrEmpty(d.label()), 120));
                edges.save(e);
                m.getEdges().add(e);
            }
        }
        maps.save(m); // atualiza updatedAt
    }

    // ==================== DASHBOARD / PESQUISA ====================

    @Transactional(readOnly = true)
    public Dashboard dashboard(UUID uid) {
        return new Dashboard(
                characters.countByUserId(uid),
                powers.countByUserId(uid),
                stories.countByUserId(uid),
                notes.countByUserId(uid),
                maps.countByUserId(uid),
                characters.findTop10ByUserIdOrderByUpdatedAtDesc(uid),
                stories.findTop10ByUserIdOrderByUpdatedAtDesc(uid),
                notes.findTop10ByUserIdOrderByUpdatedAtDesc(uid),
                activity.recent(uid));
    }

    public record Dashboard(long characters, long powers, long stories, long notes, long maps,
                            List<Character> recentCharacters, List<Story> recentStories,
                            List<Note> recentNotes, List<ActivityLog> activities) {}

    @Transactional(readOnly = true)
    public SearchResult globalSearch(UUID uid, String q) {
        String term = q == null ? "" : q.trim();
        if (term.length() < 2) return new SearchResult(List.of(), List.of(), List.of(), List.of(), List.of());
        Pageable top = org.springframework.data.domain.PageRequest.of(0, 6);
        return new SearchResult(
                characters.search(uid, term, top).getContent(),
                powers.search(uid, term, top).getContent(),
                stories.search(uid, term, top).getContent(),
                notes.search(uid, term, top).getContent(),
                maps.search(uid, term, top).getContent());
    }

    public record SearchResult(List<Character> characters, List<Power> powers, List<Story> stories,
                               List<Note> notes, List<MentalMap> maps) {}

    // ==================== helpers ====================

    private static String clip(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max);
    }

    private static String orUntitled(String s) {
        return (s == null || s.isBlank()) ? "Sem título" : s;
    }
}
