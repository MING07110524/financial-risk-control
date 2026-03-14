package com.cmj.risk.dto.risk;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class RiskRuleUpdateDTO {
    @NotNull(message = "最小值不能为空")
    private BigDecimal scoreMin;

    @NotNull(message = "最大值不能为空")
    private BigDecimal scoreMax;

    @NotNull(message = "原始得分不能为空")
    @DecimalMin(value = "0", message = "原始得分不能小于0")
    private BigDecimal scoreValue;

    @NotBlank(message = "建议预警等级不能为空")
    private String warningLevel;
}
