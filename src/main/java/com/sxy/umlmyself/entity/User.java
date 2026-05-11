package com.sxy.umlmyself.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 用户实体类
 * 对应数据库中的 user 表
 */
@Entity
@Table(name = "user")
@Data
public class User {
    
    /**
     * 用户ID（主键）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;
    
    /**
     * 用户名
     */
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;
    
    /**
     * 密码（已加密）
     */
    @Column(name = "password", nullable = false, length = 255)
    private String password;
    
    /**
     * 真实姓名
     */
    @Column(name = "real_name", length = 50)
    private String realName;
    
    /**
     * 手机号
     */
    @Column(name = "phone", length = 20, unique = true)
    private String phone;
    
    /**
     * 邮箱
     */
    @Column(name = "email", length = 100)
    private String email;
    
    /**
     * 专业ID
     */
    @Column(name = "major_id")
    private Integer majorId;
    
    /**
     * 学院ID
     */
    @Column(name = "college_id")
    private Integer collegeId;
    
    /**
     * 账号状态（1 表示正常，0 表示禁用）
     */
    @Column(name = "status")
    private Integer status;
}

