package com.cmj.risk.service.impl;

import com.cmj.risk.common.ErrorCode;
import com.cmj.risk.dto.auth.LoginRequestDTO;
import com.cmj.risk.exception.BusinessException;
import com.cmj.risk.security.DatabaseAuthUserDetailsService;
import com.cmj.risk.security.SecurityUser;
import com.cmj.risk.security.JwtTokenProvider;
import com.cmj.risk.service.AuthService;
import com.cmj.risk.service.LogService;
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
    private final DatabaseAuthUserDetailsService databaseAuthUserDetailsService;
    private final LogService logService;

    public AuthServiceImpl(
            AuthenticationManager authenticationManager,
            JwtTokenProvider jwtTokenProvider,
            DatabaseAuthUserDetailsService databaseAuthUserDetailsService,
            LogService logService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.databaseAuthUserDetailsService = databaseAuthUserDetailsService;
        this.logService = logService;
    }

    @Override
    public LoginUserVO login(LoginRequestDTO loginRequestDTO) {
        try {
            // Keep the existing JWT authentication flow unchanged while the
            // user source is switched from config-based demo accounts to the
            // persisted user table. / 保持现有 JWT 认证流程不变，只把用户来源从
            // 配置式 demo 账号切换为数据库持久化用户表。
            UsernamePasswordAuthenticationToken authenticationToken =
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            loginRequestDTO.getUsername(),
                            loginRequestDTO.getPassword()
                    );
            SecurityUser securityUser = (SecurityUser) authenticationManager.authenticate(authenticationToken).getPrincipal();

            logService.createLog(
                    "系统",
                    "登录",
                    "用户 " + securityUser.getUsername() + " 登录系统",
                    securityUser.getUsername(),
                    securityUser.getUserId());

            return LoginUserVO.builder()
                    .userId(securityUser.getUserId())
                    .username(securityUser.getUsername())
                    .realName(securityUser.getRealName())
                    .roleCode(securityUser.getRoleCode())
                    .roleName(securityUser.getRoleName())
                    .token(jwtTokenProvider.createAccessToken(securityUser))
                    .build();
        } catch (BadCredentialsException exception) {
            logService.createLog(
                    "系统",
                    "登录失败",
                    "用户 " + loginRequestDTO.getUsername() + " 登录失败：用户名或密码错误",
                    loginRequestDTO.getUsername(),
                    null);
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "用户名或密码错误");
        } catch (DisabledException exception) {
            logService.createLog(
                    "系统",
                    "登录失败",
                    "用户 " + loginRequestDTO.getUsername() + " 登录失败：当前账号已停用",
                    loginRequestDTO.getUsername(),
                    null);
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED, "当前账号已停用");
        }
    }

    @Override
    public void logout(SecurityUser securityUser) {
        if (securityUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Current user is not authenticated");
        }
        logService.createLog(
                "系统",
                "退出",
                "用户 " + securityUser.getUsername() + " 退出登录",
                securityUser.getUsername(),
                securityUser.getUserId());
    }

    @Override
    public CurrentUserVO getCurrentUser(SecurityUser securityUser) {
        if (securityUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Current user is not authenticated");
        }

        SecurityUser resolvedUser = databaseAuthUserDetailsService.findByUsername(securityUser.getUsername())
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
