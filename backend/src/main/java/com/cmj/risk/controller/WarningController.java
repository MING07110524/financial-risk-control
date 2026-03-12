package com.cmj.risk.controller;

import com.cmj.risk.common.PageResult;
import com.cmj.risk.common.Result;
import com.cmj.risk.dto.warning.WarningHandleDTO;
import com.cmj.risk.exception.BusinessException;
import com.cmj.risk.security.SecurityUser;
import com.cmj.risk.service.WarningService;
import com.cmj.risk.vo.warning.WarningDetailVO;
import com.cmj.risk.vo.warning.WarningHandleRecordVO;
import com.cmj.risk.vo.warning.WarningVO;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/warnings")
public class WarningController {
    private final WarningService warningService;

    public WarningController(WarningService warningService) {
        this.warningService = warningService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('RISK_USER', 'MANAGER')")
    public Result<PageResult<WarningVO>> pageWarnings(
            @RequestParam(defaultValue = "") String warningCode,
            @RequestParam(defaultValue = "") String warningLevel,
            @RequestParam(required = false) Integer warningStatus,
            @RequestParam(defaultValue = "") String startTime,
            @RequestParam(defaultValue = "") String endTime,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return Result.success(warningService.pageWarnings(
                warningCode, warningLevel, warningStatus, startTime, endTime, pageNum, pageSize
        ));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('RISK_USER', 'MANAGER')")
    public Result<WarningDetailVO> getWarningDetail(@PathVariable Long id) {
        return Result.success(warningService.getWarningDetail(id));
    }

    @GetMapping("/{id}/records")
    @PreAuthorize("hasAnyRole('RISK_USER', 'MANAGER')")
    public Result<List<WarningHandleRecordVO>> listWarningRecords(@PathVariable Long id) {
        return Result.success(warningService.listWarningRecords(id));
    }

    @PostMapping("/{id}/handle")
    @PreAuthorize("hasRole('RISK_USER')")
    public Result<Void> handleWarning(
            @PathVariable Long id,
            @Valid @RequestBody WarningHandleDTO dto,
            Authentication authentication
    ) {
        warningService.handleWarning(id, dto.getHandleOpinion(), dto.getHandleResult(), dto.getNextStatus(), currentUser(authentication));
        return Result.success();
    }

    private SecurityUser currentUser(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof SecurityUser securityUser) {
            return securityUser;
        }
        throw new BusinessException(com.cmj.risk.common.ErrorCode.UNAUTHORIZED, "当前用户未登录");
    }
}
