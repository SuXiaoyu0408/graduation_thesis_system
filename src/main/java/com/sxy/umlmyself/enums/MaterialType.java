package com.sxy.umlmyself.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 论文材料类型统一枚举
 */
@Getter
@AllArgsConstructor
public enum MaterialType {
    topic_selection("topic_selection", "选题申报表"),
    task_assignment("task_assignment", "课题任务书"),
    opening_report("opening_report", "开题报告"),
    mid_term_report("mid_term_report", "中期报告"),
    final_paper("final_paper", "论文终稿"),
    review_form("review_form", "审阅表"),
    evaluation_form("evaluation_form", "评阅表"),
    defense_review_form("defense_review_form", "答辩评审表");

    private final String code;
    private final String description;
}
