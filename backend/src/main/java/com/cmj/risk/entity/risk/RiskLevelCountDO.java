package com.cmj.risk.entity.risk;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskLevelCountDO {
    private String riskLevel;
    private Integer count;
}
