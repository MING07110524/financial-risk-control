package com.cmj.risk.mapper.risk;

import com.cmj.risk.component.RiskWorkflowStore.WarningRecord;
import com.cmj.risk.entity.risk.HandleSummaryCountDO;
import com.cmj.risk.entity.risk.WarningTrendCountDO;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface RiskWarningMapper {

    @Select({
            "<script>",
            "SELECT w.id, w.assessment_id, a.risk_data_id, w.warning_code, w.warning_level, w.warning_content,",
            "       d.business_no, d.customer_name, d.business_type, d.risk_desc,",
            "       a.total_score, a.risk_level, w.warning_status,",
            "       CAST(w.create_time AS CHAR(19)) AS create_time",
            "FROM risk_warning w",
            "LEFT JOIN risk_assessment a ON a.id = w.assessment_id",
            "LEFT JOIN risk_data d ON d.id = a.risk_data_id",
            "<where>",
            "  <if test='warningCode != null and warningCode != \"\"'>",
            "    AND LOWER(w.warning_code) LIKE CONCAT('%', LOWER(#{warningCode}), '%')",
            "  </if>",
            "  <if test='warningLevel != null and warningLevel != \"\"'>",
            "    AND w.warning_level = #{warningLevel}",
            "  </if>",
            "  <if test='warningStatus != null'>",
            "    AND w.warning_status = #{warningStatus}",
            "  </if>",
            "  <if test='startTime != null and startTime != \"\"'>",
            "    AND CAST(w.create_time AS DATE) >= #{startTime}",
            "  </if>",
            "  <if test='endTime != null and endTime != \"\"'>",
            "    AND CAST(w.create_time AS DATE) &lt;= #{endTime}",
            "  </if>",
            "</where>",
            "ORDER BY w.create_time DESC, w.id DESC",
            "</script>"
    })
    List<WarningRecord> listWarnings(
            @Param("warningCode") String warningCode,
            @Param("warningLevel") String warningLevel,
            @Param("warningStatus") Integer warningStatus,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime
    );

    @Select({
            "SELECT w.id, w.assessment_id, a.risk_data_id, w.warning_code, w.warning_level, w.warning_content,",
            "       d.business_no, d.customer_name, d.business_type, d.risk_desc,",
            "       a.total_score, a.risk_level, w.warning_status,",
            "       CAST(w.create_time AS CHAR(19)) AS create_time",
            "FROM risk_warning w",
            "LEFT JOIN risk_assessment a ON a.id = w.assessment_id",
            "LEFT JOIN risk_data d ON d.id = a.risk_data_id",
            "WHERE w.id = #{id}"
    })
    WarningRecord findById(@Param("id") Long id);

    @Select("SELECT COALESCE(MAX(id), 0) FROM risk_warning")
    Long getMaxId();

    @Select("SELECT COUNT(1) FROM risk_warning w JOIN risk_assessment a ON a.id = w.assessment_id WHERE a.risk_data_id = #{riskDataId}")
    int countByRiskDataId(@Param("riskDataId") Long riskDataId);

    @Select("SELECT COUNT(1) FROM risk_warning")
    long countAll();

    @Select("SELECT COUNT(1) FROM risk_warning WHERE warning_status = 2")
    long countHandled();

    @Select({
            "SELECT w.id, w.assessment_id, a.risk_data_id, w.warning_code, w.warning_level, w.warning_content,",
            "       d.business_no, d.customer_name, d.business_type, d.risk_desc,",
            "       a.total_score, a.risk_level, w.warning_status,",
            "       CAST(w.create_time AS CHAR(19)) AS create_time",
            "FROM risk_warning w",
            "LEFT JOIN risk_assessment a ON a.id = w.assessment_id",
            "LEFT JOIN risk_data d ON d.id = a.risk_data_id",
            "WHERE w.assessment_id = #{assessmentId}"
    })
    WarningRecord findByAssessmentId(@Param("assessmentId") Long assessmentId);

    @Select({
            "SELECT w.id, w.assessment_id, a.risk_data_id, w.warning_code, w.warning_level, w.warning_content,",
            "       d.business_no, d.customer_name, d.business_type, d.risk_desc,",
            "       a.total_score, a.risk_level, w.warning_status,",
            "       CAST(w.create_time AS CHAR(19)) AS create_time",
            "FROM risk_warning w",
            "LEFT JOIN risk_assessment a ON a.id = w.assessment_id",
            "LEFT JOIN risk_data d ON d.id = a.risk_data_id",
            "ORDER BY w.create_time DESC, w.id DESC",
            "LIMIT #{limit}"
    })
    List<WarningRecord> listRecentWarnings(@Param("limit") int limit);

    @Select({
            "<script>",
            "SELECT SUBSTRING(CAST(w.create_time AS CHAR(19)), 1, 10) AS date,",
            "       COUNT(1) AS total,",
            "       SUM(CASE WHEN w.warning_status = 2 THEN 1 ELSE 0 END) AS handled,",
            "       SUM(CASE WHEN w.warning_status = 2 THEN 0 ELSE 1 END) AS pending",
            "FROM risk_warning w",
            "JOIN risk_assessment a ON a.id = w.assessment_id",
            "<where>",
            "  <if test='warningLevel != null and warningLevel != \"\"'>",
            "    AND w.warning_level = #{warningLevel}",
            "  </if>",
            "  <if test='warningStatus != null'>",
            "    AND w.warning_status = #{warningStatus}",
            "  </if>",
            "  <if test='startTime != null and startTime != \"\"'>",
            "    AND CAST(w.create_time AS DATE) >= #{startTime}",
            "  </if>",
            "  <if test='endTime != null and endTime != \"\"'>",
            "    AND CAST(w.create_time AS DATE) &lt;= #{endTime}",
            "  </if>",
            "</where>",
            "GROUP BY SUBSTRING(CAST(w.create_time AS CHAR(19)), 1, 10)",
            "ORDER BY date ASC",
            "</script>"
    })
    List<WarningTrendCountDO> statWarningTrend(
            @Param("startTime") String startTime,
            @Param("endTime") String endTime,
            @Param("warningLevel") String warningLevel,
            @Param("warningStatus") Integer warningStatus
    );

    @Select({
            "<script>",
            "SELECT w.warning_status, COUNT(1) AS count",
            "FROM risk_warning w",
            "JOIN risk_assessment a ON a.id = w.assessment_id",
            "<where>",
            "  <if test='warningLevel != null and warningLevel != \"\"'>",
            "    AND w.warning_level = #{warningLevel}",
            "  </if>",
            "  <if test='warningStatus != null'>",
            "    AND w.warning_status = #{warningStatus}",
            "  </if>",
            "  <if test='startTime != null and startTime != \"\"'>",
            "    AND CAST(w.create_time AS DATE) >= #{startTime}",
            "  </if>",
            "  <if test='endTime != null and endTime != \"\"'>",
            "    AND CAST(w.create_time AS DATE) &lt;= #{endTime}",
            "  </if>",
            "</where>",
            "GROUP BY w.warning_status",
            "</script>"
    })
    List<HandleSummaryCountDO> statHandleSummary(
            @Param("startTime") String startTime,
            @Param("endTime") String endTime,
            @Param("warningLevel") String warningLevel,
            @Param("warningStatus") Integer warningStatus
    );

    @Insert("""
            INSERT INTO risk_warning (assessment_id, warning_code, warning_level, warning_content, warning_status, create_time)
            VALUES (#{assessmentId}, #{warningCode}, #{warningLevel}, #{warningContent}, #{warningStatus}, #{createTime})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(WarningRecord record);

    @Update("""
            UPDATE risk_warning
            SET warning_status = #{warningStatus}
            WHERE id = #{id}
            """)
    void updateStatus(@Param("id") Long id, @Param("warningStatus") Integer warningStatus);

    @Delete("DELETE FROM risk_warning WHERE id = #{id}")
    void deleteById(@Param("id") Long id);
}
