package com.sxy.umlmyself.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 最终成绩等级划分
 */
@Getter
@AllArgsConstructor
public enum GradeLevel {
    EXCELLENT("EXCELLENT", "优秀"),
    GOOD("GOOD", "良好"),
    AVERAGE("AVERAGE", "中等"),
    PASS("PASS", "合格"),
    FAIL("FAIL", "不合格");

    private final String code;
    private final String description;
}

