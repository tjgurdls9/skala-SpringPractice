package com.skala.teammatch.repository;

import com.skala.teammatch.entity.ProjectTechStack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectTechStackRepository extends JpaRepository<ProjectTechStack, Long> {
    List<ProjectTechStack> findByProjectId(Long projectId);
}
