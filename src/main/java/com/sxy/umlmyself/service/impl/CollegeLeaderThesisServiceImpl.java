package com.sxy.umlmyself.service.impl;

import com.sxy.umlmyself.common.BusinessException;
import com.sxy.umlmyself.dto.MajorDTO;
import com.sxy.umlmyself.dto.ProgressMonitoringDTO;
import com.sxy.umlmyself.entity.Major;
import com.sxy.umlmyself.entity.Student;
import com.sxy.umlmyself.entity.ThesisProcess;
import com.sxy.umlmyself.entity.User;
import com.sxy.umlmyself.enums.ThesisStatus;
import com.sxy.umlmyself.repository.CollegeRepository;
import com.sxy.umlmyself.repository.MajorRepository;
import com.sxy.umlmyself.repository.StudentRepository;
import com.sxy.umlmyself.repository.ThesisProcessRepository;
import com.sxy.umlmyself.repository.UserRepository;
import com.sxy.umlmyself.service.CollegeLeaderThesisService;
import com.sxy.umlmyself.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollegeLeaderThesisServiceImpl implements CollegeLeaderThesisService {

    private final ThesisProcessRepository processRepo;
    private final MajorRepository majorRepo;
    private final CollegeRepository collegeRepo;
    private final StudentRepository studentRepo;
    private final UserRepository userRepo;
    private final JwtUtil jwtUtil;

    @Override
    public List<MajorDTO> getMajors(String token) {
        Integer collegeLeaderId = jwtUtil.getUserIdFromToken(token);
        User collegeLeader = userRepo.findById(collegeLeaderId)
                .orElseThrow(() -> new BusinessException("学院领导不存在"));

        List<Major> majors = majorRepo.findByCollegeId(collegeLeader.getCollegeId());

        return majors.stream().map(major -> {
            MajorDTO dto = new MajorDTO();
            dto.setMajorId(major.getMajorId());
            dto.setMajorName(major.getMajorName());
            dto.setCollegeId(major.getCollegeId());
            collegeRepo.findById(major.getCollegeId()).ifPresent(college -> {
                dto.setCollegeName(college.getCollegeName());
            });

            long studentCount = studentRepo.findAll().stream()
                    .filter(s -> s.getMajor() != null && s.getMajor().getMajorId().equals(major.getMajorId()))
                    .count();
            dto.setStudentCount((int) studentCount);

            long topicCount = processRepo.findAll().stream()
                    .filter(p -> {
                        Student student = studentRepo.findById(p.getStudentId()).orElse(null);
                        return student != null && student.getMajor() != null
                                && student.getMajor().getMajorId().equals(major.getMajorId());
                    })
                    .count();
            dto.setTopicCount((int) topicCount);

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ProgressMonitoringDTO> getProgress(String token) {
        Integer collegeLeaderId = jwtUtil.getUserIdFromToken(token);
        User collegeLeader = userRepo.findById(collegeLeaderId)
                .orElseThrow(() -> new BusinessException("学院领导不存在"));

        List<ThesisProcess> allProcesses = processRepo.findAll().stream()
                .filter(p -> {
                    Student student = studentRepo.findById(p.getStudentId()).orElse(null);
                    return student != null && student.getCollege() != null
                            && student.getCollege().getCollegeId().equals(collegeLeader.getCollegeId());
                })
                .collect(Collectors.toList());

        long total = allProcesses.size();

        List<ProgressMonitoringDTO> progress = new ArrayList<>();

        long topicCompleted = allProcesses.stream()
                .filter(p -> p.getStatus().ordinal() >= ThesisStatus.topic_submitted.ordinal())
                .count();
        progress.add(createProgress("选题申报", topicCompleted, total));

        long openingCompleted = allProcesses.stream()
                .filter(p -> p.getStatus().ordinal() >= ThesisStatus.opening_submitted.ordinal())
                .count();
        progress.add(createProgress("开题报告", openingCompleted, total));

        long midtermCompleted = allProcesses.stream()
                .filter(p -> p.getStatus().ordinal() >= ThesisStatus.midterm_submitted.ordinal())
                .count();
        progress.add(createProgress("中期检查", midtermCompleted, total));

        long finalCompleted = allProcesses.stream()
                .filter(p -> p.getStatus().ordinal() >= ThesisStatus.final_submitted.ordinal())
                .count();
        progress.add(createProgress("论文终稿", finalCompleted, total));

        long defenseCompleted = allProcesses.stream()
                .filter(p -> p.getStatus() == ThesisStatus.defense_scored || p.getStatus() == ThesisStatus.completed)
                .count();
        progress.add(createProgress("答辩", defenseCompleted, total));

        return progress;
    }

    private ProgressMonitoringDTO createProgress(String stage, long completed, long total) {
        ProgressMonitoringDTO dto = new ProgressMonitoringDTO();
        dto.setStage(stage);
        dto.setCompleted(completed);
        dto.setTotal(total);
        dto.setPercentage(total > 0 ? (int) (completed * 100 / total) : 0);
        return dto;
    }
}
