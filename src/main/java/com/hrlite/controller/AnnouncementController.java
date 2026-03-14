package com.hrlite.controller;

import com.hrlite.dtos.ApiResponse;
import com.hrlite.entity.Announcement;
import com.hrlite.security.UserPrincipal;
import com.hrlite.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @PostMapping
    @PreAuthorize("hasAnyRole('FOUNDER', 'HR_MANAGER')")
    public ResponseEntity<ApiResponse<Announcement>> create(@RequestBody Map<String, Object> body) {
        String title = (String) body.get("title");
        String content = (String) body.get("content");
        String priority = (String) body.getOrDefault("priority", "NORMAL");
        boolean pinned = Boolean.TRUE.equals(body.get("pinned"));
        LocalDateTime expiresAt = body.get("expiresAt") != null ? LocalDateTime.parse((String) body.get("expiresAt")) : null;

        Announcement announcement = announcementService.create(title, content, priority, pinned, expiresAt);
        return ResponseEntity.ok(ApiResponse.success("Announcement created", announcement));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('FOUNDER', 'HR_MANAGER')")
    public ResponseEntity<ApiResponse<Announcement>> update(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        String title = (String) body.get("title");
        String content = (String) body.get("content");
        String priority = (String) body.getOrDefault("priority", "NORMAL");
        boolean pinned = Boolean.TRUE.equals(body.get("pinned"));
        LocalDateTime expiresAt = body.get("expiresAt") != null ? LocalDateTime.parse((String) body.get("expiresAt")) : null;

        Announcement announcement = announcementService.update(id, title, content, priority, pinned, expiresAt);
        return ResponseEntity.ok(ApiResponse.success("Announcement updated", announcement));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('FOUNDER', 'HR_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        announcementService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Announcement deleted"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<Announcement>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Page<Announcement> announcements = announcementService.getAll(principal.getTenantId(), PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(announcements));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<Announcement>>> getActive() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Announcement> announcements = announcementService.getActive(principal.getTenantId());
        return ResponseEntity.ok(ApiResponse.success(announcements));
    }
}
