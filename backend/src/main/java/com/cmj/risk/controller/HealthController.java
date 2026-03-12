package com.cmj.risk.controller;

import com.cmj.risk.common.Result;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public Result<Map<String, String>> health() {
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("service", "financial-risk-control-backend");
        payload.put("status", "UP");
        payload.put("phase", "phase-1-scaffold");
        return Result.success(payload);
    }
}
