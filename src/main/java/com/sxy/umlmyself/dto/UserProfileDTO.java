package com.sxy.umlmyself.dto;

import lombok.Data;

@Data
public class UserProfileDTO {
    private String account; // 账号/学号
    private String name; // 姓名
    private String roleId; // 角色代码, e.g., "STUDENT"
    private String phone;
    private String email;
    private String supervisor; // 指导老师姓名
    private String college; // 学院名称
    private String major; // 专业名称
}

