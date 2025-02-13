package com.teletronics.storage.controller;

import com.teletronics.storage.constants.Constants;
import com.teletronics.storage.service.TagService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
public class TagController {
    private static final Logger logger = LoggerFactory.getLogger(TagController.class);
    private final TagService tagService;

    @GetMapping("/list")
    public List<String> getTags() {
        return tagService.getTags();
    }

    @PostMapping("/")
    public ResponseEntity<?> createTag(@RequestParam String tagName) {
        var normalizedTag = tagName.trim().toLowerCase();
        if (normalizedTag.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", Constants.TAG_IS_EMPTY_ERROR));
        }

        if (tagService.tagExists(normalizedTag)) {
            return ResponseEntity.status(409).body(Map.of("message", Constants.TAG_EXISTS_ERROR));
        }

        tagService.createTag(normalizedTag);
        return ResponseEntity.ok(normalizedTag);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteTag(@RequestParam String tagName) {
        tagService.deleteTag(tagName);
        return ResponseEntity.ok("Tag deleted: " + tagName);
    }
}
