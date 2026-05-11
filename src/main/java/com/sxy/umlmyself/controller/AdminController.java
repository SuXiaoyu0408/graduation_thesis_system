package com.sxy.umlmyself.controller;

import com.sxy.umlmyself.common.ApiResponse;
import com.sxy.umlmyself.common.RequireRole;
import com.sxy.umlmyself.dto.*;
import com.sxy.umlmyself.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequireRole(requireAdmin = true)
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ApiResponse<UserListResponseDTO> getUserList(
            @ModelAttribute UserListRequestDTO request,
            @RequestHeader("Authorization") String token) {
        UserListResponseDTO response = adminService.getUserList(request);
        return ApiResponse.success(response);
    }

    @GetMapping("/users/{userId}")
    public ApiResponse<UserDetailDTO> getUserDetail(
            @PathVariable Integer userId,
            @RequestHeader("Authorization") String token) {
        UserDetailDTO user = adminService.getUserDetail(userId);
        return ApiResponse.success(user);
    }

    @PostMapping("/users")
    public ApiResponse<UserDetailDTO> createUser(
            @Validated @RequestBody CreateUserRequestDTO request,
            @RequestHeader("Authorization") String token) {
        UserDetailDTO user = adminService.createUser(request);
        return ApiResponse.success(user);
    }

    @PutMapping("/users/{userId}")
    public ApiResponse<UserDetailDTO> updateUser(
            @PathVariable Integer userId,
            @Validated @RequestBody UpdateUserRequestDTO request,
            @RequestHeader("Authorization") String token) {
        UserDetailDTO user = adminService.updateUser(userId, request);
        return ApiResponse.success(user);
    }

    @DeleteMapping("/users/{userId}")
    public ApiResponse<?> deleteUser(
            @PathVariable Integer userId,
            @RequestHeader("Authorization") String token) {
        adminService.deleteUser(userId);
        return ApiResponse.success("删除成功");
    }

    @GetMapping("/archive/statistics")
    public ApiResponse<ArchiveStatisticsDTO> getArchiveStatistics(
            @RequestHeader("Authorization") String token) {
        ArchiveStatisticsDTO statistics = adminService.getArchiveStatistics();
        return ApiResponse.success(statistics);
    }

    @PostMapping("/archive/export")
    public org.springframework.http.ResponseEntity<byte[]> exportArchiveMaterials(
            @RequestBody(required = false) java.util.List<Long> processIds,
            @RequestHeader("Authorization") String token) {
        byte[] zipBytes = adminService.exportArchiveMaterials(processIds);
        return org.springframework.http.ResponseEntity.ok()
                .header("Content-Type", "application/zip")
                .header("Content-Disposition", "attachment; filename=archive_materials.zip")
                .body(zipBytes);
    }
}
