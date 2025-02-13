package com.teletronics.storage.repository;

import com.teletronics.storage.model.TagEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagRepository extends MongoRepository<TagEntity, String> {
    boolean existsByNameIgnoreCase(String name);

    void deleteByNameIgnoreCase(String name);

    List<TagEntity> findAll();
}
