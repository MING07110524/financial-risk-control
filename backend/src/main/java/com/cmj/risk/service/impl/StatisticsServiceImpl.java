package com.cmj.risk.service.impl;

import com.cmj.risk.entity.risk.HandleSummaryCountDO;
import com.cmj.risk.entity.risk.RiskLevelCountDO;
import com.cmj.risk.entity.risk.WarningTrendCountDO;
import com.cmj.risk.mapper.risk.RiskAssessmentMapper;
import com.cmj.risk.mapper.risk.RiskDataMapper;
import com.cmj.risk.mapper.risk.RiskWarningMapper;
import com.cmj.risk.service.StatisticsService;
import com.cmj.risk.vo.statistics.DashboardStatisticsVO;
import com.cmj.risk.vo.statistics.HandleSummaryStatisticsVO;
import com.cmj.risk.vo.statistics.RiskLevelStatisticsVO;
import com.cmj.risk.vo.statistics.WarningTrendStatisticsVO;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class StatisticsServiceImpl implements StatisticsService {
    private final RiskDataMapper riskDataMapper;
    private final RiskAssessmentMapper riskAssessmentMapper;
    private final RiskWarningMapper riskWarningMapper;

    public StatisticsServiceImpl(
            RiskDataMapper riskDataMapper,
            RiskAssessmentMapper riskAssessmentMapper,
            RiskWarningMapper riskWarningMapper
    ) {
        this.riskDataMapper = riskDataMapper;
        this.riskAssessmentMapper = riskAssessmentMapper;
        this.riskWarningMapper = riskWarningMapper;
    }

    @Override
    public DashboardStatisticsVO getDashboardStatistics() {
        return DashboardStatisticsVO.builder()
                .riskDataCount((int) riskDataMapper.countAll())
                .assessmentCount((int) riskAssessmentMapper.countAll())
                .warningCount((int) riskWarningMapper.countAll())
                .handledWarningCount((int) riskWarningMapper.countHandled())
                .highRiskCount((int) riskAssessmentMapper.countEffectiveHighRisk())
                .build();
    }

    @Override
    public List<RiskLevelStatisticsVO> getRiskLevelStatistics(String startTime, String endTime, String riskLevel, Integer warningStatus) {
        Map<String, Integer> counts = riskAssessmentMapper.statRiskLevels(startTime, endTime, riskLevel).stream()
                .collect(Collectors.toMap(RiskLevelCountDO::getRiskLevel, item -> defaultInt(item.getCount())));
        return List.of(
                RiskLevelStatisticsVO.builder().riskLevel("LOW").count(counts.getOrDefault("LOW", 0)).build(),
                RiskLevelStatisticsVO.builder().riskLevel("MEDIUM").count(counts.getOrDefault("MEDIUM", 0)).build(),
                RiskLevelStatisticsVO.builder().riskLevel("HIGH").count(counts.getOrDefault("HIGH", 0)).build()
        );
    }

    @Override
    public List<WarningTrendStatisticsVO> getWarningTrendStatistics(String startTime, String endTime, String riskLevel, Integer warningStatus) {
        return riskWarningMapper.statWarningTrend(startTime, endTime, riskLevel, warningStatus).stream()
                .map(item -> WarningTrendStatisticsVO.builder()
                        .date(item.getDate())
                        .total(defaultInt(item.getTotal()))
                        .pending(defaultInt(item.getPending()))
                        .handled(defaultInt(item.getHandled()))
                        .build())
                .toList();
    }

    @Override
    public List<HandleSummaryStatisticsVO> getHandleSummaryStatistics(String startTime, String endTime, String riskLevel, Integer warningStatus) {
        Map<Integer, HandleSummaryCountDO> grouped = riskWarningMapper.statHandleSummary(startTime, endTime, riskLevel, warningStatus).stream()
                .collect(Collectors.toMap(HandleSummaryCountDO::getWarningStatus, Function.identity()));
        return List.of(
                toHandleSummary(0, "待处理", grouped),
                toHandleSummary(1, "处理中", grouped),
                toHandleSummary(2, "已处理", grouped)
        );
    }

    private HandleSummaryStatisticsVO toHandleSummary(int status, String label, Map<Integer, HandleSummaryCountDO> grouped) {
        HandleSummaryCountDO item = grouped.get(status);
        return HandleSummaryStatisticsVO.builder()
                .warningStatus(status)
                .label(label)
                .count(item == null ? 0 : defaultInt(item.getCount()))
                .build();
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }
}
