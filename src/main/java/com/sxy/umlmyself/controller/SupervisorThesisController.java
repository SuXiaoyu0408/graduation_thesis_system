package com.sxy.umlmyself.controller;

import com.sxy.umlmyself.common.ApiResponse;
import com.sxy.umlmyself.common.RequireRole;
import com.sxy.umlmyself.dto.ApproveMaterialRequestDTO;
import com.sxy.umlmyself.dto.SupervisedStudentDTO;
import com.sxy.umlmyself.dto.SupervisorScoreRequestDTO;
import com.sxy.umlmyself.enums.MaterialType;
import com.sxy.umlmyself.service.SupervisorThesisService;
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
@RequestMapping("/api/supervisor/thesis")
@RequireRole("SUPERVISOR")
@RequiredArgsConstructor
public class SupervisorThesisController {

    private final SupervisorThesisService supervisorThesisService;

    @PostMapping("/approve")
    public ApiResponse<?> approveMaterial(@Valid @RequestBody ApproveMaterialRequestDTO dto,
                                          @RequestHeader("Authorization") String token) {
        supervisorThesisService.approveMaterial(dto, token);
        return ApiResponse.success("审核操作成功");
    }

    @PostMapping("/material/{processId}/{materialType}")
    public ApiResponse<?> uploadMaterial(@PathVariable Long processId,
                                         @PathVariable MaterialType materialType,
                                         @RequestPart("file") MultipartFile file,
                                         @RequestHeader("Authorization") String token) {
        supervisorThesisService.uploadMaterial(processId, materialType, file, token);
        return ApiResponse.success("材料上传成功");
    }

    @PostMapping("/score")
    public ApiResponse<?> submitScore(@Valid @RequestBody SupervisorScoreRequestDTO dto,
                                      @RequestHeader("Authorization") String token) {
        supervisorThesisService.submitScore(dto, token);
        return ApiResponse.success("评分提交成功");
    }

    @GetMapping("/student/material/{processId}/{materialType}/preview")
    public ResponseEntity<Resource> previewStudentMaterial(
            @PathVariable Long processId,
            @PathVariable MaterialType materialType,
            @RequestHeader("Authorization") String token) {
        try {
            Resource resource = supervisorThesisService.previewStudentMaterial(processId, materialType, token);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .body(resource);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/students")
    public ApiResponse<List<SupervisedStudentDTO>> getSupervisedStudents(
            @RequestHeader("Authorization") String token) {
        try {
            List<SupervisedStudentDTO> students = supervisorThesisService.getSupervisedStudents(token);
            return ApiResponse.success(students);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
