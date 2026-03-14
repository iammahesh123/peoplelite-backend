package com.hrlite.service;

import com.hrlite.enums.Role;
import com.hrlite.entity.User;
import com.hrlite.repository.UserRepository;
import com.hrlite.exception.BusinessException;
import com.hrlite.exception.ErrorCodes;
import com.hrlite.exception.ResourceNotFoundException;
import com.hrlite.entity.TenantContext;
import com.hrlite.entity.Employee;
import com.hrlite.enums.EmployeeStatus;
import com.hrlite.repository.EmployeeRepository;
import com.hrlite.dtos.CreateEmployeeRequest;
import com.hrlite.dtos.EmployeeResponse;
import com.hrlite.dtos.UpdateEmployeeRequest;
import com.hrlite.mapper.EmployeeMapper;
import com.hrlite.entity.Tenant;
import com.hrlite.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LeaveService leaveService;
    private final NotificationEventService notificationEventService;

    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAllEmployees() {
        UUID tenantId = TenantContext.getCurrentTenant();
        return employeeRepository.findByTenantId(tenantId).stream()
                .map(EmployeeMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployee(UUID id) {
        Employee employee = findEmployeeForTenant(id);
        return EmployeeMapper.toResponse(employee);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getMyProfile(UUID employeeId) {
        Employee employee = findEmployeeForTenant(employeeId);
        return EmployeeMapper.toResponse(employee);
    }

    @Transactional
    public EmployeeResponse createEmployee(CreateEmployeeRequest request) {
        UUID tenantId = TenantContext.getCurrentTenant();

        if (employeeRepository.existsByEmailAndTenantId(request.getEmail(), tenantId)) {
            throw new BusinessException(ErrorCodes.EMPLOYEE_EMAIL_EXISTS,
                    "Employee with this email already exists");
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", tenantId));

        Employee employee = Employee.builder()
//                .tenantId(tenantId)
                .employeeCode(tenant.generateNextEmployeeCode())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .dateOfBirth(request.getDateOfBirth())
                .dateOfJoining(request.getDateOfJoining())
                .employmentType(request.getEmploymentType())
                .department(request.getDepartment())
                .designation(request.getDesignation())
                .monthlyCTC(request.getMonthlyCTC() != null ? request.getMonthlyCTC() : BigDecimal.ZERO)
                .basicSalary(request.getBasicSalary() != null ? request.getBasicSalary() : BigDecimal.ZERO)
                .hra(request.getHra() != null ? request.getHra() : BigDecimal.ZERO)
                .specialAllowance(request.getSpecialAllowance() != null ? request.getSpecialAllowance() : BigDecimal.ZERO)
                .build();

        employee = employeeRepository.save(employee);

        // Initialize leave balances for new employee
        leaveService.initializeBalancesForEmployee(employee.getId());

        tenantRepository.save(tenant);

        // Create user account for employee
        String defaultPassword = request.getPassword() != null ? request.getPassword() : "Welcome@123";
        User user = User.builder()
                .tenantId(tenantId)
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(defaultPassword))
                .role(Role.EMPLOYEE)
                .employeeId(employee.getId())
                .build();
        user = userRepository.save(user);

        // Get founder's name for the notification
        String founderName = "Founder";
        List<User> founders = userRepository.findByTenantIdAndRole(tenantId, Role.FOUNDER);
        if (!founders.isEmpty()) {
            User founder = founders.get(0);
            if (founder.getEmployeeId() != null) {
                Employee founderEmp = employeeRepository.findById(founder.getEmployeeId()).orElse(null);
                if (founderEmp != null) {
                    founderName = founderEmp.getFullName();
                }
            }
        }

        // Trigger employee created notification event
        Tenant currentTenant = tenantRepository.findById(tenantId).orElse(null);
        String companyName = currentTenant != null ? currentTenant.getName() : "";
        notificationEventService.onEmployeeCreated(tenantId, user.getId(), employee.getFullName(),
                founderName, request.getEmail(), defaultPassword, companyName);

        return EmployeeMapper.toResponse(employee);
    }

    @Transactional
    public EmployeeResponse updateEmployee(UUID id, UpdateEmployeeRequest request) {
        Employee employee = findEmployeeForTenant(id);

        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        if (request.getEmail() != null) {
            employee.setEmail(request.getEmail());
        }
        employee.setPhone(request.getPhone());
        employee.setDateOfBirth(request.getDateOfBirth());
        if (request.getEmploymentType() != null) {
            employee.setEmploymentType(request.getEmploymentType());
        }
        employee.setDepartment(request.getDepartment());
        employee.setDesignation(request.getDesignation());
        if (request.getMonthlyCTC() != null) employee.setMonthlyCTC(request.getMonthlyCTC());
        if (request.getBasicSalary() != null) employee.setBasicSalary(request.getBasicSalary());
        if (request.getHra() != null) employee.setHra(request.getHra());
        if (request.getSpecialAllowance() != null) employee.setSpecialAllowance(request.getSpecialAllowance());

        return EmployeeMapper.toResponse(employeeRepository.save(employee));
    }

    @Transactional
    public void deactivateEmployee(UUID id) {
        Employee employee = findEmployeeForTenant(id);
        employee.setStatus(EmployeeStatus.INACTIVE);
        employeeRepository.save(employee);

        // Also deactivate user account
        userRepository.findByEmployeeId(id).ifPresent(user -> {
            user.setActive(false);
            userRepository.save(user);
        });
    }

    private Employee findEmployeeForTenant(UUID id) {
        UUID tenantId = TenantContext.getCurrentTenant();
        return employeeRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
    }
}
