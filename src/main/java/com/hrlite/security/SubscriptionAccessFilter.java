package com.hrlite.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrlite.dtos.ApiResponse;
import com.hrlite.entity.Subscription;
import com.hrlite.enums.SubscriptionStatus;
import com.hrlite.exception.ErrorCodes;
import com.hrlite.repository.SubscriptionRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionAccessFilter extends OncePerRequestFilter {

    private final SubscriptionRepository subscriptionRepository;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/v1/auth/")
                || path.startsWith("/api/v1/subscription/")
                || path.startsWith("/api/v1/webhooks/")
                || path.startsWith("/api/v1/platform/")
                || path.equals("/health")
                || path.equals("/actuator/health");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            // Not authenticated — let Spring Security handle it
            filterChain.doFilter(request, response);
            return;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserPrincipal userPrincipal)) {
            filterChain.doFilter(request, response);
            return;
        }

        // SUPER_ADMIN bypasses subscription check
        if ("SUPER_ADMIN".equals(userPrincipal.getRole())) {
            filterChain.doFilter(request, response);
            return;
        }

        // Check subscription for the tenant
        Subscription subscription = subscriptionRepository
                .findTopByTenantIdOrderByCreatedAtDesc(userPrincipal.getTenantId())
                .orElse(null);

        if (subscription == null) {
            // No subscription record — allow access (backwards compatibility for existing tenants)
            // The initiateTrial is called on registration, so this covers pre-existing tenants
            filterChain.doFilter(request, response);
            return;
        }

        if (subscription.hasAccess()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Access denied — determine the reason
        String errorCode;
        String message;

        if (subscription.getStatus() == SubscriptionStatus.TRIAL || subscription.getStatus() == SubscriptionStatus.EXPIRED) {
            errorCode = ErrorCodes.SUBSCRIPTION_TRIAL_EXPIRED;
            message = "Your free trial has expired. Please subscribe to continue using HR Lite.";
        } else if (subscription.getStatus() == SubscriptionStatus.SUSPENDED) {
            errorCode = ErrorCodes.SUBSCRIPTION_INACTIVE;
            message = "Your subscription has been suspended. Please contact support.";
        } else if (subscription.getStatus() == SubscriptionStatus.CANCELLED) {
            errorCode = ErrorCodes.SUBSCRIPTION_INACTIVE;
            message = "Your subscription has been cancelled. Please subscribe to continue.";
        } else {
            errorCode = ErrorCodes.SUBSCRIPTION_INACTIVE;
            message = "Your subscription is inactive. Please subscribe to continue.";
        }

        log.info("Subscription access denied for tenant {} — status: {}", userPrincipal.getTenantId(), subscription.getStatus());

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiResponse<Void> apiResponse = ApiResponse.error(message);
        // Include errorCode in the response for frontend routing
        response.getWriter().write(objectMapper.writeValueAsString(new SubscriptionErrorResponse(false, message, errorCode)));
    }

    // Inner class for subscription-specific error response
    record SubscriptionErrorResponse(boolean success, String message, String errorCode) {}
}
