package com.hrlite.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailSettingsResponse {

    private UUID id;
    private UUID tenantId;
    private String smtpHost;
    private int smtpPort;
    private String smtpUsername;
    private String fromEmail;
    private String fromName;
    private boolean useTls;
    private LocalDateTime updatedAt;
}
