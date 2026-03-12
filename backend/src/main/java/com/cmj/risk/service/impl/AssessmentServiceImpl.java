package com.cmj.risk.service.impl;

import com.cmj.risk.common.PageResult;
import com.cmj.risk.component.RiskWorkflowStore;
import com.cmj.risk.security.SecurityUser;
import com.cmj.risk.service.AssessmentService;
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

    public AssessmentServiceImpl(RiskWorkflowStore riskWorkflowStore) {
        this.riskWorkflowStore = riskWorkflowStore;
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
        return toDetail(riskWorkflowStore.executeAssessment(riskDataId, operator));
    }

    private AssessmentDetailVO toDetail(RiskWorkflowStore.AssessmentRecord record) {
        WarningDetailVO warningDetail = riskWorkflowStore.listWarnings("", "", null, "", "").stream()
                .filter(item -> item.getAssessmentId().equals(record.getId()))
                .findFirst()
                .map(item -> WarningDetailVO.builder()
                        .id(item.getId())
                        .assessmentId(item.getAssessmentId())
                        .riskDataId(item.getRiskDataId())
                        .warningCode(item.getWarningCode())
                        .warningLevel(item.getWarningLevel())
                        .warningContent(item.getWarningContent())
                        .businessNo(item.getBusinessNo())
                        .customerName(item.getCustomerName())
                        .warningStatus(item.getWarningStatus())
                        .createTime(item.getCreateTime())
                        .businessType(item.getBusinessType())
                        .riskDesc(item.getRiskDesc())
                        .totalScore(item.getTotalScore())
                        .riskLevel(item.getRiskLevel())
                        .build())
                .orElse(null);

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
