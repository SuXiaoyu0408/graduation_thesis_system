package com.sxy.umlmyself.dto;

import lombok.Data;
import java.util.List;

/**
 * 答辩小组DTO
 */
@Data
public class DefenseTeamDTO {
    private Integer teamId;
    private Integer teamNumber;
    private String chairmanName; // 组长姓名
    private Integer chairmanId; // 组长ID
    private List<String> memberNames; // 成员姓名列表
    private List<Integer> memberIds; // 成员ID列表
    private String classroom; // 答辩教室
    private String defenseTime; // 答辩时间
    private Integer studentCount; // 学生数量
}

