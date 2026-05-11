package com.sxy.umlmyself.service;

import com.sxy.umlmyself.dto.ApproveMaterialRequestDTO;
import com.sxy.umlmyself.dto.GradeSummaryDTO;
import com.sxy.umlmyself.dto.MidtermReportDTO;
import com.sxy.umlmyself.dto.TopicSubmissionDTO;
import com.sxy.umlmyself.enums.MaterialType;
import org.springframework.core.io.Resource;

import java.util.List;

public interface MajorLeaderThesisService {

    void approveMaterial(ApproveMaterialRequestDTO dto, String token);

    Resource previewMaterial(Long processId, MaterialType materialType, String token);

    List<TopicSubmissionDTO> getTopicsForApproval(String token);

    List<MidtermReportDTO> getMidtermReports(String token);

    List<GradeSummaryDTO> getGrades(String token);
}
