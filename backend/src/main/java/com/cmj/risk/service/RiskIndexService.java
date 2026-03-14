package com.cmj.risk.service;

import com.cmj.risk.dto.risk.RiskIndexCreateDTO;
import com.cmj.risk.dto.risk.RiskIndexStatusDTO;
import com.cmj.risk.dto.risk.RiskIndexUpdateDTO;
import com.cmj.risk.dto.risk.RiskRuleCreateDTO;
import com.cmj.risk.dto.risk.RiskRuleUpdateDTO;
import com.cmj.risk.vo.risk.RiskIndexVO;
import com.cmj.risk.vo.risk.RiskRuleVO;
import java.util.List;

public interface RiskIndexService {
    List<RiskIndexVO> listRiskIndexes(String indexName, Integer status);

    RiskIndexVO createRiskIndex(RiskIndexCreateDTO dto);

    RiskIndexVO updateRiskIndex(Long id, RiskIndexUpdateDTO dto);

    RiskIndexVO updateRiskIndexStatus(Long id, RiskIndexStatusDTO dto);

    List<RiskRuleVO> listRiskRules(Long indexId);

    RiskRuleVO createRiskRule(RiskRuleCreateDTO dto);

    RiskRuleVO updateRiskRule(Long id, RiskRuleUpdateDTO dto);

    void deleteRiskRule(Long id);
}
