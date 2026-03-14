package com.hrlite.service;

import com.hrlite.dtos.TeamAvailabilityResponse;
import com.hrlite.entity.Employee;
import com.hrlite.entity.LeaveRequest;
import com.hrlite.entity.LeaveType;
import com.hrlite.entity.TenantContext;
import com.hrlite.enums.EmployeeAvailabilityStatus;
import com.hrlite.enums.EmployeeStatus;
import com.hrlite.repository.EmployeeRepository;
import com.hrlite.repository.LeaveRequestRepository;
import com.hrlite.repository.LeaveTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamAvailabilityService {

    private final EmployeeRepository employeeRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveTypeRepository leaveTypeRepository;

    public List<TeamAvailabilityResponse> getTeamAvailabilityForDate(LocalDate date) {
        UUID tenantId = TenantContext.getCurrentTenant();
        List<Employee> employees = employeeRepository.findByTenantIdAndStatus(tenantId, EmployeeStatus.ACTIVE);
        List<LeaveRequest> approvedLeaves = leaveRequestRepository.findApprovedLeavesInRange(tenantId, date, date);
        Map<UUID, LeaveType> leaveTypeMap = buildLeaveTypeMap(tenantId);

        return employees.stream()
                .map(emp -> buildTeamAvailabilityResponse(emp, date, approvedLeaves, leaveTypeMap))
                .toList();
    }

    public List<TeamAvailabilityResponse> getTeamAvailabilityForWeek(LocalDate startDate) {
        LocalDate endDate = startDate.plusDays(6);
        UUID tenantId = TenantContext.getCurrentTenant();
        List<Employee> employees = employeeRepository.findByTenantIdAndStatus(tenantId, EmployeeStatus.ACTIVE);
        List<LeaveRequest> approvedLeaves = leaveRequestRepository.findApprovedLeavesInRange(tenantId, startDate, endDate);
        Map<UUID, LeaveType> leaveTypeMap = buildLeaveTypeMap(tenantId);

        List<TeamAvailabilityResponse> results = new ArrayList<>();
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            for (Employee emp : employees) {
                results.add(buildTeamAvailabilityResponse(emp, currentDate, approvedLeaves, leaveTypeMap));
            }
            currentDate = currentDate.plusDays(1);
        }
        return results;
    }

    private TeamAvailabilityResponse buildTeamAvailabilityResponse(
            Employee employee, LocalDate date, List<LeaveRequest> approvedLeaves, Map<UUID, LeaveType> leaveTypeMap) {

        EmployeeAvailabilityStatus status = EmployeeAvailabilityStatus.AVAILABLE;
        String leaveType = null;

        for (LeaveRequest leave : approvedLeaves) {
            if (leave.getEmployeeId().equals(employee.getId()) &&
                    !date.isBefore(leave.getStartDate()) && !date.isAfter(leave.getEndDate())) {
                status = EmployeeAvailabilityStatus.ON_LEAVE;
                LeaveType lt = leaveTypeMap.get(leave.getLeaveTypeId());
                leaveType = lt != null ? lt.getName() : "Unknown";
                break;
            }
        }

        return TeamAvailabilityResponse.builder()
                .employeeId(employee.getId())
                .employeeName(employee.getFullName())
                .employeeCode(employee.getEmployeeCode())
                .department(employee.getDepartment())
                .date(date)
                .status(status)
                .leaveType(leaveType)
                .build();
    }

    private Map<UUID, LeaveType> buildLeaveTypeMap(UUID tenantId) {
        Map<UUID, LeaveType> map = new HashMap<>();
        leaveTypeRepository.findByTenantId(tenantId)
                .forEach(lt -> map.put(lt.getId(), lt));
        return map;
    }
}
