package com.hrlite.dtos;

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
public class PlatformSettingResponse {

    private UUID id;
    private String settingKey;
    private String settingValue;
    private String category;
    private LocalDateTime updatedAt;
}
