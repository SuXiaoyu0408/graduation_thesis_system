package com.sxy.umlmyself.service.impl;

import com.sxy.umlmyself.entity.User;
import com.sxy.umlmyself.repository.UserRepository;
import com.sxy.umlmyself.service.PasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 密码服务实现类
 */
@Service
public class PasswordServiceImpl implements PasswordService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
    /**
     * BCrypt 密码加密器
     */
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * Redis Key 前缀
     */
    private static final String REDIS_KEY_PREFIX = "sms:reset:";
    
    /**
     * 验证码有效期（分钟）
     */
    private static final long CODE_EXPIRE_MINUTES = 5;
    
    /**
     * 验证码校验标记 Key 前缀
     */
    private static final String VERIFY_FLAG_PREFIX = "sms:verify:";
    
    /**
     * 验证码校验标记有效期（分钟）
     */
    private static final long VERIFY_FLAG_EXPIRE_MINUTES = 10;
    
    /**
     * 发送验证码
     * 
     * @param phone 手机号
     * @throws RuntimeException 当手机号不存在时抛出异常
     */
    @Override
    public void sendSmsCode(String phone) {
        // 1. 校验手机号是否存在
        userRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("该手机号未注册"));
        
        // 2. 生成 6 位随机数字验证码
        String code = generateVerificationCode();
        
        // 3. 保存到 Redis（5 分钟过期）
        String redisKey = REDIS_KEY_PREFIX + phone;
        stringRedisTemplate.opsForValue().set(redisKey, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        
        // 注意：实际项目中这里应该调用短信服务发送验证码
        // 这里仅做演示，验证码会存储在 Redis 中
        System.out.println("验证码已发送到手机号 " + phone + "，验证码：" + code);
    }
    
    /**
     * 校验验证码
     * 
     * @param phone 手机号
     * @param code 验证码
     * @throws RuntimeException 当验证码不存在、不匹配或已过期时抛出异常
     */
    @Override
    public void verifyCode(String phone, String code) {
        // 1. 从 Redis 获取验证码
        String redisKey = REDIS_KEY_PREFIX + phone;
        String storedCode = stringRedisTemplate.opsForValue().get(redisKey);
        
        // 2. 校验验证码是否存在
        if (storedCode == null || storedCode.isEmpty()) {
            throw new RuntimeException("验证码不存在或已过期");
        }
        
        // 3. 校验验证码是否匹配
        if (!storedCode.equals(code)) {
            throw new RuntimeException("验证码错误");
        }
        
        // 4. 校验成功后，设置验证通过标记（用于重置密码时校验）
        String verifyFlagKey = VERIFY_FLAG_PREFIX + phone;
        stringRedisTemplate.opsForValue().set(verifyFlagKey, "verified", VERIFY_FLAG_EXPIRE_MINUTES, TimeUnit.MINUTES);
        
        // 5. 删除验证码（防止重复使用）
        stringRedisTemplate.delete(redisKey);
    }
    
    /**
     * 重置密码
     * 
     * @param phone 手机号
     * @param newPassword 新密码
     * @param confirmPassword 确认密码
     * @throws RuntimeException 当两次密码不一致、验证码未校验或用户不存在时抛出异常
     */
    @Override
    public void resetPassword(String phone, String newPassword, String confirmPassword) {
        // 1. 校验两次密码是否一致
        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("两次输入的密码不一致");
        }
        
        // 2. 校验验证码是否已校验通过
        String verifyFlagKey = VERIFY_FLAG_PREFIX + phone;
        String verifyFlag = stringRedisTemplate.opsForValue().get(verifyFlagKey);
        if (verifyFlag == null || !verifyFlag.equals("verified")) {
            throw new RuntimeException("请先校验验证码");
        }
        
        // 3. 查询用户
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 4. 使用 BCrypt 加密新密码
        String encodedPassword = passwordEncoder.encode(newPassword);
        
        // 5. 更新 user 表 password
        user.setPassword(encodedPassword);
        userRepository.save(user);
        
        // 6. 删除 Redis 中的验证码校验标记
        stringRedisTemplate.delete(verifyFlagKey);
    }
    
    /**
     * 生成 6 位随机数字验证码
     * 
     * @return 验证码字符串
     */
    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // 生成 100000-999999 之间的随机数
        return String.valueOf(code);
    }
}

