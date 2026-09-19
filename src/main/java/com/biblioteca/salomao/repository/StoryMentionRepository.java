package com.biblioteca.salomao.repository;
import com.biblioteca.salomao.domain.StoryMention;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
public interface StoryMentionRepository extends JpaRepository<StoryMention, UUID> {
    List<StoryMention> findByStoryIdAndStoryUserId(UUID storyId, UUID userId);
    List<StoryMention> findByCharacterIdAndCharacterUserId(UUID characterId, UUID userId);
    void deleteByStoryIdAndCharacterId(UUID storyId, UUID characterId);
    boolean existsByStoryIdAndCharacterId(UUID storyId, UUID characterId);
}
