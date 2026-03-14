package com.hrlite.repository;

import com.hrlite.entity.PlatformFeatureFlag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlatformFeatureFlagRepository extends JpaRepository<PlatformFeatureFlag, UUID> {

    Optional<PlatformFeatureFlag> findByName(String name);
}
