package com.hrlite.repository;

import com.hrlite.entity.EmailVerificationOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailVerificationOtpRepository extends JpaRepository<EmailVerificationOtp, UUID> {

    Optional<EmailVerificationOtp> findByEmailAndOtpCodeAndVerifiedFalse(String email, String otpCode);

    void deleteByEmailAndVerifiedFalse(String email);

    Optional<EmailVerificationOtp> findTopByEmailAndVerifiedTrueOrderByCreatedAtDesc(String email);
}
