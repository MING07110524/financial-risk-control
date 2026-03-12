package com.cmj.risk.controller;

import com.cmj.risk.common.Result;
import com.cmj.risk.dto.auth.LoginRequestDTO;
import com.cmj.risk.security.SecurityUser;
import com.cmj.risk.service.AuthService;
import com.cmj.risk.vo.auth.CurrentUserVO;
import com.cmj.risk.vo.auth.LoginUserVO;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Result<LoginUserVO> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO) {
        return Result.success(authService.login(loginRequestDTO));
    }

    @PostMapping("/logout")
    public Result<Void> logout(Authentication authentication) {
        Long userId = null;
        if (authentication != null && authentication.getPrincipal() instanceof SecurityUser securityUser) {
            userId = securityUser.getUserId();
        }
        authService.logout(userId);
        return Result.success();
    }

    @GetMapping("/me")
    public Result<CurrentUserVO> me(Authentication authentication) {
        SecurityUser securityUser = authentication != null && authentication.getPrincipal() instanceof SecurityUser principal
                ? principal
                : null;
        return Result.success(authService.getCurrentUser(securityUser));
    }
}
