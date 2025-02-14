package com.teletronics.storage.controller;

import com.teletronics.storage.dto.FileEntityDTO;
import com.teletronics.storage.service.FileService;
import com.teletronics.storage.constants.Constants;
import com.teletronics.storage.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/files")
@Tag(name = "Files", description = "API for file upload")
@RequiredArgsConstructor
@Validated
public class FileController {
    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    private final FileService fileService;
    private final TagService tagService;

    @Operation(summary = "Start file upload")
    @PostMapping("/")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "is_public", required = false, defaultValue = "true") boolean isPublic,
            @RequestParam(value = "tags", required = false) @Size(max = 5) List<String> tags,
            @RequestHeader("user_id") String userId) {

        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", Constants.EMPTY_FILE_ERROR));
            }

            if (fileService.fileExists(userId, file)) {
                return ResponseEntity.status(409).body(Map.of("message", Constants.FILE_EXISTS_ERROR));
            }

            var processedTags = tagService.processTags(tags);
            if (!tagService.allTagsExist(processedTags)) {
                return ResponseEntity.badRequest().body(Map.of("message", Constants.TAG_IS_NOT_ALLOWED_ERROR + processedTags));
            }

            var fileId = UUID.randomUUID().toString();
            fileService.uploadFileAsync(fileId, userId, file, isPublic, processedTags);

            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(Map.of(
                            "message", "File upload started",
                            "status", Constants.STATUS_IN_PROGRESS,
                            "file_id", fileId));

        } catch (Exception ex) {
            logger.error("File upload failed for user: {}. File: {}. Error: {}", userId, file.getOriginalFilename(), ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("server error", ex.getMessage()));
        }
    }

    @Operation(summary = "Check file upload status")
    @GetMapping("/status/{fileId}")
    public ResponseEntity<?> getFileUploadStatus(@PathVariable String fileId) {
        var status = fileService.getUploadStatus(fileId);
        return ResponseEntity.ok(Map.of("file_id", fileId, "status", status));
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
            if (!Constants.ALLOWED_SORTFIELDS.contains(sortField.toLowerCase())) {
                return ResponseEntity.badRequest().body(Map.of("error", Constants.INVALID_SORT_FIELD_ERROR + sortField));
            }

            if (!Constants.ALLOWED_SORT_ORDERS.contains(sortOrder.toLowerCase())) {
                return ResponseEntity.badRequest().body(Map.of("error", Constants.INVALID_SORT_ORDER_ERROR + sortOrder));
            }

            var finalPage = (page == null || page < 0) ? Constants.DEFAULT_PAGE : page;
            var finalSize = (size == null || size <= 0) ? Constants.DEFAULT_SIZE : size;

            Page<FileEntityDTO> files = fileService.getFiles(userId, tag, finalPage, finalSize, sortField, sortOrder);
            return ResponseEntity.ok(Map.of("files", files));
        } catch (Exception ex) {
            logger.error("File upload failed for user: {}. Error: {}", userId, ex.getMessage(), ex);
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
            return ResponseEntity.ok(Map.of("updated file", updatedFile));
        } catch (Exception ex) {
            logger.error("File upload failed for user: {}. Error: {}", userId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("server error", ex.getMessage()));
        }
    }

    @Operation(summary = "Delete file")
    @DeleteMapping("/{fileId}")
    public ResponseEntity<?> deleteFile(@PathVariable String fileId,
                                        @RequestHeader(value = "user_id") String userId) {
        try {
            fileService.deleteFile(fileId, userId);
            return ResponseEntity.ok(Map.of("file_id", fileId, "message", "File deleted"));

        } catch (Exception ex) {
            logger.error("File upload failed for user: {}. Error: {}", userId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("server error", ex.getMessage()));
        }
    }
}
