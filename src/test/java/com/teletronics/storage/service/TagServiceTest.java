package com.teletronics.storage.service;

import com.teletronics.storage.model.TagEntity;
import com.teletronics.storage.repository.TagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagService tagService;


    @Test
    void shouldCreateTag() {
        var tagName = "kubernetes";
        when(tagRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        String result = tagService.createTag(tagName);
        assertThat(result).isEqualTo(tagName);
        verify(tagRepository, times(1)).save(any(TagEntity.class));
    }

    @Test
    void shouldDeleteTag() {
        var tagName = "java";
        tagService.deleteTag(tagName);
        verify(tagRepository, times(1)).deleteByNameIgnoreCase(tagName);
    }

    @Test
    void shouldCheckIfAllTagsExist() {
        Set<String> tags = Set.of("java", "go");
        when(tagRepository.countByNameInIgnoreCase(tags)).thenReturn(Long.valueOf(2));

        boolean result = tagService.allTagsExist(tags);

        assertThat(result).isTrue();
        verify(tagRepository, times(1)).countByNameInIgnoreCase(tags);
    }

    @Test
    void shouldReturnFalseIfSomeTagsDoNotExist() {
        Set<String> tags = Set.of("java", "unknown");
        when(tagRepository.countByNameInIgnoreCase(tags)).thenReturn(Long.valueOf(1));

        boolean result = tagService.allTagsExist(tags);

        assertThat(result).isFalse();
        verify(tagRepository, times(1)).countByNameInIgnoreCase(tags);
    }

    @Test
    void shouldCheckIfTagExists() {
        String tag = "docker";
        when(tagRepository.countByNameIgnoreCase(tag)).thenReturn(Long.valueOf(1));

        boolean result = tagService.tagExists(tag);

        assertThat(result).isTrue();
        verify(tagRepository, times(1)).countByNameIgnoreCase(tag);
    }

    @Test
    void shouldNormalizeTags() {
        var inputTags = List.of("  Java  ", "Spring ", " DOCKER");
        var expectedTags = Set.of("java", "spring", "docker");

        var processedTags = tagService.processTags(inputTags);

        assertThat(processedTags).isEqualTo(expectedTags);
    }

    @Test
    void shouldReturnExpectedListOfTags() {
        when(tagRepository.findAll()).thenReturn(List.of(
                new TagEntity("java"),
                new TagEntity("go"),
                new TagEntity("python")
        ));

        var tags = tagService.getTags();

        assertThat(tags).containsExactlyInAnyOrder("java", "go", "python");
        verify(tagRepository, times(1)).findAll();
    }
}
