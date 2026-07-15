package com.campus.backend.service;

import com.campus.common.vo.ClassStatsVO;
import com.campus.common.vo.RankingItemVO;
import com.campus.common.vo.ScoreDistributionVO;
import com.campus.common.vo.TrendItemVO;

import java.util.List;

public interface StatsService {
    ClassStatsVO getClassStats(Long classId, Long examId, Long courseId);
    List<ScoreDistributionVO> getScoreDistribution(Long examId, Long courseId, Long classId);
    List<RankingItemVO> getRanking(Long examId, Long courseId, Long classId);
    List<TrendItemVO> getTrend(Long classId, Long courseId);
}
