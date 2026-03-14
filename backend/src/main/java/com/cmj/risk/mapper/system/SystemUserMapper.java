package com.cmj.risk.mapper.system;

import com.cmj.risk.entity.system.SystemUserDO;
import com.cmj.risk.entity.system.SystemUserWithRoleDO;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SystemUserMapper {

    @Select({
            "<script>",
            "SELECT u.id, u.username, u.password, u.real_name, u.phone, u.status, u.create_time, u.update_time,",
            "       r.id AS role_id, r.role_name, r.role_code",
            "FROM sys_user u",
            "LEFT JOIN sys_user_role ur ON ur.user_id = u.id",
            "LEFT JOIN sys_role r ON r.id = ur.role_id",
            "<where>",
            "  <if test='username != null and username != \"\"'>",
            "    AND LOWER(u.username) LIKE CONCAT('%', LOWER(#{username}), '%')",
            "  </if>",
            "  <if test='realName != null and realName != \"\"'>",
            "    AND LOWER(u.real_name) LIKE CONCAT('%', LOWER(#{realName}), '%')",
            "  </if>",
            "  <if test='roleCode != null and roleCode != \"\"'>",
            "    AND LOWER(r.role_code) = LOWER(#{roleCode})",
            "  </if>",
            "  <if test='status != null'>",
            "    AND u.status = #{status}",
            "  </if>",
            "</where>",
            "ORDER BY u.id",
            "</script>"
    })
    List<SystemUserWithRoleDO> listUsers(
            @Param("username") String username,
            @Param("realName") String realName,
            @Param("roleCode") String roleCode,
            @Param("status") Integer status
    );

    @Select("""
            SELECT u.id, u.username, u.password, u.real_name, u.phone, u.status, u.create_time, u.update_time,
                   r.id AS role_id, r.role_name, r.role_code
            FROM sys_user u
            LEFT JOIN sys_user_role ur ON ur.user_id = u.id
            LEFT JOIN sys_role r ON r.id = ur.role_id
            WHERE u.id = #{id}
            """)
    SystemUserWithRoleDO findUserById(@Param("id") Long id);

    @Select("""
            SELECT u.id, u.username, u.password, u.real_name, u.phone, u.status, u.create_time, u.update_time,
                   r.id AS role_id, r.role_name, r.role_code
            FROM sys_user u
            LEFT JOIN sys_user_role ur ON ur.user_id = u.id
            LEFT JOIN sys_role r ON r.id = ur.role_id
            WHERE u.username = #{username}
            """)
    SystemUserWithRoleDO findUserByUsername(@Param("username") String username);

    @Select("""
            <script>
            SELECT COUNT(1)
            FROM sys_user
            WHERE LOWER(username) = LOWER(#{username})
              <if test='excludeId != null'>
                AND id &lt;&gt; #{excludeId}
              </if>
            </script>
            """)
    int countByUsername(@Param("username") String username, @Param("excludeId") Long excludeId);

    @Insert("""
            INSERT INTO sys_user (username, password, real_name, phone, status)
            VALUES (#{username}, #{password}, #{realName}, #{phone}, #{status})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertUser(SystemUserDO user);

    @Update("""
            UPDATE sys_user
            SET username = #{username},
                password = #{password},
                real_name = #{realName},
                phone = #{phone},
                status = #{status},
                update_time = CURRENT_TIMESTAMP
            WHERE id = #{id}
            """)
    void updateUser(SystemUserDO user);

    @Update("""
            UPDATE sys_user
            SET status = #{status},
                update_time = CURRENT_TIMESTAMP
            WHERE id = #{id}
            """)
    void updateUserStatus(@Param("id") Long id, @Param("status") Integer status);

    @Delete("DELETE FROM sys_user WHERE id = #{id}")
    void deleteUser(@Param("id") Long id);
}
