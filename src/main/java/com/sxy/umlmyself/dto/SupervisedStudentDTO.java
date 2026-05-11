package com.sxy.umlmyself.dto;

import lombok.Data;
import java.util.List;

/**
 * 指导学生的论文流程信息DTO
 */
@Data
public class SupervisedStudentDTO {
    private Long processId;
    private Integer studentId;
    private String studentName;
    private String studentNo;
    private String majorName;
    private String collegeName;
    private String thesisTitle;
    private String status;
    private String statusName;
    
    /**
     * 材料提交状态
     */
    @Data
    public static class MaterialStatus {
        private String materialType;
        private String materialName;
        private String status; // none, pending, approved, rejected
        private String rejectReason;
    }
    
    private List<MaterialStatus> materials;
}

