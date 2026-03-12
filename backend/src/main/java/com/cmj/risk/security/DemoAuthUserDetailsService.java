package com.cmj.risk.security;

import com.cmj.risk.config.AuthDemoUsersProperties;
import jakarta.annotation.PostConstruct;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class DemoAuthUserDetailsService implements UserDetailsService {
    private final AuthDemoUsersProperties authDemoUsersProperties;
    private final PasswordEncoder passwordEncoder;
    private final Map<String, SecurityUser> usersByUsername = new LinkedHashMap<>();

    public DemoAuthUserDetailsService(
            AuthDemoUsersProperties authDemoUsersProperties,
            PasswordEncoder passwordEncoder
    ) {
        this.authDemoUsersProperties = authDemoUsersProperties;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void init() {
        usersByUsername.clear();
        for (AuthDemoUsersProperties.DemoUserItem demoUserItem : authDemoUsersProperties.getDemoUsers()) {
            // Encode the demo password at startup so the authentication flow is
            // already using PasswordEncoder + UserDetailsService like a real
            // backend. / 在启动时把演示密码编码成 BCrypt，这样虽然账号源还是 demo
            // 配置，但认证流程已经是“真实后端”的 UserDetailsService + PasswordEncoder。
            SecurityUser securityUser = SecurityUser.builder()
                    .userId(demoUserItem.getUserId())
                    .username(demoUserItem.getUsername())
                    .password(passwordEncoder.encode(demoUserItem.getPassword()))
                    .realName(demoUserItem.getRealName())
                    .roleCode(demoUserItem.getRoleCode())
                    .roleName(demoUserItem.getRoleName())
                    .enabled(demoUserItem.isEnabled())
                    .build();
            usersByUsername.put(securityUser.getUsername(), securityUser);
        }
    }

    @Override
    public SecurityUser loadUserByUsername(String username) throws UsernameNotFoundException {
        return findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Demo auth user not found: " + username));
    }

    public Optional<SecurityUser> findByUsername(String username) {
        return Optional.ofNullable(usersByUsername.get(username));
    }
}
