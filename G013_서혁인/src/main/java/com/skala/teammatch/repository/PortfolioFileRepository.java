package com.skala.teammatch.repository;

import com.skala.teammatch.entity.PortfolioFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortfolioFileRepository extends JpaRepository<PortfolioFile, Long> {
    List<PortfolioFile> findByMemberId(Long memberId);
}
