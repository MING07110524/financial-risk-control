package com.cmj.risk.mapper.system;

import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SystemUserRoleMapper {

    @Select("""
            SELECT role_id
            FROM sys_user_role
            WHERE user_id = #{userId}
            ORDER BY role_id
            """)
    List<Long> listRoleIdsByUserId(@Param("userId") Long userId);

    @Insert("""
            INSERT INTO sys_user_role (user_id, role_id)
            VALUES (#{userId}, #{roleId})
            """)
    void insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    @Delete("""
            DELETE FROM sys_user_role
            WHERE user_id = #{userId}
            """)
    void deleteByUserId(@Param("userId") Long userId);
}
