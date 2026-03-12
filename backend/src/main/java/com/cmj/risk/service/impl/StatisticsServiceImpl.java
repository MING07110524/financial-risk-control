package com.cmj.risk.service.impl;

import com.cmj.risk.component.RiskWorkflowStore;
import com.cmj.risk.service.StatisticsService;
import com.cmj.risk.vo.statistics.DashboardStatisticsVO;
import com.cmj.risk.vo.statistics.HandleSummaryStatisticsVO;
import com.cmj.risk.vo.statistics.RiskLevelStatisticsVO;
import com.cmj.risk.vo.statistics.WarningTrendStatisticsVO;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class StatisticsServiceImpl implements StatisticsService {
    private final RiskWorkflowStore riskWorkflowStore;

    public StatisticsServiceImpl(RiskWorkflowStore riskWorkflowStore) {
        this.riskWorkflowStore = riskWorkflowStore;
    }

    @Override
    public DashboardStatisticsVO getDashboardStatistics() {
        RiskWorkflowStore.DashboardSummary dashboardSummary = riskWorkflowStore.getDashboardSummary();
        return DashboardStatisticsVO.builder()
                .riskDataCount(dashboardSummary.getRiskDataCount())
                .assessmentCount(dashboardSummary.getAssessmentCount())
                .warningCount(dashboardSummary.getWarningCount())
                .handledWarningCount(dashboardSummary.getHandledWarningCount())
                .highRiskCount(dashboardSummary.getHighRiskCount())
                .build();
    }

    @Override
    public List<RiskLevelStatisticsVO> getRiskLevelStatistics(String startTime, String endTime, String riskLevel, Integer warningStatus) {
        return riskWorkflowStore.getRiskLevelStats(startTime, endTime, riskLevel).stream()
                .map(item -> RiskLevelStatisticsVO.builder()
                        .riskLevel(item.getRiskLevel())
                        .count(item.getCount())
                        .build())
                .toList();
    }

    @Override
    public List<WarningTrendStatisticsVO> getWarningTrendStatistics(String startTime, String endTime, String riskLevel, Integer warningStatus) {
        return riskWorkflowStore.getWarningTrendStats(startTime, endTime, riskLevel, warningStatus).stream()
                .map(item -> WarningTrendStatisticsVO.builder()
                        .date(item.getDate())
                        .total(item.getTotal())
                        .pending(item.getPending())
                        .handled(item.getHandled())
                        .build())
                .toList();
    }

    @Override
    public List<HandleSummaryStatisticsVO> getHandleSummaryStatistics(String startTime, String endTime, String riskLevel, Integer warningStatus) {
        return riskWorkflowStore.getHandleSummaryStats(startTime, endTime, riskLevel, warningStatus).stream()
                .map(item -> HandleSummaryStatisticsVO.builder()
                        .warningStatus(item.getWarningStatus())
                        .label(item.getLabel())
                        .count(item.getCount())
                        .build())
                .toList();
    }
}
