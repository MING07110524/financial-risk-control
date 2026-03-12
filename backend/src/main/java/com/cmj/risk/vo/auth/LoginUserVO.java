package com.cmj.risk.vo.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginUserVO {
    private Long userId;
    private String username;
    private String realName;
    private String roleCode;
    private String roleName;
    private String token;
}
