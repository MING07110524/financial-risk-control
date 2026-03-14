package com.cmj.risk.entity.risk;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WarningTrendCountDO {
    private String date;
    private Integer total;
    private Integer pending;
    private Integer handled;
}
