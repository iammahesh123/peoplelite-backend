package com.hrlite.service;

import com.hrlite.dtos.AuditTrailResponse;
import com.hrlite.dtos.StorageBreakdownResponse;
import com.hrlite.dtos.SystemErrorLogResponse;
import com.hrlite.dtos.SystemHealthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SystemHealthService {

    @Transactional(readOnly = true)
    public SystemHealthResponse getSystemHealth() {
        // Get system metrics
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

        long jvmMemoryUsed = memoryBean.getHeapMemoryUsage().getUsed() / (1024 * 1024); // Convert to MB
        long jvmMemoryMax = memoryBean.getHeapMemoryUsage().getMax() / (1024 * 1024);
        double cpuUsage = osBean.getAvailableProcessors() * 100; // Convert to percentage
        if (cpuUsage < 0) {
            cpuUsage = 0;
        }

        // Mock disk usage (in real app, this would come from actual filesystem)
        double diskUsed = 5.5;
        double diskTotal = 10.0;

        long activeConnections = getActiveConnections();

        SystemHealthResponse.Metrics metrics = SystemHealthResponse.Metrics.builder()
                .jvmMemoryUsed(jvmMemoryUsed)
                .jvmMemoryMax(jvmMemoryMax)
                .cpuUsage(Math.round(cpuUsage * 100.0) / 100.0)
                .diskUsed(diskUsed)
                .diskTotal(diskTotal)
                .activeConnections(activeConnections)
                .build();

        // Mock services status
        List<SystemHealthResponse.ServiceStatus> services = new ArrayList<>();
        services.add(SystemHealthResponse.ServiceStatus.builder()
                .name("API Server")
                .status("UP")
                .responseTime(45.5)
                .build());
        services.add(SystemHealthResponse.ServiceStatus.builder()
                .name("Database")
                .status("UP")
                .responseTime(12.3)
                .build());
        services.add(SystemHealthResponse.ServiceStatus.builder()
                .name("Cache")
                .status("UP")
                .responseTime(5.1)
                .build());
        services.add(SystemHealthResponse.ServiceStatus.builder()
                .name("Email Service")
                .status("UP")
                .responseTime(250.0)
                .build());

        // Calculate uptime (mock: assume running for certain hours)
        String uptime = "15d 4h 23m";

        // Performance metrics
        SystemHealthResponse.Performance performance = SystemHealthResponse.Performance.builder()
                .avgResponseTime(52.3)
                .requestsPerMinute(1240.5)
                .errorRate(0.08)
                .p95ResponseTime(234.5)
                .build();

        return SystemHealthResponse.builder()
                .services(services)
                .uptime(uptime)
                .metrics(metrics)
                .performance(performance)
                .lastCheckedAt(LocalDateTime.now())
                .build();
    }

    @Transactional(readOnly = true)
    public SystemErrorLogResponse getErrorLogs(String level) {
        // Mock error logs (in real app, this would query actual error log storage)
        List<SystemErrorLogResponse.ErrorLog> logs = new ArrayList<>();

        if ("ERROR".equals(level) || level == null) {
            logs.add(SystemErrorLogResponse.ErrorLog.builder()
                    .id(UUID.randomUUID().toString())
                    .level("ERROR")
                    .message("NullPointerException in payroll processing")
                    .exception("java.lang.NullPointerException")
                    .source("PayrollService.calculateDeductions()")
                    .timestamp(LocalDateTime.now().minusHours(2))
                    .build());
        }

        if ("WARN".equals(level) || level == null) {
            logs.add(SystemErrorLogResponse.ErrorLog.builder()
                    .id(UUID.randomUUID().toString())
                    .level("WARN")
                    .message("High memory usage detected")
                    .exception("")
                    .source("SystemMonitor")
                    .timestamp(LocalDateTime.now().minusMinutes(30))
                    .build());

            logs.add(SystemErrorLogResponse.ErrorLog.builder()
                    .id(UUID.randomUUID().toString())
                    .level("WARN")
                    .message("Slow database query detected")
                    .exception("")
                    .source("EmployeeRepository.findByTenantId()")
                    .timestamp(LocalDateTime.now().minusMinutes(15))
                    .build());
        }

        return SystemErrorLogResponse.builder()
                .logs(logs)
                .total((long) logs.size())
                .build();
    }

    @Transactional(readOnly = true)
    public StorageBreakdownResponse getStorageBreakdown() {
        // Mock storage breakdown (in real app, this would query actual storage metrics)
        List<StorageBreakdownResponse.TenantStorage> tenants = new ArrayList<>();

        tenants.add(StorageBreakdownResponse.TenantStorage.builder()
                .tenantId(UUID.randomUUID().toString())
                .tenantName("Acme Corp")
                .usedGB(2.5)
                .allocatedGB(5.0)
                .usagePercentage(50.0)
                .build());

        tenants.add(StorageBreakdownResponse.TenantStorage.builder()
                .tenantId(UUID.randomUUID().toString())
                .tenantName("TechStart Inc")
                .usedGB(1.2)
                .allocatedGB(5.0)
                .usagePercentage(24.0)
                .build());

        tenants.add(StorageBreakdownResponse.TenantStorage.builder()
                .tenantId(UUID.randomUUID().toString())
                .tenantName("Global Solutions")
                .usedGB(3.8)
                .allocatedGB(5.0)
                .usagePercentage(76.0)
                .build());

        double totalUsed = tenants.stream().mapToDouble(StorageBreakdownResponse.TenantStorage::getUsedGB).sum();
        double totalAllocated = tenants.stream().mapToDouble(StorageBreakdownResponse.TenantStorage::getAllocatedGB).sum();
        double usagePercentage = (totalUsed / totalAllocated) * 100;

        return StorageBreakdownResponse.builder()
                .tenants(tenants)
                .totalUsedGB(totalUsed)
                .totalAllocatedGB(totalAllocated)
                .usagePercentage(Math.round(usagePercentage * 100.0) / 100.0)
                .build();
    }

    @Transactional(readOnly = true)
    public AuditTrailResponse getAuditTrail(int page, int limit) {
        // Mock audit trail (in real app, this would query actual audit log storage)
        List<AuditTrailResponse.AuditEntry> entries = new ArrayList<>();

        String[] actions = {"CREATE", "UPDATE", "DELETE", "APPROVE", "REJECT"};
        String[] entityTypes = {"EMPLOYEE", "LEAVE", "PAYSLIP", "PAYROLL_RUN", "ONBOARDING_TASK"};

        for (int i = 0; i < Math.min(limit, 10); i++) {
            entries.add(AuditTrailResponse.AuditEntry.builder()
                    .id(UUID.randomUUID().toString())
                    .tenantId(UUID.randomUUID().toString())
                    .userId(UUID.randomUUID().toString())
                    .action(actions[i % actions.length])
                    .entityType(entityTypes[i % entityTypes.length])
                    .entityId(UUID.randomUUID().toString())
                    .changes("{\"status\": \"APPROVED\"}")
                    .ipAddress("192.168.1." + (100 + i))
                    .timestamp(LocalDateTime.now().minusHours(i))
                    .build());
        }

        return AuditTrailResponse.builder()
                .entries(entries)
                .total(1234L)
                .page(page)
                .build();
    }

    private long getActiveConnections() {
        // Mock active connections - in real app, get from connection pool
        return 42;
    }
}
