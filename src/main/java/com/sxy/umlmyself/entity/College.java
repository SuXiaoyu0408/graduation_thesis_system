package com.sxy.umlmyself.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 学院实体类
 * 对应数据库中的 college 表
 */
@Entity
@Table(name = "college")
@Data
public class College {
    
    /**
     * 学院ID（主键）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "college_id")
    private Integer collegeId;
    
    /**
     * 学院名称
     */
    @Column(name = "college_name", length = 100)
    private String collegeName;
    
    /**
     * 学院代码
     */
    @Column(name = "college_code", length = 50, unique = true)
    private String collegeCode;
}

