package com.teletronics.storage.service;

import com.teletronics.storage.dto.FileEntityDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.teletronics.storage.constants.Constants;
import com.teletronics.storage.model.FileEntity;
import com.teletronics.storage.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class FileService {
    private static final Logger logger = LoggerFactory.getLogger(FileService.class);

    private final Map<String, String> uploadStatus = new ConcurrentHashMap<>();

    private final S3Client s3Client;
    private final FileRepository fileRepository;

    @Value("${minio.bucket}")
    private String s3Bucket;

    @Async("teletronicsPool")
    public void uploadFileAsync(String fileId, String userId, MultipartFile file, boolean isPublic, Set<String> tags) {
        try {
            uploadStatus.put(fileId, Constants.STATUS_IN_PROGRESS);
            var fileName = file.getOriginalFilename().replace(" ", "_");
            var fileKey = userId + "/" + fileName;

            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(s3Bucket)
                            .key(fileKey)
                            .build(),
                    RequestBody.fromBytes(file.getBytes())
            );

            var downloadUrl = String.format("%s/%s/%s", Constants.URL_PREFIX, s3Bucket, fileKey);
            var newFile = FileEntity.builder()
                    .id(fileId)
                    .filename(fileName)
                    .ownerId(userId)
                    .fileHash(generateFileHash(file))
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .uploadDate(Instant.now())
                    .isPublic(isPublic)
                    .tags(tags)
                    .downloadUrl(downloadUrl)
                    .build();

            fileRepository.save(newFile);
            uploadStatus.put(fileId, Constants.STATUS_COMPLETED);
        } catch (Exception ex) {
            uploadStatus.put(fileId, Constants.STATUS_FAILED);
            throw new RuntimeException(Constants.FILE_UPLOAD_ERROR, ex);
        }
    }

    public boolean fileExists(String ownerId, MultipartFile file) {
        try {
            var fileHash = generateFileHash(file);
            var filename = file.getOriginalFilename();

            return Boolean.TRUE.equals(fileRepository.existsByOwnerIdAndFilenameOrFileHash(ownerId, filename, fileHash));
        } catch (Exception ex) {
            logger.error(Constants.FILE_EXISTS_CHECK_ERROR, ex.getMessage(), ex);
            throw new RuntimeException(Constants.FILE_EXISTS_CHECK_ERROR, ex);
        }
    }

    public Page<FileEntityDTO> getFiles(String ownerId, String tag, int page, int size, String sortField, String sortOrder) {
        Sort.Direction direction = sortOrder.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        Page<FileEntity> fileEntities;

        if (tag != null && !tag.isEmpty()) {
            fileEntities = fileRepository.findByTagAndAccess(tag.toLowerCase(), ownerId, pageable);
        } else {
            fileEntities = fileRepository.findByPublicOrOwner(ownerId, pageable);
        }

        return fileEntities.map(fileToDTOMapper);
    }

    public FileEntityDTO updateFileName(String fileId, String newFilename, String userId) {
        var file = fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException(Constants.FILE_NOT_FOUND_ERROR));

        if (!file.getOwnerId().equals(userId)) {
            throw new IllegalArgumentException(Constants.USER_IS_NOT_FILE_OWNER_ERROR);
        }

        if (newFilename == null || newFilename.trim().isEmpty()) {
            throw new IllegalArgumentException(Constants.EMPTY_FILE_NAME_ERROR);
        }

        newFilename = newFilename.trim().replace(" ", "_");
        if (file.getFilename().equals(newFilename)) {
            return fileToDTOMapper.apply(file);
        }

        var oldFileKey = file.getOwnerId() + "/" + file.getFilename();
        var newFileKey = file.getOwnerId() + "/" + newFilename;

        try {
            s3Client.copyObject(CopyObjectRequest.builder()
                    .sourceBucket(s3Bucket)
                    .sourceKey(oldFileKey)
                    .destinationBucket(s3Bucket)
                    .destinationKey(newFileKey)
                    .build());

            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(s3Bucket)
                    .key(oldFileKey)
                    .build());

            var newDownloadUrl = String.format("%s/%s/%s", Constants.URL_PREFIX, s3Bucket, newFileKey);
            file.setFilename(newFilename);
            file.setDownloadUrl(newDownloadUrl);
            fileRepository.save(file);

            logger.info("File renamed: {} -> {}", oldFileKey, newFileKey);
            return fileToDTOMapper.apply(file);

        } catch (Exception ex) {
            logger.error("Failed to rename file: {}, error={}", fileId, ex.getMessage(), ex);
            throw new RuntimeException(Constants.FILE_RENAME_ERROR, ex);
        }
    }

    public String getUploadStatus(String fileId) {
        return uploadStatus.getOrDefault(fileId, Constants.STATUS_NOT_FOUND);
    }

    public void deleteFile(String fileId, String userId) {
        var fileOptional = fileRepository.findById(fileId);
        if (fileOptional.isEmpty()) {
            return;
        }

        var file = fileOptional.get();
        if (!file.getOwnerId().equals(userId)) {
            throw new IllegalArgumentException(Constants.USER_IS_NOT_FILE_OWNER_ERROR);
        }

        var fileKey = file.getOwnerId() + "/" + file.getFilename();
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(s3Bucket)
                    .key(fileKey)
                    .build());

            fileRepository.deleteById(fileId);
        } catch (Exception ex) {
            logger.error("Failed to delete file: {}, error={}", fileId, ex.getMessage(), ex);
            throw new RuntimeException(Constants.FILE_DELETE_ERROR, ex);
        }
    }

    private String generateFileHash(MultipartFile file) throws NoSuchAlgorithmException, IOException {
        var digest = MessageDigest.getInstance("SHA-256");

        try (InputStream inputStream = file.getInputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }

        byte[] hashBytes = digest.digest();
        return Base64.getEncoder().encodeToString(hashBytes);
    }

    private static final Function<FileEntity, FileEntityDTO> fileToDTOMapper = file -> FileEntityDTO.builder()
            .id(file.getId())
            .filename(file.getFilename())
            .tags(file.getTags())
            .contentType(file.getContentType())
            .fileSize(file.getFileSize())
            .uploadDate(file.getUploadDate())
            .downloadUrl(file.getDownloadUrl())
            .build();
}
