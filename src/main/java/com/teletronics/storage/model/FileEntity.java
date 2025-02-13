package com.teletronics.storage.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Set;

@Document(collection = "files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileEntity {
    @Id
    private String id;
    private String ownerId;
    private String filename;
    private boolean isPublic;
    private Set<String> tags;
    private long size;
    private String downloadUrl;
}
