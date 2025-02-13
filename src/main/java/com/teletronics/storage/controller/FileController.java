package com.teletronics.storage.controller;

import com.teletronics.storage.model.FileEntity;
import com.teletronics.storage.service.FileService;
import com.teletronics.storage.constants.Constants;
import com.teletronics.storage.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/files")
@Tag(name = "Files", description = "API for file upload")
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

    @Operation(summary = "Load file to storage")
    @PostMapping("/")
    public ResponseEntity<?> uploadFile(@RequestParam(value = "file") MultipartFile file,
                                        @RequestParam(value = "is_public", required = false, defaultValue = "true") boolean isPublic,
                                        @RequestParam(value = "tags", required = false) List<String> tags,
                                        @RequestHeader(value = "user_id") String userId) {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", Constants.EMPTY_FILE_ERROR));
        }

        if (tags.size() > Constants.MAX_TAGS_NUMBER) {
            return ResponseEntity.badRequest().body(Map.of("message", Constants.TAGS_NUMBER_EXCEED_ERROR));
        }

        if (fileService.fileExists(userId, file)) {
            return ResponseEntity.status(409).body(Map.of("message", Constants.FILE_EXISTS_ERROR));
        }
        return ResponseEntity.ok(fileService.uploadFile(userId, file, isPublic, tags));
    }

    @GetMapping("/list")
    public ResponseEntity<Page<FileEntity>> getFiles(
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "uploadDate") String sortField,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @RequestHeader(value = "user_id") String userId) {

        Page<FileEntity> files = fileService.getFiles(userId, tag, page, size, sortField, sortOrder);
        return ResponseEntity.ok(files);
    }

    @Operation(summary = "Delete file")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable String id) {
        fileService.deleteFile(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }
}
