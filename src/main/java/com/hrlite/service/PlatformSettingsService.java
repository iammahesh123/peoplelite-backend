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

        String subject = "HR Lite Platform - Test Email";
        String htmlBody = """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8">
            <style>
                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                .header { background-color: #1a1a2e; color: white; padding: 20px; text-align: center; }
                .header h1 { margin: 0; font-size: 28px; }
                .content { padding: 20px; background-color: #f9f9f9; }
                .success-box { background-color: #d4edda; border: 1px solid #c3e6cb; padding: 15px; border-radius: 4px; margin: 20px 0; text-align: center; }
                .success-box p { color: #155724; margin: 5px 0; font-weight: bold; }
                .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
            </style>
            </head>
            <body>
                <div class="container">
                    <div class="header"><h1>HR Lite Platform</h1></div>
                    <div class="content">
                        <p>Hello Super Admin,</p>
                        <p>This is a test email from the HR Lite Platform.</p>
                        <div class="success-box">
                            <p>Platform email configuration is working correctly!</p>
                        </div>
                        <p>If you received this email, your platform SMTP settings are properly configured.</p>
                    </div>
                    <div class="footer"><p><strong>HR Lite Platform Admin</strong></p></div>
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
