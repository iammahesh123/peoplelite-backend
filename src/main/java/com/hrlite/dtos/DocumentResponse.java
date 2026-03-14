package com.hrlite.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DocumentResponse {
    private UUID id;
    private UUID employeeId;
    private String documentType;
    private String fileName;
    private long fileSize;
    private String contentType;
    private LocalDateTime createdAt;
}
