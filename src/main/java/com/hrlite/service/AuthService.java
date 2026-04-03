package com.hrlite.service;

import com.hrlite.dtos.*;
import com.hrlite.entity.EmailVerificationOtp;
import com.hrlite.entity.PasswordResetToken;
import com.hrlite.entity.RefreshToken;
import com.hrlite.enums.Role;
import com.hrlite.entity.User;
import com.hrlite.repository.EmailVerificationOtpRepository;
import com.hrlite.repository.PasswordResetTokenRepository;
import com.hrlite.repository.RefreshTokenRepository;
import com.hrlite.repository.UserRepository;
import com.hrlite.exception.BusinessException;
import com.hrlite.exception.ErrorCodes;
import com.hrlite.security.JwtTokenProvider;
import com.hrlite.entity.TenantContext;
import com.hrlite.entity.Employee;
import com.hrlite.enums.EmploymentType;
import com.hrlite.enums.EmployeeStatus;
import com.hrlite.repository.EmployeeRepository;
import com.hrlite.entity.Tenant;
import com.hrlite.repository.TenantRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailVerificationOtpRepository emailVerificationOtpRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final EmailService emailService;
    private final NotificationEventService notificationEventService;
    private final SubscriptionService subscriptionService;

    @Transactional
    public void sendRegistrationOtp(RegisterRequest request) {
        // Validate email doesn't already exist in users table
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException(ErrorCodes.AUTH_EMAIL_EXISTS,
                    "An account with this email already exists");
        }

        // Generate 6-digit OTP
        String otpCode = String.format("%06d", new SecureRandom().nextInt(999999));

        // Delete any existing unverified OTPs for this email
        emailVerificationOtpRepository.deleteByEmailAndVerifiedFalse(request.getEmail());

        // Hash the password with BCrypt
        String passwordHash = passwordEncoder.encode(request.getPassword());

        // Save EmailVerificationOtp
        EmailVerificationOtp otp = EmailVerificationOtp.builder()
                .email(request.getEmail())
                .otpCode(otpCode)
                .companyName(request.getCompanyName())
                .fullName(request.getFullName())
                .passwordHash(passwordHash)
                .verified(false)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .createdAt(LocalDateTime.now())
                .build();
        emailVerificationOtpRepository.save(otp);

        // Send OTP email using EmailService
        emailService.sendOtpEmail(request.getEmail(), otpCode, request.getFullName());
    }

    @Transactional
    public AuthResponse verifyOtpAndRegister(String email, String otpCode) {
        // Find OTP record by email and otpCode where verified = false
        EmailVerificationOtp otp = emailVerificationOtpRepository
                .findByEmailAndOtpCodeAndVerifiedFalse(email, otpCode)
                .orElseThrow(() -> new BusinessException(ErrorCodes.AUTH_INVALID_CREDENTIALS,
                        "Invalid OTP", HttpStatus.BAD_REQUEST));

        // Check if OTP is expired
        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCodes.AUTH_PASSWORD_RESET_TOKEN_EXPIRED,
                    "OTP expired, please request a new one", HttpStatus.BAD_REQUEST);
        }

        // Mark OTP as verified
        otp.setVerified(true);
        emailVerificationOtpRepository.save(otp);

        // Create the Tenant, Employee, User (same logic as current register)
        String slug = generateSlug(otp.getCompanyName());
        Tenant tenant = Tenant.builder()
                .name(otp.getCompanyName())
                .slug(slug)
                .build();
        tenant = tenantRepository.save(tenant);

        // Initiate 30-day free trial for the new tenant
        subscriptionService.initiateTrial(tenant);

        // Set tenant context for downstream operations
        TenantContext.setCurrentTenant(tenant.getId());

        // Create founder as employee
        String[] nameParts = otp.getFullName().split(" ", 2);
        Employee employee = Employee.builder()
