package com.cmj.risk.entity.system;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class SystemUserWithRoleDO {
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String phone;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long roleId;
    private String roleName;
    private String roleCode;
}
