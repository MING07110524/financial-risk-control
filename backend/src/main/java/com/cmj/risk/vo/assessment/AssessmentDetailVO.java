package com.cmj.risk.vo.assessment;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentDetailVO {
    private Long id;
    private Long riskDataId;
    private String businessNo;
    private String customerName;
    private String businessType;
    private String riskDesc;
    private BigDecimal totalScore;
    private String riskLevel;
    private Integer assessmentStatus;
    private String assessmentTime;
    private String assessmentByName;
    private Integer dataStatus;
    private Boolean warningGenerated;
    private List<AssessmentIndexResultVO> indexResults;
    private WarningSimpleVO warningInfo;
}
