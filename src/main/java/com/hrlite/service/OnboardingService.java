package com.hrlite.service;

import com.hrlite.dtos.OnboardingStatusResponse;
import com.hrlite.dtos.OnboardingTaskResponse;
import com.hrlite.dtos.OnboardingTemplateRequest;
import com.hrlite.dtos.OnboardingTemplateResponse;
import com.hrlite.exception.ResourceNotFoundException;
import com.hrlite.security.UserPrincipal;
import com.hrlite.entity.TenantContext;
import com.hrlite.entity.Employee;
import com.hrlite.repository.EmployeeRepository;
import com.hrlite.entity.OnboardingTask;
import com.hrlite.entity.OnboardingTemplate;
import com.hrlite.repository.OnboardingTaskRepository;
import com.hrlite.repository.OnboardingTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final OnboardingTemplateRepository templateRepository;
    private final OnboardingTaskRepository taskRepository;
    private final EmployeeRepository employeeRepository;

    // --- Templates ---

    @Transactional(readOnly = true)
    public List<OnboardingTemplateResponse> getTemplates() {
        UUID tenantId = TenantContext.getCurrentTenant();
        return templateRepository.findByTenantIdOrderByOrderIndexAsc(tenantId).stream()
                .map(this::toTemplateResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public OnboardingTemplateResponse createTemplate(OnboardingTemplateRequest request) {
        OnboardingTemplate template = OnboardingTemplate.builder()
                .taskName(request.getTaskName())
                .description(request.getDescription())
                .orderIndex(request.getOrderIndex())
                .build();
        return toTemplateResponse(templateRepository.save(template));
    }

    // --- Tasks ---

    @Transactional
    public OnboardingStatusResponse initializeForEmployee(UUID employeeId) {
        UUID tenantId = TenantContext.getCurrentTenant();
        Employee employee = employeeRepository.findByIdAndTenantId(employeeId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", employeeId));

        List<OnboardingTemplate> templates = templateRepository
                .findByTenantIdAndActiveTrueOrderByOrderIndexAsc(tenantId);

        for (OnboardingTemplate template : templates) {
            OnboardingTask task = OnboardingTask.builder()
                    .employeeId(employeeId)
                    .templateId(template.getId())
                    .taskName(template.getTaskName())
                    .description(template.getDescription())
                    .orderIndex(template.getOrderIndex())
                    .build();
            taskRepository.save(task);
        }

        return getEmployeeOnboarding(employeeId);
    }

    @Transactional(readOnly = true)
    public OnboardingStatusResponse getEmployeeOnboarding(UUID employeeId) {
        UUID tenantId = TenantContext.getCurrentTenant();
        Employee employee = employeeRepository.findByIdAndTenantId(employeeId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", employeeId));

        List<OnboardingTask> tasks = taskRepository.findByEmployeeIdOrderByOrderIndexAsc(employeeId);
        long total = tasks.size();
        long completed = tasks.stream().filter(OnboardingTask::isCompleted).count();
        int progress = total > 0 ? (int) ((completed * 100) / total) : 0;

        return OnboardingStatusResponse.builder()
                .employeeId(employeeId)
                .employeeName(employee.getFullName())
                .totalTasks(total)
                .completedTasks(completed)
                .progressPercent(progress)
                .tasks(tasks.stream().map(this::toTaskResponse).collect(Collectors.toList()))
                .build();
    }

    @Transactional
    public OnboardingTaskResponse completeTask(UUID taskId, UserPrincipal principal) {
        OnboardingTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("OnboardingTask", taskId));

        task.setCompleted(true);
        task.setCompletedAt(LocalDateTime.now());
        task.setCompletedBy(principal.getUserId());

        return toTaskResponse(taskRepository.save(task));
    }

    private OnboardingTemplateResponse toTemplateResponse(OnboardingTemplate t) {
        return OnboardingTemplateResponse.builder()
                .id(t.getId())
                .taskName(t.getTaskName())
                .description(t.getDescription())
                .orderIndex(t.getOrderIndex())
                .active(t.isActive())
                .build();
    }

    private OnboardingTaskResponse toTaskResponse(OnboardingTask t) {
        return OnboardingTaskResponse.builder()
                .id(t.getId())
                .employeeId(t.getEmployeeId())
                .taskName(t.getTaskName())
                .description(t.getDescription())
                .orderIndex(t.getOrderIndex())
                .completed(t.isCompleted())
                .completedAt(t.getCompletedAt())
                .build();
    }
}
