package com.hrlite.controller;

import com.hrlite.dtos.*;
import com.hrlite.security.UserPrincipal;
import com.hrlite.service.OfferLetterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/offer-letters")
@RequiredArgsConstructor
@PreAuthorize("hasRole('FOUNDER')")
public class OfferLetterController {

    private final OfferLetterService offerLetterService;

    @GetMapping("/templates")
    public ResponseEntity<ApiResponse<List<OfferLetterTemplateResponse>>> getTemplates() {
        return ResponseEntity.ok(ApiResponse.success(offerLetterService.getTemplates()));
    }

    @PostMapping("/templates")
    public ResponseEntity<ApiResponse<OfferLetterTemplateResponse>> createTemplate(
            @Valid @RequestBody OfferLetterTemplateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Template created", offerLetterService.createTemplate(request)));
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<OfferLetterResponse>> generateOfferLetter(
            @Valid @RequestBody GenerateOfferLetterRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Offer letter generated",
                        offerLetterService.generateOfferLetter(request, principal)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OfferLetterResponse>>> getOfferLetters() {
        return ResponseEntity.ok(ApiResponse.success(offerLetterService.getOfferLetters()));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadOfferLetterPdf(@PathVariable UUID id) {
        byte[] pdf = offerLetterService.downloadOfferLetterPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=offer-letter.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
