package com.skala.teammatch.service;

import com.skala.teammatch.dto.TechStackDemandDto;
import com.skala.teammatch.dto.UserRankingDto;
import com.skala.teammatch.mapper.StatisticsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsService {

    private static final int TOP_N = 10;

    private final StatisticsMapper statisticsMapper;

    public List<TechStackDemandDto> getTopDemandedTechStacks() {
        return statisticsMapper.findTopDemandedTechStacks(TOP_N);
    }

    public List<UserRankingDto> getTopCompletedUsers() {
        return statisticsMapper.findTopCompletedUsers(TOP_N);
    }
}
