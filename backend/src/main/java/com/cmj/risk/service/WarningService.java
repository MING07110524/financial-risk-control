package com.cmj.risk.service;

import com.cmj.risk.common.PageResult;
import com.cmj.risk.security.SecurityUser;
import com.cmj.risk.vo.warning.WarningDetailVO;
import com.cmj.risk.vo.warning.WarningHandleRecordVO;
import com.cmj.risk.vo.warning.WarningVO;
import java.util.List;

public interface WarningService {
    PageResult<WarningVO> pageWarnings(
            String warningCode,
            String warningLevel,
            Integer warningStatus,
            String startTime,
            String endTime,
            int pageNum,
            int pageSize
    );

    WarningDetailVO getWarningDetail(Long id);

    List<WarningHandleRecordVO> listWarningRecords(Long warningId);

    void handleWarning(Long warningId, String handleOpinion, String handleResult, Integer nextStatus, SecurityUser operator);

    List<WarningVO> listRecentWarnings(int limit);
}
