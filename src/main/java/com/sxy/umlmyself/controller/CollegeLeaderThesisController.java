package com.sxy.umlmyself.controller;

import com.sxy.umlmyself.common.ApiResponse;
import com.sxy.umlmyself.common.RequireRole;
import com.sxy.umlmyself.dto.MajorDTO;
import com.sxy.umlmyself.dto.ProgressMonitoringDTO;
import com.sxy.umlmyself.service.CollegeLeaderThesisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/college-leader/thesis")
@RequireRole("COLLEGE_LEADER")
@RequiredArgsConstructor
public class CollegeLeaderThesisController {

    private final CollegeLeaderThesisService collegeLeaderThesisService;

    @GetMapping("/majors")
    public ApiResponse<List<MajorDTO>> getMajors(
            @RequestHeader("Authorization") String token) {
        try {
            List<MajorDTO> majors = collegeLeaderThesisService.getMajors(token);
            return ApiResponse.success(majors);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/progress")
    public ApiResponse<List<ProgressMonitoringDTO>> getProgress(
            @RequestHeader("Authorization") String token) {
        try {
            List<ProgressMonitoringDTO> progress = collegeLeaderThesisService.getProgress(token);
            return ApiResponse.success(progress);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
