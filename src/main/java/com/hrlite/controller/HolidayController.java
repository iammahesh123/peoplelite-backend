package com.hrlite.controller;

import com.hrlite.dtos.ApiResponse;
import com.hrlite.dtos.HolidayRequest;
import com.hrlite.entity.Holiday;
import com.hrlite.security.UserPrincipal;
import com.hrlite.service.HolidayService;
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
@RequestMapping("/api/v1/holidays")
@RequiredArgsConstructor
public class HolidayController {

    private final HolidayService holidayService;

    @PostMapping
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<Holiday>> createHoliday(
            @Valid @RequestBody HolidayRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        Holiday holiday = holidayService.create(
                request.getName(), request.getDate(), request.isOptional());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Holiday created", holiday));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<Void>> deleteHoliday(@PathVariable UUID id) {
        holidayService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Holiday deleted", null));
    }

    @GetMapping
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<List<Holiday>>> getHolidaysByYear(
            @RequestParam int year) {
        return ResponseEntity.ok(ApiResponse.success(holidayService.getByYear(year)));
    }
}
