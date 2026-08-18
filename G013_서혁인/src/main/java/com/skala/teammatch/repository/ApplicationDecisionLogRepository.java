package com.skala.teammatch.repository;

import com.skala.teammatch.entity.ApplicationDecisionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationDecisionLogRepository extends JpaRepository<ApplicationDecisionLog, Long> {
}
