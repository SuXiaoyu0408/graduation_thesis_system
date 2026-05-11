package com.sxy.umlmyself.controller;

import com.sxy.umlmyself.common.ApiResponse;
import com.sxy.umlmyself.dto.ConfirmRoleRequestDTO;
import com.sxy.umlmyself.dto.ConfirmRoleResponseDTO;
import com.sxy.umlmyself.dto.LoginRequestDTO;
import com.sxy.umlmyself.dto.LoginResponseDTO;
import com.sxy.umlmyself.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 登录控制器
 * 处理登录相关的请求
 */
@RestController
public class LoginController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 用户登录接口（阶段一：身份认证）
     * 校验用户名和密码，返回用户的所有角色（不生成Token）
     * 
     * @param loginRequest 登录请求 DTO（包含用户名和密码）
     * @return 统一响应结果，成功时包含 userId、username、roles
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequest) {
        try {
            // 调用服务层处理登录业务逻辑
            LoginResponseDTO loginResponse = userService.login(loginRequest);
            
            // 返回成功响应（包含用户信息和角色列表）
            return ApiResponse.success("登录成功，请选择角色", loginResponse);
        } catch (RuntimeException e) {
            // 捕获业务异常，返回错误响应
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            // 捕获其他异常，返回通用错误响应
            return ApiResponse.error(500, "系统错误：" + e.getMessage());
        }
    }
    
    /**
     * 确认角色接口（阶段二：确认角色并生成Token）
     * 校验用户ID和角色ID的关联关系，生成JWT Token
     * 
     * @param confirmRoleRequest 确认角色请求 DTO（包含 userId 和 roleId）
     * @param response HTTP 响应对象，用于设置响应头
     * @return 统一响应结果，成功时包含 token、userId、username、roleId、roleCode
     */
    @PostMapping("/login/confirm-role")
    public ApiResponse<ConfirmRoleResponseDTO> confirmRole(
            @RequestBody ConfirmRoleRequestDTO confirmRoleRequest,
            HttpServletResponse response) {
        try {
            // 调用服务层处理确认角色业务逻辑
            ConfirmRoleResponseDTO confirmRoleResponse = userService.confirmRole(confirmRoleRequest);
            
            // 将 Token 设置到响应头中（Authorization: Bearer xxx）
            response.setHeader("Authorization", "Bearer " + confirmRoleResponse.getToken());
            
            // 返回成功响应（响应体中也会包含 token 等信息）
            return ApiResponse.success("登录成功", confirmRoleResponse);
        } catch (RuntimeException e) {
            // 捕获业务异常，返回错误响应
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            // 捕获其他异常，返回通用错误响应
            return ApiResponse.error(500, "系统错误：" + e.getMessage());
        }
    }
}

