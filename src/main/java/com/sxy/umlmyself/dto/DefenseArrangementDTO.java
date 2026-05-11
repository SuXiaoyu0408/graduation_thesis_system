package com.sxy.umlmyself.dto;

import lombok.Data;
import java.util.List;

/**
 * 答辩安排DTO
 */
@Data
public class DefenseArrangementDTO {
    /**
     * 答辩小组ID
     */
    private Integer teamId;
    
    /**
     * 答辩小组编号（兼容前端）
     */
    private Integer teamNumber;
    
    /**
     * 答辩教室
     */
    private String classroom;
    
    /**
     * 答辩时间
     */
    private String defenseTime;
    
    /**
     * 小组组长姓名
     */
    private String chairmanName;
    
    /**
     * 答辩学生列表
     */
    private List<DefenseStudentInfoDTO> students;
    
    /**
     * 答辩学生信息（内部类）
     */
    @Data
    public static class DefenseStudentInfoDTO {
        /**
         * 学生姓名
         */
        private String studentName;
        
        /**
         * 学生姓名（兼容前端）
         */
        private String name;
        
        /**
         * 论文题目
         */
        private String thesisTitle;
        
        /**
         * 论文题目（兼容前端）
         */
        private String topic;
    }
}

