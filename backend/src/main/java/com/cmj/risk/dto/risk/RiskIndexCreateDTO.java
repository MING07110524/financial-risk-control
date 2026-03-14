package com.cmj.risk.dto.risk;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class RiskIndexCreateDTO {
    @NotBlank(message = "指标名称不能为空")
    private String indexName;

    @NotBlank(message = "指标编码不能为空")
    private String indexCode;

    @NotNull(message = "权重不能为空")
    @DecimalMin(value = "0.01", message = "权重必须大于 0")
    private BigDecimal weightValue;

    @NotBlank(message = "指标说明不能为空")
    private String indexDesc;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
