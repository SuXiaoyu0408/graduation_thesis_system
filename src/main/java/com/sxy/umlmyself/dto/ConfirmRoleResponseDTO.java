package com.sxy.umlmyself.dto;

import lombok.Data;

/**
 * 确认角色响应 DTO
 * 用于返回确认角色后的用户信息和 Token
 */
@Data
public class ConfirmRoleResponseDTO {
    
    /**
     * JWT Token
     */
    private String token;
    
    /**
     * 用户ID
     */
    private Integer userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 角色ID
     */
    private Integer roleId;
    
    /**
     * 角色代码
     */
    private String roleCode;
}

