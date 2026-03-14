package com.hrlite.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProbationUpdateRequest {

    @NotNull(message = "New probation end date is required")
    private LocalDate newProbationEndDate;

    private String reason;
}
