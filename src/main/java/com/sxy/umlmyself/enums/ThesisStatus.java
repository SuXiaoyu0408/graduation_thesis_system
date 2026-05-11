package com.sxy.umlmyself.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 论文流程节点状态
 */
@Getter
@AllArgsConstructor
public enum ThesisStatus {
    init("init", "未开始"),
    topic_submitted("topic_submitted", "已提交选题申报表"),
    topic_approved("topic_approved", "选题已通过"),
    topic_rejected("topic_rejected", "选题被驳回"),
    opening_submitted("opening_submitted", "已提交开题报告"),
    opening_approved("opening_approved", "开题已通过"),
    opening_rejected("opening_rejected", "开题被驳回"),
    midterm_submitted("midterm_submitted", "已提交中期报告"),
    midterm_approved("midterm_approved", "中期已通过"),
    midterm_rejected("midterm_rejected", "中期被驳回"),
    final_submitted("final_submitted", "已提交终稿"),
    final_approved("final_approved", "终稿已通过"),
    final_rejected("final_rejected", "终稿被驳回"),
    defense_scored("defense_scored", "答辩评分已完成"),
    completed("completed", "论文流程已完成");

    private final String code;
    private final String description;
}