//                .tenantId(tenant.getId())
                .employeeCode(tenant.generateNextEmployeeCode())
                .firstName(nameParts[0])
                .lastName(nameParts.length > 1 ? nameParts[1] : "")
                .email(otp.getEmail())
                .dateOfJoining(LocalDate.now())
                .employmentType(EmploymentType.FULL_TIME)
                .status(EmployeeStatus.ACTIVE)
                .monthlyCTC(BigDecimal.ZERO)
                .basicSalary(BigDecimal.ZERO)
                .hra(BigDecimal.ZERO)
                .specialAllowance(BigDecimal.ZERO)
                .build();
        employee = employeeRepository.save(employee);
        tenantRepository.save(tenant);

        // Create user
        User user = User.builder()
                .tenantId(tenant.getId())
                .email(otp.getEmail())
                .passwordHash(otp.getPasswordHash())
                .role(Role.FOUNDER)
                .employeeId(employee.getId())
                .build();
        user = userRepository.save(user);

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), tenant.getId(), user.getRole().name(), employee.getId());
        String refreshToken = createRefreshToken(user.getId());

        // Trigger welcome registration notification event
        notificationEventService.onWelcomeRegistration(tenant.getId(), user.getId(),
                otp.getFullName(), otp.getEmail(), otp.getCompanyName());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .tenantId(tenant.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .fullName(otp.getFullName())
                .employeeId(employee.getId())
                .plan(tenant.getPlan())
                .build();
    }

    @Transactional
    @Deprecated(forRemoval = true)
    public AuthResponse register(RegisterRequest request) {
        // Check if email already exists globally (for founders)
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException(ErrorCodes.AUTH_EMAIL_EXISTS,
                    "An account with this email already exists");
        }

        // Create tenant
        String slug = generateSlug(request.getCompanyName());
        Tenant tenant = Tenant.builder()
                .name(request.getCompanyName())
                .slug(slug)
                .build();
        tenant = tenantRepository.save(tenant);

        // Set tenant context for downstream operations
        TenantContext.setCurrentTenant(tenant.getId());

        // Create founder as employee
        String[] nameParts = request.getFullName().split(" ", 2);
        Employee employee = Employee.builder()
