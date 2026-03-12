package com.cmj.risk.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.security.auth")
public class AuthDemoUsersProperties {
    private List<DemoUserItem> demoUsers = new ArrayList<>();

    @Getter
    @Setter
    public static class DemoUserItem {
        private Long userId;
        private String username;
        private String password;
        private String realName;
        private String roleCode;
        private String roleName;
        private boolean enabled = true;
    }
}
