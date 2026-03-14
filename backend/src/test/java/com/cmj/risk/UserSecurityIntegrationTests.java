package com.cmj.risk;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserSecurityIntegrationTests {

    @Autowired
    private WebApplicationContext webApplicationContext;

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
    void adminShouldNotDisableSelf() throws Exception {
        mockMvc.perform(put("/api/users/1/status")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken("admin-demo", "demo"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", 0))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40000))
                .andExpect(jsonPath("$.message").value("不能停用当前登录用户"));
    }

    @Test
    void adminShouldNotDeleteSelf() throws Exception {
        mockMvc.perform(delete("/api/users/1")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken("admin-demo", "demo")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40000))
                .andExpect(jsonPath("$.message").value("不能删除当前登录用户"));
    }

    @Test
    void anonymousShouldReceiveUnauthorizedOnSensitiveWriteInterface() throws Exception {
        mockMvc.perform(put("/api/users/2/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", 0))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100))
                .andExpect(jsonPath("$.message").value("Unauthorized"));
    }

    @Test
    void nonAdminShouldReceiveForbiddenOnUserManagementInterface() throws Exception {
        mockMvc.perform(delete("/api/users/1")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken("risk-demo", "demo")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300))
                .andExpect(jsonPath("$.message").value("Forbidden"));
    }

    @Test
    void adminShouldBeAbleToDisableOtherUser() throws Exception {
        mockMvc.perform(put("/api/users/2/status")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken("admin-demo", "demo"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", 0))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.status").value(0));
    }

    @Test
    void adminShouldBeAbleToDeleteOtherUser() throws Exception {
        mockMvc.perform(delete("/api/users/2")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken("admin-demo", "demo")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("success"));
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
