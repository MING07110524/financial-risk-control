package com.cmj.risk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cmj.risk.common.PageResult;
import com.cmj.risk.dto.system.UserCreateDTO;
import com.cmj.risk.dto.system.UserUpdateDTO;
import com.cmj.risk.exception.BusinessException;
import com.cmj.risk.security.SecurityUser;
import com.cmj.risk.service.UserService;
import com.cmj.risk.vo.system.RoleVO;
import com.cmj.risk.vo.system.UserVO;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserManagementIntegrationTests {

    @Autowired
    private UserService userService;

    @BeforeEach
    void setUp() {
        SecurityUser adminUser = SecurityUser.builder()
                .userId(1L)
                .username("admin-demo")
                .password("")
                .realName("演示管理员")
                .roleCode("ADMIN")
                .roleName("系统管理员")
                .enabled(true)
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(adminUser, null, adminUser.getAuthorities()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void listUsersShouldReturnPagedUsers() {
        PageResult<UserVO> result = userService.pageUsers(null, null, null, null, 1, 10);
        assertThat(result.getTotal()).isGreaterThan(0);
        assertThat(result.getRecords()).isNotEmpty();
    }

    @Test
    void listUsersWithFiltersShouldWork() {
        PageResult<UserVO> result = userService.pageUsers("admin", null, "ADMIN", null, 1, 10);
        assertThat(result.getRecords()).isNotEmpty();
        assertThat(result.getRecords().get(0).getUsername()).contains("admin");
    }

    @Test
    void getUserByIdShouldReturnDetail() {
        UserVO user = userService.getUserById(1L);
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getUsername()).isNotNull();
    }

    @Test
    void createUserShouldRejectDuplicateUsername() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setUsername("admin-demo");
        dto.setPassword("test123456");
        dto.setRealName("测试用户");
        dto.setRoleIds(List.of(1L));

        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户名已存在");
    }

    @Test
    void createUserShouldWork() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setUsername("newuser");
        dto.setPassword("test123456");
        dto.setRealName("新用户");
        dto.setPhone("13900000000");
        dto.setRoleIds(List.of(2L));

        UserVO created = userService.createUser(dto);
        assertThat(created.getId()).isNotNull();
        assertThat(created.getUsername()).isEqualTo("newuser");
        assertThat(created.getRealName()).isEqualTo("新用户");
    }

    @Test
    void updateUserShouldWork() {
        UserCreateDTO createDto = new UserCreateDTO();
        createDto.setUsername("updatetest");
        createDto.setPassword("test123456");
        createDto.setRealName("更新测试");
        createDto.setRoleIds(List.of(2L));
        UserVO created = userService.createUser(createDto);

        UserUpdateDTO updateDto = new UserUpdateDTO();
        updateDto.setRealName("更新后的名称");
        updateDto.setPhone("13800000000");

        UserVO updated = userService.updateUser(created.getId(), updateDto);
        assertThat(updated.getRealName()).isEqualTo("更新后的名称");
    }

    @Test
    void updateUserShouldRejectDuplicateUsername() {
        UserCreateDTO createDto = new UserCreateDTO();
        createDto.setUsername("duplicateTest");
        createDto.setPassword("test123456");
        createDto.setRealName("重复测试");
        createDto.setRoleIds(List.of(2L));
        userService.createUser(createDto);

        UserUpdateDTO updateDto = new UserUpdateDTO();
        updateDto.setUsername("admin-demo");

        assertThatThrownBy(() -> userService.updateUser(2L, updateDto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户名已存在");
    }

    @Test
    void listRolesShouldReturnAllRoles() {
        List<RoleVO> roles = userService.listRoles();
        assertThat(roles).hasSize(3);
        assertThat(roles).extracting("roleCode")
                .containsExactlyInAnyOrder("ADMIN", "RISK_USER", "MANAGER");
    }
}
