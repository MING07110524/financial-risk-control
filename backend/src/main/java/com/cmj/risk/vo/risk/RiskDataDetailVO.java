package com.cmj.risk.vo.risk;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskDataDetailVO {
    private Long id;
    private String businessNo;
    private String customerName;
    private String businessType;
    private String riskDesc;
    private Integer dataStatus;
    private Long createBy;
    private String createByName;
    private String createTime;
    private String updateTime;
    private List<RiskDataIndexValueVO> indexValues;
    private List<String> missingEnabledIndexNames;
}
