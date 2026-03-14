package com.hrlite.exception;

import com.hrlite.enums.Feature;

public class FeatureNotAvailableException extends RuntimeException {

    private final Feature feature;
    private final String requiredPlan;

    public FeatureNotAvailableException(Feature feature, String requiredPlan) {
        super(String.format("This feature requires the %s plan. Please upgrade to access %s.",
                requiredPlan, feature.name().toLowerCase().replace("_", " ")));
        this.feature = feature;
        this.requiredPlan = requiredPlan;
    }

    public Feature getFeature() {
        return feature;
    }

    public String getRequiredPlan() {
        return requiredPlan;
    }
}
