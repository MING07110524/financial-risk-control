package com.cmj.risk.mapper.risk;

import com.cmj.risk.component.RiskDemoStore;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface RiskDataMapper {

    @Select({
            "<script>",
            "SELECT d.id, d.business_no, d.customer_name, d.business_type, d.risk_desc, d.data_status,",
            "       d.create_by, u.real_name AS create_by_name,",
            "       CAST(d.create_time AS CHAR(19)) AS create_time,",
            "       CAST(d.update_time AS CHAR(19)) AS update_time",
            "FROM risk_data d",
            "LEFT JOIN sys_user u ON u.id = d.create_by",
            "<where>",
            "  <if test='businessNo != null and businessNo != \"\"'>",
            "    AND LOWER(d.business_no) LIKE CONCAT('%', LOWER(#{businessNo}), '%')",
            "  </if>",
            "  <if test='customerName != null and customerName != \"\"'>",
            "    AND LOWER(d.customer_name) LIKE CONCAT('%', LOWER(#{customerName}), '%')",
            "  </if>",
            "  <if test='businessType != null and businessType != \"\"'>",
            "    AND LOWER(d.business_type) LIKE CONCAT('%', LOWER(#{businessType}), '%')",
            "  </if>",
            "  <if test='dataStatus != null'>",
            "    AND d.data_status = #{dataStatus}",
            "  </if>",
            "</where>",
            "ORDER BY d.create_time DESC, d.id DESC",
            "</script>"
    })
    List<RiskDemoStore.RiskDataRecord> listRiskData(
            @Param("businessNo") String businessNo,
            @Param("customerName") String customerName,
            @Param("businessType") String businessType,
            @Param("dataStatus") Integer dataStatus
    );

    @Select("""
            SELECT d.id, d.business_no, d.customer_name, d.business_type, d.risk_desc, d.data_status,
                   d.create_by, u.real_name AS create_by_name,
                   CAST(d.create_time AS CHAR(19)) AS create_time,
                   CAST(d.update_time AS CHAR(19)) AS update_time
            FROM risk_data d
            LEFT JOIN sys_user u ON u.id = d.create_by
            WHERE d.id = #{id}
            """)
    RiskDemoStore.RiskDataRecord findById(@Param("id") Long id);

    @Select("""
            SELECT id, business_no, customer_name, business_type, risk_desc, data_status,
                   create_by, CAST(create_time AS CHAR(19)) AS create_time,
                   CAST(update_time AS CHAR(19)) AS update_time
            FROM risk_data
            WHERE LOWER(business_no) = LOWER(#{businessNo})
            """)
    RiskDemoStore.RiskDataRecord findByBusinessNo(@Param("businessNo") String businessNo);

    @Select("""
            <script>
            SELECT COUNT(1)
            FROM risk_data
            WHERE LOWER(business_no) = LOWER(#{businessNo})
              <if test='excludeId != null'>
                AND id &lt;&gt; #{excludeId}
              </if>
            </script>
            """)
    int countByBusinessNo(@Param("businessNo") String businessNo, @Param("excludeId") Long excludeId);

    @Insert("""
            INSERT INTO risk_data (business_no, customer_name, business_type, risk_desc, data_status, create_by)
            VALUES (#{businessNo}, #{customerName}, #{businessType}, #{riskDesc}, #{dataStatus}, #{createBy})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(RiskDemoStore.RiskDataRecord record);

    @Update("""
            UPDATE risk_data
            SET customer_name = #{customerName},
                business_type = #{businessType},
                risk_desc = #{riskDesc},
                data_status = #{dataStatus},
                update_time = CURRENT_TIMESTAMP
            WHERE id = #{id}
            """)
    void update(RiskDemoStore.RiskDataRecord record);

    @Update("""
            UPDATE risk_data
            SET data_status = #{dataStatus},
                update_time = CURRENT_TIMESTAMP
            WHERE id = #{id}
            """)
    void updateStatus(@Param("id") Long id, @Param("dataStatus") Integer dataStatus);

    @Delete("""
            DELETE FROM risk_data
            WHERE id = #{id}
            """)
    void deleteById(@Param("id") Long id);

    @Select("SELECT COUNT(1) FROM risk_data")
    long countAll();
}
