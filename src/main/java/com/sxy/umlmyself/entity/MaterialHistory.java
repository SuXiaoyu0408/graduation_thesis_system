package com.sxy.umlmyself.entity;

import com.sxy.umlmyself.enums.MaterialType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 用于存储所有类型论文材料的历史记录，实现版本控制
 */
@Entity
@Table(name = "material_history")
@Data
public class MaterialHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @Column(name = "process_id", nullable = false)
    private Long processId;

    @Enumerated(EnumType.STRING)
    @Column(name = "material_type", length = 50, nullable = false)
    private MaterialType materialType;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "original_filename", length = 255)
    private String originalFilename;

    @Column(name = "version", nullable = false)
    private Integer version = 1;

    @Column(name = "uploader_id", nullable = false)
    private Integer uploaderId; // 上传者（学生/教师 等）

    @Column(name = "is_latest", nullable = false)
    private Boolean latest = true;

    @Column(name = "rejected_reason", length = 500)
    private String rejectedReason;

    @CreationTimestamp
    @Column(name = "uploaded_at", updatable = false)
    private LocalDateTime uploadedAt;
}

