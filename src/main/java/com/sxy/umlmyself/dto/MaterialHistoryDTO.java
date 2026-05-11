package com.sxy.umlmyself.dto;

import com.sxy.umlmyself.enums.MaterialType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialHistoryDTO {

    private Long historyId;

    private MaterialType materialType;

    private String filePath;

    private Integer version;

    private String uploaderName; // 上传者真实姓名

    private Boolean isLatest;

    private String rejectedReason;

    private LocalDateTime uploadedAt;

    private String originalFilename; // 文件的原始名称
}

