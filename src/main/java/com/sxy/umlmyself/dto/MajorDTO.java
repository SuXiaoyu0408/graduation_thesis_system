package com.sxy.umlmyself.dto;

import lombok.Data;

/**
 * 专业DTO
 */
@Data
public class MajorDTO {
    private Integer majorId;
    private String majorName;
    private Integer collegeId;
    private String collegeName;
    private Integer studentCount; // 学生人数
    private Integer topicCount; // 课题数量
}

