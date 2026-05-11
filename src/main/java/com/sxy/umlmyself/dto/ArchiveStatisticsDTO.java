package com.sxy.umlmyself.dto;

import lombok.Data;

import java.util.List;

/**
 * 归档统计DTO
 */
@Data
public class ArchiveStatisticsDTO {
    
    /**
     * 总完成数
     */
    private Long totalCompleted;
    
    /**
     * 按学院统计
     */
    private List<CollegeStatistics> collegeStatistics;
    
    /**
     * 按专业统计
     */
    private List<MajorStatistics> majorStatistics;
    
    /**
     * 学院统计
     */
    @Data
    public static class CollegeStatistics {
        private Integer collegeId;
        private String collegeName;
        private Long completedCount;
    }
    
    /**
     * 专业统计
     */
    @Data
    public static class MajorStatistics {
        private Integer majorId;
        private String majorName;
        private Integer collegeId;
        private String collegeName;
        private Long completedCount;
    }
}

