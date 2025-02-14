package com.teletronics.storage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teletronics.storage.constants.Constants;
import com.teletronics.storage.dto.FileEntityDTO;
import com.teletronics.storage.service.FileService;
import com.teletronics.storage.service.TagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FileController.class)
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileService fileService;

    @MockBean
    private TagService tagService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private String userId;
    private String fileId;
    private FileEntityDTO fileDTO;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID().toString();
        fileId = UUID.randomUUID().toString();

        fileDTO = FileEntityDTO.builder()
                .id(fileId)
                .filename("test_file.txt")
                .tags(Set.of("java", "go"))
                .contentType("text/plain")
                .fileSize(12345)
                .uploadDate(Instant.now())
                .downloadUrl("http://minio/teletroincs/test_file.txt")
                .build();
    }

    @Test
    void shouldStartFileUpload() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt",
                "text/plain", "Hello World".getBytes());

        when(fileService.fileExists(anyString(), any())).thenReturn(false);
        when(tagService.allTagsExist(any())).thenReturn(true);
        doNothing().when(fileService).uploadFileAsync(anyString(), anyString(), any(), anyBoolean(), any());

        mockMvc.perform(multipart("/files/")
                        .file(file)
                        .param("is_public", "true")
                        .param("tags", "java", "go")
                        .header("user_id", userId))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.message").value("File upload started"))
                .andExpect(jsonPath("$.status").value("in_progress"))
                .andExpect(jsonPath("$.file_id").isNotEmpty());
    }

    @Test
    void shouldCheckFileUploadStatus() throws Exception {
        when(fileService.getUploadStatus(fileId)).thenReturn(Constants.STATUS_COMPLETED);

        mockMvc.perform(get("/files/status/{fileId}", fileId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.file_id").value(fileId))
                .andExpect(jsonPath("$.status").value(Constants.STATUS_COMPLETED));
    }

    @Test
    void shouldGetFilesList() throws Exception {
        Page<FileEntityDTO> filesPage = new PageImpl<>(List.of(fileDTO));
        when(fileService.getFiles(anyString(), any(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(filesPage);

        mockMvc.perform(get("/files/list")
                        .header("user_id", userId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.files.content[0].filename").value("test_file.txt"));
    }

    @Test
    void shouldRenameFile() throws Exception {
        when(fileService.updateFileName(fileId, "new_name.txt", userId)).thenReturn(fileDTO);

        mockMvc.perform(put("/files/{fileId}", fileId)
                        .param("newFilename", "new_name.txt")
                        .header("user_id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['updated file'].filename").value("test_file.txt"));
    }

    @Test
    void shouldDeleteFile() throws Exception {
        doNothing().when(fileService).deleteFile(fileId, userId);

        mockMvc.perform(delete("/files/{fileId}", fileId)
                        .header("user_id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.file_id").value(fileId))
                .andExpect(jsonPath("$.message").value("File deleted"));
    }
}
