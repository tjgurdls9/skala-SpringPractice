package com.skala.teammatch.repository;

import com.skala.teammatch.entity.RecruitRole;
import com.skala.teammatch.entity.RecruitSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecruitSlotRepository extends JpaRepository<RecruitSlot, Long> {
    List<RecruitSlot> findByProjectId(Long projectId);
    Optional<RecruitSlot> findByProjectIdAndRole(Long projectId, RecruitRole role);
}
