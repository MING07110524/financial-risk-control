package com.cmj.risk.service.impl;

import com.cmj.risk.component.RiskDemoStore;
import com.cmj.risk.service.RiskIndexService;
import com.cmj.risk.vo.risk.RiskIndexVO;
import com.cmj.risk.vo.risk.RiskRuleVO;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RiskIndexServiceImpl implements RiskIndexService {
    private final RiskDemoStore riskDemoStore;

    public RiskIndexServiceImpl(RiskDemoStore riskDemoStore) {
        this.riskDemoStore = riskDemoStore;
    }

    @Override
    public List<RiskIndexVO> listRiskIndexes(String indexName, Integer status) {
        return riskDemoStore.listRiskIndexes(indexName, status).stream()
                .map(item -> RiskIndexVO.builder()
                        .id(item.getId())
                        .indexName(item.getIndexName())
                        .indexCode(item.getIndexCode())
                        .weightValue(item.getWeightValue())
                        .indexDesc(item.getIndexDesc())
                        .status(item.getStatus())
                        .build())
                .toList();
    }

    @Override
    public List<RiskRuleVO> listRiskRules(Long indexId) {
        return riskDemoStore.listRiskRules(indexId).stream()
                .map(item -> RiskRuleVO.builder()
                        .id(item.getId())
                        .indexId(item.getIndexId())
                        .indexName(item.getIndexName())
                        .scoreMin(item.getScoreMin())
                        .scoreMax(item.getScoreMax())
                        .scoreValue(item.getScoreValue())
                        .warningLevel(item.getWarningLevel())
                        .build())
                .toList();
    }
}
