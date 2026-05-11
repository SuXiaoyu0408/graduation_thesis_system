package com.sxy.umlmyself.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SupervisorScoreRequestDTO {

    @NotNull
    private Long processId;

    @Min(0) @Max(20)
    private Integer topicReview;      // 选题与文献综述（20）

    @Min(0) @Max(10)
    private Integer innovation;       // 创新性（10）

    @Min(0) @Max(35)
    private Integer theoryKnowledge;  // 基础理论与专业知识（35）

    @Min(0) @Max(35)
    private Integer attitudeAndWriting; // 态度、写作水平、规范、综合能力（35）
}

