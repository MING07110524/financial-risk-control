package com.cmj.risk.vo.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarningTrendStatisticsVO {
    private String date;
    private Integer total;
    private Integer pending;
    private Integer handled;
}
