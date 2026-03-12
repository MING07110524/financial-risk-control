package com.cmj.risk.service;

import com.cmj.risk.vo.risk.RiskIndexVO;
import com.cmj.risk.vo.risk.RiskRuleVO;
import java.util.List;

public interface RiskIndexService {
    List<RiskIndexVO> listRiskIndexes(String indexName, Integer status);

    List<RiskRuleVO> listRiskRules(Long indexId);
}
