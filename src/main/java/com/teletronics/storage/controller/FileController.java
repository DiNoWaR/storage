package com.teletronics.storage.controller;

import com.teletronics.storage.model.FileEntity;
import com.teletronics.storage.service.FileService;
import com.teletronics.storage.constants.Constants;
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

    @Operation(summary = "Load file to storage")
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
                                        @RequestParam(value = "is_public", defaultValue = "true") boolean isPublic,
                                        @RequestParam("tags") List<String> tags,
                                        @RequestHeader("user_id") String userId) {

        if (userId == null || userId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Ошибка: user_id обязателен"));
        }

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Ошибка: Файл обязателен и не может быть пустым"));
        }

        if (tags.size() > Constants.MAX_TAGS_NUMBER) {
            return ResponseEntity.badRequest().body(Map.of("message", "Ошибка: Максимальное количество тегов — 5"));
        }

        if (fileService.fileExists(userId, file)) {
            return ResponseEntity.status(409).body(Map.of("message", "Файл уже загружен"));
        }

        return ResponseEntity.ok(fileService.uploadFile(userId, file, tags));
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
