package com.sxy.umlmyself.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 更新通知请求DTO
 */
@Data
public class UpdateNoticeRequestDTO {
    
    /**
     * 通知标题（必填）
     */
    @NotBlank(message = "通知标题不能为空")
    private String title;
    
    /**
     * 通知内容（必填）
     */
    @NotBlank(message = "通知内容不能为空")
    private String content;
}

