package com.sxy.umlmyself.dto;

import lombok.Data;

/**
 * 校验验证码请求 DTO
 */
@Data
public class VerifyCodeRequestDTO {
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 验证码
     */
    private String code;
}

