package com.teletronics.storage.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Set;

@Getter
@Setter
@Builder
public class FileEntityDTO {
    private String id;
    private String filename;
    private Set<String> tags;
    private String contentType;
    private long fileSize;
    private Instant uploadDate;
    private String downloadUrl;
}
