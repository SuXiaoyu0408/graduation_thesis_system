package com.sxy.umlmyself.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 评分表：记录指导老师 / 评阅老师 / 答辩小组的结构化评分
 */
@Entity
@Table(name = "score_sheet")
@Data
public class ScoreSheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "score_id")
    private Long scoreId;

    /** 关联 ThesisProcess 主键 */
    @Column(name = "process_id", nullable = false)
    private Long processId;

    /** 评分者角色：SUPERVISOR / REVIEWER / DEFENSE */
    @Column(name = "scorer_role", nullable = false, length = 30)
    private String scorerRole;

    /** 评分者用户ID */
    @Column(name = "scorer_user_id", nullable = false)
    private Integer scorerUserId;

    @Column(name = "score_item1", precision = 5, scale = 2)
    private BigDecimal scoreItem1;
    @Column(name = "score_item2", precision = 5, scale = 2)
    private BigDecimal scoreItem2;
    @Column(name = "score_item3", precision = 5, scale = 2)
    private BigDecimal scoreItem3;
    @Column(name = "score_item4", precision = 5, scale = 2)
    private BigDecimal scoreItem4;

    @Column(name = "total_score", precision = 5, scale = 2)
    private BigDecimal totalScore;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
