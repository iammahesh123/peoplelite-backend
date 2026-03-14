package com.hrlite.controller;

import com.hrlite.dtos.ApiResponse;
import com.hrlite.security.UserPrincipal;
import com.hrlite.dtos.JoiningLetterResponse;
import com.hrlite.service.JoiningLetterService;
import com.hrlite.dtos.OfferLetterResponse;
import com.hrlite.service.OfferLetterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/my-letters")
@RequiredArgsConstructor
public class EmployeeLetterController {

    private final OfferLetterService offerLetterService;
    private final JoiningLetterService joiningLetterService;

    @GetMapping("/offer-letters")
    public ResponseEntity<ApiResponse<List<OfferLetterResponse>>> getMyOfferLetters(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<OfferLetterResponse> letters = offerLetterService.getOfferLettersByEmployee(principal.getEmployeeId());
        return ResponseEntity.ok(ApiResponse.success(letters));
    }

    @GetMapping("/offer-letters/{id}/pdf")
    public ResponseEntity<byte[]> downloadMyOfferLetterPdf(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        byte[] pdf = offerLetterService.downloadOfferLetterPdfForEmployee(id, principal.getEmployeeId());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=offer-letter.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/joining-letters")
    public ResponseEntity<ApiResponse<List<JoiningLetterResponse>>> getMyJoiningLetters(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<JoiningLetterResponse> letters = joiningLetterService.getLettersByEmployee(principal.getEmployeeId());
        return ResponseEntity.ok(ApiResponse.success(letters));
    }

    @GetMapping("/joining-letters/{id}/pdf")
    public ResponseEntity<byte[]> downloadMyJoiningLetterPdf(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        byte[] pdf = joiningLetterService.getLetterPdfForEmployee(id, principal.getEmployeeId());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=joining-letter.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
