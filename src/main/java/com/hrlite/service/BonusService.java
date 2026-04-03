package com.hrlite.service;

import com.hrlite.entity.Bonus;
import com.hrlite.entity.TenantContext;
import com.hrlite.enums.BonusType;
import com.hrlite.exception.ResourceNotFoundException;
import com.hrlite.repository.BonusRepository;
import com.hrlite.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BonusService {

    private final BonusRepository repository;

    @Transactional
    public Bonus createBonus(String name, BigDecimal amount, BonusType bonusType,
                             int month, int year, UUID employeeId, boolean applyToAll,
                             String remarks, UserPrincipal principal) {
        Bonus bonus = Bonus.builder()
                .name(name)
                .amount(amount)
                .bonusType(bonusType)
                .month(month)
                .year(year)
                .employeeId(applyToAll ? null : employeeId)
                .applyToAll(applyToAll)
                .remarks(remarks)
                .createdBy(principal.getUserId())
                .build();
        return repository.save(bonus);
    }

    @Transactional
    public void deleteBonus(UUID bonusId) {
        Bonus bonus = repository.findById(bonusId)
                .orElseThrow(() -> new ResourceNotFoundException("Bonus", bonusId));
        repository.delete(bonus);
    }

    @Transactional(readOnly = true)
    public List<Bonus> getBonuses(int month, int year) {
        UUID tenantId = TenantContext.getCurrentTenant();
        return repository.findByTenantIdAndMonthAndYear(tenantId, month, year);
    }

    @Transactional(readOnly = true)
    public List<Bonus> getAllBonuses() {
        UUID tenantId = TenantContext.getCurrentTenant();
        return repository.findByTenantIdOrderByYearDescMonthDesc(tenantId);
    }
}
