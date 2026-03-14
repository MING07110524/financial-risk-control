package com.cmj.risk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cmj.risk.common.ErrorCode;
import com.cmj.risk.dto.risk.RiskIndexCreateDTO;
import com.cmj.risk.dto.risk.RiskIndexStatusDTO;
import com.cmj.risk.dto.risk.RiskRuleCreateDTO;
import com.cmj.risk.dto.risk.RiskRuleUpdateDTO;
import com.cmj.risk.exception.BusinessException;
import com.cmj.risk.security.SecurityUser;
import com.cmj.risk.service.RiskIndexService;
import com.cmj.risk.vo.risk.RiskIndexVO;
import com.cmj.risk.vo.risk.RiskRuleVO;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RiskRuleManagementIntegrationTests {

    @Autowired
    private RiskIndexService riskIndexService;

    @BeforeEach
    void setUp() {
        SecurityUser adminUser = SecurityUser.builder()
                .userId(1L)
                .username("admin-demo")
                .password("")
                .realName("演示管理员")
                .roleCode("ADMIN")
                .roleName("系统管理员")
                .enabled(true)
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(adminUser, null, adminUser.getAuthorities()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createRiskRuleShouldRejectRangeOverlapInSameIndex() {
        RiskRuleCreateDTO dto = new RiskRuleCreateDTO();
        dto.setIndexId(1L);
        dto.setScoreMin(new BigDecimal("30"));
        dto.setScoreMax(new BigDecimal("50"));
        dto.setScoreValue(new BigDecimal("10"));
        dto.setWarningLevel("LOW");

        assertThatThrownBy(() -> riskIndexService.createRiskRule(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("区间不能重叠")
                .extracting("code")
                .isEqualTo(ErrorCode.RULE_RANGE_CONFLICT.getCode());
    }

    @Test
    void enableRiskIndexShouldRejectWhenIndexHasNoRules() {
        RiskIndexVO createdIndex = createDraftIndex("RULE_EMPTY_TEST", new BigDecimal("10.00"));

        RiskIndexStatusDTO statusDTO = new RiskIndexStatusDTO();
        statusDTO.setStatus(1);

        assertThatThrownBy(() -> riskIndexService.updateRiskIndexStatus(createdIndex.getId(), statusDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("无法启用")
                .extracting("code")
                .isEqualTo(ErrorCode.BAD_REQUEST.getCode());
    }

    @Test
    void createUpdateDeleteRiskRuleShouldWork() {
        // Free enabled weight for enabling new indexes in this test.
        disableIndex(4L);

        RiskIndexVO createdIndex = createDraftIndex("RULE_CRUD_TEST", new BigDecimal("10.00"));

        RiskRuleVO first = riskIndexService.createRiskRule(ruleCreate(createdIndex.getId(), "0", "50", "10", "LOW"));
        RiskRuleVO second = riskIndexService.createRiskRule(ruleCreate(createdIndex.getId(), "50.01", "999", "20", "MEDIUM"));
        assertThat(first.getId()).isNotNull();
        assertThat(second.getId()).isNotNull();

        RiskRuleUpdateDTO updateDTO = new RiskRuleUpdateDTO();
        updateDTO.setScoreMin(new BigDecimal("0"));
        updateDTO.setScoreMax(new BigDecimal("49.99"));
        updateDTO.setScoreValue(new BigDecimal("12"));
        updateDTO.setWarningLevel("LOW");
        RiskRuleVO updated = riskIndexService.updateRiskRule(first.getId(), updateDTO);
        assertThat(updated.getScoreMax()).isEqualByComparingTo("49.99");
        assertThat(updated.getScoreValue()).isEqualByComparingTo("12");

        RiskIndexStatusDTO enableDTO = new RiskIndexStatusDTO();
        enableDTO.setStatus(1);
        riskIndexService.updateRiskIndexStatus(createdIndex.getId(), enableDTO);

        riskIndexService.deleteRiskRule(second.getId());
        List<RiskRuleVO> remaining = riskIndexService.listRiskRules(createdIndex.getId());
        assertThat(remaining).hasSize(1);
        assertThat(remaining.getFirst().getId()).isEqualTo(first.getId());
    }

    @Test
    void deleteRiskRuleShouldBeRejectedWhenEnabledIndexWouldHaveNoRules() {
        disableIndex(4L);

        RiskIndexVO createdIndex = createDraftIndex("RULE_DELETE_GUARD", new BigDecimal("10.00"));
        RiskRuleVO rule = riskIndexService.createRiskRule(ruleCreate(createdIndex.getId(), "0", "999", "10", "LOW"));

        RiskIndexStatusDTO enableDTO = new RiskIndexStatusDTO();
        enableDTO.setStatus(1);
        riskIndexService.updateRiskIndexStatus(createdIndex.getId(), enableDTO);

        assertThatThrownBy(() -> riskIndexService.deleteRiskRule(rule.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("至少保留一条")
                .extracting("code")
                .isEqualTo(ErrorCode.BAD_REQUEST.getCode());
    }

    private RiskIndexVO createDraftIndex(String indexCode, BigDecimal weightValue) {
        RiskIndexCreateDTO dto = new RiskIndexCreateDTO();
        dto.setIndexName("规则测试指标");
        dto.setIndexCode(indexCode);
        dto.setWeightValue(weightValue);
        dto.setIndexDesc("用于验证规则 CRUD 的最小可写能力。");
        dto.setStatus(0);
        return riskIndexService.createRiskIndex(dto);
    }

    private void disableIndex(Long id) {
        RiskIndexStatusDTO statusDTO = new RiskIndexStatusDTO();
        statusDTO.setStatus(0);
        riskIndexService.updateRiskIndexStatus(id, statusDTO);
    }

    private RiskRuleCreateDTO ruleCreate(
            Long indexId,
            String scoreMin,
            String scoreMax,
            String scoreValue,
            String warningLevel
    ) {
        RiskRuleCreateDTO dto = new RiskRuleCreateDTO();
        dto.setIndexId(indexId);
        dto.setScoreMin(new BigDecimal(scoreMin));
        dto.setScoreMax(new BigDecimal(scoreMax));
        dto.setScoreValue(new BigDecimal(scoreValue));
        dto.setWarningLevel(warningLevel);
        return dto;
    }
}
