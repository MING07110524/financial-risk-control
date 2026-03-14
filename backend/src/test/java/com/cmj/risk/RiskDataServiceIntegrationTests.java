package com.cmj.risk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cmj.risk.dto.risk.RiskDataCreateDTO;
import com.cmj.risk.dto.risk.RiskDataIndexValueItemDTO;
import com.cmj.risk.dto.risk.RiskDataUpdateDTO;
import com.cmj.risk.dto.risk.RiskIndexCreateDTO;
import com.cmj.risk.dto.risk.RiskIndexStatusDTO;
import com.cmj.risk.dto.risk.RiskRuleCreateDTO;
import com.cmj.risk.exception.BusinessException;
import com.cmj.risk.security.SecurityUser;
import com.cmj.risk.service.RiskDataService;
import com.cmj.risk.service.RiskIndexService;
import com.cmj.risk.vo.risk.RiskDataDetailVO;
import com.cmj.risk.vo.risk.RiskIndexVO;
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
class RiskDataServiceIntegrationTests {

    @Autowired
    private RiskDataService riskDataService;

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
    void createRiskDataShouldPersistDetailValuesInBackendStore() {
        RiskDataCreateDTO dto = new RiskDataCreateDTO();
        dto.setBusinessNo("FRC-202603-900");
        dto.setCustomerName("后端真实接口测试企业");
        dto.setBusinessType("测试贷款");
        dto.setRiskDesc("用于验证 B2 风险数据真实接口。");
        dto.setIndexValues(List.of(
                item(1L, "55"),
                item(2L, "1.45"),
                item(3L, "1"),
                item(4L, "132")
        ));

        RiskDataDetailVO detailVO = riskDataService.createRiskData(dto, demoRiskUser());

        assertThat(detailVO.getBusinessNo()).isEqualTo("FRC-202603-900");
        assertThat(detailVO.getIndexValues()).hasSize(4);
        assertThat(detailVO.getDataStatus()).isEqualTo(0);
    }

