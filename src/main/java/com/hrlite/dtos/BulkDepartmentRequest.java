package com.hrlite.dtos;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BulkDepartmentRequest {
    @NotEmpty(message = "At least one department name is required")
    private List<String> names;
}
