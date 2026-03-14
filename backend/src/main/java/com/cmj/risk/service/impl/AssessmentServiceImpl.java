package com.cmj.risk.service.impl;

import com.cmj.risk.common.PageResult;
import com.cmj.risk.component.RiskWorkflowStore;
import com.cmj.risk.security.SecurityUser;
import com.cmj.risk.service.AssessmentService;
import com.cmj.risk.service.LogService;
import com.cmj.risk.vo.assessment.AssessmentDetailVO;
import com.cmj.risk.vo.assessment.AssessmentIndexResultVO;
import com.cmj.risk.vo.assessment.AssessmentVO;
import com.cmj.risk.vo.assessment.WarningSimpleVO;
import com.cmj.risk.vo.warning.WarningDetailVO;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AssessmentServiceImpl implements AssessmentService {
    private final RiskWorkflowStore riskWorkflowStore;
    private final LogService logService;

    public AssessmentServiceImpl(RiskWorkflowStore riskWorkflowStore, LogService logService) {
        this.riskWorkflowStore = riskWorkflowStore;
        this.logService = logService;
    }

    @Override
    public PageResult<AssessmentVO> pageAssessments(
            String businessNo,
            String riskLevel,
            Integer assessmentStatus,
            String startTime,
            String endTime,
            Long riskDataId,
            int pageNum,
            int pageSize
    ) {
        List<AssessmentVO> records = riskWorkflowStore.listAssessments(businessNo, riskLevel, assessmentStatus, startTime, endTime, riskDataId)
                .stream()
                .map(item -> AssessmentVO.builder()
                        .id(item.getId())
                        .riskDataId(item.getRiskDataId())
                        .businessNo(item.getBusinessNo())
                        .customerName(item.getCustomerName())
                        .totalScore(item.getTotalScore())
                        .riskLevel(item.getRiskLevel())
                        .assessmentStatus(item.getAssessmentStatus())
                        .assessmentTime(item.getAssessmentTime())
                        .assessmentByName(item.getAssessmentByName())
                        .warningGenerated(riskWorkflowStore.listWarnings("", "", null, "", "").stream().anyMatch(warning -> warning.getAssessmentId().equals(item.getId())))
                        .build())
                .toList();
        int start = Math.max(pageNum - 1, 0) * pageSize;
        int end = Math.min(start + pageSize, records.size());
        List<AssessmentVO> pageRecords = start >= records.size() ? List.of() : records.subList(start, end);
        return new PageResult<>((long) records.size(), pageRecords);
    }

    @Override
    public AssessmentDetailVO getAssessmentDetail(Long id) {
        return toDetail(riskWorkflowStore.getAssessment(id));
    }

    @Override
    public AssessmentDetailVO executeAssessment(Long riskDataId, SecurityUser operator) {
        RiskWorkflowStore.AssessmentRecord result = riskWorkflowStore.executeAssessment(riskDataId, operator);
        logService.createLog("风险评估", "执行", "执行评估，风险等级：" + result.getRiskLevel() + "，风险得分：" + result.getTotalScore(), operator.getUsername(), operator.getUserId());
        return toDetail(result);
    }

    private AssessmentDetailVO toDetail(RiskWorkflowStore.AssessmentRecord record) {
        RiskWorkflowStore.WarningRecord warningRecord = riskWorkflowStore.findWarningByAssessmentId(record.getId());
        WarningDetailVO warningDetail = warningRecord == null ? null : WarningDetailVO.builder()
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

        return AssessmentDetailVO.builder()
                .id(record.getId())
                .riskDataId(record.getRiskDataId())
                .businessNo(record.getBusinessNo())
                .customerName(record.getCustomerName())
                .businessType(record.getBusinessType())
                .riskDesc(record.getRiskDesc())
                .totalScore(record.getTotalScore())
                .riskLevel(record.getRiskLevel())
                .assessmentStatus(record.getAssessmentStatus())
                .assessmentTime(record.getAssessmentTime())
                .assessmentByName(record.getAssessmentByName())
                .dataStatus(record.getDataStatus())
                .warningGenerated(warningDetail != null)
                .indexResults(record.getIndexResults().stream()
                        .map(item -> AssessmentIndexResultVO.builder()
                                .indexId(item.getIndexId())
                                .indexCode(item.getIndexCode())
                                .indexName(item.getIndexName())
                                .indexValue(item.getIndexValue())
                                .weightValue(item.getWeightValue())
                                .scoreValue(item.getScoreValue())
                                .weightedScore(item.getWeightedScore())
                                .warningLevel(item.getWarningLevel())
                                .build())
                        .toList())
                .warningInfo(warningDetail == null ? null : WarningSimpleVO.builder()
                        .warningId(warningDetail.getId())
                        .warningCode(warningDetail.getWarningCode())
                        .warningLevel(warningDetail.getWarningLevel())
                        .warningStatus(warningDetail.getWarningStatus())
                        .warningContent(warningDetail.getWarningContent())
                        .build())
                .build();
    }
}
