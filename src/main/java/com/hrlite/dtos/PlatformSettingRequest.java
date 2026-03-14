package com.hrlite.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformSettingRequest {

    @NotBlank(message = "Setting key is required")
    private String settingKey;

    private String settingValue;
    private String category;
}
