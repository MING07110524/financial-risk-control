package com.cmj.risk.service.impl;

import com.cmj.risk.common.ErrorCode;
import com.cmj.risk.common.PageResult;
import com.cmj.risk.dto.system.UserCreateDTO;
import com.cmj.risk.dto.system.UserStatusDTO;
import com.cmj.risk.dto.system.UserUpdateDTO;
import com.cmj.risk.entity.system.SystemRoleDO;
import com.cmj.risk.entity.system.SystemUserDO;
import com.cmj.risk.entity.system.SystemUserWithRoleDO;
import com.cmj.risk.exception.BusinessException;
import com.cmj.risk.mapper.system.SystemRoleMapper;
import com.cmj.risk.mapper.system.SystemUserMapper;
import com.cmj.risk.mapper.system.SystemUserRoleMapper;
import com.cmj.risk.security.SecurityUser;
import com.cmj.risk.service.LogService;
import com.cmj.risk.service.UserService;
import com.cmj.risk.vo.system.RoleVO;
import com.cmj.risk.vo.system.UserVO;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SystemUserMapper systemUserMapper;
    private final SystemRoleMapper systemRoleMapper;
    private final SystemUserRoleMapper systemUserRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final LogService logService;

    public UserServiceImpl(
            SystemUserMapper systemUserMapper,
            SystemRoleMapper systemRoleMapper,
            SystemUserRoleMapper systemUserRoleMapper,
            PasswordEncoder passwordEncoder,
            LogService logService
    ) {
        this.systemUserMapper = systemUserMapper;
        this.systemRoleMapper = systemRoleMapper;
        this.systemUserRoleMapper = systemUserRoleMapper;
        this.passwordEncoder = passwordEncoder;
        this.logService = logService;
    }

    @Override
    public PageResult<UserVO> pageUsers(String username, String realName, String roleCode, Integer status, int pageNum, int pageSize) {
        List<UserVO> records = systemUserMapper.listUsers(username, realName, roleCode, status).stream()
                .map(this::toUserVO)
                .toList();
        int start = Math.max(pageNum - 1, 0) * pageSize;
        int end = Math.min(start + pageSize, records.size());
        List<UserVO> pageRecords = start >= records.size() ? List.of() : records.subList(start, end);
        return new PageResult<>((long) records.size(), pageRecords);
    }

    @Override
    public UserVO getUserById(Long id) {
        return toUserVO(requireUser(id));
    }

    @Override
    public UserVO createUser(UserCreateDTO dto) {
        SecurityUser operator = getCurrentOperator();
        String username = dto.getUsername().trim();
        validateUniqueUsername(username, null);
        validatePassword(dto.getPassword());

        Long roleId = resolveSingleRoleId(dto.getRoleIds(), true);

        SystemUserDO user = new SystemUserDO();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRealName(dto.getRealName().trim());
        user.setPhone(normalizeOptional(dto.getPhone()));
        user.setStatus(1);
        systemUserMapper.insertUser(user);
        systemUserRoleMapper.insertUserRole(user.getId(), roleId);

        logService.createLog("用户", "新增", "新增用户 " + username, operator.getUsername(), operator.getUserId());
        return getUserById(user.getId());
    }

    @Override
    public UserVO updateUser(Long id, UserUpdateDTO dto) {
        SecurityUser operator = getCurrentOperator();
        SystemUserWithRoleDO current = requireUser(id);

        String username = dto.getUsername() != null ? dto.getUsername().trim() : current.getUsername();
        validateUniqueUsername(username, id);

        SystemUserDO user = new SystemUserDO();
        user.setId(id);
        user.setUsername(username);
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            validatePassword(dto.getPassword());
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        } else {
            user.setPassword(current.getPassword());
        }
        user.setRealName(dto.getRealName() != null ? dto.getRealName().trim() : current.getRealName());
        user.setPhone(dto.getPhone() != null ? normalizeOptional(dto.getPhone()) : current.getPhone());
        user.setStatus(current.getStatus());
        systemUserMapper.updateUser(user);

        if (dto.getRoleIds() != null) {
            Long roleId = resolveSingleRoleId(dto.getRoleIds(), true);
            systemUserRoleMapper.deleteByUserId(id);
            systemUserRoleMapper.insertUserRole(id, roleId);
        }

        logService.createLog("用户", "编辑", "编辑用户 " + current.getUsername(), operator.getUsername(), operator.getUserId());
        return getUserById(id);
    }

    @Override
    public UserVO updateUserStatus(Long id, UserStatusDTO dto) {
        SecurityUser operator = getCurrentOperator();
        if (operator.getUserId().equals(id) && !dto.getStatus().equals(1)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不能停用当前登录用户");
        }

        validateStatus(dto.getStatus());
        SystemUserWithRoleDO current = requireUser(id);
        systemUserMapper.updateUserStatus(id, dto.getStatus());

        String action = dto.getStatus() == 1 ? "启用" : "停用";
        logService.createLog("用户", action, action + "用户 " + current.getUsername(), operator.getUsername(), operator.getUserId());
        return getUserById(id);
    }

    @Override
    public void deleteUser(Long id) {
        SecurityUser operator = getCurrentOperator();
        if (operator.getUserId().equals(id)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不能删除当前登录用户");
        }

        SystemUserWithRoleDO current = requireUser(id);
        systemUserRoleMapper.deleteByUserId(id);
        systemUserMapper.deleteUser(id);
        logService.createLog("用户", "删除", "删除用户 " + current.getUsername(), operator.getUsername(), operator.getUserId());
    }

    @Override
    public List<RoleVO> listRoles() {
        return systemRoleMapper.listRoles().stream()
                .map(this::toRoleVO)
                .toList();
    }

    private UserVO toUserVO(SystemUserWithRoleDO item) {
        List<Long> roleIds = item.getRoleId() == null ? List.of() : List.of(item.getRoleId());

        return UserVO.builder()
                .id(item.getId())
                .username(item.getUsername())
                .realName(item.getRealName())
                .phone(item.getPhone())
                .status(item.getStatus())
                .roleName(item.getRoleName())
                .roleCode(item.getRoleCode())
                .roleIds(roleIds)
                .createTime(formatTime(item.getCreateTime()))
                .updateTime(formatTime(item.getUpdateTime()))
                .build();
    }

    private RoleVO toRoleVO(SystemRoleDO item) {
        return RoleVO.builder()
                .id(item.getId())
                .roleName(item.getRoleName())
                .roleCode(item.getRoleCode())
                .remark(item.getRemark())
                .build();
    }

    private SecurityUser getCurrentOperator() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof SecurityUser securityUser) {
            return securityUser;
        }
        throw new BusinessException(ErrorCode.UNAUTHORIZED, "未登录或登录已过期");
    }

    private SystemUserWithRoleDO requireUser(Long id) {
        SystemUserWithRoleDO user = systemUserMapper.findUserById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "未找到对应的用户");
        }
        return user;
    }

    private void validateUniqueUsername(String username, Long currentId) {
        if (systemUserMapper.countByUsername(username, currentId) > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "用户名已存在，请更换后重试");
        }
    }

    private Long resolveSingleRoleId(List<Long> roleIds, boolean required) {
        if (roleIds == null || roleIds.isEmpty()) {
            if (required) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "至少需要选择一个角色");
            }
            return null;
        }

        Long roleId = roleIds.getFirst();
        if (systemRoleMapper.findById(roleId) == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "所选角色不存在");
        }
        return roleId;
    }

    private void validateStatus(Integer status) {
        if (!Objects.equals(status, 0) && !Objects.equals(status, 1)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "状态只允许为 0 或 1");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 6) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "密码长度不能少于6位");
        }
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private String formatTime(LocalDateTime value) {
        return value == null ? null : value.format(DATE_TIME_FORMATTER);
    }
}
