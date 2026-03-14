package com.hrlite.dtos;

import com.hrlite.enums.CandidateStage;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateStageUpdateRequest {

    @NotNull(message = "Stage is required")
    private CandidateStage stage;

    private String notes;
}
