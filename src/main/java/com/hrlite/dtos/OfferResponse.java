package com.hrlite.dtos;

import com.hrlite.enums.OfferStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferResponse {

    private UUID id;
    private UUID candidateId;
    private String candidateName;
    private UUID jobOpeningId;
    private String jobTitle;
    private BigDecimal offeredSalary;
    private LocalDate joiningDate;
    private String designation;
    private String department;
    private String offerNotes;
    private OfferStatus status;
    private LocalDateTime sentAt;
    private LocalDateTime respondedAt;
    private LocalDateTime createdAt;
}
