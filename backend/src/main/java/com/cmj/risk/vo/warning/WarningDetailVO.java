package com.cmj.risk.vo.warning;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarningDetailVO {
    private Long id;
    private Long assessmentId;
    private Long riskDataId;
    private String warningCode;
    private String warningLevel;
    private String warningContent;
    private String businessNo;
    private String customerName;
    private Integer warningStatus;
    private String createTime;
    private String businessType;
    private String riskDesc;
    private BigDecimal totalScore;
    private String riskLevel;
}
