package com.cmj.risk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cmj.risk.dto.system.UserStatusDTO;
import com.cmj.risk.exception.BusinessException;
import com.cmj.risk.dto.auth.LoginRequestDTO;
import com.cmj.risk.security.JwtTokenProvider;
import com.cmj.risk.security.SecurityUser;
import com.cmj.risk.service.AuthService;
import com.cmj.risk.service.UserService;
import com.cmj.risk.vo.auth.CurrentUserVO;
import com.cmj.risk.vo.auth.LoginUserVO;
import java.util.Optional;
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
class AuthServiceIntegrationTests {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserService userService;

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
    void loginShouldReturnJwtTokenForDemoRiskUser() {
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setUsername("risk-demo");
        loginRequestDTO.setPassword("demo");

        LoginUserVO loginUserVO = authService.login(loginRequestDTO);

        assertThat(loginUserVO.getUsername()).isEqualTo("risk-demo");
        assertThat(loginUserVO.getRoleCode()).isEqualTo("RISK_USER");
        assertThat(loginUserVO.getToken()).isNotBlank();
    }

    @Test
    void getCurrentUserShouldResolveRoleAndRealNameFromToken() {
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setUsername("manager-demo");
        loginRequestDTO.setPassword("demo");

        LoginUserVO loginUserVO = authService.login(loginRequestDTO);
        Optional<SecurityUser> securityUser = jwtTokenProvider.parseToken(loginUserVO.getToken());

        assertThat(securityUser).isPresent();

        CurrentUserVO currentUserVO = authService.getCurrentUser(securityUser.orElseThrow());

        assertThat(currentUserVO.getUsername()).isEqualTo("manager-demo");
        assertThat(currentUserVO.getRoleCode()).isEqualTo("MANAGER");
        assertThat(currentUserVO.getRealName()).isEqualTo("演示管理者");
    }

    @Test
    void disabledUserShouldNotBeAbleToLogin() {
        UserStatusDTO statusDTO = new UserStatusDTO();
        statusDTO.setStatus(0);
        userService.updateUserStatus(2L, statusDTO);

        LoginRequestDTO loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setUsername("risk-demo");
        loginRequestDTO.setPassword("demo");

        assertThatThrownBy(() -> authService.login(loginRequestDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("当前账号已停用");
    }
}
