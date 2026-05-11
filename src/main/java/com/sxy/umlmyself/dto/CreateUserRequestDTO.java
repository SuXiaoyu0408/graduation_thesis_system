package com.sxy.umlmyself.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

/**
 * 创建用户请求DTO
 */
@Data
public class CreateUserRequestDTO {
    
    /**
     * 用户名（必填）
     */
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    /**
     * 密码（必填）
     */
    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^.{6,}$", message = "密码长度至少6位")
    private String password;
    
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
     * 角色ID列表（必填，至少一个角色）
     */
    @NotNull(message = "角色不能为空")
    private List<Integer> roleIds;
    
    /**
     * 账号状态（1=正常，0=禁用，默认为1）
     */
    private Integer status = 1;
}

