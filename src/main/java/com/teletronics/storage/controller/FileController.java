package com.teletronics.storage.controller;

import com.teletronics.storage.dto.FileEntityDTO;
import com.teletronics.storage.service.FileService;
import com.teletronics.storage.constants.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(FileController.class);
    private final FileService fileService;

    @Operation(summary = "Load file to storage")
    @PostMapping("/")
    public ResponseEntity<?> uploadFile(@RequestParam(value = "file") MultipartFile file,
                                        @RequestParam(value = "is_public", required = false, defaultValue = "true") boolean isPublic,
                                        @RequestParam(value = "tags", required = false) List<String> tags,
                                        @RequestHeader(value = "user_id") String userId) {

        try {
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
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("server error", ex.getMessage()));
        }
    }

    @Operation(summary = "Get files list")
    @GetMapping("/list")
    public ResponseEntity<?> getFiles(
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "uploadDate") String sortField,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @RequestHeader(value = "user_id") String userId) {

        try {
            var finalPage = (page == null || page < 0) ? Constants.DEFAULT_PAGE : page;
            var finalSize = (size == null || size <= 0) ? Constants.DEFAULT_SIZE : size;

            Page<FileEntityDTO> files = fileService.getFiles(userId, tag, finalPage, finalSize, sortField, sortOrder);
            return ResponseEntity.ok(files);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("server error", ex.getMessage()));
        }
    }

    @Operation(summary = "Rename a file")
    @PutMapping("/{fileId}")
    public ResponseEntity<?> renameFile(@PathVariable String fileId,
                                        @RequestParam String newFilename,
                                        @RequestHeader(value = "user_id") String userId) {
        try {
            FileEntityDTO updatedFile = fileService.updateFileName(fileId, newFilename, userId);
            return ResponseEntity.ok(updatedFile);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("server error", ex.getMessage()));
        }
    }

    @Operation(summary = "Delete file")
    @DeleteMapping("/{fileId}")
    public ResponseEntity<?> deleteFile(@PathVariable String fileId,
                                        @RequestHeader(value = "user_id") String userId) {
        try {
            fileService.deleteFile(fileId, userId);
            return ResponseEntity.noContent().build();

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("server error", ex.getMessage()));
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }
}
