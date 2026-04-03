package com.hrlite.dtos;

import com.hrlite.enums.BonusType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BonusRequest {

    @NotNull(message = "Bonus name is required")
    private String name;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    @NotNull(message = "Bonus type is required")
    private BonusType bonusType;

    @NotNull(message = "Month is required")
    @Min(1)
    private Integer month;

    @NotNull(message = "Year is required")
    @Min(2020)
    private Integer year;

    private UUID employeeId;
    private boolean applyToAll;
    private String remarks;
}
