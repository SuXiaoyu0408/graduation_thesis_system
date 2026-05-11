package com.sxy.umlmyself.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NoticeDTO {
    private Integer noticeId;
    private String title;
    private String content;
    private LocalDateTime createTime;
}

