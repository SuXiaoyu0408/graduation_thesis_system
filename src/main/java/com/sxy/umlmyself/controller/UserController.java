package com.sxy.umlmyself.controller;

import com.sxy.umlmyself.common.ApiResponse;
import com.sxy.umlmyself.dto.UserProfileDTO;
import com.sxy.umlmyself.dto.UserUpdateDTO;
import com.sxy.umlmyself.service.UserService;
import com.sxy.umlmyself.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户相关接口
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/profile")
    public ApiResponse<UserProfileDTO> getUserProfile(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ApiResponse.error(401, "未提供有效的认证信息");
        }
        String token = authHeader.substring(7);

        try {
            Integer userId = jwtUtil.getUserIdFromToken(token);
            UserProfileDTO userProfile = userService.getCurrentUserProfile(userId);
            if (userProfile != null) {
                return ApiResponse.success(userProfile);
            } else {
                return ApiResponse.error(404, "用户不存在");
            }
        } catch (Exception e) {
            return ApiResponse.error(401, "Token无效或已过期");
        }
    }

    /**
     * 更新当前登录用户信息
     */
    @PutMapping("/profile")
    public ApiResponse<Void> updateUserProfile(@RequestBody UserUpdateDTO userUpdateDTO, HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ApiResponse.error(401, "未提供有效的认证信息");
        }
        String token = authHeader.substring(7);
        try {
            Integer userId = jwtUtil.getUserIdFromToken(token);
            userService.updateUserProfile(userId, userUpdateDTO);
            return ApiResponse.success();
        } catch (Exception e) {
            return ApiResponse.error(401, e.getMessage());
        }
    }
}