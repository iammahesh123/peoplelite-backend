package com.hrlite.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TenantResponse {
    private UUID id;
    private String name;
    private String slug;
    private String plan;
    private String employeeCodePrefix;
    private boolean active;
    private String logoUrl;
    private String ceoSignatureUrl;
    private String companyAddress;
    private String companyPhone;
    private String companyEmail;
    private String companyWebsite;
    private String companyCin;
    private String ceoName;
}
