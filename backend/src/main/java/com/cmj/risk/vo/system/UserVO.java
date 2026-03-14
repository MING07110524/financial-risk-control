package com.cmj.risk.vo.system;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVO {
    private Long id;
    private String username;
    private String realName;
    private String phone;
    private Integer status;
    private String roleName;
    private String roleCode;
    private List<Long> roleIds;
    private String createTime;
    private String updateTime;
}
