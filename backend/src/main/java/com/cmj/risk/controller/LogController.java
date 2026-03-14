package com.cmj.risk.controller;

import com.cmj.risk.common.PageResult;
import com.cmj.risk.common.Result;
import com.cmj.risk.service.LogService;
import com.cmj.risk.vo.system.LogVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class LogController {
    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @GetMapping("/logs")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<LogVO>> pageLogs(
            @RequestParam(required = false) String moduleName,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String operator,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return Result.success(logService.pageLogs(moduleName, operationType, operator, startTime, endTime, pageNum, pageSize));
    }
}
