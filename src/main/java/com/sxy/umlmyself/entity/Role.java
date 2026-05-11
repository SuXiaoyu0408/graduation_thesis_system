package com.sxy.umlmyself.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 角色实体类
 * 对应数据库中的 role 表
 */
@Entity
@Table(name = "role")
@Data
public class Role {
    
    /**
     * 角色ID（主键）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Integer roleId;
    
    /**
     * 角色代码
     */
    @Column(name = "role_code", length = 50)
    private String roleCode;
    
    /**
     * 角色名称
     */
    @Column(name = "role_name", length = 50)
    private String roleName;
}

