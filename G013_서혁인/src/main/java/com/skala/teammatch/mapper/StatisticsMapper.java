package com.skala.teammatch.mapper;

import com.skala.teammatch.dto.TechStackDemandDto;
import com.skala.teammatch.dto.UserRankingDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 통계/랭킹 전용 SQL Mapper.
 *
 * CRUD 는 JPA 로, 여러 테이블을 조인/집계하는 통계 조회는 MyBatis 로 처리한다.
 * 실제 SQL 은 resources/mapper/StatisticsMapper.xml 에 있다.
 */
@Mapper
public interface StatisticsMapper {

    /** 가장 수요가 많은 기술 스택 TOP N (프로젝트에 붙은 태그 집계) */
    List<TechStackDemandDto> findTopDemandedTechStacks(@Param("limit") int limit);

    /** 최다 프로젝트 완료 유저 랭킹 TOP N */
    List<UserRankingDto> findTopCompletedUsers(@Param("limit") int limit);
}
