package com.sxy.umlmyself.controller;

import com.sxy.umlmyself.common.ApiResponse;
import com.sxy.umlmyself.common.RequireRole;
import com.sxy.umlmyself.dto.ApproveMaterialRequestDTO;
import com.sxy.umlmyself.dto.GradeSummaryDTO;
import com.sxy.umlmyself.dto.MidtermReportDTO;
import com.sxy.umlmyself.dto.TopicSubmissionDTO;
import com.sxy.umlmyself.enums.MaterialType;
import com.sxy.umlmyself.service.MajorLeaderThesisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/major-leader/thesis")
@RequireRole("MAJOR_LEADER")
@RequiredArgsConstructor
public class MajorLeaderThesisController {

    private final MajorLeaderThesisService majorLeaderThesisService;

    @PostMapping("/approve")
    public ApiResponse<?> approveMaterial(@Valid @RequestBody ApproveMaterialRequestDTO dto,
                                          @RequestHeader("Authorization") String token) {
        try {
            majorLeaderThesisService.approveMaterial(dto, token);
            return ApiResponse.success("审核操作成功");
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/material/{processId}/{materialType}/preview")
    public ResponseEntity<Resource> previewMaterial(
            @PathVariable Long processId,
            @PathVariable MaterialType materialType,
            @RequestHeader("Authorization") String token) {
        try {
            Resource resource = majorLeaderThesisService.previewMaterial(processId, materialType, token);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .body(resource);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/topics")
    public ApiResponse<List<TopicSubmissionDTO>> getTopicsForApproval(
            @RequestHeader("Authorization") String token) {
        try {
            List<TopicSubmissionDTO> topics = majorLeaderThesisService.getTopicsForApproval(token);
            return ApiResponse.success(topics);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/midterm-reports")
    public ApiResponse<List<MidtermReportDTO>> getMidtermReports(
            @RequestHeader("Authorization") String token) {
        try {
            List<MidtermReportDTO> reports = majorLeaderThesisService.getMidtermReports(token);
            return ApiResponse.success(reports);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/grades")
    public ApiResponse<List<GradeSummaryDTO>> getGrades(
            @RequestHeader("Authorization") String token) {
        try {
            List<GradeSummaryDTO> grades = majorLeaderThesisService.getGrades(token);
            return ApiResponse.success(grades);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
