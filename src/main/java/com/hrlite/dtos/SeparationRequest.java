package com.hrlite.dtos;

import com.hrlite.enums.SeparationType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeparationRequest {

    @NotNull(message = "Employee ID is required")
    private UUID employeeId;

    @NotNull(message = "Separation type is required")
    private SeparationType separationType;

    private LocalDate resignationDate;

    @NotNull(message = "Last working date is required")
    private LocalDate lastWorkingDate;

    @Builder.Default
    private Integer noticePeriodDays = 30;

    private String reason;
}
