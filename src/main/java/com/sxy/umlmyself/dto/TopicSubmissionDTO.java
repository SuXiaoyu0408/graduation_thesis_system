package com.sxy.umlmyself.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 待审核选题DTO
 */
@Data
public class TopicSubmissionDTO {
    private Long processId;
    private Integer studentId;
    private String studentName;
    private String studentNo;
    private String supervisorName;
    private String thesisTitle;
    private String status;
    private String statusName;
    private LocalDateTime submittedAt;
}

