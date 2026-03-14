package com.cmj.risk;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AssistantControllerIntegrationTests {

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
    void loggedInUserShouldReceiveDisabledMessageOnAssistantQuery() throws Exception {
        mockMvc.perform(post("/api/assistant/query")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken("risk-demo", "demo"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("prompt", "帮我总结当前预警"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(50100))
                .andExpect(jsonPath("$.message").value("AI assistant is disabled in V1"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void loggedInUserShouldReceiveDisabledMessageOnAssistantAction() throws Exception {
        mockMvc.perform(post("/api/assistant/action")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken("admin-demo", "demo"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("action", "reassess-latest"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(50100))
                .andExpect(jsonPath("$.message").value("AI assistant is disabled in V1"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void anonymousUserShouldReceiveUnauthorizedOnAssistantQuery() throws Exception {
        mockMvc.perform(post("/api/assistant/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("prompt", "hello"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100))
                .andExpect(jsonPath("$.message").value("Unauthorized"));
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
