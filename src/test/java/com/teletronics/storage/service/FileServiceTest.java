package com.teletronics.storage.service;

import com.teletronics.storage.constants.Constants;
import com.teletronics.storage.dto.FileEntityDTO;
import com.teletronics.storage.model.FileEntity;
import com.teletronics.storage.repository.FileRepository;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private MultipartFile mockFile;

    @InjectMocks
    private FileService fileService;

    private FileEntity testFileEntity;
    private final String fileId = "12345";
    private final String userId = "user123";
    private final String fileName = "test.txt";
    private final String newFileName = "updated.txt";
    private final Set<String> tags = Set.of("java", "spring");
    private final String fileKey = userId + "/" + fileName;

    @BeforeEach
    void setUp() {
        testFileEntity = FileEntity.builder()
                .id(fileId)
                .filename(fileName)
                .ownerId(userId)
                .fileHash("hash123")
                .contentType("text/plain")
                .fileSize(1024)
                .uploadDate(Instant.now())
                .isPublic(true)
                .tags(tags)
                .downloadUrl("http://teletronics/" + fileKey)
                .build();
    }

    @Test
    void shouldCheckIfFileExists() throws Exception {
        when(mockFile.getOriginalFilename()).thenReturn(fileName);
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("test data".getBytes())); // Эмуляция InputStream
        when(fileRepository.existsByOwnerIdAndFilenameOrFileHash(anyString(), anyString(), anyString()))
                .thenReturn(true);

        boolean exists = fileService.fileExists(userId, mockFile);

        assertThat(exists).isTrue();
        verify(fileRepository, times(1)).existsByOwnerIdAndFilenameOrFileHash(anyString(), anyString(), anyString());
    }

    @Test
    void shouldGetFiles() {
        var page = new PageImpl<>(List.of(testFileEntity), PageRequest.of(0, 10), 1); // Создаём Page
        when(fileRepository.findByPublicOrOwner(anyString(), any())).thenReturn(page);

        var files = fileService.getFiles(userId, null, 0, 10, "uploadDate", "desc");

        assertThat(files.getContent()).hasSize(1);
        verify(fileRepository, times(1)).findByPublicOrOwner(anyString(), any());
    }

    @Test
    void shouldRenameFile() {
        when(fileRepository.findById(fileId)).thenReturn(Optional.of(testFileEntity));
        when(s3Client.copyObject(any(CopyObjectRequest.class))).thenReturn(CopyObjectResponse.builder().build());
        when(s3Client.deleteObject(any(DeleteObjectRequest.class))).thenReturn(DeleteObjectResponse.builder().build());

        FileEntityDTO updatedFile = fileService.updateFileName(fileId, newFileName, userId);

        assertThat(updatedFile.getFilename()).isEqualTo(newFileName);
        verify(fileRepository, times(1)).save(any(FileEntity.class));
    }

    @Test
    void shouldNotRenameFileIfNotOwner() {
        when(fileRepository.findById(fileId)).thenReturn(Optional.of(testFileEntity));

        var exception = assertThrows(IllegalArgumentException.class,
                () -> fileService.updateFileName(fileId, newFileName, "wrongUser"));

        assertThat(exception.getMessage()).isEqualTo(Constants.USER_IS_NOT_FILE_OWNER_ERROR);
    }

    @Test
    void shouldGetUploadStatus() throws Exception {
        when(mockFile.getOriginalFilename()).thenReturn("testfile.txt");
        when(mockFile.getBytes()).thenReturn("test content".getBytes());
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("test content".getBytes()));

        fileService.uploadFileAsync(fileId, userId, mockFile, true, tags);

        Awaitility.await()
                .atMost(1, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> assertThat(fileService.getUploadStatus(fileId)).isEqualTo(Constants.STATUS_COMPLETED));
    }

    @Test
    void shouldDeleteFile() {
        when(fileRepository.findById(fileId)).thenReturn(Optional.of(testFileEntity));
        when(s3Client.deleteObject(any(DeleteObjectRequest.class))).thenReturn(DeleteObjectResponse.builder().build());

        fileService.deleteFile(fileId, userId);

        verify(fileRepository, times(1)).deleteById(fileId);
    }

    @Test
    void shouldNotDeleteFileIfNotOwner() {
        when(fileRepository.findById(fileId)).thenReturn(Optional.of(testFileEntity));

        var exception = assertThrows(IllegalArgumentException.class,
                () -> fileService.deleteFile(fileId, "wrongUser"));

        assertThat(exception.getMessage()).isEqualTo(Constants.USER_IS_NOT_FILE_OWNER_ERROR);
    }
}
