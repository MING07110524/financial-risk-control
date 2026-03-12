package com.cmj.risk.dto.risk;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class RiskDataIndexValueItemDTO {
    @NotNull(message = "指标ID不能为空")
    private Long indexId;

    @NotNull(message = "指标值不能为空")
    @DecimalMin(value = "0", message = "指标值不能小于0")
    private BigDecimal indexValue;
}