    @Test
    void duplicateBusinessNoShouldBeRejected() {
        RiskDataCreateDTO dto = new RiskDataCreateDTO();
        dto.setBusinessNo("FRC-202603-001");
        dto.setCustomerName("重复编号测试企业");
        dto.setBusinessType("测试贷款");
        dto.setRiskDesc("用于验证编号冲突。");
        dto.setIndexValues(List.of(
                item(1L, "55"),
                item(2L, "1.45"),
                item(3L, "1"),
                item(4L, "132")
        ));

        assertThatThrownBy(() -> riskDataService.createRiskData(dto, demoRiskUser()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("业务编号已存在");
    }

    @Test
    void riskIndexesShouldExposeFourEnabledIndexesAndRules() {
        assertThat(riskIndexService.listRiskIndexes(null, 1)).hasSize(4);
        assertThat(riskIndexService.listRiskRules(1L)).hasSize(3);
    }

    @Test
    void backfillShouldChangeRiskDataStatusToPendingWhenRecordHasNoHistory() {
        disableExistingIndexToFreeWeight();

        RiskIndexCreateDTO indexCreateDTO = new RiskIndexCreateDTO();
        indexCreateDTO.setIndexName("舆情风险");
        indexCreateDTO.setIndexCode("PUBLIC_OPINION_BACKFILL");
        indexCreateDTO.setWeightValue(new BigDecimal("10.00"));
        indexCreateDTO.setIndexDesc("用于验证待补录回收。");
        indexCreateDTO.setStatus(0);
        RiskIndexVO createdIndex = riskIndexService.createRiskIndex(indexCreateDTO);
        riskIndexService.createRiskRule(rule(createdIndex.getId(), "0", "999", "20", "LOW"));

        RiskIndexStatusDTO statusDTO = new RiskIndexStatusDTO();
        statusDTO.setStatus(1);
        riskIndexService.updateRiskIndexStatus(createdIndex.getId(), statusDTO);

        RiskDataDetailVO before = riskDataService.getRiskDataDetail(1L);
        assertThat(before.getDataStatus()).isEqualTo(3);

        RiskDataUpdateDTO updateDTO = updatePayload(before, List.of(
                item(1L, "68"),
                item(2L, "1.25"),
                item(3L, "1"),
                item(createdIndex.getId(), "22")
        ));

        RiskDataDetailVO after = riskDataService.updateRiskData(1L, updateDTO, demoRiskUser());
        assertThat(after.getDataStatus()).isEqualTo(0);
        assertThat(after.getMissingEnabledIndexNames()).isEmpty();
    }

    @Test
    void backfillShouldChangeRiskDataStatusToReassessmentWhenRecordHasHistory() {
        disableExistingIndexToFreeWeight();

        RiskIndexCreateDTO indexCreateDTO = new RiskIndexCreateDTO();
        indexCreateDTO.setIndexName("舆情风险");
        indexCreateDTO.setIndexCode("PUBLIC_OPINION_BACKFILL_HISTORY");
        indexCreateDTO.setWeightValue(new BigDecimal("10.00"));
        indexCreateDTO.setIndexDesc("用于验证有历史业务补录回收。");
        indexCreateDTO.setStatus(0);
        RiskIndexVO createdIndex = riskIndexService.createRiskIndex(indexCreateDTO);
        riskIndexService.createRiskRule(rule(createdIndex.getId(), "0", "999", "20", "LOW"));

        RiskIndexStatusDTO statusDTO = new RiskIndexStatusDTO();
        statusDTO.setStatus(1);
        riskIndexService.updateRiskIndexStatus(createdIndex.getId(), statusDTO);

        RiskDataDetailVO before = riskDataService.getRiskDataDetail(2L);
        assertThat(before.getDataStatus()).isEqualTo(3);

        RiskDataUpdateDTO updateDTO = updatePayload(before, List.of(
                item(1L, "35"),
                item(2L, "1.8"),
                item(3L, "0"),
                item(createdIndex.getId(), "15")
        ));

        RiskDataDetailVO after = riskDataService.updateRiskData(2L, updateDTO, demoRiskUser());
        assertThat(after.getDataStatus()).isEqualTo(2);
        assertThat(after.getMissingEnabledIndexNames()).isEmpty();
    }

    private RiskDataUpdateDTO updatePayload(
            RiskDataDetailVO detailVO,
            List<RiskDataIndexValueItemDTO> items
    ) {
        RiskDataUpdateDTO updateDTO = new RiskDataUpdateDTO();
        updateDTO.setCustomerName(detailVO.getCustomerName());
        updateDTO.setBusinessType(detailVO.getBusinessType());
        updateDTO.setRiskDesc(detailVO.getRiskDesc());
        updateDTO.setIndexValues(items);
        return updateDTO;
    }

    private void disableExistingIndexToFreeWeight() {
        RiskIndexStatusDTO statusDTO = new RiskIndexStatusDTO();
        statusDTO.setStatus(0);
        riskIndexService.updateRiskIndexStatus(4L, statusDTO);
    }

    private RiskDataIndexValueItemDTO item(Long indexId, String value) {
        RiskDataIndexValueItemDTO itemDTO = new RiskDataIndexValueItemDTO();
        itemDTO.setIndexId(indexId);
        itemDTO.setIndexValue(new BigDecimal(value));
        return itemDTO;
    }

    private RiskRuleCreateDTO rule(Long indexId, String scoreMin, String scoreMax, String scoreValue, String warningLevel) {
        RiskRuleCreateDTO dto = new RiskRuleCreateDTO();
        dto.setIndexId(indexId);
        dto.setScoreMin(new BigDecimal(scoreMin));
        dto.setScoreMax(new BigDecimal(scoreMax));
        dto.setScoreValue(new BigDecimal(scoreValue));
        dto.setWarningLevel(warningLevel);
        return dto;
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
