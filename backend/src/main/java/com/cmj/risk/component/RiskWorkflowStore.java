package com.cmj.risk.component;

import com.cmj.risk.common.ErrorCode;
import com.cmj.risk.entity.risk.RiskAssessmentIndexResultDO;
import com.cmj.risk.exception.BusinessException;
import com.cmj.risk.mapper.risk.RiskAssessmentIndexResultMapper;
import com.cmj.risk.mapper.risk.RiskAssessmentMapper;
import com.cmj.risk.mapper.risk.RiskWarningMapper;
import com.cmj.risk.mapper.risk.WarningHandleRecordMapper;
import com.cmj.risk.security.SecurityUser;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RiskWorkflowStore {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RiskDemoStore riskDemoStore;
    private final RiskAssessmentMapper riskAssessmentMapper;
    private final RiskAssessmentIndexResultMapper riskAssessmentIndexResultMapper;
    private final RiskWarningMapper riskWarningMapper;
    private final WarningHandleRecordMapper warningHandleRecordMapper;

    public RiskWorkflowStore(
            RiskDemoStore riskDemoStore,
            RiskAssessmentMapper riskAssessmentMapper,
            RiskAssessmentIndexResultMapper riskAssessmentIndexResultMapper,
            RiskWarningMapper riskWarningMapper,
            WarningHandleRecordMapper warningHandleRecordMapper
    ) {
        this.riskDemoStore = riskDemoStore;
        this.riskAssessmentMapper = riskAssessmentMapper;
        this.riskAssessmentIndexResultMapper = riskAssessmentIndexResultMapper;
        this.riskWarningMapper = riskWarningMapper;
        this.warningHandleRecordMapper = warningHandleRecordMapper;
    }

    public List<AssessmentRecord> listAssessments(
            String businessNo,
            String riskLevel,
            Integer assessmentStatus,
            String startTime,
            String endTime,
            Long riskDataId
    ) {
        return riskAssessmentMapper.listAssessments(normalize(businessNo), riskLevel, assessmentStatus, startTime, endTime, riskDataId)
                .stream()
                .map(this::copyAssessmentRecordWithoutIndexResults)
                .toList();
    }

    public AssessmentRecord getAssessment(Long assessmentId) {
        AssessmentRecord assessmentRecord = riskAssessmentMapper.findById(assessmentId);
        if (assessmentRecord == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "未找到对应的评估记录");
        }
        AssessmentRecord copied = copyAssessmentRecordWithoutIndexResults(assessmentRecord);
        copied.setIndexResults(copyIndexResults(riskAssessmentIndexResultMapper.listByAssessmentId(assessmentId)));
        return copied;
    }

    @Transactional
    public AssessmentRecord executeAssessment(Long riskDataId, SecurityUser operator) {
        RiskDemoStore.RiskDataRecord riskDataRecord = riskDemoStore.getRiskData(riskDataId);
        List<AssessmentIndexResultRecord> indexResults = buildAssessmentIndexResults(riskDataRecord, riskDemoStore.listEnabledRiskIndexes());

        riskAssessmentMapper.invalidateEffectiveByRiskDataId(riskDataId);

        String now = now();
        BigDecimal totalScore = indexResults.stream()
                .map(AssessmentIndexResultRecord::getWeightedScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, BigDecimal.ROUND_HALF_UP);
        String riskLevel = resolveRiskLevel(totalScore);

        AssessmentRecord assessmentRecord = AssessmentRecord.builder()
                .riskDataId(riskDataId)
                .businessNo(riskDataRecord.getBusinessNo())
                .customerName(riskDataRecord.getCustomerName())
                .businessType(riskDataRecord.getBusinessType())
                .riskDesc(riskDataRecord.getRiskDesc())
                .totalScore(totalScore)
                .riskLevel(riskLevel)
                .assessmentStatus(1)
                .assessmentTime(now)
                .assessmentBy(operator.getUserId())
                .assessmentByName(operator.getRealName())
                .dataStatus(1)
                .build();
        riskAssessmentMapper.insert(assessmentRecord);

        if (!indexResults.isEmpty()) {
            riskAssessmentIndexResultMapper.insertBatch(assessmentRecord.getId(), toIndexResultDOs(indexResults));
        }

        if (!Objects.equals(riskLevel, "LOW")) {
            riskWarningMapper.insert(WarningRecord.builder()
                    .assessmentId(assessmentRecord.getId())
                    .warningCode(createWarningCode(now, assessmentRecord.getId()))
                    .warningLevel(riskLevel)
                    .warningContent(buildWarningContent(riskLevel, riskDataRecord.getCustomerName(), riskDataRecord.getBusinessNo()))
                    .warningStatus(0)
                    .createTime(now)
                    .build());
        }

        riskDemoStore.setRiskDataStatus(riskDataId, 1);
        return getAssessment(assessmentRecord.getId());
    }

    public List<WarningRecord> listWarnings(
            String warningCode,
            String warningLevel,
            Integer warningStatus,
            String startTime,
            String endTime
    ) {
        return riskWarningMapper.listWarnings(normalize(warningCode), warningLevel, warningStatus, startTime, endTime)
                .stream()
                .map(this::copyWarningRecordWithoutHandleRecords)
                .toList();
    }

    public WarningRecord getWarning(Long warningId) {
        WarningRecord warningRecord = riskWarningMapper.findById(warningId);
        if (warningRecord == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "未找到对应的预警记录");
        }
        WarningRecord copied = copyWarningRecordWithoutHandleRecords(warningRecord);
        copied.setHandleRecords(copyHandleRecords(warningHandleRecordMapper.listByWarningId(warningId)));
        return copied;
    }

    public WarningRecord findWarningByAssessmentId(Long assessmentId) {
        WarningRecord warningRecord = riskWarningMapper.findByAssessmentId(assessmentId);
        return warningRecord == null ? null : copyWarningRecordWithoutHandleRecords(warningRecord);
    }

    public List<WarningHandleRecord> listWarningRecords(Long warningId) {
        return copyHandleRecords(warningHandleRecordMapper.listByWarningId(warningId));
    }

    @Transactional
    public void handleWarning(Long warningId, String handleOpinion, String handleResult, Integer nextStatus, SecurityUser operator) {
        WarningRecord warningRecord = riskWarningMapper.findById(warningId);
        if (warningRecord == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "未找到对应的预警记录");
        }
        if (Objects.equals(warningRecord.getWarningStatus(), 2)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "该预警已处理完成，不能重复提交");
        }
        if (!Objects.equals(nextStatus, 1) && !Objects.equals(nextStatus, 2)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "下一状态只允许为处理中或已处理");
        }

        warningHandleRecordMapper.insert(WarningHandleRecord.builder()
                .warningId(warningId)
                .handleUserId(operator.getUserId())
                .handleUserName(operator.getRealName())
                .handleOpinion(handleOpinion)
                .handleResult(handleResult)
                .nextStatus(nextStatus)
                .handleTime(now())
                .build());
        riskWarningMapper.updateStatus(warningId, nextStatus);
    }

    public List<WarningRecord> listRecentWarnings(int limit) {
        return riskWarningMapper.listRecentWarnings(limit).stream()
                .map(this::copyWarningRecordWithoutHandleRecords)
                .toList();
    }

    public boolean hasRiskDataHistory(Long riskDataId) {
        return riskAssessmentMapper.countByRiskDataId(riskDataId) > 0 || riskWarningMapper.countByRiskDataId(riskDataId) > 0;
    }

    public boolean hasEffectiveAssessment(Long riskDataId) {
        return riskAssessmentMapper.findEffectiveByRiskDataId(riskDataId) != null;
    }

    @Transactional
    public void invalidateEffectiveAssessments(Long riskDataId) {
        riskAssessmentMapper.invalidateEffectiveByRiskDataId(riskDataId);
    }

    @Transactional
    public void invalidateEffectiveAssessments(List<Long> riskDataIds) {
        for (Long riskDataId : riskDataIds) {
            invalidateEffectiveAssessments(riskDataId);
        }
    }

    private List<AssessmentIndexResultRecord> buildAssessmentIndexResults(
            RiskDemoStore.RiskDataRecord riskDataRecord,
            List<RiskDemoStore.RiskIndexRecord> enabledIndexes
    ) {
        List<AssessmentIndexResultRecord> results = new ArrayList<>();
        for (RiskDemoStore.RiskIndexRecord riskIndexRecord : enabledIndexes) {
            RiskDemoStore.RiskDataIndexValueRecord indexValueRecord = riskDataRecord.getIndexValues().stream()
                    .filter(item -> item.getIndexId().equals(riskIndexRecord.getId()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(ErrorCode.INDEX_VALUE_INCOMPLETE, "该业务数据缺少启用指标值，无法执行评估"));
            RiskDemoStore.RiskRuleRecord matchedRule = riskDemoStore.listRiskRules(riskIndexRecord.getId()).stream()
                    .filter(rule -> indexValueRecord.getIndexValue().compareTo(rule.getScoreMin()) >= 0
                            && indexValueRecord.getIndexValue().compareTo(rule.getScoreMax()) <= 0)
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "指标 " + riskIndexRecord.getIndexName() + " 缺少可用评分规则"));
            BigDecimal weightedScore = matchedRule.getScoreValue()
                    .multiply(riskIndexRecord.getWeightValue())
                    .divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
            results.add(AssessmentIndexResultRecord.builder()
                    .indexId(riskIndexRecord.getId())
                    .indexCode(riskIndexRecord.getIndexCode())
                    .indexName(riskIndexRecord.getIndexName())
                    .indexValue(indexValueRecord.getIndexValue())
                    .weightValue(riskIndexRecord.getWeightValue())
                    .scoreValue(matchedRule.getScoreValue())
                    .weightedScore(weightedScore)
                    .warningLevel(matchedRule.getWarningLevel())
                    .build());
        }
        return results;
    }

    private List<RiskAssessmentIndexResultDO> toIndexResultDOs(List<AssessmentIndexResultRecord> records) {
        return records.stream()
                .map(item -> RiskAssessmentIndexResultDO.builder()
                        .indexId(item.getIndexId())
                        .indexCode(item.getIndexCode())
                        .indexName(item.getIndexName())
                        .indexValue(item.getIndexValue())
                        .weightValue(item.getWeightValue())
                        .scoreValue(item.getScoreValue())
                        .weightedScore(item.getWeightedScore())
                        .warningLevel(item.getWarningLevel())
                        .build())
                .toList();
    }

    private String resolveRiskLevel(BigDecimal totalScore) {
        if (totalScore.compareTo(new BigDecimal("80")) >= 0) {
            return "HIGH";
        }
        if (totalScore.compareTo(new BigDecimal("60")) >= 0) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private String createWarningCode(String now, Long assessmentId) {
        return "WARN-" + now.substring(0, 10).replace("-", "") + "-" + String.format("%03d", assessmentId);
    }

    private String buildWarningContent(String riskLevel, String customerName, String businessNo) {
        return customerName + " 的业务 " + businessNo + " 评估结果为" + (Objects.equals(riskLevel, "HIGH") ? "高风险" : "中风险") + "，请尽快跟进。";
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private String now() {
        return LocalDateTime.now().format(DATE_TIME_FORMATTER);
    }

    private AssessmentRecord copyAssessmentRecordWithoutIndexResults(AssessmentRecord source) {
        return AssessmentRecord.builder()
                .id(source.getId())
                .riskDataId(source.getRiskDataId())
                .businessNo(source.getBusinessNo())
                .customerName(source.getCustomerName())
                .businessType(source.getBusinessType())
                .riskDesc(source.getRiskDesc())
                .totalScore(source.getTotalScore())
                .riskLevel(source.getRiskLevel())
                .assessmentStatus(source.getAssessmentStatus())
                .assessmentTime(source.getAssessmentTime())
                .assessmentBy(source.getAssessmentBy())
                .assessmentByName(source.getAssessmentByName())
                .dataStatus(source.getDataStatus())
                .indexResults(new ArrayList<>())
                .build();
    }

    private WarningRecord copyWarningRecordWithoutHandleRecords(WarningRecord source) {
        return WarningRecord.builder()
                .id(source.getId())
                .assessmentId(source.getAssessmentId())
                .riskDataId(source.getRiskDataId())
                .warningCode(source.getWarningCode())
                .warningLevel(source.getWarningLevel())
                .warningContent(source.getWarningContent())
                .businessNo(source.getBusinessNo())
                .customerName(source.getCustomerName())
                .businessType(source.getBusinessType())
                .riskDesc(source.getRiskDesc())
                .totalScore(source.getTotalScore())
                .riskLevel(source.getRiskLevel())
                .warningStatus(source.getWarningStatus())
                .createTime(source.getCreateTime())
                .handleRecords(new ArrayList<>())
                .build();
    }

    private List<AssessmentIndexResultRecord> copyIndexResults(List<AssessmentIndexResultRecord> source) {
        if (source == null) {
            return new ArrayList<>();
        }
        return source.stream()
                .map(item -> AssessmentIndexResultRecord.builder()
                        .indexId(item.getIndexId())
                        .indexCode(item.getIndexCode())
                        .indexName(item.getIndexName())
                        .indexValue(item.getIndexValue())
                        .weightValue(item.getWeightValue())
                        .scoreValue(item.getScoreValue())
                        .weightedScore(item.getWeightedScore())
                        .warningLevel(item.getWarningLevel())
                        .build())
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    private List<WarningHandleRecord> copyHandleRecords(List<WarningHandleRecord> source) {
        if (source == null) {
            return new ArrayList<>();
        }
        return source.stream()
                .map(item -> WarningHandleRecord.builder()
                        .id(item.getId())
                        .warningId(item.getWarningId())
                        .handleUserId(item.getHandleUserId())
                        .handleUserName(item.getHandleUserName())
                        .handleOpinion(item.getHandleOpinion())
                        .handleResult(item.getHandleResult())
                        .nextStatus(item.getNextStatus())
                        .handleTime(item.getHandleTime())
                        .build())
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssessmentRecord {
        private Long id;
        private Long riskDataId;
        private String businessNo;
        private String customerName;
        private String businessType;
        private String riskDesc;
        private BigDecimal totalScore;
        private String riskLevel;
        private Integer assessmentStatus;
        private String assessmentTime;
        private Long assessmentBy;
        private String assessmentByName;
        private Integer dataStatus;
        private List<AssessmentIndexResultRecord> indexResults;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssessmentIndexResultRecord {
        private Long indexId;
        private String indexCode;
        private String indexName;
        private BigDecimal indexValue;
        private BigDecimal weightValue;
        private BigDecimal scoreValue;
        private BigDecimal weightedScore;
        private String warningLevel;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WarningRecord {
        private Long id;
        private Long assessmentId;
        private Long riskDataId;
        private String warningCode;
        private String warningLevel;
        private String warningContent;
        private String businessNo;
        private String customerName;
        private String businessType;
        private String riskDesc;
        private BigDecimal totalScore;
        private String riskLevel;
        private Integer warningStatus;
        private String createTime;
        private List<WarningHandleRecord> handleRecords;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WarningHandleRecord {
        private Long id;
        private Long warningId;
        private Long handleUserId;
        private String handleUserName;
        private String handleOpinion;
        private String handleResult;
        private Integer nextStatus;
        private String handleTime;
    }
}
