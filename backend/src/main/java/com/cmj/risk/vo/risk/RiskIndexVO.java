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
public class RiskIndexVO {
    private Long id;
    private String indexName;
    private String indexCode;
    private BigDecimal weightValue;
    private String indexDesc;
    private Integer status;
}
