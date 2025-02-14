package com.teletronics.storage.controller;

import com.teletronics.storage.constants.Constants;
import com.teletronics.storage.service.TagService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
public class TagController {
    private static final Logger logger = LoggerFactory.getLogger(TagController.class);
    private final TagService tagService;

    @GetMapping("/list")
    public ResponseEntity<?> getTags() {
        try {
            return ResponseEntity.ok(Map.of("tags", tagService.getTags()));
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("server error", ex.getMessage()));
        }
    }

    @PostMapping("/")
    public ResponseEntity<?> createTag(@RequestParam String tagName) {
        try {
            var normalizedTag = tagName.trim().toLowerCase();
            if (normalizedTag.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", Constants.TAG_IS_EMPTY_ERROR));
            }

            if (tagService.tagExists(normalizedTag)) {
                return ResponseEntity.status(409).body(Map.of("message", Constants.TAG_EXISTS_ERROR));
            }

            tagService.createTag(normalizedTag);
            return ResponseEntity.ok(Map.of("Tag created", normalizedTag));
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("server error", ex.getMessage()));
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteTag(@RequestParam String tagName) {
        try {
            tagService.deleteTag(tagName);
            return ResponseEntity.ok(Map.of("Tag deleted", tagName));
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("server error", ex.getMessage()));
        }
    }
}
