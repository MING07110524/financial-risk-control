package com.cmj.risk.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.security.jwt")
public class JwtProperties {
    private String issuer;
    private long expirationMinutes;
    private String secret;
}
