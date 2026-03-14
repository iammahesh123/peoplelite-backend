package com.hrlite.controller;

import com.hrlite.dtos.*;
import com.hrlite.security.UserPrincipal;
import com.hrlite.service.JoiningLetterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/joining-letters")
@RequiredArgsConstructor
@PreAuthorize("hasRole('FOUNDER')")
public class JoiningLetterController {

    private final JoiningLetterService joiningLetterService;

    @GetMapping("/templates")
    public ResponseEntity<ApiResponse<List<JoiningLetterTemplateResponse>>> getTemplates() {
        return ResponseEntity.ok(ApiResponse.success(joiningLetterService.getTemplates()));
    }

    @PostMapping("/templates")
    public ResponseEntity<ApiResponse<JoiningLetterTemplateResponse>> createTemplate(
            @Valid @RequestBody JoiningLetterTemplateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Template created", joiningLetterService.createTemplate(request)));
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<JoiningLetterResponse>> generateJoiningLetter(
            @Valid @RequestBody GenerateJoiningLetterRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Joining letter generated",
                        joiningLetterService.generateLetter(request, principal.getUserId())));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<JoiningLetterResponse>>> getJoiningLetters() {
        return ResponseEntity.ok(ApiResponse.success(joiningLetterService.getLetters()));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadJoiningLetterPdf(@PathVariable UUID id) {
        byte[] pdf = joiningLetterService.getLetterPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=joining-letter.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
