package com.cmj.risk.dto.risk;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

@Data
public class RiskDataUpdateDTO {
    @NotBlank(message = "客户名称不能为空")
    private String customerName;

    @NotBlank(message = "业务类型不能为空")
    private String businessType;

    @NotBlank(message = "风险说明不能为空")
    private String riskDesc;

    @Valid
    @NotEmpty(message = "指标值明细不能为空")
    private List<RiskDataIndexValueItemDTO> indexValues;
}
