package com.sxy.umlmyself.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 中期检查报告DTO
 */
@Data
public class MidtermReportDTO {
    private Long processId;
    private Integer studentId;
    private String studentName;
    private String studentNo;
    private String supervisorName;
    private String thesisTitle;
    private String status;
    private String statusName;
    private String rejectReason;
    private LocalDateTime submittedAt;
}

