package com.hrlite.service;

import com.hrlite.exception.ResourceNotFoundException;
import com.hrlite.entity.TenantContext;
import com.hrlite.entity.Tenant;
import com.hrlite.repository.TenantRepository;
import com.hrlite.dtos.TenantResponse;
import com.hrlite.dtos.UpdateTenantRequest;
import com.hrlite.utils.S3StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;
    private final S3StorageService s3StorageService;

    @Transactional(readOnly = true)
    public TenantResponse getCurrentTenant() {
        UUID tenantId = TenantContext.getCurrentTenant();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", tenantId));
        return toResponse(tenant);
    }

    @Transactional
    public TenantResponse updateTenant(UpdateTenantRequest request) {
        UUID tenantId = TenantContext.getCurrentTenant();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", tenantId));

        tenant.setName(request.getName());
        if (request.getEmployeeCodePrefix() != null) {
            tenant.setEmployeeCodePrefix(request.getEmployeeCodePrefix());
        }
        if (request.getCompanyAddress() != null) {
            tenant.setCompanyAddress(request.getCompanyAddress());
        }
        if (request.getCompanyPhone() != null) {
            tenant.setCompanyPhone(request.getCompanyPhone());
        }
        if (request.getCompanyEmail() != null) {
            tenant.setCompanyEmail(request.getCompanyEmail());
        }
        if (request.getCompanyWebsite() != null) {
            tenant.setCompanyWebsite(request.getCompanyWebsite());
        }
        if (request.getCompanyCin() != null) {
            tenant.setCompanyCin(request.getCompanyCin());
        }
        if (request.getCeoName() != null) {
            tenant.setCeoName(request.getCeoName());
        }

        return toResponse(tenantRepository.save(tenant));
    }

    public Tenant getTenantEntity(UUID tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", tenantId));
    }

    @Transactional
    public TenantResponse uploadBrandingFile(UUID tenantId, MultipartFile file, String type) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", tenantId));

        try {
            String storageKey = s3StorageService.upload(
                    tenantId.toString(),
                    "branding",
                    file.getOriginalFilename(),
                    file.getBytes(),
                    file.getContentType()
            );

            if ("logo".equals(type)) {
                tenant.setLogoUrl(storageKey);
            } else if ("signature".equals(type)) {
                tenant.setCeoSignatureUrl(storageKey);
            }

            Tenant saved = tenantRepository.save(tenant);
            return toResponse(saved);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file: " + e.getMessage());
        }
    }

    private TenantResponse toResponse(Tenant tenant) {
        return TenantResponse.builder()
                .id(tenant.getId())
                .name(tenant.getName())
                .slug(tenant.getSlug())
                .plan(tenant.getPlan())
                .employeeCodePrefix(tenant.getEmployeeCodePrefix())
                .active(tenant.isActive())
                .logoUrl(tenant.getLogoUrl())
                .ceoSignatureUrl(tenant.getCeoSignatureUrl())
                .companyAddress(tenant.getCompanyAddress())
                .companyPhone(tenant.getCompanyPhone())
                .companyEmail(tenant.getCompanyEmail())
                .companyWebsite(tenant.getCompanyWebsite())
                .companyCin(tenant.getCompanyCin())
                .ceoName(tenant.getCeoName())
                .build();
    }
}
