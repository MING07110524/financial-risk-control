package com.cmj.risk.security;

import com.cmj.risk.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {
    private final JwtProperties jwtProperties;
    private SecretKey signingKey;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @PostConstruct
    public void init() {
        signingKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(SecurityUser securityUser) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(jwtProperties.getExpirationMinutes(), ChronoUnit.MINUTES);

        return Jwts.builder()
                .issuer(jwtProperties.getIssuer())
                .subject(securityUser.getUsername())
                .claim("userId", securityUser.getUserId())
                .claim("realName", securityUser.getRealName())
                .claim("roleCode", securityUser.getRoleCode())
                .claim("roleName", securityUser.getRoleName())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
    }

    public Optional<SecurityUser> parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Long userId = claims.get("userId", Number.class).longValue();
            String realName = claims.get("realName", String.class);
            String roleCode = claims.get("roleCode", String.class);
            String roleName = claims.get("roleName", String.class);
            String username = claims.getSubject();

            return Optional.of(SecurityUser.builder()
                    .userId(userId)
                    .username(username)
                    .password("")
                    .realName(realName)
                    .roleCode(roleCode)
                    .roleName(roleName)
                    .enabled(true)
                    .build());
        } catch (JwtException | IllegalArgumentException exception) {
            return Optional.empty();
        }
    }
}
