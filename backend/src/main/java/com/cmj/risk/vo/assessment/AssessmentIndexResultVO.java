package com.cmj.risk.vo.assessment;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentIndexResultVO {
    private Long indexId;
    private String indexCode;
    private String indexName;
    private BigDecimal indexValue;
    private BigDecimal weightValue;
    private BigDecimal scoreValue;
    private BigDecimal weightedScore;
    private String warningLevel;
}
