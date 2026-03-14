package com.cmj.risk.entity.risk;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskAssessmentIndexResultDO {
    private Long id;
    private Long assessmentId;
    private Long indexId;
    private String indexCode;
    private String indexName;
    private BigDecimal indexValue;
    private BigDecimal weightValue;
    private BigDecimal scoreValue;
    private BigDecimal weightedScore;
    private String warningLevel;
}
