package com.cmj.risk;

import static org.assertj.core.api.Assertions.assertThat;

import com.cmj.risk.dto.auth.LoginRequestDTO;
import com.cmj.risk.security.JwtTokenProvider;
import com.cmj.risk.security.SecurityUser;
import com.cmj.risk.service.AuthService;
import com.cmj.risk.vo.auth.CurrentUserVO;
import com.cmj.risk.vo.auth.LoginUserVO;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AuthServiceIntegrationTests {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

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
}
