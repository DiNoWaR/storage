package com.teletronics.storage.service;

import com.teletronics.storage.model.FileEntity;
import com.teletronics.storage.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;

    public FileEntity uploadFile(MultipartFile file, String visibility, List<String> tags) {
        FileEntity newFile = FileEntity.builder()
                .filename(file.getOriginalFilename())
                .visibility(visibility)
                .tags(tags)
                .size(file.getSize())
                .downloadUrl("/files/download/" + UUID.randomUUID())
                .build();

        return fileRepository.save(newFile);
    }

    public List<FileEntity> getFiles(String tag) {
        return tag == null ? fileRepository.findAll() : fileRepository.findByTagsContaining(tag);
    }

    public void deleteFile(String id) {
        fileRepository.deleteById(id);
    }
}
