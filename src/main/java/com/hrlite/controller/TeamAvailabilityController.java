package com.hrlite.controller;

import com.hrlite.dtos.ApiResponse;
import com.hrlite.dtos.TeamAvailabilityResponse;
import com.hrlite.service.TeamAvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/team-availability")
@RequiredArgsConstructor
public class TeamAvailabilityController {

    private final TeamAvailabilityService teamAvailabilityService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TeamAvailabilityResponse>>> getTeamAvailabilityForDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<TeamAvailabilityResponse> availability = teamAvailabilityService.getTeamAvailabilityForDate(date);
        return ResponseEntity.ok(ApiResponse.success(availability));
    }

    @GetMapping("/week")
    public ResponseEntity<ApiResponse<List<TeamAvailabilityResponse>>> getTeamAvailabilityForWeek(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {
        List<TeamAvailabilityResponse> availability = teamAvailabilityService.getTeamAvailabilityForWeek(startDate);
        return ResponseEntity.ok(ApiResponse.success(availability));
    }
}
