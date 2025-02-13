package com.teletronics.storage.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
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

    @Indexed
    private String ownerId;

    @Indexed
    private String filename;

    @Indexed
    private String fileHash;

    @Indexed
    private boolean isPublic;

    @Indexed
    private Set<String> tags;

    private String contentType;
    private long fileSize;
    private Instant uploadDate;

    private String downloadUrl;
}
