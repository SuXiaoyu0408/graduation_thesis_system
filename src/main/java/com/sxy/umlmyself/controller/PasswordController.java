package com.sxy.umlmyself.controller;

import com.sxy.umlmyself.common.ApiResponse;
import com.sxy.umlmyself.dto.ResetPasswordRequestDTO;
import com.sxy.umlmyself.dto.SendSmsCodeRequestDTO;
import com.sxy.umlmyself.dto.VerifyCodeRequestDTO;
import com.sxy.umlmyself.service.PasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 密码管理控制器
 * 处理找回密码、重置密码相关的请求
 */
@RestController
@RequestMapping("/password")
public class PasswordController {
    
    @Autowired
    private PasswordService passwordService;
    
    /**
     * 发送验证码接口
     * 
     * @param request 发送验证码请求 DTO（包含手机号）
     * @return 统一响应结果
     */
    @PostMapping("/sms-code")
    public ApiResponse<Void> sendSmsCode(@RequestBody SendSmsCodeRequestDTO request) {
        try {
            // 调用服务层处理发送验证码业务逻辑
            passwordService.sendSmsCode(request.getPhone());
            
            // 返回成功响应
            return ApiResponse.success("验证码已发送", null);
        } catch (RuntimeException e) {
            // 捕获业务异常，返回错误响应
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            // 捕获其他异常，返回通用错误响应
            return ApiResponse.error(500, "系统错误：" + e.getMessage());
        }
    }
    
    /**
     * 校验验证码接口
     * 
     * @param request 校验验证码请求 DTO（包含手机号和验证码）
     * @return 统一响应结果
     */
    @PostMapping("/verify-code")
    public ApiResponse<Void> verifyCode(@RequestBody VerifyCodeRequestDTO request) {
        try {
            // 调用服务层处理校验验证码业务逻辑
            passwordService.verifyCode(request.getPhone(), request.getCode());
            
            // 返回成功响应
            return ApiResponse.success("验证码校验成功", null);
        } catch (RuntimeException e) {
            // 捕获业务异常，返回错误响应
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            // 捕获其他异常，返回通用错误响应
            return ApiResponse.error(500, "系统错误：" + e.getMessage());
        }
    }
    
    /**
     * 重置密码接口
     * 
     * @param request 重置密码请求 DTO（包含手机号、新密码、确认密码）
     * @return 统一响应结果
     */
    @PostMapping("/reset")
    public ApiResponse<Void> resetPassword(@RequestBody ResetPasswordRequestDTO request) {
        try {
            // 调用服务层处理重置密码业务逻辑
            passwordService.resetPassword(
                    request.getPhone(),
                    request.getNewPassword(),
                    request.getConfirmPassword()
            );
            
            // 返回成功响应
            return ApiResponse.success("密码重置成功", null);
        } catch (RuntimeException e) {
            // 捕获业务异常，返回错误响应
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            // 捕获其他异常，返回通用错误响应
            return ApiResponse.error(500, "系统错误：" + e.getMessage());
        }
    }
}

