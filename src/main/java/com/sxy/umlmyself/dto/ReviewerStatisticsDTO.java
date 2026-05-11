package com.sxy.umlmyself.dto;

import lombok.Data;

/**
 * 评阅统计DTO
 */
@Data
public class ReviewerStatisticsDTO {
    private Integer totalReviewed;      // 已评阅论文数
    private Double avgScore;            // 平均分
    private Integer pending;            // 待评阅数
    private Integer excellent;          // 优秀论文数（>=90分）
}

