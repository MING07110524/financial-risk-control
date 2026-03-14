package com.cmj.risk.mapper.system;

import com.cmj.risk.entity.system.SystemLogDO;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SystemLogMapper {

    @Insert("""
            INSERT INTO sys_log (user_id, operator, module_name, operation_type, operation_desc)
            VALUES (#{userId}, #{operator}, #{moduleName}, #{operationType}, #{operationDesc})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertLog(SystemLogDO log);

    @Select({
            "<script>",
            "SELECT COUNT(1)",
            "FROM sys_log l",
            "<where>",
            "  <if test='moduleName != null and moduleName != \"\"'>",
            "    AND LOWER(l.module_name) LIKE CONCAT('%', LOWER(#{moduleName}), '%')",
            "  </if>",
            "  <if test='operationType != null and operationType != \"\"'>",
            "    AND LOWER(l.operation_type) = LOWER(#{operationType})",
            "  </if>",
            "  <if test='operator != null and operator != \"\"'>",
            "    AND LOWER(COALESCE(l.operator, '')) LIKE CONCAT('%', LOWER(#{operator}), '%')",
            "  </if>",
            "  <if test='startTime != null and startTime != \"\"'>",
            "    AND CAST(l.operation_time AS CHAR(19)) &gt;= #{startTime}",
            "  </if>",
            "  <if test='endTime != null and endTime != \"\"'>",
            "    AND CAST(l.operation_time AS CHAR(19)) &lt;= #{endTime}",
            "  </if>",
            "</where>",
            "</script>"
    })
    long countLogs(
            @Param("moduleName") String moduleName,
            @Param("operationType") String operationType,
            @Param("operator") String operator,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime
    );

    @Select({
            "<script>",
            "SELECT l.id, l.user_id, l.operator, l.module_name, l.operation_type, l.operation_desc, l.operation_time",
            "FROM sys_log l",
            "<where>",
            "  <if test='moduleName != null and moduleName != \"\"'>",
            "    AND LOWER(l.module_name) LIKE CONCAT('%', LOWER(#{moduleName}), '%')",
            "  </if>",
            "  <if test='operationType != null and operationType != \"\"'>",
            "    AND LOWER(l.operation_type) = LOWER(#{operationType})",
            "  </if>",
            "  <if test='operator != null and operator != \"\"'>",
            "    AND LOWER(COALESCE(l.operator, '')) LIKE CONCAT('%', LOWER(#{operator}), '%')",
            "  </if>",
            "  <if test='startTime != null and startTime != \"\"'>",
            "    AND CAST(l.operation_time AS CHAR(19)) &gt;= #{startTime}",
            "  </if>",
            "  <if test='endTime != null and endTime != \"\"'>",
            "    AND CAST(l.operation_time AS CHAR(19)) &lt;= #{endTime}",
            "  </if>",
            "</where>",
            "ORDER BY l.id DESC",
            "LIMIT #{limit} OFFSET #{offset}",
            "</script>"
    })
    List<SystemLogDO> listLogs(
            @Param("moduleName") String moduleName,
            @Param("operationType") String operationType,
            @Param("operator") String operator,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime,
            @Param("offset") int offset,
            @Param("limit") int limit
    );
}
