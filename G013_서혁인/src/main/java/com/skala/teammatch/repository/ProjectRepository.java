package com.skala.teammatch.repository;

import com.skala.teammatch.entity.Project;
import com.skala.teammatch.entity.Project.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByStatusOrderByCreatedAtDesc(ProjectStatus status);
    List<Project> findAllByOrderByCreatedAtDesc();
    long countByStatus(ProjectStatus status);
}
