package com.sxy.umlmyself.dto;

import lombok.Data;
import java.util.List;

/**
 * 登录响应 DTO（阶段一：身份认证）
 * 用于返回登录成功后的用户信息和角色列表（不包含密码和Token）
 */
@Data
public class LoginResponseDTO {
    
    /**
     * 用户ID
     */
    private Integer userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 角色列表（包含 roleId、roleCode、roleName）
     */
    private List<RoleDTO> roles;
}

