package com.teletronics.storage.repository;

import com.teletronics.storage.model.FileEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends MongoRepository<FileEntity, String> {

    @Query("{'$or': [{'isPublic': true}, {'ownerId': ?1}], 'tags': ?0}")
    Page<FileEntity> findByTagAndAccess(String tag, String ownerId, Pageable pageable);

    @Query("{'$or': [{'isPublic': true}, {'ownerId': ?0}]}")
    Page<FileEntity> findByPublicOrOwner(String ownerId, Pageable pageable);

    boolean existsByOwnerIdAndFilenameOrFileHash(String ownerId, String filename, String fileHash);
}