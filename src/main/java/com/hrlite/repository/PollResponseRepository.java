package com.hrlite.repository;

import com.hrlite.entity.PollResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PollResponseRepository extends JpaRepository<PollResponse, UUID> {

    List<PollResponse> findByPollId(UUID pollId);

    Optional<PollResponse> findByPollIdAndEmployeeId(UUID pollId, UUID employeeId);

    @Query("SELECT pr.responseValue, COUNT(pr.id) FROM PollResponse pr WHERE pr.pollId = :pollId GROUP BY pr.responseValue")
    List<Object[]> countResponsesByPollId(UUID pollId);

    long countByPollId(UUID pollId);

    boolean existsByPollIdAndEmployeeId(UUID pollId, UUID employeeId);
}
