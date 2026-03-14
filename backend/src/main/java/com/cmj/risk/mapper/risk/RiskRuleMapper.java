package com.cmj.risk.mapper.risk;

import com.cmj.risk.component.RiskDemoStore;
import java.math.BigDecimal;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface RiskRuleMapper {

    @Select("""
            SELECT r.id, r.index_id, i.index_name, r.score_min, r.score_max, r.score_value, r.warning_level
            FROM risk_rule r
            INNER JOIN risk_index i ON i.id = r.index_id
            WHERE r.index_id = #{indexId}
            ORDER BY r.score_min, r.id
            """)
    List<RiskDemoStore.RiskRuleRecord> listByIndexId(@Param("indexId") Long indexId);

    @Select("""
            SELECT r.id, r.index_id, i.index_name, r.score_min, r.score_max, r.score_value, r.warning_level
            FROM risk_rule r
            INNER JOIN risk_index i ON i.id = r.index_id
            WHERE r.id = #{id}
            """)
    RiskDemoStore.RiskRuleRecord findById(@Param("id") Long id);

    @Select("""
            <script>
            SELECT COUNT(1)
            FROM risk_rule
            WHERE index_id = #{indexId}
              AND score_min &lt;= #{scoreMax}
              AND score_max &gt;= #{scoreMin}
              <if test='excludeId != null'>
                AND id &lt;&gt; #{excludeId}
              </if>
            </script>
            """)
    int countConflictingRules(
            @Param("indexId") Long indexId,
            @Param("excludeId") Long excludeId,
            @Param("scoreMin") BigDecimal scoreMin,
            @Param("scoreMax") BigDecimal scoreMax
    );

    @Select("""
            SELECT COUNT(1)
            FROM risk_rule
            WHERE index_id = #{indexId}
            """)
    int countByIndexId(@Param("indexId") Long indexId);

    @Insert("""
            INSERT INTO risk_rule (index_id, score_min, score_max, score_value, warning_level)
            VALUES (#{indexId}, #{scoreMin}, #{scoreMax}, #{scoreValue}, #{warningLevel})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(RiskDemoStore.RiskRuleRecord record);

    @Update("""
            UPDATE risk_rule
            SET score_min = #{scoreMin},
                score_max = #{scoreMax},
                score_value = #{scoreValue},
                warning_level = #{warningLevel}
            WHERE id = #{id}
            """)
    void update(RiskDemoStore.RiskRuleRecord record);

    @Delete("DELETE FROM risk_rule WHERE id = #{id}")
    void deleteById(@Param("id") Long id);
}
