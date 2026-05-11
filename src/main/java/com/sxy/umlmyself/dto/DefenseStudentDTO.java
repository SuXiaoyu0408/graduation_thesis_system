package com.sxy.umlmyself.dto;

import lombok.Data;

/**
 * 答辩小组待答辩学生DTO
 */
@Data
public class DefenseStudentDTO {
    private Long processId;
    private Integer studentId;
    private String studentName;
    private String studentNo;
    private String supervisorName;
    private String thesisTitle;
    private String status;
    private String statusName;
    private Boolean hasDefenseReviewForm; // 是否已上传答辩评审表
    private Boolean hasScore; // 是否已评分
}

