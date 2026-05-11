package com.sxy.umlmyself.service.impl;

import com.sxy.umlmyself.common.BusinessException;
import com.sxy.umlmyself.dto.ApproveMaterialRequestDTO;
import com.sxy.umlmyself.dto.GradeSummaryDTO;
import com.sxy.umlmyself.dto.MidtermReportDTO;
import com.sxy.umlmyself.dto.TopicSubmissionDTO;
import com.sxy.umlmyself.entity.MaterialHistory;
import com.sxy.umlmyself.entity.Student;
import com.sxy.umlmyself.entity.ThesisProcess;
import com.sxy.umlmyself.entity.User;
import com.sxy.umlmyself.enums.MaterialType;
import com.sxy.umlmyself.enums.ThesisStatus;
import com.sxy.umlmyself.repository.MaterialHistoryRepository;
import com.sxy.umlmyself.repository.StudentRepository;
import com.sxy.umlmyself.repository.ThesisProcessRepository;
import com.sxy.umlmyself.repository.UserRepository;
import com.sxy.umlmyself.repository.ScoreSheetRepository;
import com.sxy.umlmyself.repository.FinalGradeRepository;
import com.sxy.umlmyself.service.FileService;
import com.sxy.umlmyself.service.MajorLeaderThesisService;
import com.sxy.umlmyself.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MajorLeaderThesisServiceImpl implements MajorLeaderThesisService {

    private final ThesisProcessRepository processRepo;
    private final MaterialHistoryRepository historyRepo;
    private final StudentRepository studentRepo;
    private final UserRepository userRepo;
    private final ScoreSheetRepository scoreSheetRepo;
    private final FinalGradeRepository finalGradeRepo;
    private final FileService fileService;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public void approveMaterial(ApproveMaterialRequestDTO dto, String token) {
        Integer majorLeaderId = jwtUtil.getUserIdFromToken(token);
        ThesisProcess process = processRepo.findById(dto.getProcessId())
                .orElseThrow(() -> new BusinessException("论文流程不存在"));

        Student student = studentRepo.findById(process.getStudentId())
                .orElseThrow(() -> new BusinessException("学生不存在"));
        User majorLeader = userRepo.findById(majorLeaderId)
                .orElseThrow(() -> new BusinessException("专业负责人不存在"));
        if (student.getMajor() == null || !student.getMajor().getMajorId().equals(majorLeader.getMajorId())) {
            throw new BusinessException("无权操作非本专业的学生论文");
        }

        List<MaterialHistory> histories = historyRepo
                .findByProcessIdAndMaterialTypeOrderByVersionDesc(process.getProcessId(), dto.getMaterialType());
        if (histories.isEmpty()) throw new BusinessException("未找到对应材料");
        MaterialHistory latestHistory = histories.get(0);

        if (Boolean.TRUE.equals(dto.getPass())) {
            latestHistory.setRejectedReason(null);
            updateApprovalStatus(process, dto.getMaterialType(), true);
            if (dto.getMaterialType() == MaterialType.topic_selection && process.getThesisTitle() == null) {
                String topicTitle = extractTopicTitleFromFilename(latestHistory.getOriginalFilename());
                if (topicTitle != null && !topicTitle.trim().isEmpty()) {
                    process.setThesisTitle(topicTitle);
                }
            }
            checkAndAdvanceMainStatus(process);
        } else {
            latestHistory.setRejectedReason(dto.getReason());
            updateApprovalStatus(process, dto.getMaterialType(), false);
        }

        historyRepo.save(latestHistory);
        processRepo.save(process);
    }

    @Override
    public Resource previewMaterial(Long processId, MaterialType materialType, String token) {
        Integer majorLeaderId = jwtUtil.getUserIdFromToken(token);
        ThesisProcess process = processRepo.findById(processId)
                .orElseThrow(() -> new BusinessException("论文流程不存在"));
        Student student = studentRepo.findById(process.getStudentId())
                .orElseThrow(() -> new BusinessException("学生不存在"));
        User majorLeader = userRepo.findById(majorLeaderId)
                .orElseThrow(() -> new BusinessException("专业负责人不存在"));
        if (student.getMajor() == null || !student.getMajor().getMajorId().equals(majorLeader.getMajorId())) {
            throw new BusinessException("无权操作非本专业的学生论文");
        }

        MaterialHistory latest = historyRepo
                .findByProcessIdAndMaterialTypeOrderByVersionDesc(processId, materialType)
                .stream()
                .filter(h -> Boolean.TRUE.equals(h.getLatest()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("未找到最新版本材料"));

        try {
            return fileService.previewFile(latest.getFilePath());
        } catch (IOException e) {
            throw new BusinessException("文件预览失败: " + e.getMessage());
        }
    }

    @Override
    public List<TopicSubmissionDTO> getTopicsForApproval(String token) {
        Integer majorLeaderId = jwtUtil.getUserIdFromToken(token);
        User majorLeader = userRepo.findById(majorLeaderId)
                .orElseThrow(() -> new BusinessException("专业负责人不存在"));

        List<ThesisProcess> processes = processRepo.findAll().stream()
                .filter(p -> p.getStatus() == ThesisStatus.topic_submitted)
                .filter(p -> {
                    Student student = studentRepo.findById(p.getStudentId()).orElse(null);
                    return student != null && student.getMajor() != null
                            && student.getMajor().getMajorId().equals(majorLeader.getMajorId());
                })
                .filter(p -> !Boolean.TRUE.equals(p.getTopicMajorLeaderApproved()))
                .collect(Collectors.toList());

        return processes.stream().map(process -> {
            TopicSubmissionDTO dto = new TopicSubmissionDTO();
            dto.setProcessId(process.getProcessId());
            dto.setThesisTitle(process.getThesisTitle());
            dto.setStatus(process.getStatus().getCode());
            dto.setStatusName(process.getStatus().getDescription());

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

            List<MaterialHistory> histories = historyRepo
                    .findByProcessIdAndMaterialTypeOrderByVersionDesc(process.getProcessId(), MaterialType.topic_selection);
            if (!histories.isEmpty()) {
                dto.setSubmittedAt(histories.get(0).getUploadedAt());
            }

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<MidtermReportDTO> getMidtermReports(String token) {
        Integer majorLeaderId = jwtUtil.getUserIdFromToken(token);
        User majorLeader = userRepo.findById(majorLeaderId)
                .orElseThrow(() -> new BusinessException("专业负责人不存在"));

        List<ThesisProcess> processes = processRepo.findAll().stream()
                .filter(p -> p.getStatus() == ThesisStatus.midterm_submitted
                        || p.getStatus() == ThesisStatus.midterm_approved)
                .filter(p -> {
                    Student student = studentRepo.findById(p.getStudentId()).orElse(null);
                    return student != null && student.getMajor() != null
                            && student.getMajor().getMajorId().equals(majorLeader.getMajorId());
                })
                .collect(Collectors.toList());

        return processes.stream().map(process -> {
            MidtermReportDTO dto = new MidtermReportDTO();
            dto.setProcessId(process.getProcessId());
            dto.setThesisTitle(process.getThesisTitle());
            dto.setStatus(process.getStatus().getCode());
            dto.setStatusName(process.getStatus().getDescription());

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

            List<MaterialHistory> histories = historyRepo
                    .findByProcessIdAndMaterialTypeOrderByVersionDesc(process.getProcessId(), MaterialType.mid_term_report);
            if (!histories.isEmpty()) {
                MaterialHistory latest = histories.get(0);
                dto.setSubmittedAt(latest.getUploadedAt());
                dto.setRejectReason(latest.getRejectedReason());
            }

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<GradeSummaryDTO> getGrades(String token) {
        Integer majorLeaderId = jwtUtil.getUserIdFromToken(token);
        User majorLeader = userRepo.findById(majorLeaderId)
                .orElseThrow(() -> new BusinessException("专业负责人不存在"));

        List<ThesisProcess> processes = processRepo.findAll().stream()
                .filter(p -> {
                    Student student = studentRepo.findById(p.getStudentId()).orElse(null);
                    return student != null && student.getMajor() != null
                            && student.getMajor().getMajorId().equals(majorLeader.getMajorId());
                })
                .filter(p -> p.getStatus() == ThesisStatus.completed
                        || p.getStatus() == ThesisStatus.defense_scored)
                .collect(Collectors.toList());

        return processes.stream().map(process -> {
            GradeSummaryDTO dto = new GradeSummaryDTO();
            dto.setProcessId(process.getProcessId());
            dto.setThesisTitle(process.getThesisTitle());

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

            List<com.sxy.umlmyself.entity.ScoreSheet> supervisorScores = scoreSheetRepo
                    .findByProcessIdAndScorerRole(process.getProcessId(), "SUPERVISOR");
            if (!supervisorScores.isEmpty()) {
                dto.setSupervisorScore(supervisorScores.get(0).getTotalScore());
            }

            List<com.sxy.umlmyself.entity.ScoreSheet> reviewerScores = scoreSheetRepo
                    .findByProcessIdAndScorerRole(process.getProcessId(), "REVIEWER");
            if (!reviewerScores.isEmpty()) {
                dto.setReviewerScore(reviewerScores.get(0).getTotalScore());
            }

            List<com.sxy.umlmyself.entity.ScoreSheet> defenseScores = scoreSheetRepo
                    .findByProcessIdAndScorerRole(process.getProcessId(), "DEFENSE");
            if (!defenseScores.isEmpty()) {
                dto.setDefenseScore(defenseScores.get(0).getTotalScore());
            }

            finalGradeRepo.findByProcessId(process.getProcessId()).ifPresent(grade -> {
                dto.setFinalScore(grade.getFinalScore());
                dto.setGradeLevel(grade.getGradeLevel() != null ? grade.getGradeLevel().getCode() : null);
            });

            return dto;
        }).collect(Collectors.toList());
    }

    private void updateApprovalStatus(ThesisProcess process, MaterialType materialType, boolean approved) {
        switch (materialType) {
            case topic_selection:
                process.setTopicMajorLeaderApproved(approved);
                break;
            case opening_report:
                process.setOpeningMajorLeaderApproved(approved);
                break;
            case task_assignment:
                process.setTaskMajorLeaderApproved(approved);
                break;
            default:
                throw new BusinessException("该材料类型不由专业负责人审核: " + materialType);
        }
    }

    private void checkAndAdvanceMainStatus(ThesisProcess process) {
        if (process.getStatus() == ThesisStatus.topic_submitted &&
                Boolean.TRUE.equals(process.getTopicSupervisorApproved()) &&
                Boolean.TRUE.equals(process.getTopicMajorLeaderApproved()) &&
                Boolean.TRUE.equals(process.getTopicCollegeLeaderApproved())) {
            process.setStatus(ThesisStatus.topic_approved);
        } else if (process.getStatus() == ThesisStatus.opening_submitted &&
                Boolean.TRUE.equals(process.getOpeningSupervisorApproved()) &&
                Boolean.TRUE.equals(process.getOpeningMajorLeaderApproved())) {
            process.setStatus(ThesisStatus.opening_approved);
        }
    }

    private String extractTopicTitleFromFilename(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return null;
        }
        String nameWithoutExt = filename;
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            nameWithoutExt = filename.substring(0, lastDotIndex);
        }
        nameWithoutExt = nameWithoutExt.replaceAll("(?i)_?v\\d+$", "");
        nameWithoutExt = nameWithoutExt.replaceAll("(?i)_?版本\\d+$", "");
        nameWithoutExt = nameWithoutExt.replaceAll("(?i)^选题申报表[_-]?", "");
        nameWithoutExt = nameWithoutExt.replaceAll("(?i)[_-]?选题申报表$", "");
        nameWithoutExt = nameWithoutExt.replaceAll("(?i)^选题[_-]?", "");
        nameWithoutExt = nameWithoutExt.replaceAll("(?i)[_-]?选题$", "");
        nameWithoutExt = nameWithoutExt.trim().replaceAll("^[_-]+|[_-]+$", "");
        if (nameWithoutExt.isEmpty()) {
            int dotIndex = filename.lastIndexOf('.');
            return dotIndex > 0 ? filename.substring(0, dotIndex) : filename;
        }
        return nameWithoutExt;
    }
}
