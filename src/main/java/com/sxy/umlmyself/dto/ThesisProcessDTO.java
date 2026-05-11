package com.sxy.umlmyself.dto;

import lombok.Data;
import java.util.List;

@Data
public class ThesisProcessDTO {
    private Long processId; // 论文流程ID
    private String title;
    private String supervisor;
    private String status;
    private String rejectReason;
    private List<FileHistoryDTO> history;
}

