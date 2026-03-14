package com.cmj.risk.controller;

import com.cmj.risk.common.ErrorCode;
import com.cmj.risk.common.Result;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assistant")
public class AssistantController {
    private static final String DISABLED_MESSAGE = "AI assistant is disabled in V1";

    @PostMapping("/query")
    public Result<Void> query(@RequestBody(required = false) Map<String, Object> payload) {
        return Result.failure(ErrorCode.FEATURE_DISABLED, DISABLED_MESSAGE);
    }

    @PostMapping("/action")
    public Result<Void> action(@RequestBody(required = false) Map<String, Object> payload) {
        return Result.failure(ErrorCode.FEATURE_DISABLED, DISABLED_MESSAGE);
    }
}
