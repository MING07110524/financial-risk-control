package com.cmj.risk.service.impl;

import com.cmj.risk.component.RiskDemoStore;
import com.cmj.risk.component.RiskWorkflowStore;
import com.cmj.risk.dto.risk.RiskIndexCreateDTO;
import com.cmj.risk.dto.risk.RiskIndexStatusDTO;
import com.cmj.risk.dto.risk.RiskIndexUpdateDTO;
import com.cmj.risk.dto.risk.RiskRuleCreateDTO;
import com.cmj.risk.dto.risk.RiskRuleUpdateDTO;
import com.cmj.risk.exception.BusinessException;
import com.cmj.risk.common.ErrorCode;
import com.cmj.risk.security.SecurityUser;
import com.cmj.risk.service.LogService;
import com.cmj.risk.service.RiskIndexService;
import com.cmj.risk.vo.risk.RiskIndexVO;
import com.cmj.risk.vo.risk.RiskRuleVO;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class RiskIndexServiceImpl implements RiskIndexService {
    private static final String LOG_MODULE_NAME = "指标规则";

    private final RiskDemoStore riskDemoStore;
    private final RiskWorkflowStore riskWorkflowStore;
    private final LogService logService;

    public RiskIndexServiceImpl(RiskDemoStore riskDemoStore, RiskWorkflowStore riskWorkflowStore, LogService logService) {
        this.riskDemoStore = riskDemoStore;
        this.riskWorkflowStore = riskWorkflowStore;
        this.logService = logService;
    }

    @Override
    public List<RiskIndexVO> listRiskIndexes(String indexName, Integer status) {
        return riskDemoStore.listRiskIndexes(indexName, status).stream()
                .map(this::toRiskIndexVO)
                .toList();
    }

    @Override
    public RiskIndexVO createRiskIndex(RiskIndexCreateDTO dto) {
        SecurityUser operator = getCurrentOperator();
        RiskDemoStore.RiskIndexRecord created = riskDemoStore.createRiskIndex(RiskDemoStore.RiskIndexRecord.builder()
                .indexName(dto.getIndexName().trim())
                .indexCode(dto.getIndexCode())
                .weightValue(dto.getWeightValue())
                .indexDesc(dto.getIndexDesc().trim())
                .status(dto.getStatus())
                .build());
        RiskIndexVO riskIndexVO = toRiskIndexVO(created);
        if (dto.getStatus() == 1) {
            syncRiskDataStatusAfterIndexChange(riskIndexVO.getId(), dto.getStatus());
        }
        logService.createLog(LOG_MODULE_NAME, "新增", describeIndex(riskIndexVO), operator.getUsername(), operator.getUserId());
        return riskIndexVO;
    }

    @Override
    public RiskIndexVO updateRiskIndex(Long id, RiskIndexUpdateDTO dto) {
        SecurityUser operator = getCurrentOperator();
        RiskDemoStore.RiskIndexRecord updated = riskDemoStore.updateRiskIndex(id, RiskDemoStore.RiskIndexRecord.builder()
                .indexName(dto.getIndexName().trim())
                .indexCode(dto.getIndexCode())
                .weightValue(dto.getWeightValue())
                .indexDesc(dto.getIndexDesc().trim())
                .build());
        RiskIndexVO riskIndexVO = toRiskIndexVO(updated);
        logService.createLog(LOG_MODULE_NAME, "编辑", describeIndex(riskIndexVO), operator.getUsername(), operator.getUserId());
        return riskIndexVO;
    }

    @Override
    public RiskIndexVO updateRiskIndexStatus(Long id, RiskIndexStatusDTO dto) {
        SecurityUser operator = getCurrentOperator();
        RiskIndexVO riskIndexVO = toRiskIndexVO(riskDemoStore.updateRiskIndexStatus(id, dto.getStatus()));
        syncRiskDataStatusAfterIndexChange(id, dto.getStatus());
        logService.createLog(
                LOG_MODULE_NAME,
                dto.getStatus() == 1 ? "启用" : "停用",
                describeIndex(riskIndexVO),
                operator.getUsername(),
                operator.getUserId());
        return riskIndexVO;
    }

    @Override
    public List<RiskRuleVO> listRiskRules(Long indexId) {
        return riskDemoStore.listRiskRules(indexId).stream()
                .map(this::toRiskRuleVO)
                .toList();
    }

    @Override
    public RiskRuleVO createRiskRule(RiskRuleCreateDTO dto) {
        SecurityUser operator = getCurrentOperator();
        RiskDemoStore.RiskIndexRecord riskIndexRecord = riskDemoStore.getRiskIndex(dto.getIndexId());
        RiskDemoStore.RiskRuleRecord created = riskDemoStore.createRiskRule(RiskDemoStore.RiskRuleRecord.builder()
                .indexId(dto.getIndexId())
                .indexName(riskIndexRecord.getIndexName())
                .scoreMin(dto.getScoreMin())
                .scoreMax(dto.getScoreMax())
                .scoreValue(dto.getScoreValue())
                .warningLevel(dto.getWarningLevel())
                .build());
        RiskRuleVO riskRuleVO = toRiskRuleVO(created);
        logService.createLog(LOG_MODULE_NAME, "新增", describeRule(riskRuleVO), operator.getUsername(), operator.getUserId());
        return riskRuleVO;
    }

    @Override
    public RiskRuleVO updateRiskRule(Long id, RiskRuleUpdateDTO dto) {
        SecurityUser operator = getCurrentOperator();
        Long indexId = riskDemoStore.findRuleIndexId(id);
        RiskDemoStore.RiskIndexRecord riskIndexRecord = riskDemoStore.getRiskIndex(indexId);
        RiskDemoStore.RiskRuleRecord updated = riskDemoStore.updateRiskRule(id, RiskDemoStore.RiskRuleRecord.builder()
                .indexId(indexId)
                .indexName(riskIndexRecord.getIndexName())
                .scoreMin(dto.getScoreMin())
                .scoreMax(dto.getScoreMax())
                .scoreValue(dto.getScoreValue())
                .warningLevel(dto.getWarningLevel())
                .build());
        RiskRuleVO riskRuleVO = toRiskRuleVO(updated);
        logService.createLog(LOG_MODULE_NAME, "编辑", describeRule(riskRuleVO), operator.getUsername(), operator.getUserId());
        return riskRuleVO;
    }

    @Override
    public void deleteRiskRule(Long id) {
        SecurityUser operator = getCurrentOperator();
        Long indexId = riskDemoStore.findRuleIndexId(id);
        RiskRuleVO riskRuleVO = riskDemoStore.listRiskRules(indexId).stream()
                .filter(item -> item.getId().equals(id))
                .findFirst()
                .map(this::toRiskRuleVO)
                .orElseThrow();
        riskDemoStore.deleteRiskRule(id);
        logService.createLog(LOG_MODULE_NAME, "删除", describeRule(riskRuleVO), operator.getUsername(), operator.getUserId());
    }

    private SecurityUser getCurrentOperator() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof SecurityUser securityUser) {
            return securityUser;
        }
        throw new BusinessException(ErrorCode.UNAUTHORIZED, "未登录或登录已过期");
    }

    private String describeIndex(RiskIndexVO riskIndexVO) {
        return "指标 " + riskIndexVO.getIndexName() + "(" + riskIndexVO.getIndexCode() + ")";
    }

    private String describeRule(RiskRuleVO riskRuleVO) {
        return "规则 " + riskRuleVO.getIndexName()
                + " [" + riskRuleVO.getScoreMin().stripTrailingZeros().toPlainString()
                + ", " + riskRuleVO.getScoreMax().stripTrailingZeros().toPlainString()
                + "] -> " + riskRuleVO.getWarningLevel();
    }

    private RiskIndexVO toRiskIndexVO(RiskDemoStore.RiskIndexRecord item) {
        return RiskIndexVO.builder()
                .id(item.getId())
                .indexName(item.getIndexName())
                .indexCode(item.getIndexCode())
                .weightValue(item.getWeightValue())
                .indexDesc(item.getIndexDesc())
                .status(item.getStatus())
                .build();
    }

    private RiskRuleVO toRiskRuleVO(RiskDemoStore.RiskRuleRecord item) {
        return RiskRuleVO.builder()
                .id(item.getId())
                .indexId(item.getIndexId())
                .indexName(item.getIndexName())
                .scoreMin(item.getScoreMin())
                .scoreMax(item.getScoreMax())
                .scoreValue(item.getScoreValue())
                .warningLevel(item.getWarningLevel())
                .build();
    }

    private void syncRiskDataStatusAfterIndexChange(Long indexId, Integer nextStatus) {
        // When index configuration changes, old "current effective" assessments
        // are no longer reliable. We invalidate them first, then recompute the
        // risk data status based on whether enabled index values are complete.
        // / 指标配置一旦变更，旧的“当前有效”评估口径就已经不可靠。这里先统一
        // 失效旧评估，再按“是否补齐所有启用指标值”重算风险数据状态。
        List<Long> affectedRiskDataIds = riskDemoStore.listRiskData("", "", "", null).stream()
                .map(RiskDemoStore.RiskDataRecord::getId)
                .toList();

        if (nextStatus == 1) {
            List<Long> missingIndexRiskDataIds = riskDemoStore.listRiskDataIdsMissingEnabledIndex(indexId);
            riskWorkflowStore.invalidateEffectiveAssessments(missingIndexRiskDataIds);
        }

        for (Long riskDataId : affectedRiskDataIds) {
            boolean hadEffectiveAssessment = riskWorkflowStore.hasEffectiveAssessment(riskDataId);
            if (hadEffectiveAssessment) {
                riskWorkflowStore.invalidateEffectiveAssessments(riskDataId);
            }

            boolean hasAllEnabledIndexValues = riskDemoStore.hasAllEnabledIndexValues(riskDataId);
            if (!hasAllEnabledIndexValues) {
                riskDemoStore.setRiskDataStatus(riskDataId, 3);
                continue;
            }

            if (riskWorkflowStore.hasRiskDataHistory(riskDataId)) {
                riskDemoStore.setRiskDataStatus(riskDataId, 2);
            } else {
                riskDemoStore.setRiskDataStatus(riskDataId, 0);
            }
        }
    }
}
