package com.skala.teammatch.repository;

import com.skala.teammatch.entity.TeamApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamApplicationRepository extends JpaRepository<TeamApplication, Long> {
    List<TeamApplication> findByProjectIdOrderByAppliedAtDesc(Long projectId);
    List<TeamApplication> findByApplicantIdOrderByAppliedAtDesc(Long applicantId);
    boolean existsByProjectIdAndApplicantIdAndSlotId(Long projectId, Long applicantId, Long slotId);
}
