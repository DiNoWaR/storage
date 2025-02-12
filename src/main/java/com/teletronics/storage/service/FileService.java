package com.teletronics.storage.service;

import com.teletronics.storage.model.FileEntity;
import com.teletronics.storage.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private final S3Client s3Client;
    private final FileRepository fileRepository;

    @Value("${minio.bucket}")
    private String s3Bucket;

    public FileEntity uploadFile(String userId, MultipartFile file, List<String> tags) {
        var fileKey = UUID.randomUUID() + "_" + file.getOriginalFilename();

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(s3Bucket)
                            .key(fileKey)
                            .build(),
                    RequestBody.fromBytes(file.getBytes())
            );
        } catch (Exception e) {
            throw new RuntimeException("Ошибка загрузки файла в MinIO", e);
        }

        var downloadUrl = String.format("http://localhost:9000/%s/%s", s3Bucket, fileKey);
        var newFile = FileEntity.builder()
                .filename(file.getOriginalFilename())
                .ownerId(userId)
                .tags(tags)
                .size(file.getSize())
                .downloadUrl(downloadUrl)
                .build();

        return fileRepository.save(newFile);
    }

    public boolean fileExists(String userId, MultipartFile file) {
        try {
            String fileHash = generateFileHash(file);
            String filename = file.getOriginalFilename();

            return fileRepository.existsByUserIdAndFilenameOrFileHash(userId, filename, fileHash);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при проверке файла", e);
        }
    }

    public List<FileEntity> getFiles(String tag) {
        return tag == null ? fileRepository.findAll() : fileRepository.findByTagsContaining(tag);
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
}
