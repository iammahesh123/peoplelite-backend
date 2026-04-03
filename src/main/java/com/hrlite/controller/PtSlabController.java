package com.hrlite.controller;

import com.hrlite.dtos.ApiResponse;
import com.hrlite.entity.PtSlab;
import com.hrlite.security.UserPrincipal;
import com.hrlite.service.PtSlabService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pt-slabs")
@RequiredArgsConstructor
public class PtSlabController {

    private final PtSlabService ptSlabService;

    @GetMapping
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<List<PtSlab>>> getSlabs() {
        return ResponseEntity.ok(ApiResponse.success(ptSlabService.getSlabs()));
    }

    @PostMapping
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<List<PtSlab>>> saveSlabs(
            @Valid @RequestBody List<PtSlab> slabs,
            @AuthenticationPrincipal UserPrincipal principal) {
        List<PtSlab> savedSlabs = ptSlabService.saveSlabs(slabs);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("PT slabs saved", savedSlabs));
    }
}
