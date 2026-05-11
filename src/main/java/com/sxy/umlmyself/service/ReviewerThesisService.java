package com.sxy.umlmyself.service;

import com.sxy.umlmyself.dto.ReviewerPaperDTO;
import com.sxy.umlmyself.dto.ReviewerScoreRequestDTO;
import com.sxy.umlmyself.dto.ReviewerStatisticsDTO;
import com.sxy.umlmyself.enums.MaterialType;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ReviewerThesisService {

    void uploadEvaluationForm(Long processId, MultipartFile file, String token);

    void submitScore(ReviewerScoreRequestDTO dto, String token);

    Resource previewStudentMaterial(Long processId, MaterialType materialType, String token);

    Map<String, Object> downloadStudentMaterial(Long processId, MaterialType materialType, String token);

    List<ReviewerPaperDTO> getPapersForReview(String token);

    ReviewerScoreRequestDTO getScore(Long processId, String token);

    ReviewerStatisticsDTO getStatistics(String token);
}
