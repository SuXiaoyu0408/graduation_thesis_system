package com.sxy.umlmyself.dto;

import lombok.Data;

import java.util.List;

/**
 * 用户详情DTO
 */
@Data
public class UserDetailDTO {
    
    /**
     * 用户ID
     */
    private Integer userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 学院ID
     */
    private Integer collegeId;
    
    /**
     * 学院名称
     */
    private String collegeName;
    
    /**
     * 专业ID
     */
    private Integer majorId;
    
    /**
     * 专业名称
     */
    private String majorName;
    
    /**
     * 账号状态（1=正常，0=禁用）
     */
    private Integer status;
    
    /**
     * 角色列表
     */
    private List<RoleDTO> roles;
}

