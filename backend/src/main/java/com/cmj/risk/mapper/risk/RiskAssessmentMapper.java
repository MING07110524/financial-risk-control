package com.cmj.risk.mapper.risk;

import com.cmj.risk.component.RiskWorkflowStore.AssessmentRecord;
import com.cmj.risk.entity.risk.RiskLevelCountDO;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface RiskAssessmentMapper {

    @Select({
            "<script>",
            "SELECT a.id, a.risk_data_id, d.business_no, d.customer_name, d.business_type, d.risk_desc,",
            "       a.total_score, a.risk_level, a.assessment_status,",
            "       CAST(a.assessment_time AS CHAR(19)) AS assessment_time,",
            "       a.assessment_by, u.real_name AS assessment_by_name,",
            "       d.data_status",
            "FROM risk_assessment a",
            "LEFT JOIN risk_data d ON d.id = a.risk_data_id",
            "LEFT JOIN sys_user u ON u.id = a.assessment_by",
            "<where>",
            "  <if test='businessNo != null and businessNo != \"\"'>",
            "    AND LOWER(d.business_no) LIKE CONCAT('%', LOWER(#{businessNo}), '%')",
            "  </if>",
            "  <if test='riskLevel != null and riskLevel != \"\"'>",
            "    AND a.risk_level = #{riskLevel}",
            "  </if>",
            "  <if test='assessmentStatus != null'>",
            "    AND a.assessment_status = #{assessmentStatus}",
            "  </if>",
            "  <if test='riskDataId != null'>",
            "    AND a.risk_data_id = #{riskDataId}",
            "  </if>",
            "  <if test='startTime != null and startTime != \"\"'>",
            "    AND CAST(a.assessment_time AS DATE) >= #{startTime}",
            "  </if>",
            "  <if test='endTime != null and endTime != \"\"'>",
            "    AND CAST(a.assessment_time AS DATE) &lt;= #{endTime}",
            "  </if>",
            "</where>",
            "ORDER BY a.assessment_time DESC, a.id DESC",
            "</script>"
    })
    List<AssessmentRecord> listAssessments(
            @Param("businessNo") String businessNo,
            @Param("riskLevel") String riskLevel,
            @Param("assessmentStatus") Integer assessmentStatus,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime,
            @Param("riskDataId") Long riskDataId
    );

    @Select({
            "SELECT a.id, a.risk_data_id, d.business_no, d.customer_name, d.business_type, d.risk_desc,",
            "       a.total_score, a.risk_level, a.assessment_status,",
            "       CAST(a.assessment_time AS CHAR(19)) AS assessment_time,",
            "       a.assessment_by, u.real_name AS assessment_by_name,",
            "       d.data_status",
            "FROM risk_assessment a",
            "LEFT JOIN risk_data d ON d.id = a.risk_data_id",
            "LEFT JOIN sys_user u ON u.id = a.assessment_by",
            "WHERE a.id = #{id}"
    })
    AssessmentRecord findById(@Param("id") Long id);

    @Select({
            "SELECT a.id, a.risk_data_id, d.business_no, d.customer_name, d.business_type, d.risk_desc,",
            "       a.total_score, a.risk_level, a.assessment_status,",
            "       CAST(a.assessment_time AS CHAR(19)) AS assessment_time,",
            "       a.assessment_by, u.real_name AS assessment_by_name,",
            "       d.data_status",
            "FROM risk_assessment a",
            "LEFT JOIN risk_data d ON d.id = a.risk_data_id",
            "LEFT JOIN sys_user u ON u.id = a.assessment_by",
            "WHERE a.risk_data_id = #{riskDataId} AND a.assessment_status = 1"
    })
    AssessmentRecord findEffectiveByRiskDataId(@Param("riskDataId") Long riskDataId);

    @Select("SELECT COALESCE(MAX(id), 0) FROM risk_assessment")
    Long getMaxId();

    @Insert("""
            INSERT INTO risk_assessment (risk_data_id, total_score, risk_level, assessment_status, assessment_time, assessment_by)
            VALUES (#{riskDataId}, #{totalScore}, #{riskLevel}, #{assessmentStatus}, #{assessmentTime}, #{assessmentBy})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(AssessmentRecord record);

    @Update("""
            UPDATE risk_assessment
            SET assessment_status = #{assessmentStatus}
            WHERE id = #{id}
            """)
    void updateStatus(@Param("id") Long id, @Param("assessmentStatus") Integer assessmentStatus);

    @Update("""
            UPDATE risk_assessment
            SET assessment_status = 0
            WHERE risk_data_id = #{riskDataId} AND assessment_status = 1
            """)
    void invalidateEffectiveByRiskDataId(@Param("riskDataId") Long riskDataId);

    @Select("SELECT COUNT(1) FROM risk_assessment WHERE risk_data_id = #{riskDataId}")
    int countByRiskDataId(@Param("riskDataId") Long riskDataId);

    @Select("SELECT COUNT(1) FROM risk_assessment")
    long countAll();

    @Select("SELECT COUNT(1) FROM risk_assessment WHERE assessment_status = 1 AND risk_level = 'HIGH'")
    long countEffectiveHighRisk();

    @Select({
            "<script>",
            "SELECT risk_level, COUNT(1) AS count",
            "FROM risk_assessment",
            "<where>",
            "  assessment_status = 1",
            "  <if test='riskLevel != null and riskLevel != \"\"'>",
            "    AND risk_level = #{riskLevel}",
            "  </if>",
            "  <if test='startTime != null and startTime != \"\"'>",
            "    AND CAST(assessment_time AS DATE) >= #{startTime}",
            "  </if>",
            "  <if test='endTime != null and endTime != \"\"'>",
            "    AND CAST(assessment_time AS DATE) &lt;= #{endTime}",
            "  </if>",
            "</where>",
            "GROUP BY risk_level",
            "</script>"
    })
    List<RiskLevelCountDO> statRiskLevels(
            @Param("startTime") String startTime,
            @Param("endTime") String endTime,
            @Param("riskLevel") String riskLevel
    );

    @Delete("DELETE FROM risk_assessment WHERE id = #{id}")
    void deleteById(@Param("id") Long id);
}
