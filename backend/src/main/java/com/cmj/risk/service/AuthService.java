package com.cmj.risk.service;

import com.cmj.risk.dto.auth.LoginRequestDTO;
import com.cmj.risk.security.SecurityUser;
import com.cmj.risk.vo.auth.CurrentUserVO;
import com.cmj.risk.vo.auth.LoginUserVO;

public interface AuthService {
    LoginUserVO login(LoginRequestDTO loginRequestDTO);

    void logout(SecurityUser securityUser);

    CurrentUserVO getCurrentUser(SecurityUser securityUser);
}
