package com.cmj.risk.vo.assessment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarningSimpleVO {
    private Long warningId;
    private String warningCode;
    private String warningLevel;
    private Integer warningStatus;
    private String warningContent;
}
