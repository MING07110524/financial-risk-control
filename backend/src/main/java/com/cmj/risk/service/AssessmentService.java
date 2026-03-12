package com.cmj.risk.service;

import com.cmj.risk.common.PageResult;
import com.cmj.risk.security.SecurityUser;
import com.cmj.risk.vo.assessment.AssessmentDetailVO;
import com.cmj.risk.vo.assessment.AssessmentVO;

public interface AssessmentService {
    PageResult<AssessmentVO> pageAssessments(
            String businessNo,
            String riskLevel,
            Integer assessmentStatus,
            String startTime,
            String endTime,
            Long riskDataId,
            int pageNum,
            int pageSize
    );

    AssessmentDetailVO getAssessmentDetail(Long id);

    AssessmentDetailVO executeAssessment(Long riskDataId, SecurityUser operator);
}
