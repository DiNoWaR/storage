package com.teletronics.storage.repository;

import com.teletronics.storage.model.TagEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface TagRepository extends MongoRepository<TagEntity, String> {
    long countByNameIgnoreCase(String name);
    long countByNameInIgnoreCase(Set<String> names);

    void deleteByNameIgnoreCase(String name);

    List<TagEntity> findAll();
}
