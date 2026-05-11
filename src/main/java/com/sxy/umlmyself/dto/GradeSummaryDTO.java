package com.sxy.umlmyself.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 成绩汇总DTO
 */
@Data
public class GradeSummaryDTO {
    private Long processId;
    private Integer studentId;
    private String studentName;
    private String studentNo;
    private String supervisorName;
    private String thesisTitle;
    private BigDecimal supervisorScore; // 指导老师评分
    private BigDecimal reviewerScore; // 评阅老师评分
    private BigDecimal defenseScore; // 答辩小组评分
    private BigDecimal finalScore; // 最终成绩
    private String gradeLevel; // 成绩等级
}

