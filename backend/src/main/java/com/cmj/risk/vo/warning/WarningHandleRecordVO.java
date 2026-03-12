package com.cmj.risk.vo.warning;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarningHandleRecordVO {
    private Long id;
    private Long warningId;
    private Long handleUserId;
    private String handleUserName;
    private String handleOpinion;
    private String handleResult;
    private Integer nextStatus;
    private String handleTime;
}
