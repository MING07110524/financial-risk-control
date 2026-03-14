package com.cmj.risk.dto.system;

import lombok.Data;

@Data
public class LogQueryDTO {
    private String moduleName;
    private String operationType;
    private String operator;
    private String startTime;
    private String endTime;
    private int pageNum = 1;
    private int pageSize = 10;
}
