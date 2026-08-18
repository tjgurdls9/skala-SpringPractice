package com.skala.teammatch.actuator;

import com.skala.teammatch.entity.Project.ProjectStatus;
import com.skala.teammatch.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * 커스텀 Health Indicator.
 *
 * /actuator/health 안에 "activeProjects" 항목으로 노출된다.
 * 현재 모집 중(RECRUITING)인 프로젝트가 0개면 신규 회원이 지원할 곳이 없다는 뜻이므로 DOWN 으로 본다.
 * Spring Boot 의 기본 StatusAggregator 는 하위 지표 중 가장 나쁜 상태를 상위로 전파하므로,
 * 이 지표가 DOWN 이면 /actuator/health 전체 status 도 DOWN 이 된다.
 */
@Component("activeProjects")
@RequiredArgsConstructor
public class ActiveProjectHealthIndicator implements HealthIndicator {

    private final ProjectRepository projectRepository;

    @Override
    public Health health() {
        try {
            long recruitingCount = projectRepository.countByStatus(ProjectStatus.RECRUITING);

            Health.Builder builder = recruitingCount > 0 ? Health.up() : Health.down();
            return builder
                    .withDetail("recruitingProjectCount", recruitingCount)
                    .withDetail("reason", recruitingCount > 0
                            ? "모집 중인 프로젝트 있음"
                            : "모집 중인 프로젝트가 없어 신규 지원 불가")
                    .build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
