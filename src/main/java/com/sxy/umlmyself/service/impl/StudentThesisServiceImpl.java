package com.sxy.umlmyself.service.impl;

import com.sxy.umlmyself.common.BusinessException;
import com.sxy.umlmyself.dto.DefenseArrangementDTO;
import com.sxy.umlmyself.dto.MaterialHistoryDTO;
import com.sxy.umlmyself.dto.ThesisProcessDTO;
import com.sxy.umlmyself.entity.*;
import com.sxy.umlmyself.enums.MaterialType;
import com.sxy.umlmyself.enums.ThesisStatus;
import com.sxy.umlmyself.repository.*;
import com.sxy.umlmyself.service.FileService;
import com.sxy.umlmyself.service.StudentThesisService;
import com.sxy.umlmyself.util.JwtUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentThesisServiceImpl implements StudentThesisService {

    private final ThesisProcessRepository processRepo;
    private final MaterialHistoryRepository historyRepo;
    private final StudentRepository studentRepo;
    private final UserRepository userRepo;
    private final TeacherRepository teacherRepo;
    private final FileService fileService;
    private final JwtUtil jwtUtil;
    private final HttpServletRequest request;

    @PersistenceContext
    private EntityManager entityManager;

    private Integer getCurrentUserId() {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException("认证头缺失或格式不正确");
        }
        String token = jwtUtil.cleanToken(authHeader.substring(7));
        return jwtUtil.getUserIdFromToken(token);
    }

    @Override
    @Transactional
    public void uploadMaterial(Long processId, MaterialType materialType, MultipartFile file) {
        Integer studentId = getCurrentUserId();
        log.info("学生 {} 上传材料，流程ID: {}, 材料类型: {}, 文件名: {}",
                studentId, processId, materialType, file.getOriginalFilename());

        ThesisProcess process = processRepo.findById(processId)
                .orElseThrow(() -> new BusinessException("论文流程不存在"));

        Student student = studentRepo.findByUser_UserIdWithCollegeAndMajor(studentId)
                .orElseThrow(() -> new BusinessException("学生信息不存在"));

        if (!process.getStudentId().equals(student.getStuId())) {
            throw new BusinessException("无权操作此论文流程");
        }

        if (materialType != MaterialType.topic_selection &&
            materialType != MaterialType.opening_report &&
            materialType != MaterialType.mid_term_report &&
            materialType != MaterialType.final_paper) {
            throw new BusinessException("学生无权上传此类型材料");
        }

        try {
            String filePath = fileService.uploadFile(file, processId, materialType.name(), studentId);

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
            newHistory.setUploaderId(studentId);
            newHistory.setLatest(true);
            newHistory.setRejectedReason(null);
            newHistory.setUploadedAt(LocalDateTime.now());
            historyRepo.save(newHistory);

            try {
                updateProcessFilePathByNativeSQL(processId, materialType, filePath);
            } catch (Exception e) {
                log.error("更新thesis_process表文件路径失败，流程ID: {}, 错误: {}", processId, e.getMessage(), e);
                if (e.getMessage() != null && (e.getMessage().contains("Unknown column") ||
                    e.getMessage().contains("does not exist") ||
                    e.getMessage().contains("column") && e.getMessage().contains("not found"))) {
                    throw new BusinessException("数据库字段不存在，请执行数据库迁移脚本: add_file_path_fields_to_thesis_process.sql");
                }
                throw new BusinessException("更新文件路径失败: " + e.getMessage());
            }

            updateProcessStatus(process, materialType);
            processRepo.save(process);

        } catch (IOException e) {
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> downloadHistoryMaterial(Long historyId) {
        MaterialHistory history = historyRepo.findById(historyId)
                .orElseThrow(() -> new BusinessException("材料历史记录不存在"));
        validateStudentAccess(history.getProcessId());
        try {
            Resource resource = fileService.downloadFile(history.getFilePath());
            Map<String, Object> result = new HashMap<>();
            result.put("resource", resource);
            result.put("filename", history.getOriginalFilename());
            return result;
        } catch (IOException e) {
            throw new BusinessException("文件下载失败: " + e.getMessage());
        }
    }

    @Override
    public List<MaterialHistoryDTO> getMaterialHistory(Long processId, MaterialType materialType) {
        validateStudentAccess(processId);
        List<MaterialHistory> histories = historyRepo
                .findByProcessIdAndMaterialTypeOrderByVersionDesc(processId, materialType);
        return histories.stream().map(h -> {
            MaterialHistoryDTO dto = new MaterialHistoryDTO();
            dto.setHistoryId(h.getHistoryId());
            dto.setMaterialType(h.getMaterialType());
            dto.setFilePath(h.getFilePath());
            dto.setVersion(h.getVersion());
            dto.setIsLatest(h.getLatest());
            dto.setRejectedReason(h.getRejectedReason());
            dto.setUploadedAt(h.getUploadedAt());
            userRepo.findById(h.getUploaderId()).ifPresent(user -> {
                dto.setUploaderName(user.getRealName());
            });
            dto.setOriginalFilename(h.getOriginalFilename());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public String getRejectedReason(Long processId, MaterialType materialType) {
        validateStudentAccess(processId);
        return historyRepo
                .findByProcessIdAndMaterialTypeOrderByVersionDesc(processId, materialType)
                .stream()
                .filter(h -> Boolean.TRUE.equals(h.getLatest()))
                .findFirst()
                .map(MaterialHistory::getRejectedReason)
                .orElse(null);
    }

    @Override
    @Transactional
    public ThesisProcessDTO getMyThesisProcess() {
        Integer studentId = getCurrentUserId();
        Student student = studentRepo.findByUser_UserIdWithCollegeAndMajor(studentId)
                .orElseThrow(() -> new BusinessException("学生信息不存在"));

        ThesisProcess process = processRepo.findFirstByStudentIdOrderByProcessIdDesc(student.getStuId())
                .orElseGet(() -> {
                    log.info("学生 {} 没有论文流程，自动创建初始流程", student.getStuId());
                    ThesisProcess newProcess = new ThesisProcess();
                    newProcess.setStudentId(student.getStuId());
                    if (student.getTeaSupervisorId() != null) {
                        teacherRepo.findByTeaId(student.getTeaSupervisorId()).ifPresent(teacher -> {
                            if (teacher.getUser() != null && teacher.getUser().getUserId() != null) {
                                newProcess.setSupervisorId(teacher.getUser().getUserId());
                            }
                        });
                    }
                    newProcess.setStatus(ThesisStatus.init);
                    ThesisProcess savedProcess = processRepo.save(newProcess);
                    log.info("为学生 {} 创建了新的论文流程，ID: {}, 初始状态: {}", student.getStuId(), savedProcess.getProcessId(), savedProcess.getStatus());
                    return savedProcess;
                });

        String thesisTitle = process.getThesisTitle();
        if (thesisTitle == null || thesisTitle.trim().isEmpty()) {
            List<MaterialHistory> topicHistories = historyRepo
                    .findByProcessIdAndMaterialTypeOrderByVersionDesc(process.getProcessId(), MaterialType.topic_selection);
            if (!topicHistories.isEmpty()) {
                String extractedTitle = extractTopicTitleFromFilename(topicHistories.get(0).getOriginalFilename());
                if (extractedTitle != null && !extractedTitle.trim().isEmpty()) {
                    thesisTitle = extractedTitle;
                    process.setThesisTitle(extractedTitle);
                    processRepo.save(process);
                }
            }
        }

        ThesisProcessDTO dto = new ThesisProcessDTO();
        dto.setProcessId(process.getProcessId());
        dto.setTitle(thesisTitle);
        dto.setStatus(process.getStatus() != null ? process.getStatus().getCode() : null);

        if (student.getTeaSupervisorId() != null) {
            teacherRepo.findByTeaId(student.getTeaSupervisorId()).ifPresent(teacher -> {
                String supervisorName = teacher.getTeaName();
                if (supervisorName != null && !supervisorName.trim().isEmpty()) {
                    if (supervisorName.contains("学生")) {
                        log.error("警告：学生 {} 的指导老师ID {} 对应的Teacher.teaName为'{}'，这可能是数据错误！",
                                student.getStuId(), student.getTeaSupervisorId(), supervisorName);
                    } else {
                        dto.setSupervisor(supervisorName);
                    }
                }
                if (teacher.getUser() != null && teacher.getUser().getUserId() != null) {
                    Integer teacherUserId = teacher.getUser().getUserId();
                    if (!teacherUserId.equals(process.getSupervisorId())) {
                        process.setSupervisorId(teacherUserId);
                        processRepo.save(process);
                    }
                }
            });

            if (dto.getSupervisor() == null || dto.getSupervisor().isEmpty()) {
                if (process.getSupervisorId() != null) {
                    userRepo.findById(process.getSupervisorId()).ifPresent(user -> {
                        boolean isStudent = studentRepo.findByUser_UserId(process.getSupervisorId()).isPresent();
                        if (!isStudent) {
                            dto.setSupervisor(user.getRealName());
                        }
                    });
                }
            }
        } else if (process.getSupervisorId() != null) {
            userRepo.findById(process.getSupervisorId()).ifPresent(user -> {
                boolean isStudent = studentRepo.findByUser_UserId(process.getSupervisorId()).isPresent();
                if (!isStudent) {
                    dto.setSupervisor(user.getRealName());
                }
            });
        }

        return dto;
    }

    @Override
    public DefenseArrangementDTO getDefenseArrangement(Long processId) {
        validateStudentAccess(processId);

        ThesisProcess process = processRepo.findById(processId)
                .orElseThrow(() -> new BusinessException("论文流程不存在"));

        if (process.getDefenseTeamId() == null) {
            return null;
        }

        DefenseArrangementDTO dto = new DefenseArrangementDTO();
        dto.setTeamId(process.getDefenseTeamId());
        dto.setTeamNumber(process.getDefenseTeamId());
        dto.setClassroom(null);
        dto.setDefenseTime(null);
        dto.setChairmanName(null);

        List<ThesisProcess> teamProcesses = processRepo.findByDefenseTeamId(process.getDefenseTeamId());
        List<DefenseArrangementDTO.DefenseStudentInfoDTO> students = teamProcesses.stream()
                .map(tp -> {
                    Student s = studentRepo.findById(tp.getStudentId()).orElse(null);
                    if (s == null) return null;
                    DefenseArrangementDTO.DefenseStudentInfoDTO info = new DefenseArrangementDTO.DefenseStudentInfoDTO();
                    info.setStudentName(s.getStuName());
                    info.setName(s.getStuName());
                    info.setThesisTitle(tp.getThesisTitle());
                    info.setTopic(tp.getThesisTitle());
                    return info;
                })
                .filter(s -> s != null)
                .collect(Collectors.toList());

        dto.setStudents(students);
        return dto;
    }

    private void validateStudentAccess(Long processId) {
        Integer studentId = getCurrentUserId();
        Student student = studentRepo.findByUser_UserIdWithCollegeAndMajor(studentId)
                .orElseThrow(() -> new BusinessException("学生信息不存在"));
        ThesisProcess process = processRepo.findById(processId)
                .orElseThrow(() -> new BusinessException("论文流程不存在"));
        if (!process.getStudentId().equals(student.getStuId())) {
            throw new BusinessException("无权访问此论文流程");
        }
    }

    private void updateProcessFilePathByNativeSQL(Long processId, MaterialType materialType, String filePath) {
        String sql;
        switch (materialType) {
            case topic_selection:
                sql = "UPDATE thesis_process SET topic_selection_file_path = :filePath WHERE process_id = :processId";
                break;
            case opening_report:
                sql = "UPDATE thesis_process SET opening_report_file_path = :filePath WHERE process_id = :processId";
                break;
            case mid_term_report:
                sql = "UPDATE thesis_process SET mid_term_report_file_path = :filePath WHERE process_id = :processId";
                break;
            case final_paper:
                sql = "UPDATE thesis_process SET final_paper_file_path = :filePath WHERE process_id = :processId";
                break;
            default:
                return;
        }
        entityManager.createNativeQuery(sql)
                .setParameter("filePath", filePath)
                .setParameter("processId", processId)
                .executeUpdate();
    }

    private void updateProcessStatus(ThesisProcess process, MaterialType materialType) {
        switch (materialType) {
            case topic_selection:
                process.setStatus(ThesisStatus.topic_submitted);
                break;
            case opening_report:
                process.setStatus(ThesisStatus.opening_submitted);
                break;
            case mid_term_report:
                process.setStatus(ThesisStatus.midterm_submitted);
                break;
            case final_paper:
                process.setStatus(ThesisStatus.final_submitted);
                break;
            default:
                break;
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
