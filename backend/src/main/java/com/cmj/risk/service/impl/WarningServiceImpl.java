package com.cmj.risk.service.impl;

import com.cmj.risk.common.PageResult;
import com.cmj.risk.component.RiskWorkflowStore;
import com.cmj.risk.security.SecurityUser;
import com.cmj.risk.service.LogService;
import com.cmj.risk.service.WarningService;
import com.cmj.risk.vo.warning.WarningDetailVO;
import com.cmj.risk.vo.warning.WarningHandleRecordVO;
import com.cmj.risk.vo.warning.WarningVO;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class WarningServiceImpl implements WarningService {
    private final RiskWorkflowStore riskWorkflowStore;
    private final LogService logService;

    public WarningServiceImpl(RiskWorkflowStore riskWorkflowStore, LogService logService) {
        this.riskWorkflowStore = riskWorkflowStore;
        this.logService = logService;
    }

    @Override
    public PageResult<WarningVO> pageWarnings(
            String warningCode,
            String warningLevel,
            Integer warningStatus,
            String startTime,
            String endTime,
            int pageNum,
            int pageSize
    ) {
        List<WarningVO> records = riskWorkflowStore.listWarnings(warningCode, warningLevel, warningStatus, startTime, endTime)
                .stream()
                .map(this::toWarningVO)
                .toList();
        int start = Math.max(pageNum - 1, 0) * pageSize;
        int end = Math.min(start + pageSize, records.size());
        List<WarningVO> pageRecords = start >= records.size() ? List.of() : records.subList(start, end);
        return new PageResult<>((long) records.size(), pageRecords);
    }

    @Override
    public WarningDetailVO getWarningDetail(Long id) {
        RiskWorkflowStore.WarningRecord warningRecord = riskWorkflowStore.getWarning(id);
        return WarningDetailVO.builder()
                .id(warningRecord.getId())
                .assessmentId(warningRecord.getAssessmentId())
                .riskDataId(warningRecord.getRiskDataId())
                .warningCode(warningRecord.getWarningCode())
                .warningLevel(warningRecord.getWarningLevel())
                .warningContent(warningRecord.getWarningContent())
                .businessNo(warningRecord.getBusinessNo())
                .customerName(warningRecord.getCustomerName())
                .warningStatus(warningRecord.getWarningStatus())
                .createTime(warningRecord.getCreateTime())
                .businessType(warningRecord.getBusinessType())
                .riskDesc(warningRecord.getRiskDesc())
                .totalScore(warningRecord.getTotalScore())
                .riskLevel(warningRecord.getRiskLevel())
                .build();
    }

    @Override
    public List<WarningHandleRecordVO> listWarningRecords(Long warningId) {
        return riskWorkflowStore.listWarningRecords(warningId).stream()
                .map(item -> WarningHandleRecordVO.builder()
                        .id(item.getId())
                        .warningId(item.getWarningId())
                        .handleUserId(item.getHandleUserId())
                        .handleUserName(item.getHandleUserName())
                        .handleOpinion(item.getHandleOpinion())
                        .handleResult(item.getHandleResult())
                        .nextStatus(item.getNextStatus())
                        .handleTime(item.getHandleTime())
                        .build())
                .toList();
    }

    @Override
    public void handleWarning(Long warningId, String handleOpinion, String handleResult, Integer nextStatus, SecurityUser operator) {
        RiskWorkflowStore.WarningRecord warning = riskWorkflowStore.getWarning(warningId);
        String warningCode = warning.getWarningCode();
        riskWorkflowStore.handleWarning(warningId, handleOpinion, handleResult, nextStatus, operator);
        logService.createLog("预警", "处理", "处理预警 " + warningCode + "，处理意见：" + handleOpinion, operator.getUsername(), operator.getUserId());
    }

    @Override
    public List<WarningVO> listRecentWarnings(int limit) {
        return riskWorkflowStore.listRecentWarnings(limit).stream()
                .map(this::toWarningVO)
                .toList();
    }

    private WarningVO toWarningVO(RiskWorkflowStore.WarningRecord warningRecord) {
        return WarningVO.builder()
                .id(warningRecord.getId())
                .assessmentId(warningRecord.getAssessmentId())
                .riskDataId(warningRecord.getRiskDataId())
                .warningCode(warningRecord.getWarningCode())
                .warningLevel(warningRecord.getWarningLevel())
                .warningContent(warningRecord.getWarningContent())
                .businessNo(warningRecord.getBusinessNo())
                .customerName(warningRecord.getCustomerName())
                .warningStatus(warningRecord.getWarningStatus())
                .createTime(warningRecord.getCreateTime())
                .build();
    }
}
