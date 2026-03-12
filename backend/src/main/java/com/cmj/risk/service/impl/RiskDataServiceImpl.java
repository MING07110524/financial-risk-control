package com.cmj.risk.service.impl;

import com.cmj.risk.common.PageResult;
import com.cmj.risk.component.RiskDemoStore;
import com.cmj.risk.component.RiskWorkflowStore;
import com.cmj.risk.common.ErrorCode;
import com.cmj.risk.exception.BusinessException;
import com.cmj.risk.dto.risk.RiskDataCreateDTO;
import com.cmj.risk.dto.risk.RiskDataUpdateDTO;
import com.cmj.risk.security.SecurityUser;
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

    public RiskDataServiceImpl(RiskDemoStore riskDemoStore, RiskWorkflowStore riskWorkflowStore) {
        this.riskDemoStore = riskDemoStore;
        this.riskWorkflowStore = riskWorkflowStore;
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
        return toRiskDataDetailVO(created);
    }

    @Override
    public RiskDataDetailVO updateRiskData(Long id, RiskDataUpdateDTO dto, SecurityUser operator) {
        String businessNo = riskDemoStore.getRiskData(id).getBusinessNo();
        RiskDemoStore.RiskDataRecord updated = riskDemoStore.updateRiskData(id, buildDraftRecord(dto, businessNo, operator));
        return toRiskDataDetailVO(updated);
    }

    @Override
    public void deleteRiskData(Long id) {
        if (riskWorkflowStore.hasRiskDataHistory(id)) {
            throw new BusinessException(ErrorCode.CONFLICT, "该业务数据已有评估或预警记录，当前阶段不允许删除");
        }
        riskDemoStore.deleteRiskData(id);
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
                .build();
    }
}
