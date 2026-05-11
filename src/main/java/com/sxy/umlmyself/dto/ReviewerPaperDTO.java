package com.sxy.umlmyself.dto;

import lombok.Data;

/**
 * 评阅老师待评阅论文DTO
 */
@Data
public class ReviewerPaperDTO {
    private Long processId;
    private Integer studentId;
    private String studentName;
    private String studentNo;
    private String supervisorName;
    private String thesisTitle;
    private String status;
    private String statusName;
    private Boolean hasEvaluationForm; // 是否已上传评阅表
    private Boolean hasScore; // 是否已评分
}

