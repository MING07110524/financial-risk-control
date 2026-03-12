package com.cmj.risk.component;

import com.cmj.risk.common.ErrorCode;
import com.cmj.risk.exception.BusinessException;
import com.cmj.risk.security.SecurityUser;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
public class RiskWorkflowStore {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RiskDemoStore riskDemoStore;
    private final AtomicLong assessmentIdGenerator = new AtomicLong(1);
    private final AtomicLong warningIdGenerator = new AtomicLong(1);
    private final AtomicLong warningRecordIdGenerator = new AtomicLong(1);
    private final Map<Long, AssessmentRecord> assessments = new LinkedHashMap<>();
    private final Map<Long, WarningRecord> warnings = new LinkedHashMap<>();

    public RiskWorkflowStore(RiskDemoStore riskDemoStore) {
        this.riskDemoStore = riskDemoStore;
    }

    @PostConstruct
    public void init() {
        assessments.clear();
        warnings.clear();
        assessmentIdGenerator.set(1);
        warningIdGenerator.set(1);
        warningRecordIdGenerator.set(1);
        seedWorkflowData();
    }

    public synchronized List<AssessmentRecord> listAssessments(
            String businessNo,
            String riskLevel,
            Integer assessmentStatus,
            String startTime,
            String endTime,
            Long riskDataId
    ) {
        String businessKeyword = normalize(businessNo);
        return assessments.values().stream()
                .filter(item -> businessKeyword.isBlank() || item.getBusinessNo().toLowerCase().contains(businessKeyword))
                .filter(item -> riskLevel == null || riskLevel.isBlank() || item.getRiskLevel().equals(riskLevel))
                .filter(item -> assessmentStatus == null || item.getAssessmentStatus().equals(assessmentStatus))
                .filter(item -> riskDataId == null || item.getRiskDataId().equals(riskDataId))
                .filter(item -> inDateRange(item.getAssessmentTime(), startTime, endTime))
                .sorted(Comparator.comparing(AssessmentRecord::getAssessmentTime).reversed())
                .map(this::copyAssessmentRecord)
                .toList();
    }

