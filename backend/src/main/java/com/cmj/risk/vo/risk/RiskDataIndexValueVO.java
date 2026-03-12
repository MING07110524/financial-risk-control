package com.cmj.risk.vo.risk;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskDataIndexValueVO {
    private Long indexId;
    private String indexCode;
    private String indexName;
    private BigDecimal indexValue;
    private BigDecimal weightValue;
}
