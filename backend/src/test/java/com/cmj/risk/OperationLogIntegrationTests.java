package com.cmj.risk;

import static org.assertj.core.api.Assertions.assertThat;

import com.cmj.risk.common.PageResult;
import com.cmj.risk.dto.risk.RiskIndexCreateDTO;
import com.cmj.risk.dto.risk.RiskIndexStatusDTO;
import com.cmj.risk.dto.risk.RiskIndexUpdateDTO;
import com.cmj.risk.dto.risk.RiskDataCreateDTO;
import com.cmj.risk.dto.risk.RiskDataIndexValueItemDTO;
import com.cmj.risk.dto.risk.RiskRuleCreateDTO;
import com.cmj.risk.dto.risk.RiskRuleUpdateDTO;
import com.cmj.risk.mapper.system.SystemLogMapper;
import com.cmj.risk.security.SecurityUser;
import com.cmj.risk.service.LogService;
import com.cmj.risk.service.RiskDataService;
import com.cmj.risk.service.RiskIndexService;
import com.cmj.risk.vo.system.LogVO;
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
class OperationLogIntegrationTests {

    @Autowired
    private LogService logService;

    @Autowired
    private RiskIndexService riskIndexService;

    @Autowired
    private RiskDataService riskDataService;

    @Autowired
    private SystemLogMapper systemLogMapper;

    @BeforeEach
    void setUp() {
        setCurrentUser(adminUser());
    }

    @AfterEach
    void tearDown() {
        clearCurrentUser();
    }

    @Test
    void listLogsShouldReturnPagedLogs() {
        logService.createLog("风险数据", "新增", "新增风险数据 FR001", "risk-demo", 2L);
        logService.createLog("预警", "处理", "处理预警 WRN001", "risk-demo", 2L);

        PageResult<LogVO> firstPage = logService.pageLogs(null, null, null, null, null, 1, 1);
        PageResult<LogVO> secondPage = logService.pageLogs(null, null, null, null, null, 2, 1);

        assertThat(firstPage.getTotal()).isEqualTo(2);
        assertThat(firstPage.getRecords()).hasSize(1);
        assertThat(secondPage.getRecords()).hasSize(1);
        assertThat(systemLogMapper.countLogs(null, null, null, null, null)).isEqualTo(2L);
    }

