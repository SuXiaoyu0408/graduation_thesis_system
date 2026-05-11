package com.sxy.umlmyself.dto;

import lombok.Data;

/**
 * 进度监控DTO
 */
@Data
public class ProgressMonitoringDTO {
    private String stage; // 阶段名称
    private Long completed; // 已完成数量
    private Long total; // 总数量
    private Integer percentage; // 完成百分比
}

