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
public class AssessmentVO {
    private Long id;
    private Long riskDataId;
    private String businessNo;
    private String customerName;
    private BigDecimal totalScore;
    private String riskLevel;
    private Integer assessmentStatus;
    private String assessmentTime;
    private String assessmentByName;
    private Boolean warningGenerated;
}
