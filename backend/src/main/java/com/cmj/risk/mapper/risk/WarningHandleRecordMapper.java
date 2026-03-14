package com.cmj.risk.mapper.risk;

import com.cmj.risk.component.RiskWorkflowStore.WarningHandleRecord;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface WarningHandleRecordMapper {

    @Select({
            "SELECT r.id, r.warning_id, r.handle_user_id, u.real_name AS handle_user_name,",
            "       r.handle_opinion, r.handle_result, r.next_status,",
            "       CAST(r.handle_time AS CHAR(19)) AS handle_time",
            "FROM warning_handle_record r",
            "LEFT JOIN sys_user u ON u.id = r.handle_user_id",
            "WHERE r.warning_id = #{warningId}",
            "ORDER BY r.handle_time DESC, r.id DESC"
    })
    List<WarningHandleRecord> listByWarningId(@Param("warningId") Long warningId);

    @Select("SELECT COALESCE(MAX(id), 0) FROM warning_handle_record")
    Long getMaxId();

    @Insert("""
            INSERT INTO warning_handle_record (warning_id, handle_user_id, handle_opinion, handle_result, next_status, handle_time)
            VALUES (#{warningId}, #{handleUserId}, #{handleOpinion}, #{handleResult}, #{nextStatus}, #{handleTime})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(WarningHandleRecord record);

    @Delete("DELETE FROM warning_handle_record WHERE warning_id = #{warningId}")
    void deleteByWarningId(@Param("warningId") Long warningId);
}
