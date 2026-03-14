package com.hrlite.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingInfoRequest {

    private String billingLegalName;
    private String billingGstin;
    private String billingAddress;
    private String billingEmail;
    private String billingCurrency;
}
