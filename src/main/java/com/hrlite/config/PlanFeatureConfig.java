package com.hrlite.config;

import com.hrlite.enums.Feature;
import java.util.*;

public class PlanFeatureConfig {

    private static final Map<String, Set<Feature>> PLAN_FEATURES = new HashMap<>();
    private static final Map<String, Integer> PLAN_MAX_EMPLOYEES = new HashMap<>();

    static {
        // FREE plan: up to 5 employees
        PLAN_FEATURES.put("FREE", new HashSet<>(Arrays.asList(
                Feature.EMPLOYEES,
                Feature.DIRECTORY,
                Feature.BASIC_LEAVES,
                Feature.BASIC_PAYROLL,
                Feature.LETTER_TEMPLATES_BASIC,
                Feature.SELF_BOARDING,
                Feature.ANNOUNCEMENTS,
                Feature.TEAM_AVAILABILITY,
                Feature.POLLS
        )));
        PLAN_MAX_EMPLOYEES.put("FREE", 5);

        // STARTER plan: up to 20 employees
        Set<Feature> starterFeatures = new HashSet<>(PLAN_FEATURES.get("FREE"));
        starterFeatures.addAll(Arrays.asList(
                Feature.FULL_PAYROLL,
                Feature.ALL_LETTER_TEMPLATES,
                Feature.BULK_LETTERS,
                Feature.LEAVE_ANALYTICS,
                Feature.BIRTHDAY_ALERTS,
                Feature.AUDIT_TRAIL,
                Feature.EXPORTS,
                Feature.SALARY_REVISIONS,
                Feature.HIRING
        ));
        PLAN_FEATURES.put("STARTER", starterFeatures);
        PLAN_MAX_EMPLOYEES.put("STARTER", 20);

        // GROWTH plan: up to 50 employees
        Set<Feature> growthFeatures = new HashSet<>(PLAN_FEATURES.get("STARTER"));
        growthFeatures.addAll(Arrays.asList(
                Feature.DOCUMENT_EXPIRY,
                Feature.ADVANCED_RBAC,
                Feature.EMPLOYEE_SELF_SERVICE,
                Feature.NOTIFICATIONS,
                Feature.CUSTOM_PAY_COMPONENTS,
                Feature.ASSETS,
                Feature.PROBATION,
                Feature.SEPARATIONS,
                Feature.PAYROLL_SETTINGS
        ));
        PLAN_FEATURES.put("GROWTH", growthFeatures);
        PLAN_MAX_EMPLOYEES.put("GROWTH", 50);

        // BUSINESS plan: up to 80 employees
        Set<Feature> businessFeatures = new HashSet<>(PLAN_FEATURES.get("GROWTH"));
        businessFeatures.addAll(Arrays.asList(
                Feature.SSO,
                Feature.API_ACCESS,
                Feature.CUSTOM_INTEGRATIONS
        ));
        PLAN_FEATURES.put("BUSINESS", businessFeatures);
        PLAN_MAX_EMPLOYEES.put("BUSINESS", 80);
    }

    public static Set<Feature> getFeaturesForPlan(String plan) {
        Set<Feature> features = PLAN_FEATURES.get(plan);
        if (features == null) {
            throw new IllegalArgumentException("Unknown plan: " + plan);
        }
        return new HashSet<>(features);
    }

    public static int getMaxEmployees(String plan) {
        Integer maxEmployees = PLAN_MAX_EMPLOYEES.get(plan);
        if (maxEmployees == null) {
            throw new IllegalArgumentException("Unknown plan: " + plan);
        }
        return maxEmployees;
    }

    public static boolean hasPlan(String plan) {
        return PLAN_FEATURES.containsKey(plan);
    }

    public static Set<String> getAllPlans() {
        return new HashSet<>(PLAN_FEATURES.keySet());
    }
}
