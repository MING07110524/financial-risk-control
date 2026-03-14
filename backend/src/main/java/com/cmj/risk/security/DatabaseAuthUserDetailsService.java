package com.cmj.risk.security;

import com.cmj.risk.entity.system.SystemUserWithRoleDO;
import com.cmj.risk.mapper.system.SystemUserMapper;
import java.util.Optional;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DatabaseAuthUserDetailsService implements UserDetailsService {
    private final SystemUserMapper systemUserMapper;

    public DatabaseAuthUserDetailsService(SystemUserMapper systemUserMapper) {
        this.systemUserMapper = systemUserMapper;
    }

    @Override
    public SecurityUser loadUserByUsername(String username) throws UsernameNotFoundException {
        return findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public Optional<SecurityUser> findByUsername(String username) {
        SystemUserWithRoleDO user = systemUserMapper.findUserByUsername(username);
        if (user == null || user.getRoleCode() == null) {
            return Optional.empty();
        }

        return Optional.of(SecurityUser.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .password(user.getPassword())
                .realName(user.getRealName())
                .roleCode(user.getRoleCode())
                .roleName(user.getRoleName())
                .enabled(user.getStatus() != null && user.getStatus() == 1)
                .build());
    }
}
