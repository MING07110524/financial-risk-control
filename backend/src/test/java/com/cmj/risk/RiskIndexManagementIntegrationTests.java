package com.cmj.risk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cmj.risk.common.ErrorCode;
import com.cmj.risk.dto.risk.RiskIndexCreateDTO;
import com.cmj.risk.dto.risk.RiskIndexStatusDTO;
import com.cmj.risk.dto.risk.RiskIndexUpdateDTO;
import com.cmj.risk.dto.risk.RiskRuleCreateDTO;
import com.cmj.risk.exception.BusinessException;
import com.cmj.risk.security.SecurityUser;
import com.cmj.risk.service.RiskIndexService;
import com.cmj.risk.vo.risk.RiskIndexVO;
import java.math.BigDecimal;
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
class RiskIndexManagementIntegrationTests {

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
    void createRiskIndexShouldAllowDisabledDraftIndex() {
        RiskIndexCreateDTO dto = new RiskIndexCreateDTO();
        dto.setIndexName("舆情风险");
        dto.setIndexCode("PUBLIC_OPINION_RISK");
        dto.setWeightValue(new BigDecimal("10.00"));
        dto.setIndexDesc("用于验证管理员可以新增草稿指标。\n");
        dto.setStatus(0);

        RiskIndexVO created = riskIndexService.createRiskIndex(dto);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getStatus()).isEqualTo(0);
        assertThat(created.getIndexCode()).isEqualTo("PUBLIC_OPINION_RISK");
    }

    @Test
    void createRiskIndexShouldRejectDuplicateIndexCode() {
        RiskIndexCreateDTO dto = new RiskIndexCreateDTO();
        dto.setIndexName("重复编码测试");
        dto.setIndexCode("debt_ratio");
        dto.setWeightValue(new BigDecimal("10.00"));
        dto.setIndexDesc("用于验证指标编码冲突。\n");
        dto.setStatus(0);

        assertThatThrownBy(() -> riskIndexService.createRiskIndex(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("指标编码已存在")
                .extracting("code")
                .isEqualTo(ErrorCode.DUPLICATE_INDEX_CODE.getCode());
    }

    @Test
    void enableRiskIndexShouldRejectWeightOverflow() {
        RiskIndexCreateDTO createDTO = new RiskIndexCreateDTO();
        createDTO.setIndexName("新增测试指标");
        createDTO.setIndexCode("NEW_TEST_INDEX");
        createDTO.setWeightValue(new BigDecimal("15.00"));
        createDTO.setIndexDesc("先新增为停用，再尝试启用。\n");
        createDTO.setStatus(0);

        RiskIndexVO created = riskIndexService.createRiskIndex(createDTO);

        RiskRuleCreateDTO ruleCreateDTO = new RiskRuleCreateDTO();
        ruleCreateDTO.setIndexId(created.getId());
        ruleCreateDTO.setScoreMin(new BigDecimal("0"));
        ruleCreateDTO.setScoreMax(new BigDecimal("999"));
        ruleCreateDTO.setScoreValue(new BigDecimal("10"));
        ruleCreateDTO.setWarningLevel("LOW");
        riskIndexService.createRiskRule(ruleCreateDTO);

        RiskIndexStatusDTO statusDTO = new RiskIndexStatusDTO();
        statusDTO.setStatus(1);

        assertThatThrownBy(() -> riskIndexService.updateRiskIndexStatus(created.getId(), statusDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("总权重不能超过 100");
    }

    @Test
    void updateRiskIndexShouldPersistEditableFields() {
        RiskIndexUpdateDTO dto = new RiskIndexUpdateDTO();
        dto.setIndexName("负债率-已调整说明");
        dto.setIndexCode("DEBT_RATIO");
        dto.setWeightValue(new BigDecimal("30.00"));
        dto.setIndexDesc("更新后的说明");

        RiskIndexVO updated = riskIndexService.updateRiskIndex(1L, dto);

        assertThat(updated.getIndexName()).isEqualTo("负债率-已调整说明");
        assertThat(updated.getIndexDesc()).isEqualTo("更新后的说明");
    }
}
