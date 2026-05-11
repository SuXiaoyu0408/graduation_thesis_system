package com.sxy.umlmyself.service.impl;

import com.sxy.umlmyself.dto.FinalGradeDTO;
import com.sxy.umlmyself.entity.FinalGrade;
import com.sxy.umlmyself.entity.ScoreSheet;
import com.sxy.umlmyself.entity.ThesisProcess;
import com.sxy.umlmyself.enums.GradeLevel;
import com.sxy.umlmyself.enums.ThesisStatus;
import com.sxy.umlmyself.repository.FinalGradeRepository;
import com.sxy.umlmyself.repository.ScoreSheetRepository;
import com.sxy.umlmyself.repository.ThesisProcessRepository;
import com.sxy.umlmyself.service.GradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GradeServiceImpl implements GradeService {

    private final ScoreSheetRepository scoreSheetRepo;
    private final FinalGradeRepository finalGradeRepo;
    private final ThesisProcessRepository processRepo;

    private static final BigDecimal SUPERVISOR_WEIGHT = new BigDecimal("0.4");
    private static final BigDecimal REVIEWER_WEIGHT = new BigDecimal("0.2");
    private static final BigDecimal DEFENSE_WEIGHT = new BigDecimal("0.4");

    @Override
    @Transactional
    public Optional<FinalGradeDTO> tryCalculateFinalGrade(Long processId) {
        List<ScoreSheet> scores = scoreSheetRepo.findByProcessId(processId);

        Map<String, BigDecimal> scoreMap = scores.stream()
                .collect(Collectors.toMap(ScoreSheet::getScorerRole, ScoreSheet::getTotalScore, (s1, s2) -> s1)); // Avoid duplicates

        BigDecimal supervisorScore = scoreMap.get("SUPERVISOR");
        BigDecimal reviewerScore = scoreMap.get("REVIEWER");
        BigDecimal defenseScore = scoreMap.get("DEFENSE");

        // Check if all three scores are present
        if (supervisorScore == null || reviewerScore == null || defenseScore == null) {
            return Optional.empty();
        }

        // Calculate final score
        BigDecimal finalScore = supervisorScore.multiply(SUPERVISOR_WEIGHT)
                .add(reviewerScore.multiply(REVIEWER_WEIGHT))
                .add(defenseScore.multiply(DEFENSE_WEIGHT))
                .setScale(2, RoundingMode.HALF_UP);

        GradeLevel level = determineGradeLevel(finalScore);

        // Create or update FinalGrade entity
        FinalGrade finalGrade = finalGradeRepo.findByProcessId(processId).orElse(new FinalGrade());
        finalGrade.setProcessId(processId);
        finalGrade.setTeacherScore(supervisorScore);
        finalGrade.setReviewerScore(reviewerScore);
        finalGrade.setDefenseScore(defenseScore);
        finalGrade.setFinalScore(finalScore);
        finalGrade.setGradeLevel(level);
        finalGradeRepo.save(finalGrade);

        // Update thesis process status to COMPLETED
        ThesisProcess process = processRepo.findById(processId).orElseThrow();
        process.setStatus(ThesisStatus.completed);
        processRepo.save(process);

        return Optional.of(toDto(finalGrade));
    }

    private GradeLevel determineGradeLevel(BigDecimal score) {
        double s = score.doubleValue();
        if (s >= 90) return GradeLevel.EXCELLENT;
        if (s >= 80) return GradeLevel.GOOD;
        if (s >= 70) return GradeLevel.AVERAGE;
        if (s >= 60) return GradeLevel.PASS;
        return GradeLevel.FAIL;
    }

    private FinalGradeDTO toDto(FinalGrade entity) {
        FinalGradeDTO dto = new FinalGradeDTO();
        dto.setProcessId(entity.getProcessId());
        dto.setTeacherScore(entity.getTeacherScore());
        dto.setReviewerScore(entity.getReviewerScore());
        dto.setDefenseScore(entity.getDefenseScore());
        dto.setFinalScore(entity.getFinalScore());
        dto.setGradeLevel(entity.getGradeLevel());
        return dto;
    }
}

