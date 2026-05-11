package com.sxy.umlmyself.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewerScoreRequestDTO {

    @NotNull
    private Long processId;

    @Min(0) @Max(20)
    private Integer topicReview;      // 选题与文献综述（20）

    @Min(0) @Max(10)
    private Integer innovation;       // 创新性（10）

    @Min(0) @Max(40)
    private Integer theoryKnowledge;  // 基础理论与专业知识（40）

    @Min(0) @Max(30)
    private Integer writingSkill;     // 写作水平与综合能力（30）
}