    @Test
    void listLogsWithFiltersShouldWork() {
        logService.createLog("测试模块", "新增", "新增测试数据 TS001", "test-user", null);
        logService.createLog("其他模块", "新增", "新增测试数据 OTHER001", "other-user", null);

        PageResult<LogVO> result = logService.pageLogs("测试模块", "新增", "test", null, null, 1, 10);

        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getRecords()).singleElement().satisfies(item -> {
            assertThat(item.getModuleName()).isEqualTo("测试模块");
            assertThat(item.getOperator()).isEqualTo("test-user");
        });
        assertThat(systemLogMapper.listLogs("测试模块", "新增", "test", null, null, 0, 10)).hasSize(1);
    }

    @Test
    void createRiskDataShouldGenerateLog() {
        logService.createLog("风险数据", "新增", "新增风险数据 FR001", "risk-demo", 2L);
        
        PageResult<LogVO> result = logService.pageLogs("风险数据", "新增", null, null, null, 1, 10);
        
        assertThat(result.getRecords()).isNotEmpty();
    }

    @Test
    void handleWarningShouldGenerateLog() {
        logService.createLog("预警", "处理", "处理预警 WRN001", "risk-demo", 2L);
        
        PageResult<LogVO> result = logService.pageLogs("预警", "处理", null, null, null, 1, 10);

        assertThat(result.getRecords()).isNotEmpty();
    }

    @Test
    void createRiskIndexShouldGenerateIndicatorRuleLog() {
        RiskIndexCreateDTO dto = new RiskIndexCreateDTO();
        dto.setIndexName("征信稳定性");
        dto.setIndexCode("CREDIT_STABILITY");
        dto.setWeightValue(new BigDecimal("10.00"));
        dto.setIndexDesc("用于验证指标新增日志");
        dto.setStatus(0);

        riskIndexService.createRiskIndex(dto);

        assertLatestLog("新增", "指标 征信稳定性(CREDIT_STABILITY)");
        assertLatestOperator("新增", "指标 征信稳定性(CREDIT_STABILITY)", "admin-demo", 1L);
    }

    @Test
    void updateRiskIndexShouldGenerateIndicatorRuleLog() {
        RiskIndexUpdateDTO dto = new RiskIndexUpdateDTO();
        dto.setIndexName("负债率-日志验证");
        dto.setIndexCode("DEBT_RATIO");
        dto.setWeightValue(new BigDecimal("30.00"));
        dto.setIndexDesc("用于验证指标编辑日志");

        riskIndexService.updateRiskIndex(1L, dto);

        assertLatestLog("编辑", "指标 负债率-日志验证(DEBT_RATIO)");
        assertLatestOperator("编辑", "指标 负债率-日志验证(DEBT_RATIO)", "admin-demo", 1L);
    }

    @Test
    void updateRiskIndexStatusShouldGenerateEnableAndDisableLogs() {
        RiskIndexStatusDTO disableDTO = new RiskIndexStatusDTO();
        disableDTO.setStatus(0);
        riskIndexService.updateRiskIndexStatus(1L, disableDTO);
        assertLatestLog("停用", "指标 负债率(DEBT_RATIO)");
        assertLatestOperator("停用", "指标 负债率(DEBT_RATIO)", "admin-demo", 1L);

        RiskIndexStatusDTO enableDTO = new RiskIndexStatusDTO();
        enableDTO.setStatus(1);
        riskIndexService.updateRiskIndexStatus(1L, enableDTO);
        assertLatestLog("启用", "指标 负债率(DEBT_RATIO)");
        assertLatestOperator("启用", "指标 负债率(DEBT_RATIO)", "admin-demo", 1L);
    }

    @Test
    void createRiskRuleShouldGenerateIndicatorRuleLog() {
        RiskIndexCreateDTO indexDTO = new RiskIndexCreateDTO();
        indexDTO.setIndexName("日志规则指标");
        indexDTO.setIndexCode("LOG_RULE_INDEX");
        indexDTO.setWeightValue(new BigDecimal("5.00"));
        indexDTO.setIndexDesc("用于验证规则新增日志");
        indexDTO.setStatus(0);

        Long indexId = riskIndexService.createRiskIndex(indexDTO).getId();

        RiskRuleCreateDTO dto = new RiskRuleCreateDTO();
        dto.setIndexId(indexId);
        dto.setScoreMin(new BigDecimal("0"));
        dto.setScoreMax(new BigDecimal("120"));
        dto.setScoreValue(new BigDecimal("25"));
        dto.setWarningLevel("HIGH");

        riskIndexService.createRiskRule(dto);

        assertLatestLog("新增", "规则 日志规则指标 [0, 120] -> HIGH");
        assertLatestOperator("新增", "规则 日志规则指标 [0, 120] -> HIGH", "admin-demo", 1L);
    }

    @Test
    void updateRiskRuleShouldGenerateIndicatorRuleLog() {
        RiskRuleUpdateDTO dto = new RiskRuleUpdateDTO();
        dto.setScoreMin(new BigDecimal("0"));
        dto.setScoreMax(new BigDecimal("19.99"));
        dto.setScoreValue(new BigDecimal("3"));
        dto.setWarningLevel("LOW");

        riskIndexService.updateRiskRule(1L, dto);

        assertLatestLog("编辑", "规则 负债率 [0, 19.99] -> LOW");
        assertLatestOperator("编辑", "规则 负债率 [0, 19.99] -> LOW", "admin-demo", 1L);
    }

    @Test
    void deleteRiskRuleShouldGenerateIndicatorRuleLog() {
        riskIndexService.deleteRiskRule(2L);

        assertLatestLog("删除", "规则 负债率 [40.01, 70] -> MEDIUM");
        assertLatestOperator("删除", "规则 负债率 [40.01, 70] -> MEDIUM", "admin-demo", 1L);
    }

    @Test
    void deleteRiskDataShouldGenerateRealOperatorLog() {
        RiskDataCreateDTO dto = new RiskDataCreateDTO();
        dto.setBusinessNo("FRC-LOG-DELETE-001");
        dto.setCustomerName("日志删除验证企业");
        dto.setBusinessType("流贷");
        dto.setRiskDesc("用于验证真实删除操作人日志");
        dto.setIndexValues(List.of(
                item(1L, "35"),
                item(2L, "1.8"),
                item(3L, "0"),
                item(4L, "180")
        ));

        Long riskDataId = riskDataService.createRiskData(dto, riskUser()).getId();
        riskDataService.deleteRiskData(riskDataId, riskUser());

        assertLatestOperator("删除", "删除风险数据 FRC-LOG-DELETE-001", "risk-demo", 2L);
    }

    private void assertLatestLog(String operationType, String descSnippet) {
        PageResult<LogVO> result = logService.pageLogs(null, operationType, null, null, null, 1, 20);

        assertThat(result.getRecords()).isNotEmpty();
        assertThat(result.getRecords())
                .anySatisfy(item -> {
                    assertThat(item.getOperationType()).isEqualTo(operationType);
                    assertThat(item.getOperationDesc()).contains(descSnippet);
                });
    }

    private void assertLatestOperator(String operationType, String descSnippet, String operator, Long operatorId) {
        PageResult<LogVO> result = logService.pageLogs(null, operationType, null, null, null, 1, 20);

        assertThat(result.getRecords())
                .anySatisfy(item -> {
                    assertThat(item.getOperationType()).isEqualTo(operationType);
                    assertThat(item.getOperationDesc()).contains(descSnippet);
                    assertThat(item.getOperator()).isEqualTo(operator);
                    assertThat(item.getOperatorId()).isEqualTo(operatorId);
                });
    }

    private RiskDataIndexValueItemDTO item(Long indexId, String value) {
        RiskDataIndexValueItemDTO itemDTO = new RiskDataIndexValueItemDTO();
        itemDTO.setIndexId(indexId);
        itemDTO.setIndexValue(new BigDecimal(value));
        return itemDTO;
    }

    private void setCurrentUser(SecurityUser securityUser) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities()));
    }

    private void clearCurrentUser() {
        SecurityContextHolder.clearContext();
    }

    private SecurityUser adminUser() {
        return securityUser(1L, "admin-demo", "演示管理员", "ADMIN", "系统管理员");
    }

    private SecurityUser riskUser() {
        return securityUser(2L, "risk-demo", "演示风控员", "RISK_USER", "风控人员");
    }

    private SecurityUser securityUser(Long userId, String username, String realName, String roleCode, String roleName) {
        return SecurityUser.builder()
                .userId(userId)
                .username(username)
                .password("")
                .realName(realName)
                .roleCode(roleCode)
                .roleName(roleName)
                .enabled(true)
                .build();
    }
}
