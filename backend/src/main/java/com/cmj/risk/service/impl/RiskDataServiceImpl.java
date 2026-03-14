package com.cmj.risk.service.impl;

import com.cmj.risk.common.PageResult;
import com.cmj.risk.component.RiskDemoStore;
import com.cmj.risk.component.RiskWorkflowStore;
import com.cmj.risk.common.ErrorCode;
import com.cmj.risk.exception.BusinessException;
import com.cmj.risk.dto.risk.RiskDataCreateDTO;
import com.cmj.risk.dto.risk.RiskDataUpdateDTO;
import com.cmj.risk.security.SecurityUser;
import com.cmj.risk.service.LogService;
import com.cmj.risk.service.RiskDataService;
import com.cmj.risk.vo.risk.RiskDataDetailVO;
import com.cmj.risk.vo.risk.RiskDataIndexValueVO;
import com.cmj.risk.vo.risk.RiskDataVO;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RiskDataServiceImpl implements RiskDataService {
    private final RiskDemoStore riskDemoStore;
    private final RiskWorkflowStore riskWorkflowStore;
    private final LogService logService;

    public RiskDataServiceImpl(RiskDemoStore riskDemoStore, RiskWorkflowStore riskWorkflowStore, LogService logService) {
        this.riskDemoStore = riskDemoStore;
        this.riskWorkflowStore = riskWorkflowStore;
        this.logService = logService;
    }

    @Override
    public PageResult<RiskDataVO> pageRiskData(
            String businessNo,
            String customerName,
            String businessType,
            Integer dataStatus,
            int pageNum,
            int pageSize
    ) {
        List<RiskDataVO> records = riskDemoStore.listRiskData(businessNo, customerName, businessType, dataStatus)
                .stream()
                .map(this::toRiskDataVO)
                .toList();
        int start = Math.max(pageNum - 1, 0) * pageSize;
        int end = Math.min(start + pageSize, records.size());
        List<RiskDataVO> pageRecords = start >= records.size() ? List.of() : records.subList(start, end);
        return new PageResult<>((long) records.size(), pageRecords);
    }

    @Override
    public RiskDataDetailVO getRiskDataDetail(Long id) {
        return toRiskDataDetailVO(riskDemoStore.getRiskData(id));
    }

    @Override
    public RiskDataDetailVO createRiskData(RiskDataCreateDTO dto, SecurityUser operator) {
        RiskDemoStore.RiskDataRecord created = riskDemoStore.createRiskData(buildDraftRecord(dto, dto.getBusinessNo(), operator));
        logService.createLog("风险数据", "新增", "新增风险数据 " + dto.getBusinessNo(), operator.getUsername(), operator.getUserId());
        return toRiskDataDetailVO(created);
    }

    @Override
    public RiskDataDetailVO updateRiskData(Long id, RiskDataUpdateDTO dto, SecurityUser operator) {
        RiskDemoStore.RiskDataRecord current = riskDemoStore.getRiskData(id);
        String businessNo = current.getBusinessNo();
        riskDemoStore.updateRiskData(id, buildDraftRecord(dto, businessNo, operator));

        recomputeRiskDataStatusAfterSave(id);
        logService.createLog("风险数据", "编辑", "编辑风险数据 " + businessNo, operator.getUsername(), operator.getUserId());
        return toRiskDataDetailVO(riskDemoStore.getRiskData(id));
    }

    @Override
    public void deleteRiskData(Long id, SecurityUser operator) {
        RiskDemoStore.RiskDataRecord current = riskDemoStore.getRiskData(id);
        String businessNo = current.getBusinessNo();
        if (riskWorkflowStore.hasRiskDataHistory(id)) {
            throw new BusinessException(ErrorCode.CONFLICT, "该业务数据已有评估或预警记录，当前阶段不允许删除");
        }
        riskDemoStore.deleteRiskData(id);
        logService.createLog("风险数据", "删除", "删除风险数据 " + businessNo, operator.getUsername(), operator.getUserId());
    }

    private RiskDemoStore.RiskDataRecord buildDraftRecord(RiskDataCreateDTO dto, String businessNo, SecurityUser operator) {
        return RiskDemoStore.RiskDataRecord.builder()
                .businessNo(businessNo)
                .customerName(dto.getCustomerName().trim())
                .businessType(dto.getBusinessType().trim())
                .riskDesc(dto.getRiskDesc().trim())
                .createBy(operator.getUserId())
                .createByName(operator.getRealName())
                .indexValues(dto.getIndexValues().stream()
                        .map(item -> RiskDemoStore.RiskDataIndexValueRecord.builder()
                                .indexId(item.getIndexId())
                                .indexValue(item.getIndexValue())
                                .build())
                        .toList())
                .build();
    }

    private RiskDemoStore.RiskDataRecord buildDraftRecord(RiskDataUpdateDTO dto, String businessNo, SecurityUser operator) {
        return RiskDemoStore.RiskDataRecord.builder()
                .businessNo(businessNo)
                .customerName(dto.getCustomerName().trim())
                .businessType(dto.getBusinessType().trim())
                .riskDesc(dto.getRiskDesc().trim())
                .createBy(operator.getUserId())
                .createByName(operator.getRealName())
                .indexValues(dto.getIndexValues().stream()
                        .map(item -> RiskDemoStore.RiskDataIndexValueRecord.builder()
                                .indexId(item.getIndexId())
                                .indexValue(item.getIndexValue())
                                .build())
                        .toList())
                .build();
    }

    private RiskDataVO toRiskDataVO(RiskDemoStore.RiskDataRecord record) {
        return RiskDataVO.builder()
                .id(record.getId())
                .businessNo(record.getBusinessNo())
                .customerName(record.getCustomerName())
                .businessType(record.getBusinessType())
                .riskDesc(record.getRiskDesc())
                .dataStatus(record.getDataStatus())
                .createBy(record.getCreateBy())
                .createByName(record.getCreateByName())
                .createTime(record.getCreateTime())
                .updateTime(record.getUpdateTime())
                .build();
    }

    private void recomputeRiskDataStatusAfterSave(Long riskDataId) {
        boolean hasEffectiveAssessment = riskWorkflowStore.hasEffectiveAssessment(riskDataId);
        if (hasEffectiveAssessment) {
            // A data edit can change the scoring basis, so any still-effective
            // assessment must be invalidated before we decide the next status.
            // / 风险数据一旦被修改，评分依据就可能变化，所以要先失效仍然有效的评估，
            // 再决定这条业务下一步应该进入什么状态。
            riskWorkflowStore.invalidateEffectiveAssessments(riskDataId);
        }

        if (!riskDemoStore.hasAllEnabledIndexValues(riskDataId)) {
            riskDemoStore.setRiskDataStatus(riskDataId, 3);
            return;
        }

        if (riskWorkflowStore.hasRiskDataHistory(riskDataId)) {
            riskDemoStore.setRiskDataStatus(riskDataId, 2);
            return;
        }

        riskDemoStore.setRiskDataStatus(riskDataId, 0);
    }

    private RiskDataDetailVO toRiskDataDetailVO(RiskDemoStore.RiskDataRecord record) {
        return RiskDataDetailVO.builder()
                .id(record.getId())
                .businessNo(record.getBusinessNo())
                .customerName(record.getCustomerName())
                .businessType(record.getBusinessType())
                .riskDesc(record.getRiskDesc())
                .dataStatus(record.getDataStatus())
                .createBy(record.getCreateBy())
                .createByName(record.getCreateByName())
                .createTime(record.getCreateTime())
                .updateTime(record.getUpdateTime())
                .indexValues(record.getIndexValues().stream()
                        .map(item -> {
                            RiskDemoStore.RiskIndexRecord indexRecord = riskDemoStore.getRiskIndex(item.getIndexId());
                            return RiskDataIndexValueVO.builder()
                                    .indexId(indexRecord.getId())
                                    .indexCode(indexRecord.getIndexCode())
                                    .indexName(indexRecord.getIndexName())
                                    .indexValue(item.getIndexValue())
                                    .weightValue(indexRecord.getWeightValue())
                                    .build();
                        })
                        .toList())
                .missingEnabledIndexNames(riskDemoStore.listMissingEnabledIndexNames(record.getId()))
                .build();
    }
}
