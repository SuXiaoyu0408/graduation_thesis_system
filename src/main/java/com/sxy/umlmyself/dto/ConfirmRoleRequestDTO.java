package com.sxy.umlmyself.dto;

import lombok.Data;

/**
 * 确认角色请求 DTO
 * 用于接收前端提交的用户ID和角色代码
 */
@Data
public class ConfirmRoleRequestDTO {
    
    /**
     * 用户ID
     */
    private Integer userId;
    
    /**
     * 角色代码（用户选择的角色）
     */
    private String roleCode;
}
