package com.sxy.umlmyself.dto;

import lombok.Data;

/**
 * 发送验证码请求 DTO
 */
@Data
public class SendSmsCodeRequestDTO {
    
    /**
     * 手机号
     */
    private String phone;
}

