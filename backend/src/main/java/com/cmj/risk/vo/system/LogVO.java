package com.cmj.risk.vo.system;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogVO {
    private Long id;
    private String moduleName;
    private String operationType;
    private String operationDesc;
    private String operator;
    private Long operatorId;
    private String operationTime;
}
