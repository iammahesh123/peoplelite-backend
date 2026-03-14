package com.hrlite.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HiringDashboardResponse {

    private int totalCandidates;
    private Map<String, Integer> candidatesPerStage;
    private int openPositions;
    private int closedPositions;
    private int totalHired;
    private int totalRejected;
    private double hiringSuccessRate;
    private double avgDaysToHire;
    private int pendingInterviews;
    private int pendingOffers;
}
