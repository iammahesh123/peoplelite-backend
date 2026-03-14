package com.hrlite.dtos;

import com.hrlite.enums.PollType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PollRequest {

    @NotNull(message = "Question is required")
    private String question;

    @Builder.Default
    private PollType pollType = PollType.EMOJI;

    @Builder.Default
    private boolean anonymous = true;

    private LocalDateTime expiresAt;
}
