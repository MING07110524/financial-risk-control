package com.cmj.risk.service;

import com.cmj.risk.common.PageResult;
import com.cmj.risk.dto.system.UserCreateDTO;
import com.cmj.risk.dto.system.UserStatusDTO;
import com.cmj.risk.dto.system.UserUpdateDTO;
import com.cmj.risk.vo.system.RoleVO;
import com.cmj.risk.vo.system.UserVO;
import java.util.List;

public interface UserService {
    PageResult<UserVO> pageUsers(String username, String realName, String roleCode, Integer status, int pageNum, int pageSize);
    
    UserVO getUserById(Long id);
    
    UserVO createUser(UserCreateDTO dto);
    
    UserVO updateUser(Long id, UserUpdateDTO dto);
    
    UserVO updateUserStatus(Long id, UserStatusDTO dto);
    
    void deleteUser(Long id);
    
    List<RoleVO> listRoles();
}
