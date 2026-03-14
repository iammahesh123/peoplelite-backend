package com.hrlite.dtos;

import com.hrlite.enums.PollType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PollResponse {

    private UUID id;
    private String question;
    private PollType pollType;
    private boolean anonymous;
    private boolean active;
    private LocalDateTime expiresAt;
    private Map<String, Long> results;
    private long totalResponses;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
