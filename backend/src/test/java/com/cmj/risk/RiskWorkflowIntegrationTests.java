package com.cmj.risk;

import static org.assertj.core.api.Assertions.assertThat;

import com.cmj.risk.security.SecurityUser;
import com.cmj.risk.service.AssessmentService;
import com.cmj.risk.service.StatisticsService;
import com.cmj.risk.service.WarningService;
import com.cmj.risk.vo.assessment.AssessmentDetailVO;
import com.cmj.risk.vo.statistics.DashboardStatisticsVO;
import com.cmj.risk.vo.warning.WarningVO;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RiskWorkflowIntegrationTests {

    @Autowired
    private AssessmentService assessmentService;

    @Autowired
    private WarningService warningService;

    @Autowired
    private StatisticsService statisticsService;

    @Test
    void executeAssessmentShouldGenerateWarningAndAffectStatistics() {
        DashboardStatisticsVO before = statisticsService.getDashboardStatistics();

        AssessmentDetailVO assessmentDetailVO = assessmentService.executeAssessment(1L, demoRiskUser());
        assertThat(assessmentDetailVO.getWarningGenerated()).isTrue();
        assertThat(assessmentDetailVO.getWarningInfo()).isNotNull();

        Long warningId = assessmentDetailVO.getWarningInfo().getWarningId();
        warningService.handleWarning(warningId, "完成首次核查", "已安排后续跟进", 2, demoRiskUser());

        List<WarningVO> warnings = warningService.listRecentWarnings(5);
        assertThat(warnings.stream().anyMatch(item -> item.getId().equals(warningId) && item.getWarningStatus().equals(2))).isTrue();

        DashboardStatisticsVO after = statisticsService.getDashboardStatistics();
        assertThat(after.getAssessmentCount()).isGreaterThan(before.getAssessmentCount());
        assertThat(after.getHandledWarningCount()).isGreaterThanOrEqualTo(before.getHandledWarningCount());
    }

    private SecurityUser demoRiskUser() {
        return SecurityUser.builder()
                .userId(2L)
                .username("risk-demo")
                .password("")
                .realName("演示风控员")
                .roleCode("RISK_USER")
                .roleName("风控人员")
                .enabled(true)
                .build();
    }
}