//                .tenantId(tenant.getId())
                .employeeCode(tenant.generateNextEmployeeCode())
                .firstName(nameParts[0])
                .lastName(nameParts.length > 1 ? nameParts[1] : "")
                .email(request.getEmail())
                .dateOfJoining(LocalDate.now())
                .employmentType(EmploymentType.FULL_TIME)
                .status(EmployeeStatus.ACTIVE)
                .monthlyCTC(BigDecimal.ZERO)
                .basicSalary(BigDecimal.ZERO)
                .hra(BigDecimal.ZERO)
                .specialAllowance(BigDecimal.ZERO)
                .build();
        employee = employeeRepository.save(employee);
        tenantRepository.save(tenant);

        // Create user
        User user = User.builder()
                .tenantId(tenant.getId())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.FOUNDER)
                .employeeId(employee.getId())
                .build();
        user = userRepository.save(user);

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), tenant.getId(), user.getRole().name(), employee.getId());
        String refreshToken = createRefreshToken(user.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .tenantId(tenant.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .fullName(request.getFullName())
                .employeeId(employee.getId())
                .plan(tenant.getPlan())
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCodes.AUTH_INVALID_CREDENTIALS,
                        "Invalid email or password", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCodes.AUTH_INVALID_CREDENTIALS,
                    "Invalid email or password", HttpStatus.UNAUTHORIZED);
        }

        if (!user.isActive()) {
            throw new BusinessException(ErrorCodes.AUTH_INVALID_CREDENTIALS,
                    "Account is deactivated", HttpStatus.UNAUTHORIZED);
        }

        Tenant tenant = tenantRepository.findById(user.getTenantId())
                .orElseThrow(() -> new BusinessException(ErrorCodes.TENANT_NOT_FOUND, "Tenant not found"));

        if (!tenant.isActive()) {
            throw new BusinessException(ErrorCodes.TENANT_INACTIVE,
                    "Your organization account is inactive", HttpStatus.FORBIDDEN);
        }

        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getTenantId(), user.getRole().name(), user.getEmployeeId());
        String refreshToken = createRefreshToken(user.getId());

        // Get employee name
        String fullName = "";
        if (user.getEmployeeId() != null) {
            Employee emp = employeeRepository.findById(user.getEmployeeId()).orElse(null);
            if (emp != null) {
                fullName = emp.getFullName();
            }
        }

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .tenantId(user.getTenantId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .fullName(fullName.trim())
                .employeeId(user.getEmployeeId())
                .plan(tenant.getPlan())
                .passwordChangeRequired(user.isPasswordChangeRequired())
                .build();
    }

    @Transactional
    public void changePassword(UUID userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCodes.AUTH_INVALID_CREDENTIALS, "User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new BusinessException(ErrorCodes.AUTH_INVALID_CREDENTIALS,
                    "Current password is incorrect", HttpStatus.BAD_REQUEST);
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordChangeRequired(false);
        userRepository.save(user);
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken storedToken = refreshTokenRepository.findByTokenAndRevokedFalse(request.getRefreshToken())
                .orElseThrow(() -> new BusinessException(ErrorCodes.AUTH_REFRESH_TOKEN_INVALID,
                        "Invalid or expired refresh token", HttpStatus.UNAUTHORIZED));

        if (storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            storedToken.setRevoked(true);
            refreshTokenRepository.save(storedToken);
            throw new BusinessException(ErrorCodes.AUTH_REFRESH_TOKEN_INVALID,
                    "Refresh token has expired", HttpStatus.UNAUTHORIZED);
        }

        // Revoke old token
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        User user = userRepository.findById(storedToken.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCodes.AUTH_INVALID_CREDENTIALS, "User not found"));

        Tenant tenant = tenantRepository.findById(user.getTenantId())
                .orElseThrow(() -> new BusinessException(ErrorCodes.TENANT_NOT_FOUND, "Tenant not found"));

        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getTenantId(), user.getRole().name(), user.getEmployeeId());
        String newRefreshToken = createRefreshToken(user.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken)
                .userId(user.getId())
                .tenantId(user.getTenantId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .employeeId(user.getEmployeeId())
                .plan(tenant.getPlan())
                .build();
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        refreshTokenRepository.findByTokenAndRevokedFalse(request.getRefreshToken())
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }

    @Transactional
    public AuthResponse googleLogin(GoogleLoginRequest request) {
        try {
            String userInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo?access_token=" + request.getAccessToken();
            String response = restTemplate.getForObject(userInfoUrl, String.class);
            JsonNode userInfo = objectMapper.readTree(response);

            String email = userInfo.get("email").asText();
            String googleId = userInfo.get("sub").asText();
            String name = userInfo.get("name").asText();
            String picture = userInfo.has("picture") ? userInfo.get("picture").asText() : null;

            // Check if user exists by email
            User existingUser = userRepository.findByEmail(email).orElse(null);

            if (existingUser != null) {
                // User exists, update Google info and log in
                if (existingUser.getGoogleId() == null) {
                    existingUser.setGoogleId(googleId);
                    existingUser.setAuthProvider("GOOGLE");
                    if (picture != null) {
                        existingUser.setAvatarUrl(picture);
                    }
                    userRepository.save(existingUser);
                }

                Tenant tenant = tenantRepository.findById(existingUser.getTenantId())
                        .orElseThrow(() -> new BusinessException(ErrorCodes.TENANT_NOT_FOUND, "Tenant not found"));

                if (!tenant.isActive()) {
                    throw new BusinessException(ErrorCodes.TENANT_INACTIVE,
                            "Your organization account is inactive", HttpStatus.FORBIDDEN);
                }

                String accessToken = jwtTokenProvider.generateAccessToken(
                        existingUser.getId(), existingUser.getTenantId(), existingUser.getRole().name(), existingUser.getEmployeeId());
                String refreshToken = createRefreshToken(existingUser.getId());

                return AuthResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .userId(existingUser.getId())
                        .tenantId(existingUser.getTenantId())
                        .email(existingUser.getEmail())
                        .role(existingUser.getRole().name())
                        .fullName(name)
                        .employeeId(existingUser.getEmployeeId())
                        .plan(tenant.getPlan())
                        .build();
            } else {
                // Create new tenant and user for Google signup
                String emailLocal = email.contains("@") ? email.split("@")[0] : email;
                String slug = generateSlug(emailLocal);
                Tenant tenant = Tenant.builder()
                        .name(name)
                        .slug(slug)
                        .build();
                tenant = tenantRepository.save(tenant);

                // Initiate 30-day free trial for the new tenant
                subscriptionService.initiateTrial(tenant);

                TenantContext.setCurrentTenant(tenant.getId());

                // Create employee
                String[] nameParts = name.split(" ", 2);
                Employee employee = Employee.builder()
//                        .tenantId(tenant.getId())
                        .employeeCode(tenant.generateNextEmployeeCode())
                        .firstName(nameParts[0])
                        .lastName(nameParts.length > 1 ? nameParts[1] : "")
                        .email(email)
                        .dateOfJoining(LocalDate.now())
                        .employmentType(EmploymentType.FULL_TIME)
                        .status(EmployeeStatus.ACTIVE)
                        .monthlyCTC(BigDecimal.ZERO)
                        .basicSalary(BigDecimal.ZERO)
                        .hra(BigDecimal.ZERO)
                        .specialAllowance(BigDecimal.ZERO)
                        .build();
                employee = employeeRepository.save(employee);
                tenantRepository.save(tenant);

                // Create user with Google auth
                User user = User.builder()
                        .tenantId(tenant.getId())
                        .email(email)
                        .googleId(googleId)
                        .authProvider("GOOGLE")
                        .avatarUrl(picture)
                        .role(Role.FOUNDER)
                        .employeeId(employee.getId())
                        .build();
                user = userRepository.save(user);

                // Generate tokens
                String accessToken = jwtTokenProvider.generateAccessToken(
                        user.getId(), tenant.getId(), user.getRole().name(), employee.getId());
                String refreshToken = createRefreshToken(user.getId());

                return AuthResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .userId(user.getId())
                        .tenantId(tenant.getId())
                        .email(user.getEmail())
                        .role(user.getRole().name())
                        .fullName(name)
                        .employeeId(employee.getId())
                        .plan(tenant.getPlan())
                        .build();
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCodes.AUTH_GOOGLE_LOGIN_FAILED,
                    "Google login failed: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null) {
            // Return success for security (don't reveal if email exists)
            return;
        }

        // Generate reset token
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .userId(user.getId())
                .token(token)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .used(false)
                .build();
        passwordResetTokenRepository.save(resetToken);

        // Send email with reset link
        String resetLink = "https://peoplelite.vercel.app//reset-password?token=" + token;
        String fullName = "";
        if (user.getEmployeeId() != null) {
            Employee emp = employeeRepository.findById(user.getEmployeeId()).orElse(null);
            if (emp != null) {
                fullName = emp.getFullName();
            }
        }
        emailService.sendPasswordResetEmail(user.getEmail(), fullName.trim(), resetLink);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new BusinessException(ErrorCodes.AUTH_PASSWORD_RESET_TOKEN_INVALID,
                        "Invalid reset token", HttpStatus.BAD_REQUEST));

        if (resetToken.isUsed()) {
            throw new BusinessException(ErrorCodes.AUTH_PASSWORD_RESET_TOKEN_INVALID,
                    "Reset token has already been used", HttpStatus.BAD_REQUEST);
        }

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCodes.AUTH_PASSWORD_RESET_TOKEN_EXPIRED,
                    "Reset token has expired", HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCodes.AUTH_INVALID_CREDENTIALS, "User not found"));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }

    private String createRefreshToken(UUID userId) {
        String tokenValue = jwtTokenProvider.generateRefreshToken(userId);
        RefreshToken token = RefreshToken.builder()
                .userId(userId)
                .token(tokenValue)
                .expiresAt(LocalDateTime.now().plusSeconds(jwtTokenProvider.getRefreshTokenExpiryMs() / 1000))
                .build();
        refreshTokenRepository.save(token);
        return tokenValue;
    }

    private String generateSlug(String companyName) {
        String base = companyName.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        String slug = base;
        int counter = 1;
        while (tenantRepository.existsBySlug(slug)) {
            slug = base + "-" + counter++;
        }
        return slug;
    }
}
