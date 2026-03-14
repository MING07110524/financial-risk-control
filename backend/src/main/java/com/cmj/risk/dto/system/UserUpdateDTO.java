package com.cmj.risk.dto.system;

import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

@Data
public class UserUpdateDTO {
    @Size(min = 3, max = 50, message = "用户名长度需在3-50个字符之间")
    private String username;

    @Size(min = 6, max = 20, message = "密码长度需在6-20个字符之间")
    private String password;

    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    private String realName;

    @Size(max = 20, message = "手机号长度不能超过20个字符")
    private String phone;

    private List<Long> roleIds;
}
