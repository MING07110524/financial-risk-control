package com.cmj.risk.vo.warning;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarningVO {
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
}
