package com.teletronics.storage.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileEntity {
    @Id
    private String id;
    private String filename;
    private String visibility;
    private List<String> tags;
    private long size;
    private String downloadUrl;
}
