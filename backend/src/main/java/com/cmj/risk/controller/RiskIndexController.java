package com.cmj.risk.controller;

import com.cmj.risk.common.Result;
import com.cmj.risk.service.RiskIndexService;
import com.cmj.risk.vo.risk.RiskIndexVO;
import com.cmj.risk.vo.risk.RiskRuleVO;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class RiskIndexController {
    private final RiskIndexService riskIndexService;

    public RiskIndexController(RiskIndexService riskIndexService) {
        this.riskIndexService = riskIndexService;
    }

    @GetMapping("/risk-indexes")
    @PreAuthorize("hasAnyRole('ADMIN', 'RISK_USER')")
    public Result<List<RiskIndexVO>> listRiskIndexes(
            @RequestParam(required = false) String indexName,
            @RequestParam(required = false) Integer status
    ) {
        return Result.success(riskIndexService.listRiskIndexes(indexName, status));
    }

    @GetMapping("/risk-rules")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<RiskRuleVO>> listRiskRules(@RequestParam Long indexId) {
        return Result.success(riskIndexService.listRiskRules(indexId));
    }
}
