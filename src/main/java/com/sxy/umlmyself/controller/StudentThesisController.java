package com.sxy.umlmyself.controller;

import com.sxy.umlmyself.common.ApiResponse;
import com.sxy.umlmyself.common.RequireRole;
import com.sxy.umlmyself.dto.DefenseArrangementDTO;
import com.sxy.umlmyself.dto.MaterialHistoryDTO;
import com.sxy.umlmyself.dto.ThesisProcessDTO;
import com.sxy.umlmyself.enums.MaterialType;
import com.sxy.umlmyself.service.StudentThesisService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student/thesis")
@RequireRole("STUDENT")
@RequiredArgsConstructor
public class StudentThesisController {

    private final StudentThesisService studentThesisService;

    @PostMapping("/material/{processId}/{materialType}")
    public ApiResponse<?> uploadMaterial(
            @PathVariable Long processId,
            @PathVariable MaterialType materialType,
            @RequestPart("file") MultipartFile file) {
        studentThesisService.uploadMaterial(processId, materialType, file);
        return ApiResponse.success("材料上传成功");
    }

    @GetMapping("/material/history/{historyId}/download")
    public ResponseEntity<Resource> downloadHistoryMaterial(@PathVariable Long historyId) {
        Map<String, Object> result = studentThesisService.downloadHistoryMaterial(historyId);
        Resource resource = (Resource) result.get("resource");
        String originalFilename = (String) result.get("filename");

        ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                .filename(originalFilename, StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .body(resource);
    }

    @GetMapping("/material/{processId}/{materialType}/history")
    public ApiResponse<List<MaterialHistoryDTO>> getMaterialHistory(
            @PathVariable Long processId,
            @PathVariable MaterialType materialType) {
        List<MaterialHistoryDTO> histories = studentThesisService.getMaterialHistory(processId, materialType);
        return ApiResponse.success(histories);
    }

    @GetMapping("/material/{processId}/{materialType}/rejected-reason")
    public ApiResponse<String> getRejectedReason(
            @PathVariable Long processId,
            @PathVariable MaterialType materialType) {
        String reason = studentThesisService.getRejectedReason(processId, materialType);
        return ApiResponse.success(reason);
    }

    @GetMapping("/process")
    public ApiResponse<ThesisProcessDTO> getMyThesisProcess() {
        ThesisProcessDTO process = studentThesisService.getMyThesisProcess();
        return ApiResponse.success(process);
    }

    @DeleteMapping("/material/history/{historyId}")
    public ApiResponse<?> deleteHistoryRecord(@PathVariable Long historyId) {
        studentThesisService.deleteHistoryRecord(historyId);
        return ApiResponse.success("历史记录已删除");
    }

    @GetMapping("/defense/{processId}/arrangement")
    public ApiResponse<DefenseArrangementDTO> getDefenseArrangement(@PathVariable Long processId) {
        DefenseArrangementDTO arrangement = studentThesisService.getDefenseArrangement(processId);
        return ApiResponse.success(arrangement);
    }
}
