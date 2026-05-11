package com.sxy.umlmyself.dto;

import lombok.Data;

/**
 * 角色 DTO
 * 用于返回角色信息
 */
@Data
public class RoleDTO {
    
    /**
     * 角色ID
     */
    private Integer roleId;
    
    /**
     * 角色代码
     */
    private String roleCode;
    
    /**
     * 角色名称
     */
    private String roleName;
}

