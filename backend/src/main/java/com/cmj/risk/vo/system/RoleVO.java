package com.cmj.risk.vo.system;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleVO {
    private Long id;
    private String roleName;
    private String roleCode;
    private String remark;
}
