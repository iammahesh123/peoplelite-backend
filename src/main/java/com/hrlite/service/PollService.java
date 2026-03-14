package com.hrlite.service;

import com.hrlite.dtos.PollRequest;
import com.hrlite.dtos.PollResponse;
import com.hrlite.dtos.PollResponseSubmitRequest;
import com.hrlite.entity.Poll;
import com.hrlite.entity.TenantContext;
import com.hrlite.repository.PollRepository;
import com.hrlite.repository.PollResponseRepository;
import com.hrlite.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PollService {

    private final PollRepository pollRepository;
    private final PollResponseRepository pollResponseRepository;

    public PollResponse createPoll(PollRequest request) {
        UUID tenantId = TenantContext.getCurrentTenant();
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Poll poll = Poll.builder()
                .question(request.getQuestion())
                .pollType(request.getPollType())
                .anonymous(request.isAnonymous())
                .active(true)
                .expiresAt(request.getExpiresAt())
                .createdBy(principal.getUserId())
                .build();

        poll = pollRepository.save(poll);
        return mapToResponse(poll, new HashMap<>());
    }

    public List<PollResponse> getActivePolls() {
        UUID tenantId = TenantContext.getCurrentTenant();
        return pollRepository.findByTenantIdAndActiveTrueOrderByCreatedAtDesc(tenantId)
                .stream()
                .map(poll -> mapToResponse(poll, getResultsForPoll(poll.getId())))
                .toList();
    }

    public void submitResponse(UUID pollId, PollResponseSubmitRequest request) {
        UUID tenantId = TenantContext.getCurrentTenant();
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new IllegalArgumentException("Poll not found with ID: " + pollId));

        // Delete existing response if present
        pollResponseRepository.findByPollIdAndEmployeeId(pollId, principal.getEmployeeId())
                .ifPresent(pollResponseRepository::delete);

        com.hrlite.entity.PollResponse response = com.hrlite.entity.PollResponse.builder()
                .pollId(pollId)
                .employeeId(principal.getEmployeeId())
                .responseValue(request.getResponseValue())
                .build();

        pollResponseRepository.save(response);
    }

    public PollResponse getPollResults(UUID pollId) {
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new IllegalArgumentException("Poll not found with ID: " + pollId));

        return mapToResponse(poll, getResultsForPoll(pollId));
    }

    public void closePoll(UUID pollId) {
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new IllegalArgumentException("Poll not found with ID: " + pollId));

        poll.setActive(false);
        pollRepository.save(poll);
    }

    private Map<String, Long> getResultsForPoll(UUID pollId) {
        List<Object[]> results = pollResponseRepository.countResponsesByPollId(pollId);
        return results.stream()
                .collect(Collectors.toMap(
                        row -> row[0].toString(),
                        row -> ((Number) row[1]).longValue()
                ));
    }

    private PollResponse mapToResponse(Poll poll, Map<String, Long> results) {
        return PollResponse.builder()
                .id(poll.getId())
                .question(poll.getQuestion())
                .pollType(poll.getPollType())
                .anonymous(poll.isAnonymous())
                .active(poll.isActive())
                .expiresAt(poll.getExpiresAt())
                .results(results)
                .totalResponses(pollResponseRepository.countByPollId(poll.getId()))
                .createdAt(poll.getCreatedAt())
                .updatedAt(poll.getUpdatedAt())
                .build();
    }
}
