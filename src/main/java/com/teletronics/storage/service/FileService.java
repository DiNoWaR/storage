package com.teletronics.storage.service;

import com.teletronics.storage.constants.Constants;
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
import java.util.Set;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class FileService {

    private final S3Client s3Client;
    private final FileRepository fileRepository;

    @Value("${minio.bucket}")
    private String s3Bucket;

    public FileEntity uploadFile(String userId, MultipartFile file, boolean isPublic, List<String> tags) {
        var processedTags = processTags(tags);
        var fileKey = userId + "/" + file.getOriginalFilename();

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(s3Bucket)
                            .key(fileKey)
                            .build(),
                    RequestBody.fromBytes(file.getBytes())
            );
        } catch (Exception ex) {
            throw new RuntimeException(Constants.FILE_UPLOAD_ERROR, ex);
        }

        var downloadUrl = String.format("%s/%s/%s", Constants.MINIO_PREFIX, s3Bucket, fileKey);
        var newFile = FileEntity.builder()
                .filename(file.getOriginalFilename())
                .ownerId(userId)
                .isPublic(isPublic)
                .tags(processedTags)
                .size(file.getSize())
                .downloadUrl(downloadUrl)
                .build();

        return fileRepository.save(newFile);
    }

    public boolean fileExists(String ownerId, MultipartFile file) {
        try {
            String fileHash = generateFileHash(file);
            String filename = file.getOriginalFilename();

            return fileRepository.existsByOwnerIdAndFilenameOrFileHash(ownerId, filename, fileHash);
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

    private Set<String> processTags(List<String> tags) {
        return tags.stream().map(String::toLowerCase).collect(Collectors.toSet());
    }
}
