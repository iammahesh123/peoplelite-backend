package com.hrlite.exception;

public final class ErrorCodes {

    private ErrorCodes() {}

    // Auth
    public static final String AUTH_INVALID_CREDENTIALS = "AUTH_001";
    public static final String AUTH_TOKEN_EXPIRED = "AUTH_002";
    public static final String AUTH_TOKEN_INVALID = "AUTH_003";
    public static final String AUTH_EMAIL_EXISTS = "AUTH_004";
    public static final String AUTH_REFRESH_TOKEN_INVALID = "AUTH_005";
    public static final String AUTH_GOOGLE_LOGIN_FAILED = "AUTH_006";
    public static final String AUTH_PASSWORD_RESET_TOKEN_INVALID = "AUTH_007";
    public static final String AUTH_PASSWORD_RESET_TOKEN_EXPIRED = "AUTH_008";

    // Tenant
    public static final String TENANT_NOT_FOUND = "TENANT_001";
    public static final String TENANT_SLUG_EXISTS = "TENANT_002";
    public static final String TENANT_INACTIVE = "TENANT_003";

    // Employee
    public static final String EMPLOYEE_NOT_FOUND = "EMP_001";
    public static final String EMPLOYEE_EMAIL_EXISTS = "EMP_002";
    public static final String EMPLOYEE_INACTIVE = "EMP_003";

    // Leave
    public static final String LEAVE_TYPE_NOT_FOUND = "LEAVE_001";
    public static final String LEAVE_INSUFFICIENT_BALANCE = "LEAVE_002";
    public static final String LEAVE_REQUEST_NOT_FOUND = "LEAVE_003";
    public static final String LEAVE_ALREADY_PROCESSED = "LEAVE_004";
    public static final String LEAVE_INVALID_DATES = "LEAVE_005";

    // Payroll
    public static final String PAYROLL_RUN_NOT_FOUND = "PAY_001";
    public static final String PAYROLL_ALREADY_RUN = "PAY_002";
    public static final String PAYROLL_NO_EMPLOYEES = "PAY_003";

    // Document
    public static final String DOCUMENT_NOT_FOUND = "DOC_001";
    public static final String DOCUMENT_UPLOAD_FAILED = "DOC_002";

    // Onboarding
    public static final String ONBOARDING_TEMPLATE_NOT_FOUND = "ONB_001";
    public static final String ONBOARDING_TASK_NOT_FOUND = "ONB_002";

    // Offer Letter
    public static final String OFFER_TEMPLATE_NOT_FOUND = "OFR_001";
    public static final String OFFER_LETTER_NOT_FOUND = "OFR_002";

    // Notification
    public static final String NOTIFICATION_NOT_FOUND = "NOTIF_001";

    // Settings
    public static final String SETTINGS_NOT_FOUND = "SET_001";

    // Subscription
    public static final String SUBSCRIPTION_NOT_FOUND = "SUB_001";
    public static final String SUBSCRIPTION_TRIAL_EXPIRED = "SUB_002";
    public static final String SUBSCRIPTION_INACTIVE = "SUB_003";
    public static final String PLAN_NOT_FOUND = "SUB_004";

    // Payment
    public static final String PAYMENT_ORDER_FAILED = "RPAY_001";
    public static final String PAYMENT_VERIFICATION_FAILED = "RPAY_002";
    public static final String PAYMENT_ALREADY_PROCESSED = "RPAY_003";

    // General
    public static final String ACCESS_DENIED = "GEN_001";
    public static final String RESOURCE_NOT_FOUND = "GEN_002";
    public static final String VALIDATION_FAILED = "GEN_003";
    public static final String TENANT_MISMATCH = "GEN_004";
}
