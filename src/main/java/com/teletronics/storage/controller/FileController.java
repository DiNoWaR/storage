package com.teletronics.storage.controller;

import com.teletronics.storage.model.FileEntity;
import com.teletronics.storage.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/files")
@Tag(name = "File API", description = "API for file upload")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private final int MAX_TAGS_NUMBER = 5;

    @Operation(summary = "Load file")
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
                                        @RequestParam(value = "is_public", defaultValue = "true") boolean isPublic,
                                        @RequestParam("tags") List<String> tags,
                                        @RequestHeader("user_id") String userId) {

        if (userId == null || userId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Ошибка: user_id обязателен"));
        }

        if (tags.size() > MAX_TAGS_NUMBER) {
            return ResponseEntity.badRequest().body(Map.of("error", "Ошибка: Максимальное количество тегов — 5"));
        }

        return ResponseEntity.ok(fileService.uploadFileToS3(file, tags));
    }

    @Operation(summary = "Get files list")
    @GetMapping("/list")
    public ResponseEntity<List<FileEntity>> getFiles(@RequestParam(required = false) String tag) {
        return ResponseEntity.ok(fileService.getFiles(tag));
    }

    @Operation(summary = "Delete file")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable String id) {
        fileService.deleteFile(id);
        return ResponseEntity.noContent().build();
    }
}
