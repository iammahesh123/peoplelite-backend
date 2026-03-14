package com.hrlite.controller;

import com.hrlite.dtos.ApiResponse;
import com.hrlite.security.UserPrincipal;
import com.hrlite.entity.EmployeeDocument;
import com.hrlite.dtos.DocumentResponse;
import com.hrlite.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<DocumentResponse>> uploadDocument(
            @RequestParam UUID employeeId,
            @RequestParam(required = false) String documentType,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal principal) {
        DocumentResponse response = documentService.uploadDocument(employeeId, documentType, file, principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Document uploaded", response));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> getEmployeeDocuments(
            @PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.success(documentService.getEmployeeDocuments(employeeId)));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable UUID id) {
        EmployeeDocument doc = documentService.getDocumentEntity(id);
        byte[] data = documentService.downloadDocument(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(doc.getContentType()))
                .body(data);
    }
}
