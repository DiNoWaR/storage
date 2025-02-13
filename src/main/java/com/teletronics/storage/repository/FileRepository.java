package com.teletronics.storage.repository;

import com.teletronics.storage.model.FileEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileRepository extends MongoRepository<FileEntity, String> {
    List<FileEntity> findByTagsContaining(String tag);

    boolean existsByOwnerIdAndFilenameOrFileHash(String ownerId, String filename, String fileHash);
}