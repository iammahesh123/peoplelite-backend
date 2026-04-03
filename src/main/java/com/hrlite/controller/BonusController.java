package com.hrlite.controller;

import com.hrlite.dtos.ApiResponse;
import com.hrlite.dtos.BonusRequest;
import com.hrlite.entity.Bonus;
import com.hrlite.security.UserPrincipal;
import com.hrlite.service.BonusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bonuses")
@RequiredArgsConstructor
public class BonusController {

    private final BonusService bonusService;

    @PostMapping
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<Bonus>> createBonus(
            @Valid @RequestBody BonusRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        Bonus bonus = bonusService.createBonus(
                request.getName(), request.getAmount(), request.getBonusType(),
                request.getMonth(), request.getYear(), request.getEmployeeId(),
                request.isApplyToAll(), request.getRemarks(), principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bonus created", bonus));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<Void>> deleteBonus(@PathVariable UUID id) {
        bonusService.deleteBonus(id);
        return ResponseEntity.ok(ApiResponse.success("Bonus deleted", null));
    }

    @GetMapping
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<List<Bonus>>> getAllBonuses() {
        return ResponseEntity.ok(ApiResponse.success(bonusService.getAllBonuses()));
    }

    @GetMapping("/month")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<List<Bonus>>> getBonusesByMonth(
            @RequestParam int month, @RequestParam int year) {
        return ResponseEntity.ok(ApiResponse.success(bonusService.getBonuses(month, year)));
    }
}
