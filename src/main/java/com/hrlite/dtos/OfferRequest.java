package com.hrlite.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferRequest {

    @NotNull(message = "Candidate ID is required")
    private UUID candidateId;

    @NotNull(message = "Job opening ID is required")
    private UUID jobOpeningId;

    @NotNull(message = "Offered salary is required")
    private BigDecimal offeredSalary;

    @NotNull(message = "Joining date is required")
    private LocalDate joiningDate;

    private String designation;
    private String department;
    private String offerNotes;
}
