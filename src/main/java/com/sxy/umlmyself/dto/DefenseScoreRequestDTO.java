package com.sxy.umlmyself.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DefenseScoreRequestDTO {

    @NotNull
    private Long processId;

    @Min(0) @Max(40)
    private Integer reportContent;      // 报告内容（40）

    @Min(0) @Max(10)
    private Integer reportProcess;      // 报告过程（10）

    @Min(0) @Max(50)
    private Integer defensePerformance; // 答辩表现（50）
}

