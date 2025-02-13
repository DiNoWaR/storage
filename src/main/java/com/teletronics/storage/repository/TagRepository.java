package com.teletronics.storage.repository;

import com.teletronics.storage.model.TagEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Set;

@Repository
public interface TagRepository extends MongoRepository<TagEntity, String> {
    boolean existsByNameIgnoreCase(String name);

    @Query("{ 'name': { '$in': ?0 } }")
    boolean existsByNameInIgnoreCase(Set<String> tags);

    void deleteByNameIgnoreCase(String name);

    List<TagEntity> findAll();
}
