package com.sxy.umlmyself.service.impl;

import com.sxy.umlmyself.common.BusinessException;
import com.sxy.umlmyself.dto.DefenseScoreRequestDTO;
import com.sxy.umlmyself.dto.DefenseStudentDTO;
import com.sxy.umlmyself.dto.DefenseTeamDTO;
import com.sxy.umlmyself.dto.GradeSummaryDTO;
import com.sxy.umlmyself.entity.MaterialHistory;
import com.sxy.umlmyself.entity.Role;
import com.sxy.umlmyself.entity.ScoreSheet;
import com.sxy.umlmyself.entity.Student;
import com.sxy.umlmyself.entity.ThesisProcess;
import com.sxy.umlmyself.enums.MaterialType;
import com.sxy.umlmyself.enums.ThesisStatus;
import com.sxy.umlmyself.repository.MaterialHistoryRepository;
import com.sxy.umlmyself.repository.RoleRepository;
import com.sxy.umlmyself.repository.ScoreSheetRepository;
import com.sxy.umlmyself.repository.StudentRepository;
import com.sxy.umlmyself.repository.ThesisProcessRepository;
import com.sxy.umlmyself.repository.UserRepository;
import com.sxy.umlmyself.service.DefenseTeamThesisService;
import com.sxy.umlmyself.service.GradeService;
import com.sxy.umlmyself.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefenseTeamThesisServiceImpl implements DefenseTeamThesisService {

    private final ThesisProcessRepository processRepo;
    private final MaterialHistoryRepository historyRepo;
    private final ScoreSheetRepository scoreSheetRepo;
    private final StudentRepository studentRepo;
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final JwtUtil jwtUtil;
    private final GradeService gradeService;

    private void validateDefenseTeamAccess(Long processId, Integer memberId) {
        ThesisProcess process = processRepo.findById(processId)
                .orElseThrow(() -> new BusinessException("论文流程不存在"));

        if (process.getDefenseTeamId() != null && !process.getDefenseTeamId().equals(memberId)) {
            throw new BusinessException("无权限访问，该学生未分配给您所在的答辩小组");
        }
    }

    @Override
    @Transactional
    public void submitScore(DefenseScoreRequestDTO dto, String token) {
        Integer memberId = jwtUtil.getUserIdFromToken(token);
        validateDefenseTeamAccess(dto.getProcessId(), memberId);

        int total = dto.getReportContent() + dto.getReportProcess() + dto.getDefensePerformance();

        ScoreSheet scoreSheet = new ScoreSheet();
        scoreSheet.setProcessId(dto.getProcessId());
        scoreSheet.setScorerRole("DEFENSE");
        scoreSheet.setScorerUserId(memberId);
        scoreSheet.setScoreItem1(BigDecimal.valueOf(dto.getReportContent()));
        scoreSheet.setScoreItem2(BigDecimal.valueOf(dto.getReportProcess()));
        scoreSheet.setScoreItem3(BigDecimal.valueOf(dto.getDefensePerformance()));
        scoreSheet.setTotalScore(BigDecimal.valueOf(total));
        scoreSheet.setCreatedAt(LocalDateTime.now());

        scoreSheetRepo.save(scoreSheet);
        gradeService.tryCalculateFinalGrade(dto.getProcessId());
    }

    @Override
    public List<DefenseStudentDTO> getStudentsForDefense(String token) {
        Integer memberId = jwtUtil.getUserIdFromToken(token);

        List<ThesisProcess> processes = processRepo.findByDefenseTeamId(memberId);

        return processes.stream().map(process -> {
            DefenseStudentDTO dto = new DefenseStudentDTO();
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

            List<MaterialHistory> defenseForms = historyRepo
                    .findByProcessIdAndMaterialTypeOrderByVersionDesc(process.getProcessId(), MaterialType.defense_review_form);
            dto.setHasDefenseReviewForm(!defenseForms.isEmpty());

            List<ScoreSheet> defenseScores = scoreSheetRepo.findByProcessIdAndScorerRole(process.getProcessId(), "DEFENSE");
            dto.setHasScore(!defenseScores.isEmpty());

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<GradeSummaryDTO> getGrades(String token) {
        Integer leaderId = jwtUtil.getUserIdFromToken(token);

        List<ThesisProcess> processes = processRepo.findByDefenseTeamId(leaderId).stream()
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

            List<ScoreSheet> supervisorScores = scoreSheetRepo
                    .findByProcessIdAndScorerRole(process.getProcessId(), "SUPERVISOR");
            if (!supervisorScores.isEmpty()) {
                dto.setSupervisorScore(supervisorScores.get(0).getTotalScore());
            }

            List<ScoreSheet> reviewerScores = scoreSheetRepo
                    .findByProcessIdAndScorerRole(process.getProcessId(), "REVIEWER");
            if (!reviewerScores.isEmpty()) {
                dto.setReviewerScore(reviewerScores.get(0).getTotalScore());
            }

            List<ScoreSheet> defenseScores = scoreSheetRepo
                    .findByProcessIdAndScorerRole(process.getProcessId(), "DEFENSE");
            if (!defenseScores.isEmpty()) {
                dto.setDefenseScore(defenseScores.get(0).getTotalScore());
            }

            if (dto.getSupervisorScore() != null && dto.getReviewerScore() != null && dto.getDefenseScore() != null) {
                BigDecimal finalScore = dto.getSupervisorScore()
                        .multiply(new BigDecimal("0.4"))
                        .add(dto.getReviewerScore().multiply(new BigDecimal("0.2")))
                        .add(dto.getDefenseScore().multiply(new BigDecimal("0.4")));
                dto.setFinalScore(finalScore);

                double score = finalScore.doubleValue();
                if (score >= 90) {
                    dto.setGradeLevel("优秀");
                } else if (score >= 80) {
                    dto.setGradeLevel("良好");
                } else if (score >= 70) {
                    dto.setGradeLevel("中等");
                } else if (score >= 60) {
                    dto.setGradeLevel("合格");
                } else {
                    dto.setGradeLevel("不合格");
                }
            }

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<DefenseTeamDTO> getDefenseTeams(String token) {
        Integer userId = jwtUtil.getUserIdFromToken(token);

        List<ThesisProcess> allProcesses = processRepo.findAll();

        Map<Integer, List<ThesisProcess>> teamMap = allProcesses.stream()
                .filter(p -> p.getDefenseTeamId() != null)
                .collect(Collectors.groupingBy(ThesisProcess::getDefenseTeamId));

        return teamMap.entrySet().stream().map(entry -> {
            Integer teamId = entry.getKey();
            List<ThesisProcess> teamProcesses = entry.getValue();

            DefenseTeamDTO dto = new DefenseTeamDTO();
            dto.setTeamId(teamId);
            dto.setTeamNumber(teamId);
            dto.setStudentCount(teamProcesses.size());

            userRepo.findAll().stream()
                    .filter(user -> {
                        List<Role> roles = roleRepo.findRolesByUserId(user.getUserId());
                        return roles.stream().anyMatch(r -> "DEFENSE_LEADER".equals(r.getRoleCode()));
                    })
                    .filter(user -> user.getUserId().equals(teamId))
                    .findFirst()
                    .ifPresent(leader -> {
                        dto.setChairmanId(leader.getUserId());
                        dto.setChairmanName(leader.getRealName() != null ? leader.getRealName() : leader.getUsername());
                    });

            List<String> memberNames = new ArrayList<>();
            List<Integer> memberIds = new ArrayList<>();
            userRepo.findAll().stream()
                    .filter(user -> {
                        List<Role> roles = roleRepo.findRolesByUserId(user.getUserId());
                        return roles.stream().anyMatch(r -> "DEFENSE_MEMBER".equals(r.getRoleCode()));
                    })
                    .forEach(member -> {
                        memberIds.add(member.getUserId());
                        memberNames.add(member.getRealName() != null ? member.getRealName() : member.getUsername());
                    });
            dto.setMemberIds(memberIds);
            dto.setMemberNames(memberNames);

            if (!teamProcesses.isEmpty()) {
                dto.setClassroom("待安排");
                dto.setDefenseTime("待安排");
            }

            return dto;
        }).collect(Collectors.toList());
    }
}
