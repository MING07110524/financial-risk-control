package com.cmj.risk.dto.risk;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RiskIndexStatusDTO {
    @NotNull(message = "状态不能为空")
    private Integer status;
}
