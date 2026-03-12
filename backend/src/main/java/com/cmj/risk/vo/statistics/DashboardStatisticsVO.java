package com.cmj.risk.vo.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatisticsVO {
    private Integer riskDataCount;
    private Integer assessmentCount;
    private Integer warningCount;
    private Integer handledWarningCount;
    private Integer highRiskCount;
}
