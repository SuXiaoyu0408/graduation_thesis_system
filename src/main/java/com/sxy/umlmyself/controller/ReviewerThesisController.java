package com.sxy.umlmyself.controller;

import com.sxy.umlmyself.common.ApiResponse;
import com.sxy.umlmyself.common.RequireRole;
import com.sxy.umlmyself.dto.ReviewerPaperDTO;
import com.sxy.umlmyself.dto.ReviewerScoreRequestDTO;
import com.sxy.umlmyself.dto.ReviewerStatisticsDTO;
import com.sxy.umlmyself.enums.MaterialType;
import com.sxy.umlmyself.service.ReviewerThesisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/reviewer/thesis")
@RequireRole("REVIEWER")
@RequiredArgsConstructor
public class ReviewerThesisController {

    private final ReviewerThesisService reviewerThesisService;

    @PostMapping("/evaluation-form/{processId}")
    public ApiResponse<?> uploadEvaluationForm(@PathVariable Long processId,
                                               @RequestPart("file") MultipartFile file,
                                               @RequestHeader("Authorization") String token) {
        try {
            reviewerThesisService.uploadEvaluationForm(processId, file, token);
            return ApiResponse.success("评阅表上传成功");
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/score")
    public ApiResponse<?> submitScore(@Valid @RequestBody ReviewerScoreRequestDTO dto,
                                      @RequestHeader("Authorization") String token) {
        try {
            reviewerThesisService.submitScore(dto, token);
            return ApiResponse.success("评分提交成功");
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/student/material/{processId}/{materialType}/preview")
    public ResponseEntity<Resource> previewStudentMaterial(
            @PathVariable Long processId,
            @PathVariable MaterialType materialType,
            @RequestHeader("Authorization") String token) {
        try {
            Resource resource = reviewerThesisService.previewStudentMaterial(processId, materialType, token);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .body(resource);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/student/material/{processId}/{materialType}/download")
    public ResponseEntity<Resource> downloadStudentMaterial(
            @PathVariable Long processId,
            @PathVariable MaterialType materialType,
            @RequestHeader("Authorization") String token) {
        try {
            Resource resource = reviewerThesisService.downloadStudentMaterial(processId, materialType, token);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment")
                    .body(resource);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/papers")
    public ApiResponse<List<ReviewerPaperDTO>> getPapersForReview(
            @RequestHeader("Authorization") String token) {
        try {
            List<ReviewerPaperDTO> papers = reviewerThesisService.getPapersForReview(token);
            return ApiResponse.success(papers);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/score/{processId}")
    public ApiResponse<ReviewerScoreRequestDTO> getScore(
            @PathVariable Long processId,
            @RequestHeader("Authorization") String token) {
        try {
            ReviewerScoreRequestDTO score = reviewerThesisService.getScore(processId, token);
            return ApiResponse.success(score);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/statistics")
    public ApiResponse<ReviewerStatisticsDTO> getStatistics(
            @RequestHeader("Authorization") String token) {
        try {
            ReviewerStatisticsDTO stats = reviewerThesisService.getStatistics(token);
            return ApiResponse.success(stats);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
