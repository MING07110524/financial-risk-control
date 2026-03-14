package com.cmj.risk.controller;

import com.cmj.risk.common.Result;
import com.cmj.risk.dto.risk.RiskIndexCreateDTO;
import com.cmj.risk.dto.risk.RiskIndexStatusDTO;
import com.cmj.risk.dto.risk.RiskIndexUpdateDTO;
import com.cmj.risk.dto.risk.RiskRuleCreateDTO;
import com.cmj.risk.dto.risk.RiskRuleUpdateDTO;
import com.cmj.risk.service.RiskIndexService;
import com.cmj.risk.vo.risk.RiskIndexVO;
import com.cmj.risk.vo.risk.RiskRuleVO;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PostMapping("/risk-indexes")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<RiskIndexVO> createRiskIndex(@Valid @RequestBody RiskIndexCreateDTO dto) {
        return Result.success(riskIndexService.createRiskIndex(dto));
    }

    @PutMapping("/risk-indexes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<RiskIndexVO> updateRiskIndex(@PathVariable Long id, @Valid @RequestBody RiskIndexUpdateDTO dto) {
        return Result.success(riskIndexService.updateRiskIndex(id, dto));
    }

    @PutMapping("/risk-indexes/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<RiskIndexVO> updateRiskIndexStatus(@PathVariable Long id, @Valid @RequestBody RiskIndexStatusDTO dto) {
        return Result.success(riskIndexService.updateRiskIndexStatus(id, dto));
    }

    @GetMapping("/risk-rules")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<RiskRuleVO>> listRiskRules(@RequestParam Long indexId) {
        return Result.success(riskIndexService.listRiskRules(indexId));
    }

    @PostMapping("/risk-rules")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<RiskRuleVO> createRiskRule(@Valid @RequestBody RiskRuleCreateDTO dto) {
        return Result.success(riskIndexService.createRiskRule(dto));
    }

    @PutMapping("/risk-rules/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<RiskRuleVO> updateRiskRule(@PathVariable Long id, @Valid @RequestBody RiskRuleUpdateDTO dto) {
        return Result.success(riskIndexService.updateRiskRule(id, dto));
    }

    @DeleteMapping("/risk-rules/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteRiskRule(@PathVariable Long id) {
        riskIndexService.deleteRiskRule(id);
        return Result.success();
    }
}
