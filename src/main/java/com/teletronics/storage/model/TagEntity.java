package com.teletronics.storage.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "tags")
@NoArgsConstructor
@AllArgsConstructor
public class TagEntity {

    @Id
    private String id;

    @Getter
    @Indexed(unique = true)
    private String name;

    public TagEntity(String name) {
        this.name = name;
    }
}