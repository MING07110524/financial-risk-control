package com.cmj.risk.service;

import com.cmj.risk.vo.statistics.DashboardStatisticsVO;
import com.cmj.risk.vo.statistics.HandleSummaryStatisticsVO;
import com.cmj.risk.vo.statistics.RiskLevelStatisticsVO;
import com.cmj.risk.vo.statistics.WarningTrendStatisticsVO;
import java.util.List;

public interface StatisticsService {
    DashboardStatisticsVO getDashboardStatistics();

    List<RiskLevelStatisticsVO> getRiskLevelStatistics(String startTime, String endTime, String riskLevel, Integer warningStatus);

    List<WarningTrendStatisticsVO> getWarningTrendStatistics(String startTime, String endTime, String riskLevel, Integer warningStatus);

    List<HandleSummaryStatisticsVO> getHandleSummaryStatistics(String startTime, String endTime, String riskLevel, Integer warningStatus);
}
