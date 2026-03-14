package com.cmj.risk.service;

import com.cmj.risk.common.PageResult;
import com.cmj.risk.dto.risk.RiskDataCreateDTO;
import com.cmj.risk.dto.risk.RiskDataUpdateDTO;
import com.cmj.risk.security.SecurityUser;
import com.cmj.risk.vo.risk.RiskDataDetailVO;
import com.cmj.risk.vo.risk.RiskDataVO;

public interface RiskDataService {
    PageResult<RiskDataVO> pageRiskData(
            String businessNo,
            String customerName,
            String businessType,
            Integer dataStatus,
            int pageNum,
            int pageSize
    );

    RiskDataDetailVO getRiskDataDetail(Long id);

    RiskDataDetailVO createRiskData(RiskDataCreateDTO dto, SecurityUser operator);

    RiskDataDetailVO updateRiskData(Long id, RiskDataUpdateDTO dto, SecurityUser operator);

    void deleteRiskData(Long id, SecurityUser operator);
}
