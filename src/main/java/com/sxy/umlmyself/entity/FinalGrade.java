package com.sxy.umlmyself.entity;

import com.sxy.umlmyself.enums.GradeLevel;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 最终成绩实体
 */
@Entity
@Table(name = "final_grade")
@Data
public class FinalGrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "grade_id")
    private Long gradeId;

    @Column(name = "process_id", nullable = false, unique = true)
    private Long processId;

    @Column(name = "teacher_score", precision = 5, scale = 2)
    private BigDecimal teacherScore;

    @Column(name = "reviewer_score", precision = 5, scale = 2)
    private BigDecimal reviewerScore;

    @Column(name = "defense_score", precision = 5, scale = 2)
    private BigDecimal defenseScore;

    @Column(name = "final_score", precision = 5, scale = 2)
    private BigDecimal finalScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "grade_level", length = 20)
    private GradeLevel gradeLevel;

    @CreationTimestamp
    @Column(name = "calculated_at", updatable = false)
    private LocalDateTime calculatedAt;
}
