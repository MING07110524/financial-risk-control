package com.cmj.risk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cmj.risk.dto.risk.RiskDataCreateDTO;
import com.cmj.risk.dto.risk.RiskDataIndexValueItemDTO;
import com.cmj.risk.exception.BusinessException;
import com.cmj.risk.security.SecurityUser;
import com.cmj.risk.service.RiskDataService;
import com.cmj.risk.service.RiskIndexService;
import com.cmj.risk.vo.risk.RiskDataDetailVO;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RiskDataServiceIntegrationTests {

    @Autowired
    private RiskDataService riskDataService;

    @Autowired
    private RiskIndexService riskIndexService;

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
