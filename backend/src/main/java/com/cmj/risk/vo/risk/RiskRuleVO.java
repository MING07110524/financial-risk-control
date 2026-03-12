package com.cmj.risk.vo.risk;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskRuleVO {
    private Long id;
    private Long indexId;
    private String indexName;
    private BigDecimal scoreMin;
    private BigDecimal scoreMax;
    private BigDecimal scoreValue;
    private String warningLevel;
}
