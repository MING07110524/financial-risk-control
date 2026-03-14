package com.cmj.risk.mapper.risk;

import com.cmj.risk.component.RiskDemoStore;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface RiskDataIndexValueMapper {

    @Select("""
            SELECT index_id, index_value
            FROM risk_data_index_value
            WHERE risk_data_id = #{riskDataId}
            ORDER BY index_id
            """)
    List<RiskDemoStore.RiskDataIndexValueRecord> listByRiskDataId(@Param("riskDataId") Long riskDataId);

    @Delete("DELETE FROM risk_data_index_value WHERE risk_data_id = #{riskDataId}")
    void deleteByRiskDataId(@Param("riskDataId") Long riskDataId);

    @Insert({
            "<script>",
            "INSERT INTO risk_data_index_value (risk_data_id, index_id, index_value) VALUES",
            "<foreach collection='indexValues' item='item' separator=','>",
            "  (#{riskDataId}, #{item.indexId}, #{item.indexValue})",
            "</foreach>",
            "</script>"
    })
    void insertBatch(
            @Param("riskDataId") Long riskDataId,
            @Param("indexValues") List<RiskDemoStore.RiskDataIndexValueRecord> indexValues
    );
}
