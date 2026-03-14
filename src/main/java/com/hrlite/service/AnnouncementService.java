package com.hrlite.service;

import com.hrlite.entity.Announcement;
import com.hrlite.exception.BusinessException;
import com.hrlite.exception.ErrorCodes;
import com.hrlite.repository.AnnouncementRepository;
import com.hrlite.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final AuditService auditService;

    public Announcement create(String title, String content, String priority, boolean pinned, LocalDateTime expiresAt) {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Announcement announcement = Announcement.builder()
                .tenantId(principal.getTenantId())
                .title(title)
                .content(content)
                .priority(priority != null ? priority : "NORMAL")
                .pinned(pinned)
                .authorId(principal.getUserId())
                .authorName(principal.getEmail())
                .publishedAt(LocalDateTime.now())
                .expiresAt(expiresAt)
                .build();

        Announcement saved = announcementRepository.save(announcement);
        auditService.log("CREATE", "Announcement", saved.getId().toString(), "Created announcement: " + title);
        return saved;
    }

    public Announcement update(UUID id, String title, String content, String priority, boolean pinned, LocalDateTime expiresAt) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCodes.RESOURCE_NOT_FOUND, "Announcement not found"));

        announcement.setTitle(title);
        announcement.setContent(content);
        announcement.setPriority(priority != null ? priority : announcement.getPriority());
        announcement.setPinned(pinned);
        announcement.setExpiresAt(expiresAt);
        announcement.setUpdatedAt(LocalDateTime.now());

        Announcement saved = announcementRepository.save(announcement);
        auditService.log("UPDATE", "Announcement", saved.getId().toString(), "Updated announcement: " + title);
        return saved;
    }

    public void delete(UUID id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCodes.RESOURCE_NOT_FOUND, "Announcement not found"));
        announcementRepository.delete(announcement);
        auditService.log("DELETE", "Announcement", id.toString(), "Deleted announcement: " + announcement.getTitle());
    }

    public Page<Announcement> getAll(UUID tenantId, Pageable pageable) {
        return announcementRepository.findByTenantIdOrderByPinnedDescPublishedAtDesc(tenantId, pageable);
    }

    public List<Announcement> getActive(UUID tenantId) {
        return announcementRepository.findActiveAnnouncements(tenantId, LocalDateTime.now());
    }
}
