package com.hrlite.controller;

import com.hrlite.dtos.ApiResponse;
import com.hrlite.entity.SelfBoardingInvitation;
import com.hrlite.service.SelfBoardingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/self-boarding")
@RequiredArgsConstructor
public class SelfBoardingController {

    private final SelfBoardingService selfBoardingService;

    @PostMapping("/invite/{employeeId}")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<SelfBoardingInvitation>> sendInvitation(
            @PathVariable UUID employeeId,
            @RequestBody Map<String, String> body) {
        String baseUrl = body.getOrDefault("baseUrl", "https://peoplelite.vercel.app");
        SelfBoardingInvitation invitation = selfBoardingService.sendInvitation(employeeId, baseUrl);
        return ResponseEntity.ok(ApiResponse.success("Invitation sent successfully", invitation));
    }

    @GetMapping("/invitations")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<List<SelfBoardingInvitation>>> getInvitations() {
        return ResponseEntity.ok(ApiResponse.success("Invitations retrieved", selfBoardingService.getInvitations()));
    }

    @GetMapping("/validate/{token}")
    public ResponseEntity<ApiResponse<SelfBoardingInvitation>> validateToken(@PathVariable String token) {
        SelfBoardingInvitation invitation = selfBoardingService.validateToken(token);
        return ResponseEntity.ok(ApiResponse.success("Token is valid", invitation));
    }

    @PostMapping("/complete/{token}")
    public ResponseEntity<ApiResponse<SelfBoardingInvitation>> completeOnboarding(@PathVariable String token) {
        SelfBoardingInvitation invitation = selfBoardingService.completeOnboarding(token);
        return ResponseEntity.ok(ApiResponse.success("Onboarding completed", invitation));
    }
}
