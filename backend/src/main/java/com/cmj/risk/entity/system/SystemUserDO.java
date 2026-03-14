package com.cmj.risk.entity.system;

import lombok.Data;

@Data
public class SystemUserDO {
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String phone;
    private Integer status;
}
