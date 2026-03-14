package com.hrlite.dtos;

import com.hrlite.enums.SeparationStatus;
import com.hrlite.enums.SeparationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeparationResponse {

    private UUID id;
    private UUID employeeId;
    private String employeeName;
    private SeparationType separationType;
    private LocalDate resignationDate;
    private LocalDate lastWorkingDate;
    private Integer noticePeriodDays;
    private String reason;
    private String exitInterviewNotes;
    private SeparationStatus status;
    private BigDecimal finalSettlementAmount;
    private BigDecimal remainingLeaves;
    private List<ExitChecklistItemResponse> checklistItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
