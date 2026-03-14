package com.hrlite.entity;

import com.hrlite.enums.PollType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "polls")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Poll extends TenantAwareEntity {

    @Column(name = "question", nullable = false)
    private String question;

    @Enumerated(EnumType.STRING)
    @Column(name = "poll_type", nullable = false)
    @Builder.Default
    private PollType pollType = PollType.EMOJI;

    @Column(name = "is_anonymous", nullable = false)
    @Builder.Default
    private boolean anonymous = true;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_by")
    private java.util.UUID createdBy;
}
