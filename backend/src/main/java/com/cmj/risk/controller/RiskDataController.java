package com.cmj.risk.controller;

import com.cmj.risk.common.PageResult;
import com.cmj.risk.common.Result;
import com.cmj.risk.dto.risk.RiskDataCreateDTO;
import com.cmj.risk.dto.risk.RiskDataUpdateDTO;
import com.cmj.risk.exception.BusinessException;
import com.cmj.risk.security.SecurityUser;
import com.cmj.risk.service.RiskDataService;
import com.cmj.risk.vo.risk.RiskDataDetailVO;
import com.cmj.risk.vo.risk.RiskDataVO;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
@RequestMapping("/api/risk-data")
@PreAuthorize("hasRole('RISK_USER')")
public class RiskDataController {
    private final RiskDataService riskDataService;

    public RiskDataController(RiskDataService riskDataService) {
        this.riskDataService = riskDataService;
    }

    @GetMapping
    public Result<PageResult<RiskDataVO>> pageRiskData(
            @RequestParam(defaultValue = "") String businessNo,
            @RequestParam(defaultValue = "") String customerName,
            @RequestParam(defaultValue = "") String businessType,
            @RequestParam(required = false) Integer dataStatus,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return Result.success(riskDataService.pageRiskData(businessNo, customerName, businessType, dataStatus, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public Result<RiskDataDetailVO> getRiskDataDetail(@PathVariable Long id) {
        return Result.success(riskDataService.getRiskDataDetail(id));
    }

    @PostMapping
    public Result<RiskDataDetailVO> createRiskData(
            @Valid @RequestBody RiskDataCreateDTO dto,
            Authentication authentication
    ) {
        return Result.success(riskDataService.createRiskData(dto, currentUser(authentication)));
    }

    @PutMapping("/{id}")
    public Result<RiskDataDetailVO> updateRiskData(
            @PathVariable Long id,
            @Valid @RequestBody RiskDataUpdateDTO dto,
            Authentication authentication
    ) {
        return Result.success(riskDataService.updateRiskData(id, dto, currentUser(authentication)));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteRiskData(@PathVariable Long id, Authentication authentication) {
        riskDataService.deleteRiskData(id, currentUser(authentication));
        return Result.success();
    }

    private SecurityUser currentUser(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof SecurityUser securityUser) {
            return securityUser;
        }
        throw new BusinessException(com.cmj.risk.common.ErrorCode.UNAUTHORIZED, "当前用户未登录");
    }
}
