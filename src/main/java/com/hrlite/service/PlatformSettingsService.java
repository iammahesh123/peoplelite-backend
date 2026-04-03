package com.hrlite.service;

import com.hrlite.dtos.FeatureFlagRequest;
import com.hrlite.dtos.FeatureFlagResponse;
import com.hrlite.dtos.PlatformSettingResponse;
import com.hrlite.dtos.PlatformSettingsUpdate;
import com.hrlite.exception.BusinessException;
import com.hrlite.exception.ErrorCodes;
import com.hrlite.entity.PlatformFeatureFlag;
import com.hrlite.entity.PlatformSetting;
import com.hrlite.repository.PlatformFeatureFlagRepository;
import com.hrlite.repository.PlatformSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlatformSettingsService {

    private final PlatformFeatureFlagRepository featureFlagRepository;
    private final PlatformSettingRepository settingRepository;
    private final EmailService emailService;

    // Feature Flag Methods
    public List<FeatureFlagResponse> getAllFeatureFlags() {
        return featureFlagRepository.findAll().stream()
                .map(this::mapToFeatureFlagResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public FeatureFlagResponse createFeatureFlag(FeatureFlagRequest request) {
        if (featureFlagRepository.findByName(request.getName()).isPresent()) {
            throw new BusinessException(ErrorCodes.VALIDATION_FAILED,
                    "Feature flag with name '" + request.getName() + "' already exists");
        }

        PlatformFeatureFlag flag = PlatformFeatureFlag.builder()
                .name(request.getName())
                .description(request.getDescription())
                .enabled(request.getEnabled() != null ? request.getEnabled() : false)
                .scope(request.getScope() != null ? request.getScope() : "GLOBAL")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        PlatformFeatureFlag saved = featureFlagRepository.save(flag);
        return mapToFeatureFlagResponse(saved);
    }

    @Transactional
    public FeatureFlagResponse updateFeatureFlag(UUID flagId, FeatureFlagRequest request) {
        PlatformFeatureFlag flag = featureFlagRepository.findById(flagId)
                .orElseThrow(() -> new BusinessException(ErrorCodes.RESOURCE_NOT_FOUND,
                        "Feature flag not found"));

        if (request.getName() != null) flag.setName(request.getName());
        if (request.getDescription() != null) flag.setDescription(request.getDescription());
        if (request.getEnabled() != null) flag.setEnabled(request.getEnabled());
        if (request.getScope() != null) flag.setScope(request.getScope());
        flag.setUpdatedAt(LocalDateTime.now());

        PlatformFeatureFlag saved = featureFlagRepository.save(flag);
        return mapToFeatureFlagResponse(saved);
    }

    // Platform Settings Methods
    public Map<String, List<PlatformSettingResponse>> getAllSettings() {
        List<PlatformSetting> allSettings = settingRepository.findAll();
        return allSettings.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getCategory() != null ? s.getCategory() : "general",
                        Collectors.mapping(this::mapToPlatformSettingResponse, Collectors.toList())
                ));
    }

    @Transactional
    public List<PlatformSettingResponse> updateSettings(PlatformSettingsUpdate update) {
        return update.getSettings().stream()
                .map(request -> {
                    PlatformSetting setting = settingRepository.findBySettingKey(request.getSettingKey())
                            .orElseGet(() -> PlatformSetting.builder()
                                    .settingKey(request.getSettingKey())
                                    .category(request.getCategory())
                                    .build());

                    setting.setSettingValue(request.getSettingValue());
                    if (request.getCategory() != null) {
                        setting.setCategory(request.getCategory());
                    }
                    setting.setUpdatedAt(LocalDateTime.now());

                    PlatformSetting saved = settingRepository.save(setting);
                    return mapToPlatformSettingResponse(saved);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public List<PlatformSettingResponse> updateEmailSettings(PlatformSettingsUpdate update) {
        return updateSettings(update);
    }

    @Transactional
    public List<PlatformSettingResponse> updateSecuritySettings(PlatformSettingsUpdate update) {
        return updateSettings(update);
    }

    @Transactional
    public List<PlatformSettingResponse> updateRazorpayIntegration(PlatformSettingsUpdate update) {
        return updateSettings(update);
    }

    @Transactional
    public List<PlatformSettingResponse> updateGoogleOAuthIntegration(PlatformSettingsUpdate update) {
        return updateSettings(update);
    }

    @Transactional
    public List<PlatformSettingResponse> updateS3Integration(PlatformSettingsUpdate update) {
        return updateSettings(update);
    }

    @Transactional
    public void sendTestEmail() {
        // Get platform email settings
        PlatformSetting fromEmail = settingRepository.findBySettingKey("platform.email.from")
                .orElse(null);

        String recipient = fromEmail != null && fromEmail.getSettingValue() != null
                ? fromEmail.getSettingValue()
                : "admin@hrlite.io";

        String subject = "PeopleLite Platform - Test Email";
        String htmlBody = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width,initial-scale=1.0">
                <title>Test Email</title>
            </head>
            <body style="margin:0;padding:0;background-color:#f0f2f5;font-family:'Segoe UI',Roboto,'Helvetica Neue',Arial,sans-serif;-webkit-font-smoothing:antialiased;">
                <div style="max-width:600px;margin:0 auto;padding:24px 16px;">
                    <div style="background:linear-gradient(135deg,#1e293b,#0f172a);border-radius:16px 16px 0 0;padding:32px 32px 28px;text-align:center;">
                        <div style="width:48px;height:48px;background:rgba(255,255,255,0.2);border-radius:12px;display:inline-block;line-height:48px;margin-bottom:16px;">
                            <svg xmlns='http://www.w3.org/2000/svg' width='24' height='24' viewBox='0 0 24 24' fill='none' stroke='white' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><path d='M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z'/><polyline points='22,6 12,13 2,6'/></svg>
                        </div>
                        <h1 style="margin:0;font-size:22px;font-weight:700;color:#ffffff;letter-spacing:-0.3px;">Test Email</h1>
                        <p style="margin:6px 0 0;font-size:14px;color:rgba(255,255,255,0.85);font-weight:400;">Platform email configuration check</p>
                    </div>
                    <div style="background:#ffffff;padding:32px;border-left:1px solid #e5e7eb;border-right:1px solid #e5e7eb;">
                        <p style="margin:0 0 8px;font-size:15px;color:#374151;">Hello <strong>Super Admin</strong>,</p>
                        <p style="margin:0 0 24px;font-size:14px;color:#6b7280;line-height:1.6;">
                            This is a test email from the PeopleLite Platform.
                        </p>
                        <div style="text-align:center;margin:24px 0;">
                            <div style="display:inline-block;background:linear-gradient(135deg,#ecfdf5,#d1fae5);border-radius:12px;padding:20px 32px;border:1px solid #a7f3d0;">
                                <p style="margin:0 0 4px;font-size:14px;font-weight:700;color:#065f46;">&#10003; Configuration Working</p>
                                <p style="margin:0;font-size:12px;color:#059669;">Platform SMTP settings are properly configured</p>
                            </div>
                        </div>
                        <p style="margin:16px 0 0;font-size:13px;color:#9ca3af;line-height:1.5;">
                            If you received this email, your platform email settings are correct and operational.
                        </p>
                    </div>
                    <div style="background:#f9fafb;border-radius:0 0 16px 16px;border:1px solid #e5e7eb;border-top:0;padding:24px 32px;text-align:center;">
                        <p style="margin:0 0 4px;font-size:13px;font-weight:600;color:#6b7280;">PeopleLite</p>
                        <p style="margin:0;font-size:11px;color:#9ca3af;">Platform Administration</p>
                    </div>
                </div>
            </body>
            </html>
            """;
        emailService.sendEmail(recipient, subject, htmlBody);
    }

    public List<PlatformSettingResponse> getAllSettingsList() {
        return settingRepository.findAll().stream()
                .map(this::mapToPlatformSettingResponse)
                .collect(Collectors.toList());
    }

    private FeatureFlagResponse mapToFeatureFlagResponse(PlatformFeatureFlag flag) {
        return FeatureFlagResponse.builder()
                .id(flag.getId())
                .name(flag.getName())
                .description(flag.getDescription())
                .enabled(flag.isEnabled())
                .scope(flag.getScope())
                .createdAt(flag.getCreatedAt())
                .updatedAt(flag.getUpdatedAt())
                .build();
    }

    private PlatformSettingResponse mapToPlatformSettingResponse(PlatformSetting setting) {
        return PlatformSettingResponse.builder()
                .id(setting.getId())
                .settingKey(setting.getSettingKey())
                .settingValue(setting.getSettingValue())
                .category(setting.getCategory())
                .updatedAt(setting.getUpdatedAt())
                .build();
    }
}
