package com.hrlite.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "poll_responses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PollResponse extends TenantAwareEntity {

    @Column(name = "poll_id", nullable = false)
    private UUID pollId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "response_value", nullable = false)
    private String responseValue;
}
