package com.cmj.risk.mapper.risk;

import com.cmj.risk.component.RiskWorkflowStore;
import com.cmj.risk.entity.risk.RiskAssessmentIndexResultDO;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface RiskAssessmentIndexResultMapper {

    @Select({
            "SELECT id, assessment_id, index_id, index_code, index_name, index_value, weight_value, score_value, weighted_score, warning_level",
            "FROM risk_assessment_index_result",
            "WHERE assessment_id = #{assessmentId}",
            "ORDER BY id ASC"
    })
    List<RiskWorkflowStore.AssessmentIndexResultRecord> listByAssessmentId(@Param("assessmentId") Long assessmentId);

    @Insert({
            "<script>",
            "INSERT INTO risk_assessment_index_result (assessment_id, index_id, index_code, index_name, index_value, weight_value, score_value, weighted_score, warning_level)",
            "VALUES",
            "<foreach collection='records' item='item' separator=','>",
            "(#{assessmentId}, #{item.indexId}, #{item.indexCode}, #{item.indexName}, #{item.indexValue}, #{item.weightValue}, #{item.scoreValue}, #{item.weightedScore}, #{item.warningLevel})",
            "</foreach>",
            "</script>"
    })
    void insertBatch(@Param("assessmentId") Long assessmentId, @Param("records") List<RiskAssessmentIndexResultDO> records);

    @Delete("DELETE FROM risk_assessment_index_result WHERE assessment_id = #{assessmentId}")
    void deleteByAssessmentId(@Param("assessmentId") Long assessmentId);
}
