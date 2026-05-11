package com.sxy.umlmyself.service;

/**
 * 密码服务接口
 */
public interface PasswordService {
    
    /**
     * 发送验证码
     * 
     * @param phone 手机号
     * @throws RuntimeException 当手机号不存在时抛出异常
     */
    void sendSmsCode(String phone);
    
    /**
     * 校验验证码
     * 
     * @param phone 手机号
     * @param code 验证码
     * @throws RuntimeException 当验证码不存在、不匹配或已过期时抛出异常
     */
    void verifyCode(String phone, String code);
    
    /**
     * 重置密码
     * 
     * @param phone 手机号
     * @param newPassword 新密码
     * @param confirmPassword 确认密码
     * @throws RuntimeException 当两次密码不一致、验证码未校验或用户不存在时抛出异常
     */
    void resetPassword(String phone, String newPassword, String confirmPassword);
}

