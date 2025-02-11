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

@RestController
@RequestMapping("/files")
@Tag(name = "File API", description = "API for file upload")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @Operation(summary = "Load file")
    @PostMapping("/upload")
    public ResponseEntity<FileEntity> uploadFile(@RequestParam("file") MultipartFile file,
                                                 @RequestParam("visibility") String visibility,
                                                 @RequestParam("tags") List<String> tags) {
        return ResponseEntity.ok(fileService.uploadFile(file, visibility, tags));
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
