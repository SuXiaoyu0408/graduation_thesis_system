package com.sxy.umlmyself.entity;

import com.sxy.umlmyself.enums.ThesisStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "thesis_process")
@Data
public class ThesisProcess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "process_id")
    private Long processId;

    @Column(name = "student_id", nullable = false)
    private Integer studentId;

    @Column(name = "supervisor_id")
    private Integer supervisorId;

    /**
     * 评阅老师ID（用于权限校验）
     */
    @Column(name = "reviewer_id")
    private Integer reviewerId;

    /**
     * 答辩小组ID（用于权限校验）
     */
    @Column(name = "defense_team_id")
    private Integer defenseTeamId;

    @Column(name = "thesis_title", length = 200)
    private String thesisTitle;


    @Column(name = "status", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ThesisStatus status = ThesisStatus.init;

    // --- 新增：多级审批状态字段 ---
    @Column(name = "topic_supervisor_approved")
    private Boolean topicSupervisorApproved = false;

    @Column(name = "topic_major_leader_approved")
    private Boolean topicMajorLeaderApproved = false;

    @Column(name = "topic_college_leader_approved")
    private Boolean topicCollegeLeaderApproved = false;

    @Column(name = "task_major_leader_approved")
    private Boolean taskMajorLeaderApproved = false;

    @Column(name = "task_college_leader_approved")
    private Boolean taskCollegeLeaderApproved = false;

    @Column(name = "opening_supervisor_approved")
    private Boolean openingSupervisorApproved = false;

    @Column(name = "opening_major_leader_approved")
    private Boolean openingMajorLeaderApproved = false;
    // --- 审批字段结束 ---

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
