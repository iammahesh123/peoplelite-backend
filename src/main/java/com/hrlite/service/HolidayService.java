package com.hrlite.service;

import com.hrlite.entity.Holiday;
import com.hrlite.entity.TenantContext;
import com.hrlite.exception.ResourceNotFoundException;
import com.hrlite.repository.HolidayRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class HolidayService {

    private final HolidayRepository repository;

    @Transactional
    public Holiday create(String name, LocalDate date, boolean optional) {
        Holiday holiday = Holiday.builder()
                .name(name)
                .date(date)
                .year(date.getYear())
                .optional(optional)
                .build();
        return repository.save(holiday);
    }

    @Transactional
    public void delete(UUID holidayId) {
        Holiday h = repository.findById(holidayId)
                .orElseThrow(() -> new ResourceNotFoundException("Holiday", holidayId));
        repository.delete(h);
    }

    @Transactional(readOnly = true)
    public List<Holiday> getByYear(int year) {
        UUID tenantId = TenantContext.getCurrentTenant();
        return repository.findByTenantIdAndYearOrderByDateAsc(tenantId, year);
    }

    /**
     * Returns the count of mandatory (non-optional) holidays in a given month.
     */
    @Transactional(readOnly = true)
    public int getMandatoryHolidayCountInMonth(int month, int year) {
        UUID tenantId = TenantContext.getCurrentTenant();
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        return (int) repository.countByTenantIdAndDateBetweenAndOptionalFalse(tenantId, start, end);
    }
}
