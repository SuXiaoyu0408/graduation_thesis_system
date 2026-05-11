package com.sxy.umlmyself.service;

import com.sxy.umlmyself.dto.ConfirmRoleRequestDTO;
import com.sxy.umlmyself.dto.ConfirmRoleResponseDTO;
import com.sxy.umlmyself.dto.LoginRequestDTO;
import com.sxy.umlmyself.dto.LoginResponseDTO;
import com.sxy.umlmyself.dto.UserProfileDTO;

/**
 * 用户服务接口
 */
public interface UserService {
    
    /**
     * 用户登录（阶段一：身份认证）
     * 校验用户名和密码，返回用户的所有角色（不生成Token）
     * 
     * @param loginRequest 登录请求 DTO
     * @return 登录响应 DTO（包含 userId、username、roles）
     * @throws RuntimeException 当用户名不存在、密码错误或账号被禁用时抛出异常
     */
    LoginResponseDTO login(LoginRequestDTO loginRequest);
    
    /**
     * 确认角色并生成Token（阶段二：确认角色）
     * 校验用户ID和角色ID的关联关系，生成JWT Token
     * 
     * @param confirmRoleRequest 确认角色请求 DTO
     * @return 确认角色响应 DTO（包含 token、userId、username、roleId、roleCode）
     * @throws RuntimeException 当用户不存在或角色不属于该用户时抛出异常
     */
    ConfirmRoleResponseDTO confirmRole(ConfirmRoleRequestDTO confirmRoleRequest);

    /**
     * 获取当前登录用户的综合信息
     */
    UserProfileDTO getCurrentUserProfile(Integer userId);

    /**
     * 更新当前登录用户的信息
     * @param userId 用户ID
     * @param userUpdateDTO 用户更新信息
     */
    void updateUserProfile(Integer userId, com.sxy.umlmyself.dto.UserUpdateDTO userUpdateDTO);
}

