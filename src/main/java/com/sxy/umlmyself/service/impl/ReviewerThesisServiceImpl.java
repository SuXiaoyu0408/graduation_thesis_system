package com.sxy.umlmyself.service.impl;

import com.sxy.umlmyself.common.BusinessException;
import com.sxy.umlmyself.dto.ReviewerPaperDTO;
import com.sxy.umlmyself.dto.ReviewerScoreRequestDTO;
import com.sxy.umlmyself.dto.ReviewerStatisticsDTO;
import com.sxy.umlmyself.entity.MaterialHistory;
import com.sxy.umlmyself.entity.ScoreSheet;
import com.sxy.umlmyself.entity.Student;
import com.sxy.umlmyself.entity.ThesisProcess;
import com.sxy.umlmyself.enums.MaterialType;
import com.sxy.umlmyself.repository.MaterialHistoryRepository;
import com.sxy.umlmyself.repository.ScoreSheetRepository;
import com.sxy.umlmyself.repository.StudentRepository;
import com.sxy.umlmyself.repository.ThesisProcessRepository;
import com.sxy.umlmyself.repository.UserRepository;
import com.sxy.umlmyself.service.FileService;
import com.sxy.umlmyself.service.GradeService;
import com.sxy.umlmyself.service.ReviewerThesisService;
import com.sxy.umlmyself.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewerThesisServiceImpl implements ReviewerThesisService {

    private final ThesisProcessRepository processRepo;
    private final MaterialHistoryRepository historyRepo;
    private final ScoreSheetRepository scoreSheetRepo;
    private final StudentRepository studentRepo;
    private final UserRepository userRepo;
    private final FileService fileService;
    private final JwtUtil jwtUtil;
    private final GradeService gradeService;

    private void validateReviewerAccess(Long processId, Integer reviewerId) {
        ThesisProcess process = processRepo.findById(processId)
                .orElseThrow(() -> new BusinessException("论文流程不存在"));

        if (process.getReviewerId() != null && !process.getReviewerId().equals(reviewerId)) {
            throw new BusinessException("无权限访问，该学生未分配给您评阅");
        }
    }

    @Override
    @Transactional
    public void uploadEvaluationForm(Long processId, MultipartFile file, String token) {
        Integer reviewerId = jwtUtil.getUserIdFromToken(token);
        validateReviewerAccess(processId, reviewerId);

        try {
            String filePath = fileService.uploadFile(file, processId, MaterialType.evaluation_form.name(), reviewerId);

            List<MaterialHistory> oldHistories = historyRepo
                    .findByProcessIdAndMaterialTypeOrderByVersionDesc(processId, MaterialType.evaluation_form);
            oldHistories.forEach(h -> {
                h.setLatest(false);
                historyRepo.save(h);
            });

            MaterialHistory newHistory = new MaterialHistory();
            newHistory.setProcessId(processId);
            newHistory.setMaterialType(MaterialType.evaluation_form);
            newHistory.setFilePath(filePath);
            newHistory.setOriginalFilename(file.getOriginalFilename());
            newHistory.setVersion(oldHistories.isEmpty() ? 1 : oldHistories.get(0).getVersion() + 1);
            newHistory.setUploaderId(reviewerId);
            newHistory.setLatest(true);
            newHistory.setUploadedAt(LocalDateTime.now());
            historyRepo.save(newHistory);
        } catch (IOException e) {
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void submitScore(ReviewerScoreRequestDTO dto, String token) {
        Integer reviewerId = jwtUtil.getUserIdFromToken(token);
        validateReviewerAccess(dto.getProcessId(), reviewerId);

        int total = dto.getTopicReview() + dto.getInnovation() + dto.getTheoryKnowledge() + dto.getWritingSkill();

        ScoreSheet scoreSheet = new ScoreSheet();
        scoreSheet.setProcessId(dto.getProcessId());
        scoreSheet.setScorerRole("REVIEWER");
        scoreSheet.setScorerUserId(reviewerId);
        scoreSheet.setScoreItem1(BigDecimal.valueOf(dto.getTopicReview()));
        scoreSheet.setScoreItem2(BigDecimal.valueOf(dto.getInnovation()));
        scoreSheet.setScoreItem3(BigDecimal.valueOf(dto.getTheoryKnowledge()));
        scoreSheet.setScoreItem4(BigDecimal.valueOf(dto.getWritingSkill()));
        scoreSheet.setTotalScore(BigDecimal.valueOf(total));
        scoreSheet.setCreatedAt(LocalDateTime.now());

        scoreSheetRepo.save(scoreSheet);
        gradeService.tryCalculateFinalGrade(dto.getProcessId());
    }

    @Override
    public Resource previewStudentMaterial(Long processId, MaterialType materialType, String token) {
        Integer reviewerId = jwtUtil.getUserIdFromToken(token);
        validateReviewerAccess(processId, reviewerId);

        if (materialType != MaterialType.topic_selection &&
            materialType != MaterialType.opening_report &&
            materialType != MaterialType.mid_term_report &&
            materialType != MaterialType.final_paper) {
            throw new BusinessException("无权预览此类型材料");
        }

        MaterialHistory latest = historyRepo
                .findByProcessIdAndMaterialTypeOrderByVersionDesc(processId, materialType)
                .stream()
                .filter(h -> Boolean.TRUE.equals(h.getLatest()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("未找到材料"));

        try {
            return fileService.previewFile(latest.getFilePath());
        } catch (IOException e) {
            throw new BusinessException("文件预览失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> downloadStudentMaterial(Long processId, MaterialType materialType, String token) {
        Integer reviewerId = jwtUtil.getUserIdFromToken(token);
        validateReviewerAccess(processId, reviewerId);

        if (materialType != MaterialType.topic_selection &&
            materialType != MaterialType.opening_report &&
            materialType != MaterialType.mid_term_report &&
            materialType != MaterialType.final_paper) {
            throw new BusinessException("无权下载此类型材料");
        }

        MaterialHistory latest = historyRepo
                .findByProcessIdAndMaterialTypeOrderByVersionDesc(processId, materialType)
                .stream()
                .filter(h -> Boolean.TRUE.equals(h.getLatest()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("未找到材料"));

        try {
            Resource resource = fileService.downloadFile(latest.getFilePath());
            Map<String, Object> result = new HashMap<>();
            result.put("resource", resource);
            result.put("filename", latest.getOriginalFilename());
            return result;
        } catch (IOException e) {
            throw new BusinessException("文件下载失败: " + e.getMessage());
        }
    }

    @Override
    public List<ReviewerPaperDTO> getPapersForReview(String token) {
        Integer reviewerId = jwtUtil.getUserIdFromToken(token);

        List<ThesisProcess> processes = processRepo.findByReviewerId(reviewerId);

        return processes.stream().map(process -> {
            ReviewerPaperDTO dto = new ReviewerPaperDTO();
            dto.setProcessId(process.getProcessId());
            dto.setThesisTitle(process.getThesisTitle());
            dto.setStatus(process.getStatus() != null ? process.getStatus().getCode() : null);
            dto.setStatusName(process.getStatus() != null ? process.getStatus().getDescription() : null);

            Student student = studentRepo.findById(process.getStudentId()).orElse(null);
            if (student != null) {
                dto.setStudentId(student.getStuId());
                dto.setStudentName(student.getStuName());
                if (student.getUser() != null) {
                    dto.setStudentNo(student.getUser().getUsername());
                }
            }

            if (process.getSupervisorId() != null) {
                userRepo.findById(process.getSupervisorId()).ifPresent(user -> {
                    dto.setSupervisorName(user.getRealName());
                });
            }

            List<MaterialHistory> evaluationForms = historyRepo
                    .findByProcessIdAndMaterialTypeOrderByVersionDesc(process.getProcessId(), MaterialType.evaluation_form);
            dto.setHasEvaluationForm(!evaluationForms.isEmpty());

            List<ScoreSheet> reviewerScores = scoreSheetRepo.findByProcessIdAndScorerRole(process.getProcessId(), "REVIEWER");
            dto.setHasScore(!reviewerScores.isEmpty());

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public ReviewerScoreRequestDTO getScore(Long processId, String token) {
        Integer reviewerId = jwtUtil.getUserIdFromToken(token);
        validateReviewerAccess(processId, reviewerId);

        List<ScoreSheet> reviewerScores = scoreSheetRepo.findByProcessIdAndScorerRole(processId, "REVIEWER");
        if (reviewerScores.isEmpty()) {
            return null;
        }

        ScoreSheet score = reviewerScores.get(0);
        ReviewerScoreRequestDTO dto = new ReviewerScoreRequestDTO();
        dto.setProcessId(processId);
        dto.setTopicReview(score.getScoreItem1() != null ? score.getScoreItem1().intValue() : 0);
        dto.setInnovation(score.getScoreItem2() != null ? score.getScoreItem2().intValue() : 0);
        dto.setTheoryKnowledge(score.getScoreItem3() != null ? score.getScoreItem3().intValue() : 0);
        dto.setWritingSkill(score.getScoreItem4() != null ? score.getScoreItem4().intValue() : 0);

        return dto;
    }

    @Override
    public ReviewerStatisticsDTO getStatistics(String token) {
        Integer reviewerId = jwtUtil.getUserIdFromToken(token);

        List<ThesisProcess> processes = processRepo.findByReviewerId(reviewerId);

        ReviewerStatisticsDTO stats = new ReviewerStatisticsDTO();
        int totalReviewed = 0;
        double totalScore = 0.0;
        int excellent = 0;

        for (ThesisProcess process : processes) {
            List<ScoreSheet> reviewerScores = scoreSheetRepo.findByProcessIdAndScorerRole(process.getProcessId(), "REVIEWER");
            if (!reviewerScores.isEmpty()) {
                totalReviewed++;
                BigDecimal score = reviewerScores.get(0).getTotalScore();
                if (score != null) {
                    totalScore += score.doubleValue();
                    if (score.doubleValue() >= 90) {
                        excellent++;
                    }
                }
            }
        }

        stats.setTotalReviewed(totalReviewed);
        stats.setPending(processes.size() - totalReviewed);
        stats.setAvgScore(totalReviewed > 0 ? totalScore / totalReviewed : 0.0);
        stats.setExcellent(excellent);

        return stats;
    }
}
