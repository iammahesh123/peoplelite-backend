package com.hrlite.dtos;

import com.hrlite.enums.ExitChecklistCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExitChecklistItemResponse {

    private UUID id;
    private String itemName;
    private ExitChecklistCategory category;
    private boolean completed;
    private UUID completedBy;
    private LocalDateTime completedAt;
    private String notes;
    private LocalDateTime createdAt;
}
