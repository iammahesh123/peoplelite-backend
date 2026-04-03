package com.hrlite.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private UUID userId;
    private UUID tenantId;
    private String email;
    private String role;
    private String fullName;
    private UUID employeeId;
    private String plan;
    @Builder.Default
    private boolean passwordChangeRequired = false;
}
