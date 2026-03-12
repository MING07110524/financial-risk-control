package com.cmj.risk.controller;

import com.cmj.risk.common.PageResult;
import com.cmj.risk.common.Result;
import com.cmj.risk.exception.BusinessException;
import com.cmj.risk.security.SecurityUser;
import com.cmj.risk.service.AssessmentService;
import com.cmj.risk.vo.assessment.AssessmentDetailVO;
import com.cmj.risk.vo.assessment.AssessmentVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assessments")
@PreAuthorize("hasRole('RISK_USER')")
public class AssessmentController {
    private final AssessmentService assessmentService;

    public AssessmentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @GetMapping
    public Result<PageResult<AssessmentVO>> pageAssessments(
            @RequestParam(defaultValue = "") String businessNo,
            @RequestParam(defaultValue = "") String riskLevel,
            @RequestParam(required = false) Integer assessmentStatus,
            @RequestParam(defaultValue = "") String startTime,
            @RequestParam(defaultValue = "") String endTime,
            @RequestParam(required = false) Long riskDataId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return Result.success(assessmentService.pageAssessments(
                businessNo, riskLevel, assessmentStatus, startTime, endTime, riskDataId, pageNum, pageSize
        ));
    }

    @GetMapping("/{id}")
    public Result<AssessmentDetailVO> getAssessmentDetail(@PathVariable Long id) {
        return Result.success(assessmentService.getAssessmentDetail(id));
    }

    @PostMapping("/{riskDataId}/execute")
    public Result<AssessmentDetailVO> executeAssessment(@PathVariable Long riskDataId, Authentication authentication) {
        return Result.success(assessmentService.executeAssessment(riskDataId, currentUser(authentication)));
    }

    private SecurityUser currentUser(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof SecurityUser securityUser) {
            return securityUser;
        }
        throw new BusinessException(com.cmj.risk.common.ErrorCode.UNAUTHORIZED, "当前用户未登录");
    }
}
