package com.cmj.risk.service;

import com.cmj.risk.common.PageResult;
import com.cmj.risk.vo.system.LogVO;

public interface LogService {
    PageResult<LogVO> pageLogs(String moduleName, String operationType, String operator, String startTime, String endTime, int pageNum, int pageSize);
    
    void createLog(String moduleName, String operationType, String operationDesc, String operator, Long operatorId);
}
