package com.cmj.risk.component;

import com.cmj.risk.common.ErrorCode;
import com.cmj.risk.exception.BusinessException;
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
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
public class RiskDemoStore {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AtomicLong riskDataIdGenerator = new AtomicLong(1);
    private final Map<Long, RiskIndexRecord> riskIndexes = new LinkedHashMap<>();
    private final Map<Long, List<RiskRuleRecord>> riskRulesByIndexId = new LinkedHashMap<>();
    private final Map<Long, RiskDataRecord> riskDataRecords = new LinkedHashMap<>();

    @PostConstruct
    public void init() {
        riskIndexes.clear();
        riskRulesByIndexId.clear();
        riskDataRecords.clear();
        riskDataIdGenerator.set(1);

        seedRiskIndexes();
        seedRiskRules();
        seedRiskData();
    }

    public synchronized List<RiskIndexRecord> listRiskIndexes(String indexName, Integer status) {
        String keyword = normalize(indexName);
        return riskIndexes.values().stream()
                .filter(item -> keyword.isBlank() || item.getIndexName().toLowerCase().contains(keyword))
                .filter(item -> status == null || item.getStatus().equals(status))
                .sorted(Comparator.comparing(RiskIndexRecord::getId))
                .toList();
    }

    public synchronized RiskIndexRecord getRiskIndex(Long indexId) {
        RiskIndexRecord riskIndexRecord = riskIndexes.get(indexId);
        if (riskIndexRecord == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "未找到对应的风险指标");
        }
        return riskIndexRecord;
    }

    public synchronized List<RiskRuleRecord> listRiskRules(Long indexId) {
        return riskRulesByIndexId.getOrDefault(indexId, List.of()).stream()
                .sorted(Comparator.comparing(RiskRuleRecord::getScoreMin))
                .toList();
    }

    public synchronized List<RiskDataRecord> listRiskData(
            String businessNo,
            String customerName,
            String businessType,
            Integer dataStatus
    ) {
        String businessKeyword = normalize(businessNo);
        String customerKeyword = normalize(customerName);
        String businessTypeKeyword = normalize(businessType);
        return riskDataRecords.values().stream()
                .filter(item -> businessKeyword.isBlank() || item.getBusinessNo().toLowerCase().contains(businessKeyword))
                .filter(item -> customerKeyword.isBlank() || item.getCustomerName().toLowerCase().contains(customerKeyword))
                .filter(item -> businessTypeKeyword.isBlank() || item.getBusinessType().toLowerCase().contains(businessTypeKeyword))
                .filter(item -> dataStatus == null || item.getDataStatus().equals(dataStatus))
                .sorted(Comparator.comparing(RiskDataRecord::getCreateTime).reversed())
                .map(this::copyRiskDataRecord)
                .toList();
    }

    public synchronized RiskDataRecord getRiskData(Long id) {
        RiskDataRecord record = riskDataRecords.get(id);
        if (record == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "未找到对应的风险数据");
        }
        return copyRiskDataRecord(record);
    }

    public synchronized RiskDataRecord createRiskData(RiskDataRecord draftRecord) {
        validateUniqueBusinessNo(draftRecord.getBusinessNo(), null);
        validateIndexValues(draftRecord.getIndexValues());

        Long id = riskDataIdGenerator.getAndIncrement();
        String now = now();
        draftRecord.setId(id);
        draftRecord.setCreateTime(now);
        draftRecord.setUpdateTime(now);
        draftRecord.setDataStatus(0);
        riskDataRecords.put(id, copyRiskDataRecord(draftRecord));
        return getRiskData(id);
    }

    public synchronized RiskDataRecord updateRiskData(Long id, RiskDataRecord draftRecord) {
        RiskDataRecord current = riskDataRecords.get(id);
        if (current == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "未找到对应的风险数据");
        }

        validateIndexValues(draftRecord.getIndexValues());

        current.setCustomerName(draftRecord.getCustomerName());
        current.setBusinessType(draftRecord.getBusinessType());
        current.setRiskDesc(draftRecord.getRiskDesc());
        current.setIndexValues(copyIndexValueRecords(draftRecord.getIndexValues()));
        current.setUpdateTime(now());
        if (Objects.equals(current.getDataStatus(), 1)) {
            // Keep the same user-facing meaning as the documentation: once an
            // already-assessed record changes, it should become "待重评".
            // / 保持和文档一致：已经评估过的业务一旦被修改，就要进入“待重评”状态。
            current.setDataStatus(2);
        }
        return copyRiskDataRecord(current);
    }

    public synchronized void deleteRiskData(Long id) {
        RiskDataRecord current = riskDataRecords.get(id);
        if (current == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "未找到对应的风险数据");
        }
        if (!Objects.equals(current.getDataStatus(), 0)) {
            throw new BusinessException(ErrorCode.CONFLICT, "该业务数据已有评估历史或预警关联，当前阶段不允许删除");
        }
        riskDataRecords.remove(id);
    }

    public synchronized void setRiskDataStatus(Long id, Integer dataStatus) {
        RiskDataRecord current = riskDataRecords.get(id);
        if (current == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "未找到对应的风险数据");
        }
        current.setDataStatus(dataStatus);
        current.setUpdateTime(now());
    }

    public synchronized List<RiskIndexRecord> listEnabledRiskIndexes() {
        return riskIndexes.values().stream()
                .filter(item -> Objects.equals(item.getStatus(), 1))
                .sorted(Comparator.comparing(RiskIndexRecord::getId))
                .toList();
    }

    private void validateUniqueBusinessNo(String businessNo, Long currentId) {
        boolean duplicated = riskDataRecords.values().stream()
                .anyMatch(item ->
                        item.getBusinessNo().equalsIgnoreCase(businessNo)
                                && (currentId == null || !item.getId().equals(currentId))
                );
        if (duplicated) {
            throw new BusinessException(ErrorCode.CONFLICT, "业务编号已存在，请更换后重试");
        }
    }

    private void validateIndexValues(List<RiskDataIndexValueRecord> indexValues) {
        List<Long> enabledIndexIds = listEnabledRiskIndexes().stream()
                .map(RiskIndexRecord::getId)
                .sorted()
                .toList();
        List<Long> providedIndexIds = indexValues.stream()
                .map(RiskDataIndexValueRecord::getIndexId)
                .sorted()
                .toList();

        // Require the payload to cover all enabled indexes exactly once so the
        // next phase can run the assessment without补数. / 强制请求一次性覆盖全部启用
        // 指标，避免后续评估阶段还要回头补录数据。
        if (enabledIndexIds.size() != providedIndexIds.size() || !enabledIndexIds.equals(providedIndexIds)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "所有启用指标都必须填写且只能填写一次");
        }

        boolean hasNullValue = indexValues.stream().anyMatch(item -> item.getIndexValue() == null);
        if (hasNullValue) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "所有启用指标都必须填写且值不能为空");
        }
    }

    private void seedRiskIndexes() {
        riskIndexes.put(1L, RiskIndexRecord.builder().id(1L).indexName("负债率").indexCode("DEBT_RATIO").weightValue(new BigDecimal("30.00")).indexDesc("衡量企业负债压力，越高风险越大。").status(1).build());
        riskIndexes.put(2L, RiskIndexRecord.builder().id(2L).indexName("现金流覆盖率").indexCode("CASH_FLOW_COVERAGE").weightValue(new BigDecimal("25.00")).indexDesc("衡量经营现金流覆盖债务能力，越高越稳健。").status(1).build());
        riskIndexes.put(3L, RiskIndexRecord.builder().id(3L).indexName("逾期次数").indexCode("OVERDUE_COUNT").weightValue(new BigDecimal("25.00")).indexDesc("衡量历史逾期表现，次数越多风险越高。").status(1).build());
        riskIndexes.put(4L, RiskIndexRecord.builder().id(4L).indexName("抵押覆盖率").indexCode("COLLATERAL_COVERAGE").weightValue(new BigDecimal("20.00")).indexDesc("衡量抵押物覆盖程度，越高越安全。").status(1).build());
    }

    private void seedRiskRules() {
        riskRulesByIndexId.put(1L, List.of(
                rule(1L, 1L, "负债率", "0", "40", "20", "LOW"),
                rule(2L, 1L, "负债率", "40.01", "70", "60", "MEDIUM"),
                rule(3L, 1L, "负债率", "70.01", "100", "90", "HIGH")
        ));
        riskRulesByIndexId.put(2L, List.of(
                rule(4L, 2L, "现金流覆盖率", "0", "0.99", "90", "HIGH"),
                rule(5L, 2L, "现金流覆盖率", "1", "1.59", "60", "MEDIUM"),
                rule(6L, 2L, "现金流覆盖率", "1.6", "999", "20", "LOW")
        ));
        riskRulesByIndexId.put(3L, List.of(
                rule(7L, 3L, "逾期次数", "0", "0", "20", "LOW"),
                rule(8L, 3L, "逾期次数", "1", "2", "60", "MEDIUM"),
                rule(9L, 3L, "逾期次数", "3", "99", "90", "HIGH")
        ));
        riskRulesByIndexId.put(4L, List.of(
                rule(10L, 4L, "抵押覆盖率", "0", "99.99", "90", "HIGH"),
                rule(11L, 4L, "抵押覆盖率", "100", "149.99", "60", "MEDIUM"),
                rule(12L, 4L, "抵押覆盖率", "150", "999", "20", "LOW")
        ));
    }

    private void seedRiskData() {
        createSeedRiskData("FRC-202603-001", "星河贸易有限公司", "企业贷款", "新客户首次授信，待完成人工评估。", 0, "2026-03-10 09:10:00", "2026-03-10 09:10:00", List.of(indexValue(1L, "68"), indexValue(2L, "1.25"), indexValue(3L, "1"), indexValue(4L, "130")));
        createSeedRiskData("FRC-202603-002", "晨光制造股份有限公司", "流动资金贷款", "经营稳定，已有一次低风险评估记录。", 1, "2026-03-09 11:20:00", "2026-03-09 11:20:00", List.of(indexValue(1L, "35"), indexValue(2L, "1.8"), indexValue(3L, "0"), indexValue(4L, "170")));
        createSeedRiskData("FRC-202603-003", "远航物流集团", "供应链融资", "业务规模较大，当前存在中风险预警待处理。", 1, "2026-03-08 15:30:00", "2026-03-08 15:30:00", List.of(indexValue(1L, "65"), indexValue(2L, "1.2"), indexValue(3L, "2"), indexValue(4L, "120")));
        createSeedRiskData("FRC-202603-004", "宏达置业有限公司", "项目融资", "历史上出现过高风险预警，已完成处理。", 1, "2026-03-07 16:15:00", "2026-03-07 16:15:00", List.of(indexValue(1L, "85"), indexValue(2L, "0.7"), indexValue(3L, "4"), indexValue(4L, "80")));
        createSeedRiskData("FRC-202603-005", "云峰科技有限公司", "保函业务", "业务数据已更新，等待重新评估。", 2, "2026-03-06 10:05:00", "2026-03-11 14:40:00", List.of(indexValue(1L, "75"), indexValue(2L, "0.9"), indexValue(3L, "3"), indexValue(4L, "95")));
    }

    private void createSeedRiskData(String businessNo, String customerName, String businessType, String riskDesc, Integer dataStatus, String createTime, String updateTime, List<RiskDataIndexValueRecord> indexValues) {
        Long id = riskDataIdGenerator.getAndIncrement();
        riskDataRecords.put(id, RiskDataRecord.builder()
                .id(id)
                .businessNo(businessNo)
                .customerName(customerName)
                .businessType(businessType)
                .riskDesc(riskDesc)
                .dataStatus(dataStatus)
                .createBy(2L)
                .createByName("演示风控员")
                .createTime(createTime)
                .updateTime(updateTime)
                .indexValues(copyIndexValueRecords(indexValues))
                .build());
    }

    private RiskRuleRecord rule(Long id, Long indexId, String indexName, String scoreMin, String scoreMax, String scoreValue, String warningLevel) {
        return RiskRuleRecord.builder()
                .id(id)
                .indexId(indexId)
                .indexName(indexName)
                .scoreMin(new BigDecimal(scoreMin))
                .scoreMax(new BigDecimal(scoreMax))
                .scoreValue(new BigDecimal(scoreValue))
                .warningLevel(warningLevel)
                .build();
    }

    private RiskDataIndexValueRecord indexValue(Long indexId, String value) {
        return RiskDataIndexValueRecord.builder()
                .indexId(indexId)
                .indexValue(new BigDecimal(value))
                .build();
    }

    private String now() {
        return LocalDateTime.now().format(DATE_TIME_FORMATTER);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private RiskDataRecord copyRiskDataRecord(RiskDataRecord source) {
        return RiskDataRecord.builder()
                .id(source.getId())
                .businessNo(source.getBusinessNo())
                .customerName(source.getCustomerName())
                .businessType(source.getBusinessType())
                .riskDesc(source.getRiskDesc())
                .dataStatus(source.getDataStatus())
                .createBy(source.getCreateBy())
                .createByName(source.getCreateByName())
                .createTime(source.getCreateTime())
                .updateTime(source.getUpdateTime())
                .indexValues(copyIndexValueRecords(source.getIndexValues()))
                .build();
    }

    private List<RiskDataIndexValueRecord> copyIndexValueRecords(List<RiskDataIndexValueRecord> source) {
        return source.stream()
                .map(item -> RiskDataIndexValueRecord.builder().indexId(item.getIndexId()).indexValue(item.getIndexValue()).build())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskIndexRecord {
        private Long id;
        private String indexName;
        private String indexCode;
        private BigDecimal weightValue;
        private String indexDesc;
        private Integer status;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskRuleRecord {
        private Long id;
        private Long indexId;
        private String indexName;
        private BigDecimal scoreMin;
        private BigDecimal scoreMax;
        private BigDecimal scoreValue;
        private String warningLevel;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskDataRecord {
        private Long id;
        private String businessNo;
        private String customerName;
        private String businessType;
        private String riskDesc;
        private Integer dataStatus;
        private Long createBy;
        private String createByName;
        private String createTime;
        private String updateTime;
        private List<RiskDataIndexValueRecord> indexValues;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskDataIndexValueRecord {
        private Long indexId;
        private BigDecimal indexValue;
    }
}
