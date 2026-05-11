package com.sxy.umlmyself.dto;

import lombok.Data;

/**
 * 用户列表查询请求DTO
 * 用于分页查询用户列表，支持筛选条件
 */
@Data
public class UserListRequestDTO {
    
    /**
     * 页码（从1开始）
     */
    private Integer page = 1;
    
    /**
     * 每页大小
     */
    private Integer size = 10;
    
    /**
     * 用户名（模糊查询）
     */
    private String username;
    
    /**
     * 真实姓名（模糊查询）
     */
    private String realName;
    
    /**
     * 角色ID（精确查询）
     */
    private Integer roleId;
    
    /**
     * 学院ID（精确查询）
     */
    private Integer collegeId;
    
    /**
     * 专业ID（精确查询）
     */
    private Integer majorId;
    
    /**
     * 账号状态（1=正常，0=禁用）
     */
    private Integer status;
}

