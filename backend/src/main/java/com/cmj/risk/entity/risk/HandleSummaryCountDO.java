package com.cmj.risk.entity.risk;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HandleSummaryCountDO {
    private Integer warningStatus;
    private Integer count;
}
