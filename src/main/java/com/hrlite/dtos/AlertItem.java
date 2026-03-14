package com.hrlite.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertItem {
    private String id;
    private String type; // INFO, WARNING, ERROR
    private String title;
    private String message;
    private LocalDateTime createdAt;
    private boolean read;
}
