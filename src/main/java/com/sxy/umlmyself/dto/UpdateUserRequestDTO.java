package com.sxy.umlmyself.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

/**
 * 更新用户请求DTO
 */
@Data
public class UpdateUserRequestDTO {
    
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 手机号
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    /**
     * 邮箱
     */
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", message = "邮箱格式不正确")
    private String email;
    
    /**
     * 学院ID
     */
    private Integer collegeId;
    
    /**
     * 专业ID
     */
    private Integer majorId;
    
    /**
     * 角色ID列表
     */
    private List<Integer> roleIds;
    
    /**
     * 账号状态（1=正常，0=禁用）
     */
    private Integer status;
}

