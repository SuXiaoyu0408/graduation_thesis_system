package com.sxy.umlmyself.service.impl;

import com.sxy.umlmyself.common.BusinessException;
import com.sxy.umlmyself.dto.ApproveMaterialRequestDTO;
import com.sxy.umlmyself.dto.SupervisedStudentDTO;
import com.sxy.umlmyself.dto.SupervisorScoreRequestDTO;
import com.sxy.umlmyself.entity.MaterialHistory;
import com.sxy.umlmyself.entity.ScoreSheet;
import com.sxy.umlmyself.entity.Student;
import com.sxy.umlmyself.entity.ThesisProcess;
import com.sxy.umlmyself.enums.MaterialType;
import com.sxy.umlmyself.enums.ThesisStatus;
import com.sxy.umlmyself.repository.MaterialHistoryRepository;
import com.sxy.umlmyself.repository.ScoreSheetRepository;
import com.sxy.umlmyself.repository.StudentRepository;
import com.sxy.umlmyself.repository.ThesisProcessRepository;
import com.sxy.umlmyself.repository.UserRepository;
import com.sxy.umlmyself.service.FileService;
import com.sxy.umlmyself.service.GradeService;
import com.sxy.umlmyself.service.SupervisorThesisService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupervisorThesisServiceImpl implements SupervisorThesisService {

    private final ThesisProcessRepository processRepo;
    private final MaterialHistoryRepository historyRepo;
    private final ScoreSheetRepository scoreSheetRepo;
    private final StudentRepository studentRepo;
    private final UserRepository userRepo;
    private final FileService fileService;
    private final JwtUtil jwtUtil;
    private final GradeService gradeService;

    @Override
    @Transactional
    public void approveMaterial(ApproveMaterialRequestDTO dto, String token) {
        Integer supervisorId = jwtUtil.getUserIdFromToken(token);
        log.info("指导老师 {} 审核材料，流程ID: {}, 是否通过: {}", supervisorId, dto.getProcessId(), dto.getPass());
        ThesisProcess process = processRepo.findById(dto.getProcessId())
                .orElseThrow(() -> new RuntimeException("论文流程不存在"));

        if (!process.getSupervisorId().equals(supervisorId)) {
            throw new SecurityException("无权操作此学生的论文流程");
        }

        List<MaterialHistory> histories = historyRepo
                .findByProcessIdAndMaterialTypeOrderByVersionDesc(process.getProcessId(), dto.getMaterialType());
        if (histories.isEmpty()) throw new RuntimeException("未找到对应材料");
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
    @Transactional
    public void uploadMaterial(Long processId, MaterialType materialType, MultipartFile file, String token) {
        Integer supervisorId = jwtUtil.getUserIdFromToken(token);
        ThesisProcess process = processRepo.findById(processId)
                .orElseThrow(() -> new RuntimeException("论文流程不存在"));
        if (!process.getSupervisorId().equals(supervisorId)) {
            throw new SecurityException("无权操作此学生的论文流程");
        }

        try {
            String filePath = fileService.uploadFile(file, processId, materialType.name(), supervisorId);

            List<MaterialHistory> oldHistories = historyRepo
                    .findByProcessIdAndMaterialTypeOrderByVersionDesc(processId, materialType);
            oldHistories.forEach(h -> {
                h.setLatest(false);
                historyRepo.save(h);
            });

            MaterialHistory newHistory = new MaterialHistory();
            newHistory.setProcessId(processId);
            newHistory.setMaterialType(materialType);
            newHistory.setFilePath(filePath);
            newHistory.setOriginalFilename(file.getOriginalFilename());
            newHistory.setVersion(oldHistories.isEmpty() ? 1 : oldHistories.get(0).getVersion() + 1);
            newHistory.setUploaderId(supervisorId);
            newHistory.setLatest(true);
            newHistory.setUploadedAt(LocalDateTime.now());
            historyRepo.save(newHistory);
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void submitScore(SupervisorScoreRequestDTO dto, String token) {
        Integer supervisorId = jwtUtil.getUserIdFromToken(token);

        ThesisProcess process = processRepo.findById(dto.getProcessId())
                .orElseThrow(() -> new RuntimeException("论文流程不存在"));

        if (!process.getSupervisorId().equals(supervisorId)) {
            throw new SecurityException("无权为非指导学生评分");
        }

        int total = dto.getTopicReview() + dto.getInnovation() + dto.getTheoryKnowledge() + dto.getAttitudeAndWriting();

        ScoreSheet scoreSheet = new ScoreSheet();
        scoreSheet.setProcessId(process.getProcessId());
        scoreSheet.setScorerRole("SUPERVISOR");
        scoreSheet.setScorerUserId(supervisorId);
        scoreSheet.setScoreItem1(BigDecimal.valueOf(dto.getTopicReview()));
        scoreSheet.setScoreItem2(BigDecimal.valueOf(dto.getInnovation()));
        scoreSheet.setScoreItem3(BigDecimal.valueOf(dto.getTheoryKnowledge()));
        scoreSheet.setScoreItem4(BigDecimal.valueOf(dto.getAttitudeAndWriting()));
        scoreSheet.setTotalScore(BigDecimal.valueOf(total));
        scoreSheet.setCreatedAt(LocalDateTime.now());

        scoreSheetRepo.save(scoreSheet);
        gradeService.tryCalculateFinalGrade(process.getProcessId());
    }

    @Override
    public Resource previewStudentMaterial(Long processId, MaterialType materialType, String token) {
        validateSupervisorAccess(processId, token);

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
    public List<SupervisedStudentDTO> getSupervisedStudents(String token) {
        String cleanedToken = jwtUtil.cleanToken(token);
        Integer supervisorId = jwtUtil.getUserIdFromToken(cleanedToken);
        log.info("获取指导老师 {} 的学生列表", supervisorId);

        List<ThesisProcess> processes = processRepo.findBySupervisorId(supervisorId);

        return processes.stream().map(process -> {
            SupervisedStudentDTO dto = new SupervisedStudentDTO();
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
                if (student.getMajor() != null) {
                    dto.setMajorName(student.getMajor().getMajorName());
                }
                if (student.getCollege() != null) {
                    dto.setCollegeName(student.getCollege().getCollegeName());
                }
            }

            List<SupervisedStudentDTO.MaterialStatus> materials = new ArrayList<>();
            for (MaterialType type : new MaterialType[]{
                MaterialType.topic_selection, MaterialType.opening_report,
                MaterialType.mid_term_report, MaterialType.final_paper
            }) {
                List<MaterialHistory> histories = historyRepo
                        .findByProcessIdAndMaterialTypeOrderByVersionDesc(process.getProcessId(), type);
                SupervisedStudentDTO.MaterialStatus materialStatus = new SupervisedStudentDTO.MaterialStatus();
                materialStatus.setMaterialType(type.name());
                materialStatus.setMaterialName(type.getDescription());

                if (histories.isEmpty()) {
                    materialStatus.setStatus("none");
                } else {
                    MaterialHistory latest = histories.get(0);
                    if (latest.getRejectedReason() != null) {
                        materialStatus.setStatus("rejected");
                        materialStatus.setRejectReason(latest.getRejectedReason());
                    } else if (isMaterialApproved(process, type)) {
                        materialStatus.setStatus("approved");
                    } else {
                        materialStatus.setStatus("pending");
                    }
                }
                materials.add(materialStatus);
            }
            dto.setMaterials(materials);

            return dto;
        }).collect(Collectors.toList());
    }

    private void validateSupervisorAccess(Long processId, String token) {
        Integer supervisorId = jwtUtil.getUserIdFromToken(token);
        ThesisProcess process = processRepo.findById(processId)
                .orElseThrow(() -> new BusinessException("论文流程不存在"));

        if (!process.getSupervisorId().equals(supervisorId)) {
            throw new BusinessException("无权操作此学生的论文流程");
        }
    }

    private void updateApprovalStatus(ThesisProcess process, MaterialType materialType, boolean approved) {
        switch (materialType) {
            case topic_selection:
                process.setTopicSupervisorApproved(approved);
                break;
            case opening_report:
                process.setOpeningSupervisorApproved(approved);
                break;
            case mid_term_report:
                process.setOpeningSupervisorApproved(approved);
                break;
            case final_paper:
                process.setOpeningSupervisorApproved(approved);
                break;
            default:
                break;
        }
    }

    private boolean isMaterialApproved(ThesisProcess process, MaterialType type) {
        if (type == MaterialType.topic_selection) {
            return Boolean.TRUE.equals(process.getTopicSupervisorApproved());
        } else if (type == MaterialType.opening_report) {
            return Boolean.TRUE.equals(process.getOpeningSupervisorApproved());
        } else if (type == MaterialType.mid_term_report) {
            return process.getStatus() == ThesisStatus.midterm_approved;
        } else if (type == MaterialType.final_paper) {
            return process.getStatus() == ThesisStatus.final_approved;
        }
        return false;
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
        } else if (process.getStatus() == ThesisStatus.midterm_submitted &&
                 Boolean.TRUE.equals(process.getOpeningSupervisorApproved())) {
            process.setStatus(ThesisStatus.midterm_approved);
        } else if (process.getStatus() == ThesisStatus.final_submitted &&
                 Boolean.TRUE.equals(process.getOpeningSupervisorApproved())) {
            process.setStatus(ThesisStatus.final_approved);
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
