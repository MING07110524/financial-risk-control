package com.cmj.risk.mapper.risk;

import com.cmj.risk.component.RiskDemoStore;
import java.math.BigDecimal;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface RiskIndexMapper {

    @Select({
            "<script>",
            "SELECT id, index_name, index_code, weight_value, index_desc, status",
            "FROM risk_index",
            "<where>",
            "  <if test='indexName != null and indexName != \"\"'>",
            "    AND LOWER(index_name) LIKE CONCAT('%', LOWER(#{indexName}), '%')",
            "  </if>",
            "  <if test='status != null'>",
            "    AND status = #{status}",
            "  </if>",
            "</where>",
            "ORDER BY id",
            "</script>"
    })
    List<RiskDemoStore.RiskIndexRecord> listRiskIndexes(@Param("indexName") String indexName, @Param("status") Integer status);

    @Select("""
            SELECT id, index_name, index_code, weight_value, index_desc, status
            FROM risk_index
            WHERE id = #{id}
            """)
    RiskDemoStore.RiskIndexRecord findById(@Param("id") Long id);

    @Select("""
            SELECT id, index_name, index_code, weight_value, index_desc, status
            FROM risk_index
            WHERE LOWER(index_code) = LOWER(#{indexCode})
            """)
    RiskDemoStore.RiskIndexRecord findByCode(@Param("indexCode") String indexCode);

    @Select("""
            <script>
            SELECT COALESCE(SUM(weight_value), 0)
            FROM risk_index
            WHERE status = 1
              <if test='excludeId != null'>
                AND id &lt;&gt; #{excludeId}
              </if>
            </script>
            """)
    BigDecimal sumEnabledWeights(@Param("excludeId") Long excludeId);

    @Select("""
            SELECT COUNT(1)
            FROM risk_index
            WHERE status = 1
            """)
    int countEnabledIndexes();

    @Insert("""
            INSERT INTO risk_index (index_name, index_code, weight_value, index_desc, status)
            VALUES (#{indexName}, #{indexCode}, #{weightValue}, #{indexDesc}, #{status})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(RiskDemoStore.RiskIndexRecord record);

    @Update("""
            UPDATE risk_index
            SET index_name = #{indexName},
                index_code = #{indexCode},
                weight_value = #{weightValue},
                index_desc = #{indexDesc}
            WHERE id = #{id}
            """)
    void update(RiskDemoStore.RiskIndexRecord record);

    @Update("""
            UPDATE risk_index
            SET status = #{status}
            WHERE id = #{id}
            """)
    void updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
