package com.cmj.risk.mapper.system;

import com.cmj.risk.entity.system.SystemRoleDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SystemRoleMapper {

    @Select("""
            SELECT id, role_name, role_code, remark
            FROM sys_role
            ORDER BY id
            """)
    List<SystemRoleDO> listRoles();

    @Select("""
            SELECT id, role_name, role_code, remark
            FROM sys_role
            WHERE id = #{id}
            """)
    SystemRoleDO findById(@Param("id") Long id);

    @Select("""
            SELECT id, role_name, role_code, remark
            FROM sys_role
            WHERE LOWER(role_code) = LOWER(#{roleCode})
            """)
    SystemRoleDO findByCode(@Param("roleCode") String roleCode);
}
