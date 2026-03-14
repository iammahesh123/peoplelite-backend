package com.hrlite.controller;

import com.hrlite.dtos.ApiResponse;
import com.hrlite.dtos.PlanInfoResponse;
import com.hrlite.service.PlanInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tenant/plan-info")
@RequiredArgsConstructor
public class PlanInfoController {

    private final PlanInfoService planInfoService;

    @GetMapping
    public ResponseEntity<ApiResponse<PlanInfoResponse>> getPlanInfo() {
        PlanInfoResponse response = planInfoService.getPlanInfo();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
