package com.cmj.risk.controller;

import com.cmj.risk.common.PageResult;
import com.cmj.risk.common.Result;
import com.cmj.risk.dto.system.UserCreateDTO;
import com.cmj.risk.dto.system.UserStatusDTO;
import com.cmj.risk.dto.system.UserUpdateDTO;
import com.cmj.risk.service.UserService;
import com.cmj.risk.vo.system.RoleVO;
import com.cmj.risk.vo.system.UserVO;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<UserVO>> pageUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) String roleCode,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return Result.success(userService.pageUsers(username, realName, roleCode, status, pageNum, pageSize));
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<UserVO> getUserById(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<UserVO> createUser(@Valid @RequestBody UserCreateDTO dto) {
        return Result.success(userService.createUser(dto));
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<UserVO> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO dto) {
        return Result.success(userService.updateUser(id, dto));
    }

    @PutMapping("/users/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<UserVO> updateUserStatus(@PathVariable Long id, @Valid @RequestBody UserStatusDTO dto) {
        return Result.success(userService.updateUserStatus(id, dto));
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }

    @GetMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<RoleVO>> listRoles() {
        return Result.success(userService.listRoles());
    }
}