    public synchronized AssessmentRecord getAssessment(Long assessmentId) {
        AssessmentRecord assessmentRecord = assessments.get(assessmentId);
        if (assessmentRecord == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "未找到对应的评估记录");
        }
        return copyAssessmentRecord(assessmentRecord);
    }

    public synchronized AssessmentRecord executeAssessment(Long riskDataId, SecurityUser operator) {
        RiskDemoStore.RiskDataRecord riskDataRecord = riskDemoStore.getRiskData(riskDataId);
        List<RiskDemoStore.RiskIndexRecord> enabledIndexes = riskDemoStore.listEnabledRiskIndexes();
        List<AssessmentIndexResultRecord> indexResults = buildAssessmentIndexResults(riskDataRecord, enabledIndexes);

        for (AssessmentRecord assessment : assessments.values()) {
            if (assessment.getRiskDataId().equals(riskDataId) && Objects.equals(assessment.getAssessmentStatus(), 1)) {
                assessment.setAssessmentStatus(0);
            }
        }

        String now = now();
        Long assessmentId = assessmentIdGenerator.getAndIncrement();
        BigDecimal totalScore = indexResults.stream()
                .map(AssessmentIndexResultRecord::getWeightedScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, BigDecimal.ROUND_HALF_UP);
        String riskLevel = resolveRiskLevel(totalScore);
        AssessmentRecord assessmentRecord = AssessmentRecord.builder()
                .id(assessmentId)
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
                .indexResults(copyIndexResults(indexResults))
                .build();
        assessments.put(assessmentId, assessmentRecord);

        if (!Objects.equals(riskLevel, "LOW")) {
            Long warningId = warningIdGenerator.getAndIncrement();
            WarningRecord warningRecord = WarningRecord.builder()
                    .id(warningId)
                    .assessmentId(assessmentId)
                    .riskDataId(riskDataId)
                    .warningCode(createWarningCode(warningId))
                    .warningLevel(riskLevel)
                    .warningContent(buildWarningContent(riskLevel, riskDataRecord.getCustomerName(), riskDataRecord.getBusinessNo()))
                    .businessNo(riskDataRecord.getBusinessNo())
                    .customerName(riskDataRecord.getCustomerName())
                    .businessType(riskDataRecord.getBusinessType())
                    .riskDesc(riskDataRecord.getRiskDesc())
                    .totalScore(totalScore)
                    .riskLevel(riskLevel)
                    .warningStatus(0)
                    .createTime(now)
                    .handleRecords(new ArrayList<>())
                    .build();
            warnings.put(warningId, warningRecord);
        }

        riskDemoStore.setRiskDataStatus(riskDataId, 1);
        return getAssessment(assessmentId);
    }

    public synchronized List<WarningRecord> listWarnings(
            String warningCode,
            String warningLevel,
            Integer warningStatus,
            String startTime,
            String endTime
    ) {
        String warningKeyword = normalize(warningCode);
        return warnings.values().stream()
                .filter(item -> warningKeyword.isBlank() || item.getWarningCode().toLowerCase().contains(warningKeyword))
                .filter(item -> warningLevel == null || warningLevel.isBlank() || item.getWarningLevel().equals(warningLevel))
                .filter(item -> warningStatus == null || item.getWarningStatus().equals(warningStatus))
                .filter(item -> inDateRange(item.getCreateTime(), startTime, endTime))
                .sorted(Comparator.comparing(WarningRecord::getCreateTime).reversed())
                .map(this::copyWarningRecord)
                .toList();
    }

    public synchronized WarningRecord getWarning(Long warningId) {
        WarningRecord warningRecord = warnings.get(warningId);
        if (warningRecord == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "未找到对应的预警记录");
        }
        return copyWarningRecord(warningRecord);
    }

    public synchronized List<WarningHandleRecord> listWarningRecords(Long warningId) {
        return getWarning(warningId).getHandleRecords().stream()
                .sorted(Comparator.comparing(WarningHandleRecord::getHandleTime).reversed())
                .toList();
    }

    public synchronized void handleWarning(Long warningId, String handleOpinion, String handleResult, Integer nextStatus, SecurityUser operator) {
        WarningRecord warningRecord = warnings.get(warningId);
        if (warningRecord == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "未找到对应的预警记录");
        }
        if (Objects.equals(warningRecord.getWarningStatus(), 2)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "该预警已处理完成，不能重复提交");
        }
        if (nextStatus == null || (!Objects.equals(nextStatus, 1) && !Objects.equals(nextStatus, 2))) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "下一状态只允许为处理中或已处理");
        }

        WarningHandleRecord record = WarningHandleRecord.builder()
                .id(warningRecordIdGenerator.getAndIncrement())
                .warningId(warningId)
                .handleUserId(operator.getUserId())
                .handleUserName(operator.getRealName())
                .handleOpinion(handleOpinion)
                .handleResult(handleResult)
                .nextStatus(nextStatus)
                .handleTime(now())
                .build();
        warningRecord.getHandleRecords().add(0, record);
        warningRecord.setWarningStatus(nextStatus);
    }

    public synchronized DashboardSummary getDashboardSummary() {
        long highRiskCount = assessments.values().stream()
                .filter(item -> Objects.equals(item.getAssessmentStatus(), 1))
                .filter(item -> Objects.equals(item.getRiskLevel(), "HIGH"))
                .count();
        return DashboardSummary.builder()
                .riskDataCount(riskDemoStore.listRiskData("", "", "", null).size())
                .assessmentCount(assessments.size())
                .warningCount(warnings.size())
                .handledWarningCount((int) warnings.values().stream().filter(item -> Objects.equals(item.getWarningStatus(), 2)).count())
                .highRiskCount((int) highRiskCount)
                .build();
    }

    public synchronized List<WarningRecord> listRecentWarnings(int limit) {
        return warnings.values().stream()
                .sorted(Comparator.comparing(WarningRecord::getCreateTime).reversed())
                .limit(limit)
                .map(this::copyWarningRecord)
                .toList();
    }

    public synchronized List<RiskLevelStat> getRiskLevelStats(String startTime, String endTime, String riskLevel) {
        List<AssessmentRecord> filtered = assessments.values().stream()
                .filter(item -> Objects.equals(item.getAssessmentStatus(), 1))
                .filter(item -> riskLevel == null || riskLevel.isBlank() || item.getRiskLevel().equals(riskLevel))
                .filter(item -> inDateRange(item.getAssessmentTime(), startTime, endTime))
                .toList();
        return List.of(
                new RiskLevelStat("LOW", (int) filtered.stream().filter(item -> item.getRiskLevel().equals("LOW")).count()),
                new RiskLevelStat("MEDIUM", (int) filtered.stream().filter(item -> item.getRiskLevel().equals("MEDIUM")).count()),
                new RiskLevelStat("HIGH", (int) filtered.stream().filter(item -> item.getRiskLevel().equals("HIGH")).count())
        );
    }

    public synchronized List<WarningTrendStat> getWarningTrendStats(String startTime, String endTime, String riskLevel, Integer warningStatus) {
        Map<String, WarningTrendStat> grouped = new LinkedHashMap<>();
        listWarnings("", riskLevel, warningStatus, startTime, endTime).forEach(item -> {
            String date = item.getCreateTime().substring(0, 10);
            WarningTrendStat current = grouped.getOrDefault(date, new WarningTrendStat(date, 0, 0, 0));
            current.total += 1;
            if (Objects.equals(item.getWarningStatus(), 2)) {
                current.handled += 1;
            } else {
                current.pending += 1;
            }
            grouped.put(date, current);
        });
        return grouped.values().stream()
                .sorted(Comparator.comparing(item -> item.date))
                .toList();
    }

    public synchronized List<HandleSummaryStat> getHandleSummaryStats(String startTime, String endTime, String riskLevel, Integer warningStatus) {
        List<WarningRecord> filtered = listWarnings("", riskLevel, warningStatus, startTime, endTime);
        return List.of(
                new HandleSummaryStat(0, "待处理", (int) filtered.stream().filter(item -> Objects.equals(item.getWarningStatus(), 0)).count()),
                new HandleSummaryStat(1, "处理中", (int) filtered.stream().filter(item -> Objects.equals(item.getWarningStatus(), 1)).count()),
                new HandleSummaryStat(2, "已处理", (int) filtered.stream().filter(item -> Objects.equals(item.getWarningStatus(), 2)).count())
        );
    }

    public synchronized boolean hasRiskDataHistory(Long riskDataId) {
        return assessments.values().stream().anyMatch(item -> item.getRiskDataId().equals(riskDataId))
                || warnings.values().stream().anyMatch(item -> item.getRiskDataId().equals(riskDataId));
    }

    private void seedWorkflowData() {
        SecurityUser riskUser = SecurityUser.builder()
                .userId(2L)
                .username("risk-demo")
                .password("")
                .realName("演示风控员")
                .roleCode("RISK_USER")
                .roleName("风控人员")
                .enabled(true)
                .build();

        seedAssessment(2L, "2026-03-09 11:35:00", riskUser, 1, false);
        AssessmentRecord mediumAssessment = seedAssessment(3L, "2026-03-08 15:45:00", riskUser, 1, true);
        AssessmentRecord highAssessment = seedAssessment(4L, "2026-03-07 16:30:00", riskUser, 1, true);
        seedHistoricalAssessmentForRiskDataFive(riskUser);

        WarningRecord pendingWarning = createSeedWarning(mediumAssessment, "WARN-20260308-001", 0, "2026-03-08 15:46:00");
        WarningRecord handledWarning = createSeedWarning(highAssessment, "WARN-20260307-001", 2, "2026-03-07 16:31:00");
        handledWarning.getHandleRecords().add(WarningHandleRecord.builder()
                .id(warningRecordIdGenerator.getAndIncrement())
                .warningId(handledWarning.getId())
                .handleUserId(2L)
                .handleUserName("演示风控员")
                .handleOpinion("已联系客户补充抵押物并收紧授信额度。")
                .handleResult("完成首次处置，风险已纳入重点跟踪。")
                .nextStatus(2)
                .handleTime("2026-03-08 09:30:00")
                .build());
        warnings.put(pendingWarning.getId(), pendingWarning);
        warnings.put(handledWarning.getId(), handledWarning);
    }

    private AssessmentRecord seedAssessment(Long riskDataId, String assessmentTime, SecurityUser operator, Integer assessmentStatus, boolean generateWarning) {
        RiskDemoStore.RiskDataRecord riskDataRecord = riskDemoStore.getRiskData(riskDataId);
        List<AssessmentIndexResultRecord> indexResults = buildAssessmentIndexResults(riskDataRecord, riskDemoStore.listEnabledRiskIndexes());
        BigDecimal totalScore = indexResults.stream()
                .map(AssessmentIndexResultRecord::getWeightedScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, BigDecimal.ROUND_HALF_UP);
        AssessmentRecord assessmentRecord = AssessmentRecord.builder()
                .id(assessmentIdGenerator.getAndIncrement())
                .riskDataId(riskDataId)
                .businessNo(riskDataRecord.getBusinessNo())
                .customerName(riskDataRecord.getCustomerName())
                .businessType(riskDataRecord.getBusinessType())
                .riskDesc(riskDataRecord.getRiskDesc())
                .totalScore(totalScore)
                .riskLevel(resolveRiskLevel(totalScore))
                .assessmentStatus(assessmentStatus)
                .assessmentTime(assessmentTime)
                .assessmentBy(operator.getUserId())
                .assessmentByName(operator.getRealName())
                .dataStatus(riskDataRecord.getDataStatus())
                .indexResults(copyIndexResults(indexResults))
                .build();
        assessments.put(assessmentRecord.getId(), assessmentRecord);
        return assessmentRecord;
    }

    private void seedHistoricalAssessmentForRiskDataFive(SecurityUser operator) {
        RiskDemoStore.RiskDataRecord historyRiskData = RiskDemoStore.RiskDataRecord.builder()
                .id(5L)
                .businessNo("FRC-202603-005")
                .customerName("云峰科技有限公司")
                .businessType("保函业务")
                .riskDesc("业务数据更新前的低风险评估记录。")
                .dataStatus(1)
                .createBy(2L)
                .createByName("演示风控员")
                .createTime("2026-03-06 10:05:00")
                .updateTime("2026-03-10 10:00:00")
                .indexValues(List.of(
                        RiskDemoStore.RiskDataIndexValueRecord.builder().indexId(1L).indexValue(new BigDecimal("38")).build(),
                        RiskDemoStore.RiskDataIndexValueRecord.builder().indexId(2L).indexValue(new BigDecimal("1.65")).build(),
                        RiskDemoStore.RiskDataIndexValueRecord.builder().indexId(3L).indexValue(new BigDecimal("0")).build(),
                        RiskDemoStore.RiskDataIndexValueRecord.builder().indexId(4L).indexValue(new BigDecimal("165")).build()
                ))
                .build();
        List<AssessmentIndexResultRecord> indexResults = buildAssessmentIndexResults(historyRiskData, riskDemoStore.listEnabledRiskIndexes());
        BigDecimal totalScore = indexResults.stream()
                .map(AssessmentIndexResultRecord::getWeightedScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, BigDecimal.ROUND_HALF_UP);
        assessments.put(assessmentIdGenerator.getAndIncrement(), AssessmentRecord.builder()
                .id(assessmentIdGenerator.get() - 1)
                .riskDataId(5L)
                .businessNo(historyRiskData.getBusinessNo())
                .customerName(historyRiskData.getCustomerName())
                .businessType(historyRiskData.getBusinessType())
                .riskDesc(historyRiskData.getRiskDesc())
                .totalScore(totalScore)
                .riskLevel(resolveRiskLevel(totalScore))
                .assessmentStatus(0)
                .assessmentTime("2026-03-10 10:10:00")
                .assessmentBy(operator.getUserId())
                .assessmentByName(operator.getRealName())
                .dataStatus(1)
                .indexResults(copyIndexResults(indexResults))
                .build());
    }

    private WarningRecord createSeedWarning(AssessmentRecord assessmentRecord, String warningCode, Integer warningStatus, String createTime) {
        return WarningRecord.builder()
                .id(warningIdGenerator.getAndIncrement())
                .assessmentId(assessmentRecord.getId())
                .riskDataId(assessmentRecord.getRiskDataId())
                .warningCode(warningCode)
                .warningLevel(assessmentRecord.getRiskLevel())
                .warningContent(buildWarningContent(assessmentRecord.getRiskLevel(), assessmentRecord.getCustomerName(), assessmentRecord.getBusinessNo()))
                .businessNo(assessmentRecord.getBusinessNo())
                .customerName(assessmentRecord.getCustomerName())
                .businessType(assessmentRecord.getBusinessType())
                .riskDesc(assessmentRecord.getRiskDesc())
                .totalScore(assessmentRecord.getTotalScore())
                .riskLevel(assessmentRecord.getRiskLevel())
                .warningStatus(warningStatus)
                .createTime(createTime)
                .handleRecords(new ArrayList<>())
                .build();
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
                    .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "该业务数据缺少启用指标值，无法执行评估"));
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

    private String resolveRiskLevel(BigDecimal totalScore) {
        if (totalScore.compareTo(new BigDecimal("80")) >= 0) {
            return "HIGH";
        }
        if (totalScore.compareTo(new BigDecimal("60")) >= 0) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private String createWarningCode(Long warningId) {
        String date = now().substring(0, 10).replace("-", "");
        return "WARN-" + date + "-" + String.format("%03d", warningId);
    }

    private String buildWarningContent(String riskLevel, String customerName, String businessNo) {
        String riskText = Objects.equals(riskLevel, "HIGH") ? "高风险" : "中风险";
        return customerName + " 的业务 " + businessNo + " 评估结果为" + riskText + "，请尽快跟进。";
    }

    private boolean inDateRange(String value, String startTime, String endTime) {
        String date = value.substring(0, 10);
        if (startTime != null && !startTime.isBlank() && date.compareTo(startTime.substring(0, 10)) < 0) {
            return false;
        }
        return endTime == null || endTime.isBlank() || date.compareTo(endTime.substring(0, 10)) <= 0;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private String now() {
        return LocalDateTime.now().format(DATE_TIME_FORMATTER);
    }

    private AssessmentRecord copyAssessmentRecord(AssessmentRecord source) {
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
                .indexResults(copyIndexResults(source.getIndexResults()))
                .build();
    }

    private WarningRecord copyWarningRecord(WarningRecord source) {
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
                .handleRecords(source.getHandleRecords().stream()
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
                        .collect(ArrayList::new, ArrayList::add, ArrayList::addAll))
                .build();
    }

    private List<AssessmentIndexResultRecord> copyIndexResults(List<AssessmentIndexResultRecord> source) {
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

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardSummary {
        private int riskDataCount;
        private int assessmentCount;
        private int warningCount;
        private int handledWarningCount;
        private int highRiskCount;
    }

    @AllArgsConstructor
    @Getter
    public static class RiskLevelStat {
        private final String riskLevel;
        private final int count;
    }

    @AllArgsConstructor
    @Getter
    public static class WarningTrendStat {
        private final String date;
        private int total;
        private int pending;
        private int handled;
    }

    @AllArgsConstructor
    @Getter
    public static class HandleSummaryStat {
        private final int warningStatus;
        private final String label;
        private final int count;
    }
}
