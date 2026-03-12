package com.cmj.risk.controller;

import com.cmj.risk.common.Result;
import com.cmj.risk.service.StatisticsService;
import com.cmj.risk.service.WarningService;
import com.cmj.risk.vo.statistics.DashboardStatisticsVO;
import com.cmj.risk.vo.statistics.HandleSummaryStatisticsVO;
import com.cmj.risk.vo.statistics.RiskLevelStatisticsVO;
import com.cmj.risk.vo.statistics.WarningTrendStatisticsVO;
import com.cmj.risk.vo.warning.WarningVO;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {
    private final StatisticsService statisticsService;
    private final WarningService warningService;

    public StatisticsController(StatisticsService statisticsService, WarningService warningService) {
        this.statisticsService = statisticsService;
        this.warningService = warningService;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'RISK_USER', 'MANAGER')")
    public Result<DashboardStatisticsVO> getDashboardStatistics() {
        return Result.success(statisticsService.getDashboardStatistics());
    }

    @GetMapping("/recent-warnings")
    @PreAuthorize("hasAnyRole('ADMIN', 'RISK_USER', 'MANAGER')")
    public Result<List<WarningVO>> listRecentWarnings(@RequestParam(defaultValue = "5") int limit) {
        return Result.success(warningService.listRecentWarnings(limit));
    }

    @GetMapping("/risk-level")
    @PreAuthorize("hasAnyRole('RISK_USER', 'MANAGER')")
    public Result<List<RiskLevelStatisticsVO>> getRiskLevelStatistics(
            @RequestParam(defaultValue = "") String startTime,
            @RequestParam(defaultValue = "") String endTime,
            @RequestParam(defaultValue = "") String riskLevel,
            @RequestParam(required = false) Integer warningStatus
    ) {
        return Result.success(statisticsService.getRiskLevelStatistics(startTime, endTime, riskLevel, warningStatus));
    }

    @GetMapping("/warning-trend")
    @PreAuthorize("hasAnyRole('RISK_USER', 'MANAGER')")
    public Result<List<WarningTrendStatisticsVO>> getWarningTrendStatistics(
            @RequestParam(defaultValue = "") String startTime,
            @RequestParam(defaultValue = "") String endTime,
            @RequestParam(defaultValue = "") String riskLevel,
            @RequestParam(required = false) Integer warningStatus
    ) {
        return Result.success(statisticsService.getWarningTrendStatistics(startTime, endTime, riskLevel, warningStatus));
    }

    @GetMapping("/handle-summary")
    @PreAuthorize("hasAnyRole('RISK_USER', 'MANAGER')")
    public Result<List<HandleSummaryStatisticsVO>> getHandleSummaryStatistics(
            @RequestParam(defaultValue = "") String startTime,
            @RequestParam(defaultValue = "") String endTime,
            @RequestParam(defaultValue = "") String riskLevel,
            @RequestParam(required = false) Integer warningStatus
    ) {
        return Result.success(statisticsService.getHandleSummaryStatistics(startTime, endTime, riskLevel, warningStatus));
    }
}
