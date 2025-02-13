package com.teletronics.storage.service;

import com.teletronics.storage.model.TagEntity;
import com.teletronics.storage.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagService {
    private static final Logger logger = LoggerFactory.getLogger(TagService.class);
    private final TagRepository tagRepository;

    public List<String> getTags() {
        return tagRepository.findAll().stream()
                .map(TagEntity::getName)
                .collect(Collectors.toList());
    }

    public void deleteTag(String tagName) {
        var normalizedTag = tagName.trim().toLowerCase();
        tagRepository.deleteByNameIgnoreCase(normalizedTag);
    }

    public String createTag(String tagName) {
        var newTag = new TagEntity(tagName);
        tagRepository.save(newTag);
        return tagName;
    }

    public boolean allTagsExist(Set<String> tags) {
        return tagRepository.countByNameInIgnoreCase(tags) == tags.size();
    }

    public boolean tagExists(String tag) {
        return tagRepository.countByNameIgnoreCase(tag) > 0;
    }
}
