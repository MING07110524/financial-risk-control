package com.cmj.risk.service.impl;

import com.cmj.risk.common.ErrorCode;
import com.cmj.risk.dto.auth.LoginRequestDTO;
import com.cmj.risk.exception.BusinessException;
import com.cmj.risk.security.DemoAuthUserDetailsService;
import com.cmj.risk.security.SecurityUser;
import com.cmj.risk.security.JwtTokenProvider;
import com.cmj.risk.service.AuthService;
import com.cmj.risk.vo.auth.CurrentUserVO;
import com.cmj.risk.vo.auth.LoginUserVO;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final DemoAuthUserDetailsService demoAuthUserDetailsService;

    public AuthServiceImpl(
            AuthenticationManager authenticationManager,
            JwtTokenProvider jwtTokenProvider,
            DemoAuthUserDetailsService demoAuthUserDetailsService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.demoAuthUserDetailsService = demoAuthUserDetailsService;
    }

    @Override
    public LoginUserVO login(LoginRequestDTO loginRequestDTO) {
        try {
            // Use the standard AuthenticationManager flow so the frontend is
            // already talking to a real JWT backend, even though the current
            // user source is still the smallest possible demo registry.
            // / 这里直接走 AuthenticationManager 标准认证流程，让前端已经是在
            // 调真实 JWT 后端；只是当前用户来源先保持为最小 demo 账号注册表。
            UsernamePasswordAuthenticationToken authenticationToken =
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            loginRequestDTO.getUsername(),
                            loginRequestDTO.getPassword()
                    );
            SecurityUser securityUser = (SecurityUser) authenticationManager.authenticate(authenticationToken).getPrincipal();

            return LoginUserVO.builder()
                    .userId(securityUser.getUserId())
                    .username(securityUser.getUsername())
                    .realName(securityUser.getRealName())
                    .roleCode(securityUser.getRoleCode())
                    .roleName(securityUser.getRoleName())
                    .token(jwtTokenProvider.createAccessToken(securityUser))
                    .build();
        } catch (BadCredentialsException exception) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "用户名或密码错误");
        } catch (DisabledException exception) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED, "当前账号已停用");
        }
    }

    @Override
    public void logout(Long userId) {
        // Stateless logout placeholder for phase 1.
    }

    @Override
    public CurrentUserVO getCurrentUser(SecurityUser securityUser) {
        if (securityUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Current user is not authenticated");
        }

        SecurityUser resolvedUser = demoAuthUserDetailsService.findByUsername(securityUser.getUsername())
                .orElse(securityUser);

        return CurrentUserVO.builder()
                .userId(resolvedUser.getUserId())
                .username(resolvedUser.getUsername())
                .realName(resolvedUser.getRealName())
                .roleCode(resolvedUser.getRoleCode())
                .roleName(resolvedUser.getRoleName())
                .build();
    }
}
