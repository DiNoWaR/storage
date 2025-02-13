package com.teletronics.storage.service;

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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class FileService {
    private static final Logger logger = LoggerFactory.getLogger(FileService.class);

    private final S3Client s3Client;
    private final FileRepository fileRepository;

    @Value("${minio.bucket}")
    private String s3Bucket;

    public FileEntity uploadFile(String userId, MultipartFile file, boolean isPublic, List<String> tags) {
        var processedTags = processTags(tags);
        var fileName = file.getOriginalFilename().replace(" ", "_");
        var fileKey = userId + "/" + fileName;

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(s3Bucket)
                            .key(fileKey)
                            .build(),
                    RequestBody.fromBytes(file.getBytes())
            );

            var downloadUrl = String.format("%s/%s/%s", Constants.URL_PREFIX, s3Bucket, fileKey);
            var newFile = FileEntity.builder()
                    .filename(fileName)
                    .contentType(file.getContentType())
                    .ownerId(userId)
                    .fileHash(generateFileHash(file))
                    .isPublic(isPublic)
                    .uploadDate(Instant.now())
                    .tags(processedTags)
                    .fileSize(file.getSize())
                    .downloadUrl(downloadUrl)
                    .build();

            logger.info("File uploaded successfully: key={}", fileKey);
            return fileRepository.save(newFile);

        } catch (Exception ex) {
            logger.error("Error uploading file to storage: key={}, error={}", fileKey, ex.getMessage(), ex);
            throw new RuntimeException(Constants.FILE_UPLOAD_ERROR, ex);
        }
    }

    public boolean fileExists(String ownerId, MultipartFile file) {
        try {
            var fileHash = generateFileHash(file);
            var filename = file.getOriginalFilename();

            return fileRepository.existsByOwnerIdAndFilenameOrFileHash(ownerId, filename, fileHash);
        } catch (Exception ex) {
            logger.error(Constants.FILE_EXISTS_CHECK_ERROR, ex.getMessage(), ex);
            throw new RuntimeException(Constants.FILE_EXISTS_CHECK_ERROR, ex);
        }
    }

    public Page<FileEntity> getFiles(String ownerId, String tag, int page, int size, String sortField, String sortOrder) {
        Sort.Direction direction = sortOrder.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        if (tag != null && !tag.isEmpty()) {
            return fileRepository.findByTagAndAccess(tag.toLowerCase(), ownerId, pageable);
        } else {
            return fileRepository.findByPublicOrOwner(ownerId, pageable);
        }
    }

    public void deleteFile(String id) {
        fileRepository.deleteById(id);
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

    private Set<String> processTags(List<String> tags) {
        return tags.stream().map(String::toLowerCase).collect(Collectors.toSet());
    }
}
