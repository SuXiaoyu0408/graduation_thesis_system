package com.sxy.umlmyself.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 专业实体类
 * 对应数据库中的 major 表
 */
@Entity
@Table(name = "major")
@Data
public class Major {
    
    /**
     * 专业ID（主键）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "major_id")
    private Integer majorId;
    
    /**
     * 专业名称
     */
    @Column(name = "major_name", length = 100)
    private String majorName;
    
    /**
     * 专业代码
     */
    @Column(name = "major_code", length = 50)
    private String majorCode;
    
    /**
     * 学院ID（外键）
     */
    @Column(name = "college_id")
    private Integer collegeId;
}

