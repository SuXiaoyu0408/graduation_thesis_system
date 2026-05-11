package com.sxy.umlmyself.dto;

import com.sxy.umlmyself.enums.MaterialType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 通用审核请求 DTO（指导老师 / 专业负责人 / 学院领导）
 */
@Data
public class ApproveMaterialRequestDTO {
    @NotNull
    private Long processId;

    @NotNull
    private MaterialType materialType;

    @NotNull
    private Boolean pass;

    private String reason; // 驳回原因，可为空
}

