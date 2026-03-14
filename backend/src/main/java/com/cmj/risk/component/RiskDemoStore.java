package com.cmj.risk.component;

import com.cmj.risk.common.ErrorCode;
import com.cmj.risk.exception.BusinessException;
import com.cmj.risk.mapper.risk.RiskDataIndexValueMapper;
import com.cmj.risk.mapper.risk.RiskDataMapper;
import com.cmj.risk.mapper.risk.RiskIndexMapper;
import com.cmj.risk.mapper.risk.RiskRuleMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
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
public class RiskDemoStore {
    private final RiskIndexMapper riskIndexMapper;
    private final RiskRuleMapper riskRuleMapper;
    private final RiskDataMapper riskDataMapper;
    private final RiskDataIndexValueMapper riskDataIndexValueMapper;

    public RiskDemoStore(
            RiskIndexMapper riskIndexMapper,
            RiskRuleMapper riskRuleMapper,
            RiskDataMapper riskDataMapper,
            RiskDataIndexValueMapper riskDataIndexValueMapper
    ) {
        this.riskIndexMapper = riskIndexMapper;
        this.riskRuleMapper = riskRuleMapper;
        this.riskDataMapper = riskDataMapper;
        this.riskDataIndexValueMapper = riskDataIndexValueMapper;
    }

    public List<RiskIndexRecord> listRiskIndexes(String indexName, Integer status) {
        return riskIndexMapper.listRiskIndexes(normalize(indexName), status).stream()
                .map(this::copyRiskIndexRecord)
                .toList();
    }

