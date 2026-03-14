package com.cmj.risk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cmj.risk.common.PageResult;
import com.cmj.risk.mapper.system.SystemLogMapper;
import com.cmj.risk.service.LogService;
import com.cmj.risk.vo.system.LogVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuthLogIntegrationTests {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private LogService logService;

    @Autowired
    private SystemLogMapper systemLogMapper;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void loginSuccessShouldGenerateSystemLoginLog() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "admin-demo",
                                "password", "demo"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        assertSystemLog("登录", "用户 admin-demo 登录系统", "admin-demo", 1L);
    }

    @Test
    void loginFailureShouldGenerateSystemLoginFailureLog() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "admin-demo",
                                "password", "wrong-password"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(41001))
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));

        assertSystemLog("登录失败", "用户名或密码错误", "admin-demo", null);
        assertThat(systemLogMapper.listLogs("系统", "登录失败", "admin-demo", null, null, 0, 20))
                .anySatisfy(item -> {
                    assertThat(item.getUserId()).isNull();
                    assertThat(item.getOperator()).isEqualTo("admin-demo");
                });
    }

    @Test
    void logoutShouldGenerateSystemLogoutLog() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken("risk-demo", "demo")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        assertSystemLog("退出", "用户 risk-demo 退出登录", "risk-demo", 2L);
    }

    private void assertSystemLog(String operationType, String descSnippet, String operator, Long operatorId) {
        PageResult<LogVO> result = logService.pageLogs("系统", operationType, operator, null, null, 1, 20);

        assertThat(result.getRecords())
                .anySatisfy(item -> {
                    assertThat(item.getModuleName()).isEqualTo("系统");
                    assertThat(item.getOperationType()).isEqualTo(operationType);
                    assertThat(item.getOperationDesc()).contains(descSnippet);
                    assertThat(item.getOperator()).isEqualTo(operator);
                    assertThat(item.getOperatorId()).isEqualTo(operatorId);
                });
    }

    private String bearerToken(String username, String password) throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", username,
                                "password", password))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();

        JsonNode body = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        return "Bearer " + body.path("data").path("token").asText();
    }
}
