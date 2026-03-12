package com.cmj.risk.vo.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HandleSummaryStatisticsVO {
    private Integer warningStatus;
    private String label;
    private Integer count;
}
