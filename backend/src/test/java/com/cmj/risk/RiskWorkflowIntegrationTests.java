package com.cmj.risk;

import static org.assertj.core.api.Assertions.assertThat;

import com.cmj.risk.component.RiskDemoStore;
import com.cmj.risk.dto.risk.RiskDataCreateDTO;
import com.cmj.risk.dto.risk.RiskDataIndexValueItemDTO;
import com.cmj.risk.service.AssessmentService;
import com.cmj.risk.service.RiskDataService;
import com.cmj.risk.service.StatisticsService;
import com.cmj.risk.service.WarningService;
import com.cmj.risk.security.SecurityUser;
import com.cmj.risk.vo.assessment.AssessmentDetailVO;
import com.cmj.risk.vo.statistics.DashboardStatisticsVO;
import com.cmj.risk.vo.warning.WarningVO;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RiskWorkflowIntegrationTests {

    @Autowired
    private AssessmentService assessmentService;

    @Autowired
    private RiskDataService riskDataService;

    @Autowired
    private WarningService warningService;

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private RiskDemoStore riskDemoStore;

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

    @Test
    void executeAssessmentShouldAcceptLargeOutOfRangeStyleInputValues() {
        RiskDataCreateDTO dto = new RiskDataCreateDTO();
        dto.setBusinessNo("FRC-202603-999");
        dto.setCustomerName("高风险测试企业");
        dto.setBusinessType("测试授信");
        dto.setRiskDesc("使用 123 这类测试值验证高风险区间是否能正常命中。");
        dto.setIndexValues(List.of(
                item(1L, "123"),
                item(2L, "123"),
                item(3L, "123"),
                item(4L, "123")
        ));

        Long riskDataId = riskDataService.createRiskData(dto, demoRiskUser()).getId();
        AssessmentDetailVO assessmentDetailVO = assessmentService.executeAssessment(riskDataId, demoRiskUser());

        assertThat(assessmentDetailVO.getRiskLevel()).isEqualTo("MEDIUM");
        assertThat(assessmentDetailVO.getWarningGenerated()).isTrue();
    }

    @Test
    void historicalAssessmentDetailShouldNotDriftAfterRuleUpdate() {
        AssessmentDetailVO before = assessmentService.getAssessmentDetail(2L);

        riskDemoStore.updateRiskRule(2L, RiskDemoStore.RiskRuleRecord.builder()
                .scoreMin(new BigDecimal("40.01"))
                .scoreMax(new BigDecimal("70.00"))
                .scoreValue(new BigDecimal("10.00"))
                .warningLevel("LOW")
                .build());

        AssessmentDetailVO after = assessmentService.getAssessmentDetail(2L);

        assertThat(after.getTotalScore()).isEqualByComparingTo(before.getTotalScore());
        assertThat(after.getRiskLevel()).isEqualTo(before.getRiskLevel());
        assertThat(after.getIndexResults()).usingRecursiveComparison().isEqualTo(before.getIndexResults());
    }

    private RiskDataIndexValueItemDTO item(Long indexId, String value) {
        RiskDataIndexValueItemDTO itemDTO = new RiskDataIndexValueItemDTO();
        itemDTO.setIndexId(indexId);
        itemDTO.setIndexValue(new BigDecimal(value));
        return itemDTO;
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
