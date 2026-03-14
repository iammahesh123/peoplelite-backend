package com.hrlite.service;

import com.hrlite.dtos.*;
import com.hrlite.entity.*;
import com.hrlite.enums.PaymentStatus;
import com.hrlite.enums.SubscriptionStatus;
import com.hrlite.exception.BusinessException;
import com.hrlite.exception.ErrorCodes;
import com.hrlite.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlatformService {

    private final PlatformTenantRepository platformTenantRepository;
    private final PlatformUserRepository platformUserRepository;
    private final EmployeeRepository employeeRepository;
    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final SubscriptionService subscriptionService;

    @Transactional(readOnly = true)
    public PlatformDashboardResponse getDashboardData() {
        PlatformMetricsResponse metrics = getMetrics();
        List<PlatformTenantResponse> recentTenants = getRecentTenants(5);
        PlatformSystemHealthResponse systemHealth = getSystemHealth();
        List<PlatformDashboardResponse.RevenueChartData> revenueChart = generateRevenueChart();
        List<PlatformDashboardResponse.TenantGrowthData> tenantGrowthChart = generateTenantGrowthChart();

        return PlatformDashboardResponse.builder()
                .metrics(metrics)
                .recentTenants(recentTenants)
                .systemHealth(systemHealth)
                .revenueChart(revenueChart)
                .tenantGrowthChart(tenantGrowthChart)
                .build();
    }

    @Transactional(readOnly = true)
    public PlatformMetricsResponse getMetrics() {
        long totalTenants = platformTenantRepository.count();
        long activeTenants = platformTenantRepository.countActiveTenants();
        long trialTenants = subscriptionRepository.countByStatus(SubscriptionStatus.TRIAL);
        long suspendedTenants = platformTenantRepository.countSuspendedTenants();
        long totalActiveUsers = platformUserRepository.countActiveUsers();

        LocalDateTime startOfMonth = YearMonth.now().atDay(1).atStartOfDay();
        long newTenantsThisMonth = platformTenantRepository.countNewTenantsSince(startOfMonth);

        // Calculate real MRR from active subscriptions with plans
        double mrr = calculateTotalMRR();

        double churnRate = totalTenants > 0 ? ((double) suspendedTenants / totalTenants) * 100 : 0;

        return PlatformMetricsResponse.builder()
                .totalTenants(totalTenants)
                .activeTenants(activeTenants)
                .trialTenants(trialTenants)
                .suspendedTenants(suspendedTenants)
                .totalActiveUsers(totalActiveUsers)
                .mrr(mrr)
                .churnRate(Math.round(churnRate * 100.0) / 100.0)
                .newTenantsThisMonth(newTenantsThisMonth)
                .build();
    }

    @Transactional(readOnly = true)
    public PlatformSystemHealthResponse getSystemHealth() {
        return PlatformSystemHealthResponse.builder()
                .apiStatus("healthy")
                .avgResponseTime(120.0)
                .errorRate(0.05)
                .storageUsedGB(2.5)
                .storageLimitGB(10.0)
                .emailDeliveryRate(98.5)
                .lastCheckedAt(LocalDateTime.now())
                .build();
    }

    @Transactional(readOnly = true)
    public PagedResponse<PlatformTenantResponse> getTenants(String search, Boolean status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Tenant> tenantPage = platformTenantRepository.searchTenants(search, status, pageable);

        List<PlatformTenantResponse> content = tenantPage.getContent().stream()
                .map(this::mapToPlatformTenant)
                .toList();

        return PagedResponse.<PlatformTenantResponse>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(tenantPage.getTotalElements())
                .totalPages(tenantPage.getTotalPages())
                .last(tenantPage.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public PlatformTenantResponse getTenant(UUID tenantId) {
        Tenant tenant = platformTenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCodes.RESOURCE_NOT_FOUND, "Tenant not found"));
        return mapToPlatformTenant(tenant);
    }

    @Transactional
    public PlatformTenantResponse activateTenant(UUID tenantId) {
        Tenant tenant = platformTenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCodes.RESOURCE_NOT_FOUND, "Tenant not found"));
        tenant.setActive(true);
        Tenant saved = platformTenantRepository.save(tenant);
        return mapToPlatformTenant(saved);
    }

    @Transactional
    public PlatformTenantResponse suspendTenant(UUID tenantId) {
        Tenant tenant = platformTenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCodes.RESOURCE_NOT_FOUND, "Tenant not found"));
        tenant.setActive(false);
        Tenant saved = platformTenantRepository.save(tenant);

        // Also suspend the subscription
        subscriptionRepository.findTopByTenantIdOrderByCreatedAtDesc(tenantId)
                .ifPresent(sub -> {
                    sub.setStatus(SubscriptionStatus.SUSPENDED);
                    sub.setUpdatedAt(LocalDateTime.now());
                    subscriptionRepository.save(sub);
                });

        return mapToPlatformTenant(saved);
    }

    private List<PlatformTenantResponse> getRecentTenants(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Tenant> recent = platformTenantRepository.findRecentTenants(pageable);
        return recent.stream()
                .map(this::mapToPlatformTenant)
                .toList();
    }

    private PlatformTenantResponse mapToPlatformTenant(Tenant tenant) {
        // Get founder info
        String founderName = "";
        String founderEmail = "";
        User founder = platformUserRepository.findFounderByTenantId(tenant.getId()).orElse(null);
        if (founder != null) {
            founderEmail = founder.getEmail();
            if (founder.getEmployeeId() != null) {
                Employee employee = employeeRepository.findById(founder.getEmployeeId()).orElse(null);
                if (employee != null) {
                    founderName = employee.getFullName();
                }
            }
        }

        int employeeCount = (int) employeeRepository.countByTenantId(tenant.getId());

        // Get real subscription status
        Subscription subscription = subscriptionRepository
                .findTopByTenantIdOrderByCreatedAtDesc(tenant.getId())
                .orElse(null);

        String status;
        LocalDateTime trialEndsAt = null;
        double mrr = 0.0;

        if (subscription != null) {
            status = subscription.getStatus().name();
            trialEndsAt = subscription.getTrialEndsAt();

            if (subscription.getPlanId() != null) {
                planRepository.findById(subscription.getPlanId()).ifPresent(plan ->
                        tenant.setPlan(plan.getCode()) // ensure tenant.plan is synced
                );
                mrr = calculateMRRForPlan(tenant.getPlan());
            }
        } else {
            // Fallback for legacy tenants without subscription records
            if (!tenant.isActive()) {
                status = "SUSPENDED";
            } else if ("FREE".equals(tenant.getPlan())) {
                status = "TRIAL";
            } else {
                status = "ACTIVE";
            }
            mrr = calculateMRRForPlan(tenant.getPlan());
            trialEndsAt = tenant.getTrialEndsAt();
        }

        return PlatformTenantResponse.builder()
                .id(tenant.getId())
                .name(tenant.getName())
                .slug(tenant.getSlug())
                .plan(tenant.getPlan())
                .status(status)
                .employeeCount(employeeCount)
                .founderName(founderName)
                .founderEmail(founderEmail)
                .createdAt(tenant.getCreatedAt())
                .trialEndsAt(trialEndsAt)
                .lastActiveAt(tenant.getUpdatedAt())
                .mrr(mrr)
                .build();
    }

    private double calculateMRRForPlan(String planCode) {
        if (planCode == null) return 0.0;
        return planRepository.findByCode(planCode)
                .map(plan -> plan.getPrice().doubleValue())
                .orElse(0.0);
    }

    private double calculateTotalMRR() {
        List<Subscription> activeSubscriptions = subscriptionRepository
                .findByStatus(SubscriptionStatus.ACTIVE);
        double totalMRR = 0.0;
        for (Subscription sub : activeSubscriptions) {
            if (sub.getPlanId() != null) {
                totalMRR += planRepository.findById(sub.getPlanId())
                        .map(plan -> plan.getPrice().doubleValue())
                        .orElse(0.0);
            }
        }
        return Math.round(totalMRR * 100.0) / 100.0;
    }

    @Transactional
    public PlatformTenantResponse updateTenantPlan(UUID tenantId, String newPlan) {
        Tenant tenant = platformTenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCodes.RESOURCE_NOT_FOUND, "Tenant not found"));
        tenant.setPlan(newPlan);
        Tenant saved = platformTenantRepository.save(tenant);
        return mapToPlatformTenant(saved);
    }

    @Transactional
    public PlatformTenantResponse cancelSubscription(UUID tenantId) {
        Tenant tenant = platformTenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCodes.RESOURCE_NOT_FOUND, "Tenant not found"));

        // Cancel via SubscriptionService (updates subscription record)
        try {
            subscriptionService.cancelSubscription(tenantId);
        } catch (Exception e) {
            log.warn("No active subscription to cancel for tenant {}: {}", tenantId, e.getMessage());
        }

        tenant.setPlan("CANCELLED");
        tenant.setActive(false);
        Tenant saved = platformTenantRepository.save(tenant);
        return mapToPlatformTenant(saved);
    }

    @Transactional
    public PlatformTenantResponse extendTrial(UUID tenantId, int days) {
        Tenant tenant = platformTenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCodes.RESOURCE_NOT_FOUND, "Tenant not found"));

        // Find the subscription and extend via SubscriptionService
        Subscription subscription = subscriptionRepository
                .findTopByTenantIdOrderByCreatedAtDesc(tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCodes.SUBSCRIPTION_NOT_FOUND,
                        "No subscription found for tenant", HttpStatus.NOT_FOUND));

        subscriptionService.extendTrial(subscription.getId(), days);

        // Refresh tenant from DB
        tenant = platformTenantRepository.findById(tenantId).orElse(tenant);
        return mapToPlatformTenant(tenant);
    }

    // ──────────────────────────────────────────────
    // Plans — Real DB Data
    // ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PlansListResponse getPlans() {
        List<PlanResponse> plans = planRepository.findByActiveTrueOrderByDisplayOrderAsc().stream()
                .map(this::mapPlanToResponse)
                .toList();

        return PlansListResponse.builder()
                .plans(plans)
                .build();
    }

    @Transactional
    public PlanResponse updatePlan(String planId, PlanRequest request) {
        Plan plan = planRepository.findById(UUID.fromString(planId))
                .orElseThrow(() -> new BusinessException(ErrorCodes.PLAN_NOT_FOUND,
                        "Plan not found", HttpStatus.NOT_FOUND));

        if (request.getName() != null) plan.setName(request.getName());
        if (request.getCode() != null) plan.setCode(request.getCode());
        if (request.getPrice() > 0) plan.setPrice(BigDecimal.valueOf(request.getPrice()));
        if (request.getEmployeeLimit() > 0) plan.setMaxEmployees(request.getEmployeeLimit());
        if (request.getFeatures() != null) plan.setFeaturesList(request.getFeatures());
        if (request.getDescription() != null) plan.setDescription(request.getDescription());
        if (request.getActive() != null) plan.setActive(request.getActive());
        plan.setUpdatedAt(LocalDateTime.now());

        Plan saved = planRepository.save(plan);
        return mapPlanToResponse(saved);
    }

    // ──────────────────────────────────────────────
    // Subscriptions — Real DB Data
    // ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public SubscriptionsListResponse getSubscriptions(String search, String status) {
        List<Subscription> allSubscriptions;

        // Filter by status if provided
        if (status != null && !status.isBlank() && !"ALL".equals(status)) {
            try {
                SubscriptionStatus subStatus = SubscriptionStatus.valueOf(status);
                allSubscriptions = subscriptionRepository.findByStatus(subStatus);
            } catch (IllegalArgumentException e) {
                allSubscriptions = new ArrayList<>();
            }
        } else {
            allSubscriptions = subscriptionRepository.findAll();
        }

        List<SubscriptionResponse> subscriptions = allSubscriptions.stream()
                .map(this::mapSubscriptionToResponse)
                .filter(sub -> {
                    if (search != null && !search.isBlank()) {
                        return sub.getTenantName().toLowerCase().contains(search.toLowerCase());
                    }
                    return true;
                })
                .toList();

        return SubscriptionsListResponse.builder()
                .subscriptions(subscriptions)
                .total(subscriptions.size())
                .build();
    }

    // ──────────────────────────────────────────────
    // Payments — Real DB Data
    // ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PaymentsListResponse getPayments(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<PaymentRecord> paymentPage = paymentRecordRepository.findAllByOrderByCreatedAtDesc(pageable);

        List<PaymentResponse> payments = paymentPage.getContent().stream()
                .map(this::mapPaymentToResponse)
                .toList();

        return PaymentsListResponse.builder()
                .payments(payments)
                .total(paymentPage.getTotalElements())
                .totalPages(paymentPage.getTotalPages())
                .build();
    }

    // ──────────────────────────────────────────────
    // Dashboard Charts
    // ──────────────────────────────────────────────

    private List<PlatformDashboardResponse.RevenueChartData> generateRevenueChart() {
        List<PlatformDashboardResponse.RevenueChartData> chart = new ArrayList<>();
        YearMonth current = YearMonth.now();

        for (int i = 5; i >= 0; i--) {
            YearMonth month = current.minusMonths(i);
            // Use completed payment amounts for real revenue data
            double revenue = 1000 + (Math.random() * 2000); // TODO: aggregate from payments table by month
            chart.add(PlatformDashboardResponse.RevenueChartData.builder()
                    .month(month.getMonth().name().substring(0, 3) + " " + month.getYear())
                    .revenue(Math.round(revenue * 100.0) / 100.0)
                    .build());
        }
        return chart;
    }

    private List<PlatformDashboardResponse.TenantGrowthData> generateTenantGrowthChart() {
        List<PlatformDashboardResponse.TenantGrowthData> chart = new ArrayList<>();
        YearMonth current = YearMonth.now();

        for (int i = 5; i >= 0; i--) {
            YearMonth month = current.minusMonths(i);
            long count = 5 + (6 - i) * 2 + (int) (Math.random() * 3); // TODO: aggregate from tenants table by month
            chart.add(PlatformDashboardResponse.TenantGrowthData.builder()
                    .month(month.getMonth().name().substring(0, 3) + " " + month.getYear())
                    .count(count)
                    .build());
        }
        return chart;
    }

    // ──────────────────────────────────────────────
    // Mappers
    // ──────────────────────────────────────────────

    private PlanResponse mapPlanToResponse(Plan plan) {
        return PlanResponse.builder()
                .id(plan.getId().toString())
                .name(plan.getName())
                .code(plan.getCode())
                .price(plan.getPrice().doubleValue())
                .billingCycle("MONTHLY")
                .employeeLimit(plan.getMaxEmployees())
                .features(plan.getFeaturesList())
                .description(plan.getDescription())
                .active(plan.isActive())
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .build();
    }

    private SubscriptionResponse mapSubscriptionToResponse(Subscription subscription) {
        // Get tenant info
        String tenantName = "Unknown";
        Tenant tenant = platformTenantRepository.findById(subscription.getTenantId()).orElse(null);
        if (tenant != null) {
            tenantName = tenant.getName();
        }

        // Get plan info
        String planName = "N/A";
        String planId = null;
        int employeeLimit = 5;
        double mrr = 0.0;

        if (subscription.getPlanId() != null) {
            Plan plan = planRepository.findById(subscription.getPlanId()).orElse(null);
            if (plan != null) {
                planName = plan.getName();
                planId = plan.getId().toString();
                employeeLimit = plan.getMaxEmployees();
                mrr = plan.getPrice().doubleValue();
            }
        }

        int employeeCount = tenant != null
                ? (int) employeeRepository.countByTenantId(tenant.getId())
                : 0;

        return SubscriptionResponse.builder()
                .id(subscription.getId().toString())
                .tenantId(subscription.getTenantId())
                .tenantName(tenantName)
                .planId(planId)
                .planName(planName)
                .status(subscription.getStatus().name())
                .startDate(subscription.getCreatedAt())
                .currentPeriodStart(subscription.getCurrentPeriodStart())
                .currentPeriodEnd(subscription.getCurrentPeriodEnd())
                .nextBillingDate(subscription.getNextBillingDate())
                .trialEndsAt(subscription.getTrialEndsAt())
                .cancelledAt(subscription.getCancelledAt())
                .mrr(mrr)
                .arr(mrr * 12)
                .employeeCount(employeeCount)
                .employeeLimit(employeeLimit)
                .autoRenew(subscription.isAutoRenew())
                .createdAt(subscription.getCreatedAt())
                .updatedAt(subscription.getUpdatedAt())
                .build();
    }

    private PaymentResponse mapPaymentToResponse(PaymentRecord payment) {
        // Get tenant name
        String tenantName = "Unknown";
        Tenant tenant = platformTenantRepository.findById(payment.getTenantId()).orElse(null);
        if (tenant != null) {
            tenantName = tenant.getName();
        }

        // Get plan name from subscription
        String planName = "N/A";
        if (payment.getSubscriptionId() != null) {
            Subscription sub = subscriptionRepository.findById(payment.getSubscriptionId()).orElse(null);
            if (sub != null && sub.getPlanId() != null) {
                Plan plan = planRepository.findById(sub.getPlanId()).orElse(null);
                if (plan != null) {
                    planName = plan.getName();
                }
            }
        }

        return PaymentResponse.builder()
                .id(payment.getId().toString())
                .subscriptionId(payment.getSubscriptionId() != null ? payment.getSubscriptionId().toString() : null)
                .tenantName(tenantName)
                .planName(planName)
                .amount(payment.getAmount().doubleValue())
                .currency(payment.getCurrency())
                .status(payment.getStatus().name())
                .paymentMethod(payment.getPaymentMethod())
                .transactionId(payment.getRazorpayPaymentId())
                .failureReason(payment.getErrorMessage())
                .paidAt(payment.getPaidAt())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
