package com.sxy.umlmyself.dto;

import com.sxy.umlmyself.enums.GradeLevel;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FinalGradeDTO {
    private Long processId;
    private BigDecimal teacherScore;
    private BigDecimal reviewerScore;
    private BigDecimal defenseScore;
    private BigDecimal finalScore;
    private GradeLevel gradeLevel;
}

