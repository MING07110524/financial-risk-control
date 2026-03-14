package com.cmj.risk.entity.system;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class SystemLogDO {
    private Long id;
    private Long userId;
    private String operator;
    private String moduleName;
    private String operationType;
    private String operationDesc;
    private LocalDateTime operationTime;
}
