package com.sxy.umlmyself.controller;

import com.sxy.umlmyself.common.ApiResponse;
import com.sxy.umlmyself.common.RequireRole;
import com.sxy.umlmyself.dto.DefenseScoreRequestDTO;
import com.sxy.umlmyself.dto.DefenseStudentDTO;
import com.sxy.umlmyself.dto.DefenseTeamDTO;
import com.sxy.umlmyself.dto.GradeSummaryDTO;
import com.sxy.umlmyself.service.DefenseTeamThesisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/defense-team/thesis")
@RequireRole({"DEFENSE_MEMBER", "DEFENSE_LEADER"})
@RequiredArgsConstructor
public class DefenseTeamThesisController {

    private final DefenseTeamThesisService defenseTeamThesisService;

    @PostMapping("/score")
    public ApiResponse<?> submitScore(@Valid @RequestBody DefenseScoreRequestDTO dto,
                                      @RequestHeader("Authorization") String token) {
        try {
            defenseTeamThesisService.submitScore(dto, token);
            return ApiResponse.success("答辩评分提交成功");
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/students")
    public ApiResponse<List<DefenseStudentDTO>> getStudentsForDefense(
            @RequestHeader("Authorization") String token) {
        try {
            List<DefenseStudentDTO> students = defenseTeamThesisService.getStudentsForDefense(token);
            return ApiResponse.success(students);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/grades")
    public ApiResponse<List<GradeSummaryDTO>> getGrades(
            @RequestHeader("Authorization") String token) {
        try {
            List<GradeSummaryDTO> grades = defenseTeamThesisService.getGrades(token);
            return ApiResponse.success(grades);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/teams")
    public ApiResponse<List<DefenseTeamDTO>> getDefenseTeams(
            @RequestHeader("Authorization") String token) {
        try {
            List<DefenseTeamDTO> teams = defenseTeamThesisService.getDefenseTeams(token);
            return ApiResponse.success(teams);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
