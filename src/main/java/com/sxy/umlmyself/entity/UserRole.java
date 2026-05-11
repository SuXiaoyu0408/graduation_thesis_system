package com.sxy.umlmyself.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 用户角色关联实体类
 * 对应数据库中的 user_role 表
 */
@Entity
@Table(name = "user_role")
@Data
public class UserRole {
    
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_role_id")
    private Integer userRoleId;
    
    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Integer userId;
    
    /**
     * 角色ID
     */
    @Column(name = "role_id", nullable = false)
    private Integer roleId;
}

