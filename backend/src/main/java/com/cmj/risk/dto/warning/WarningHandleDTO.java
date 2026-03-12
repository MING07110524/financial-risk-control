package com.cmj.risk.dto.warning;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WarningHandleDTO {
    @NotBlank(message = "处理意见不能为空")
    private String handleOpinion;

    @NotBlank(message = "处理结果不能为空")
    private String handleResult;

    @NotNull(message = "下一状态不能为空")
    @Min(value = 1, message = "下一状态只允许为处理中或已处理")
    @Max(value = 2, message = "下一状态只允许为处理中或已处理")
    private Integer nextStatus;
}