    public RiskIndexRecord getRiskIndex(Long indexId) {
        RiskIndexRecord riskIndexRecord = riskIndexMapper.findById(indexId);
        if (riskIndexRecord == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "未找到对应的风险指标");
        }
        return copyRiskIndexRecord(riskIndexRecord);
    }

    @Transactional
    public RiskIndexRecord createRiskIndex(RiskIndexRecord draftRecord) {
        validateRiskIndexStatus(draftRecord.getStatus());
        String normalizedCode = normalizeIndexCode(draftRecord.getIndexCode());
        validateUniqueIndexCode(normalizedCode, null);
        validateIndexHasRulesBeforeEnable(null, draftRecord.getStatus());
        validateEnabledWeightLimit(null, draftRecord.getWeightValue(), draftRecord.getStatus());

        RiskIndexRecord created = copyRiskIndexRecord(draftRecord);
        created.setIndexCode(normalizedCode);
        riskIndexMapper.insert(created);
        return getRiskIndex(created.getId());
    }

    @Transactional
    public RiskIndexRecord updateRiskIndex(Long id, RiskIndexRecord draftRecord) {
        RiskIndexRecord current = riskIndexMapper.findById(id);
        if (current == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "未找到对应的风险指标");
        }

        String normalizedCode = normalizeIndexCode(draftRecord.getIndexCode());
        validateUniqueIndexCode(normalizedCode, id);
        validateEnabledWeightLimit(id, draftRecord.getWeightValue(), current.getStatus());

        current.setIndexName(draftRecord.getIndexName());
        current.setIndexCode(normalizedCode);
        current.setWeightValue(draftRecord.getWeightValue());
        current.setIndexDesc(draftRecord.getIndexDesc());
        riskIndexMapper.update(current);
        return getRiskIndex(id);
    }

    @Transactional
    public RiskIndexRecord updateRiskIndexStatus(Long id, Integer status) {
        RiskIndexRecord current = riskIndexMapper.findById(id);
        if (current == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "未找到对应的风险指标");
        }

        validateRiskIndexStatus(status);
        validateIndexHasRulesBeforeEnable(id, status);
        validateEnabledWeightLimit(id, current.getWeightValue(), status);
        riskIndexMapper.updateStatus(id, status);
        return getRiskIndex(id);
    }

    public List<RiskRuleRecord> listRiskRules(Long indexId) {
        getRiskIndex(indexId);
        return riskRuleMapper.listByIndexId(indexId).stream()
                .sorted(Comparator.comparing(RiskRuleRecord::getScoreMin).thenComparing(RiskRuleRecord::getId))
                .map(this::copyRiskRuleRecord)
                .toList();
    }

    @Transactional
    public RiskRuleRecord createRiskRule(RiskRuleRecord draftRecord) {
        RiskIndexRecord riskIndexRecord = getRiskIndex(draftRecord.getIndexId());
        validateRuleRange(draftRecord.getScoreMin(), draftRecord.getScoreMax());
        validateRuleConflict(draftRecord.getIndexId(), null, draftRecord.getScoreMin(), draftRecord.getScoreMax());

        RiskRuleRecord created = copyRiskRuleRecord(draftRecord);
        created.setIndexName(riskIndexRecord.getIndexName());
        riskRuleMapper.insert(created);
        return getRiskRule(created.getId());
    }

    @Transactional
    public RiskRuleRecord updateRiskRule(Long id, RiskRuleRecord draftRecord) {
        RiskRuleRecord current = getRiskRule(id);
        validateRuleRange(draftRecord.getScoreMin(), draftRecord.getScoreMax());
        validateRuleConflict(current.getIndexId(), id, draftRecord.getScoreMin(), draftRecord.getScoreMax());

        current.setScoreMin(draftRecord.getScoreMin());
        current.setScoreMax(draftRecord.getScoreMax());
        current.setScoreValue(draftRecord.getScoreValue());
        current.setWarningLevel(draftRecord.getWarningLevel());
        riskRuleMapper.update(current);
        return getRiskRule(id);
    }

    @Transactional
    public void deleteRiskRule(Long id) {
        RiskRuleRecord current = getRiskRule(id);
        if (Objects.equals(getRiskIndex(current.getIndexId()).getStatus(), 1)
                && riskRuleMapper.countByIndexId(current.getIndexId()) == 1) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "启用中的指标必须至少保留一条评分规则");
        }
        riskRuleMapper.deleteById(id);
    }

    public List<RiskDataRecord> listRiskData(
            String businessNo,
            String customerName,
            String businessType,
            Integer dataStatus
    ) {
        return riskDataMapper.listRiskData(normalize(businessNo), normalize(customerName), normalize(businessType), dataStatus)
                .stream()
                .map(this::attachIndexValues)
                .sorted(Comparator.comparing(RiskDataRecord::getCreateTime).reversed().thenComparing(RiskDataRecord::getId).reversed())
                .toList();
    }

    public RiskDataRecord getRiskData(Long id) {
        RiskDataRecord record = riskDataMapper.findById(id);
        if (record == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "未找到对应的风险数据");
        }
        return attachIndexValues(record);
    }

    @Transactional
    public RiskDataRecord createRiskData(RiskDataRecord draftRecord) {
        validateUniqueBusinessNo(draftRecord.getBusinessNo(), null);
        validateIndexValues(draftRecord.getIndexValues());

        RiskDataRecord created = copyRiskDataRecord(draftRecord);
        created.setDataStatus(0);
        riskDataMapper.insert(created);
        replaceRiskDataIndexValues(created.getId(), draftRecord.getIndexValues());
        return getRiskData(created.getId());
    }

    @Transactional
    public RiskDataRecord updateRiskData(Long id, RiskDataRecord draftRecord) {
        RiskDataRecord current = riskDataMapper.findById(id);
        if (current == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "未找到对应的风险数据");
        }

        validateIndexValues(draftRecord.getIndexValues());

        current.setCustomerName(draftRecord.getCustomerName());
        current.setBusinessType(draftRecord.getBusinessType());
        current.setRiskDesc(draftRecord.getRiskDesc());
        if (Objects.equals(current.getDataStatus(), 1)) {
            current.setDataStatus(2);
        }
        riskDataMapper.update(current);
        replaceRiskDataIndexValues(id, draftRecord.getIndexValues());
        return getRiskData(id);
    }

    @Transactional
    public void deleteRiskData(Long id) {
        RiskDataRecord current = riskDataMapper.findById(id);
        if (current == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "未找到对应的风险数据");
        }
        if (!Objects.equals(current.getDataStatus(), 0)) {
            throw new BusinessException(ErrorCode.CONFLICT, "该业务数据已有评估历史或预警关联，当前阶段不允许删除");
        }
        riskDataIndexValueMapper.deleteByRiskDataId(id);
        riskDataMapper.deleteById(id);
    }

    @Transactional
    public void setRiskDataStatus(Long id, Integer dataStatus) {
        RiskDataRecord current = riskDataMapper.findById(id);
        if (current == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "未找到对应的风险数据");
        }
        riskDataMapper.updateStatus(id, dataStatus);
    }

    public List<Long> listRiskDataIdsMissingEnabledIndex(Long indexId) {
        return listRiskData("", "", "", null).stream()
                .filter(item -> item.getIndexValues().stream().noneMatch(value -> value.getIndexId().equals(indexId)))
                .map(RiskDataRecord::getId)
                .toList();
    }

    public boolean hasAllEnabledIndexValues(Long riskDataId) {
        RiskDataRecord current = getRiskData(riskDataId);
        List<Long> enabledIndexIds = listEnabledRiskIndexes().stream()
                .map(RiskIndexRecord::getId)
                .sorted()
                .toList();
        List<Long> existingIndexIds = current.getIndexValues().stream()
                .map(RiskDataIndexValueRecord::getIndexId)
                .sorted()
                .toList();
        return enabledIndexIds.equals(existingIndexIds);
    }

    public List<String> listMissingEnabledIndexNames(Long riskDataId) {
        RiskDataRecord current = getRiskData(riskDataId);
        return listEnabledRiskIndexes().stream()
                .filter(index -> current.getIndexValues().stream().noneMatch(value -> value.getIndexId().equals(index.getId())))
                .map(RiskIndexRecord::getIndexName)
                .toList();
    }

    public List<RiskIndexRecord> listEnabledRiskIndexes() {
        return riskIndexMapper.listRiskIndexes(null, 1).stream()
                .map(this::copyRiskIndexRecord)
                .toList();
    }

    public Long findRuleIndexId(Long ruleId) {
        return getRiskRule(ruleId).getIndexId();
    }

    private RiskRuleRecord getRiskRule(Long id) {
        RiskRuleRecord riskRuleRecord = riskRuleMapper.findById(id);
        if (riskRuleRecord == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "未找到对应的评分规则");
        }
        return copyRiskRuleRecord(riskRuleRecord);
    }

    private void replaceRiskDataIndexValues(Long riskDataId, List<RiskDataIndexValueRecord> indexValues) {
        riskDataIndexValueMapper.deleteByRiskDataId(riskDataId);
        if (!indexValues.isEmpty()) {
            riskDataIndexValueMapper.insertBatch(riskDataId, copyIndexValueRecords(indexValues));
        }
    }

    private RiskDataRecord attachIndexValues(RiskDataRecord source) {
        RiskDataRecord copied = copyRiskDataRecord(source);
        copied.setIndexValues(copyIndexValueRecords(riskDataIndexValueMapper.listByRiskDataId(source.getId())));
        return copied;
    }

    private void validateUniqueBusinessNo(String businessNo, Long currentId) {
        if (riskDataMapper.countByBusinessNo(businessNo, currentId) > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "业务编号已存在，请更换后重试");
        }
    }

    private void validateUniqueIndexCode(String indexCode, Long currentId) {
        RiskIndexRecord duplicated = riskIndexMapper.findByCode(indexCode);
        if (duplicated != null && (currentId == null || !duplicated.getId().equals(currentId))) {
            throw new BusinessException(ErrorCode.DUPLICATE_INDEX_CODE, "指标编码已存在，请更换后重试");
        }
    }

    private void validateEnabledWeightLimit(Long currentId, BigDecimal candidateWeight, Integer candidateStatus) {
        if (!Objects.equals(candidateStatus, 1)) {
            return;
        }

        BigDecimal enabledWeightTotal = riskIndexMapper.sumEnabledWeights(currentId).add(candidateWeight);
        if (enabledWeightTotal.compareTo(new BigDecimal("100.00")) > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "启用指标总权重不能超过 100，请先调整后重试");
        }
    }

    private void validateRiskIndexStatus(Integer status) {
        if (!Objects.equals(status, 0) && !Objects.equals(status, 1)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "指标状态只允许为 0 或 1");
        }
    }

    private void validateIndexHasRulesBeforeEnable(Long indexId, Integer status) {
        if (!Objects.equals(status, 1)) {
            return;
        }
        if (indexId == null || riskRuleMapper.countByIndexId(indexId) == 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "该指标尚未配置评分规则，无法启用");
        }
    }

    private void validateRuleRange(BigDecimal scoreMin, BigDecimal scoreMax) {
        if (scoreMin.compareTo(scoreMax) > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "评分规则最小值不能大于最大值");
        }
    }

    private void validateRuleConflict(Long indexId, Long currentRuleId, BigDecimal scoreMin, BigDecimal scoreMax) {
        if (riskRuleMapper.countConflictingRules(indexId, currentRuleId, scoreMin, scoreMax) > 0) {
            throw new BusinessException(ErrorCode.RULE_RANGE_CONFLICT, "同一指标下评分规则区间不能重叠");
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
        if (enabledIndexIds.size() != providedIndexIds.size() || !enabledIndexIds.equals(providedIndexIds)) {
            throw new BusinessException(ErrorCode.INDEX_VALUE_INCOMPLETE, "所有启用指标都必须填写且只能填写一次");
        }

        boolean hasNullValue = indexValues.stream().anyMatch(item -> item.getIndexValue() == null);
        if (hasNullValue) {
            throw new BusinessException(ErrorCode.INDEX_VALUE_INCOMPLETE, "所有启用指标都必须填写且值不能为空");
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeIndexCode(String value) {
        return value == null ? "" : value.trim().toUpperCase();
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
                .indexValues(source.getIndexValues() == null ? new ArrayList<>() : copyIndexValueRecords(source.getIndexValues()))
                .build();
    }

    private RiskIndexRecord copyRiskIndexRecord(RiskIndexRecord source) {
        return RiskIndexRecord.builder()
                .id(source.getId())
                .indexName(source.getIndexName())
                .indexCode(source.getIndexCode())
                .weightValue(source.getWeightValue())
                .indexDesc(source.getIndexDesc())
                .status(source.getStatus())
                .build();
    }

    private RiskRuleRecord copyRiskRuleRecord(RiskRuleRecord source) {
        return RiskRuleRecord.builder()
                .id(source.getId())
                .indexId(source.getIndexId())
                .indexName(source.getIndexName())
                .scoreMin(source.getScoreMin())
                .scoreMax(source.getScoreMax())
                .scoreValue(source.getScoreValue())
                .warningLevel(source.getWarningLevel())
                .build();
    }

    private List<RiskDataIndexValueRecord> copyIndexValueRecords(List<RiskDataIndexValueRecord> source) {
        return source.stream()
                .map(item -> RiskDataIndexValueRecord.builder()
                        .indexId(item.getIndexId())
                        .indexValue(item.getIndexValue())
                        .build())
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
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
