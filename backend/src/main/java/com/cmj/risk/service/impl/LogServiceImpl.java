package com.cmj.risk.service.impl;

import com.cmj.risk.common.PageResult;
import com.cmj.risk.entity.system.SystemLogDO;
import com.cmj.risk.mapper.system.SystemLogMapper;
import com.cmj.risk.service.LogService;
import com.cmj.risk.vo.system.LogVO;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class LogServiceImpl implements LogService {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final SystemLogMapper systemLogMapper;

    public LogServiceImpl(SystemLogMapper systemLogMapper) {
        this.systemLogMapper = systemLogMapper;
    }

    @Override
    public PageResult<LogVO> pageLogs(
            String moduleName,
            String operationType,
            String operator,
            String startTime,
            String endTime,
            int pageNum,
            int pageSize
    ) {
        int safePageNum = Math.max(pageNum, 1);
        int safePageSize = Math.max(pageSize, 1);
        String normalizedModuleName = normalizeFilter(moduleName);
        String normalizedOperationType = normalizeFilter(operationType);
        String normalizedOperator = normalizeFilter(operator);
        String normalizedStartTime = normalizeFilter(startTime);
        String normalizedEndTime = normalizeFilter(endTime);

        long total = systemLogMapper.countLogs(
                normalizedModuleName,
                normalizedOperationType,
                normalizedOperator,
                normalizedStartTime,
                normalizedEndTime);
        if (total == 0L) {
            return PageResult.<LogVO>empty();
        }

        int offset = (safePageNum - 1) * safePageSize;
        List<LogVO> records = systemLogMapper.listLogs(
                        normalizedModuleName,
                        normalizedOperationType,
                        normalizedOperator,
                        normalizedStartTime,
                        normalizedEndTime,
                        offset,
                        safePageSize)
                .stream()
                .map(this::toLogVO)
                .toList();

        return new PageResult<LogVO>(total, records);
    }

    @Override
    public void createLog(String moduleName, String operationType, String operationDesc, String operator, Long operatorId) {
        SystemLogDO log = new SystemLogDO();
        log.setUserId(operatorId);
        log.setOperator(normalizeNullableValue(operator));
        log.setModuleName(moduleName);
        log.setOperationType(operationType);
        log.setOperationDesc(operationDesc);
        systemLogMapper.insertLog(log);
    }

    private LogVO toLogVO(SystemLogDO item) {
        return LogVO.builder()
                .id(item.getId())
                .moduleName(item.getModuleName())
                .operationType(item.getOperationType())
                .operationDesc(item.getOperationDesc())
                .operator(item.getOperator())
                .operatorId(item.getUserId())
                .operationTime(formatTime(item.getOperationTime()))
                .build();
    }

    private String normalizeFilter(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private String normalizeNullableValue(String value) {
        return normalizeFilter(value);
    }

    private String formatTime(LocalDateTime value) {
        return value == null ? null : value.format(DATE_TIME_FORMATTER);
    }
}
