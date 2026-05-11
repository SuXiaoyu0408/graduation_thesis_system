package com.sxy.umlmyself.controller;

import com.sxy.umlmyself.common.ApiResponse;
import com.sxy.umlmyself.common.RequireRole;
import com.sxy.umlmyself.dto.CreateNoticeRequestDTO;
import com.sxy.umlmyself.dto.NoticeDTO;
import com.sxy.umlmyself.dto.UpdateNoticeRequestDTO;
import com.sxy.umlmyself.service.NoticeService;
import com.sxy.umlmyself.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;
    private final JwtUtil jwtUtil;

    /**
     * 获取最新通知列表（公开接口）
     */
    @GetMapping("/latest")
    public ApiResponse<List<NoticeDTO>> getLatestNotices(@RequestParam(defaultValue = "5") int limit) {
        try {
            List<NoticeDTO> notices = noticeService.getLatestNotices(limit);
            return ApiResponse.success(notices);
        } catch (Exception e) {
            return ApiResponse.error(500, "获取通知列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建通知（管理员接口）
     */
    @PostMapping("/admin")
    @RequireRole(requireAdmin = true)
    public ApiResponse<NoticeDTO> createNotice(
            @Validated @RequestBody CreateNoticeRequestDTO request,
            @RequestHeader("Authorization") String token) {
        try {
            // @RequireRole注解已通过AOP进行权限校验，这里只需获取userId
            Integer userId = jwtUtil.getUserIdFromToken(extractToken(token));
            
            NoticeDTO notice = noticeService.createNotice(request, userId);
            return ApiResponse.success(notice);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 更新通知（管理员接口）
     */
    @PutMapping("/admin/{noticeId}")
    @RequireRole(requireAdmin = true)
    public ApiResponse<NoticeDTO> updateNotice(
            @PathVariable Integer noticeId,
            @Validated @RequestBody UpdateNoticeRequestDTO request,
            @RequestHeader("Authorization") String token) {
        try {
            // @RequireRole注解已通过AOP进行权限校验，这里只需获取userId
            Integer userId = jwtUtil.getUserIdFromToken(extractToken(token));
            
            NoticeDTO notice = noticeService.updateNotice(noticeId, request);
            return ApiResponse.success(notice);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 删除通知（管理员接口）
     */
    @DeleteMapping("/admin/{noticeId}")
    @RequireRole(requireAdmin = true)
    public ApiResponse<?> deleteNotice(
            @PathVariable Integer noticeId,
            @RequestHeader("Authorization") String token) {
        try {
            // @RequireRole注解已通过AOP进行权限校验
            noticeService.deleteNotice(noticeId);
            return ApiResponse.success("删除成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 分页查询所有通知（管理员接口）
     */
    @GetMapping("/admin")
    @RequireRole(requireAdmin = true)
    public ApiResponse<List<NoticeDTO>> getAllNotices(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestHeader("Authorization") String token) {
        try {
            // @RequireRole注解已通过AOP进行权限校验
            List<NoticeDTO> notices = noticeService.getAllNotices(page, size);
            return ApiResponse.success(notices);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 从Authorization header中提取token
     */
    private String extractToken(String authHeader) {
        if (authHeader == null) {
            return null;
        }
        
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        
        // 清理token中的非法字符（JWT字符串只能包含Base64URL字符）
        token = token.trim();
        // 移除所有控制字符（ASCII 0-31）和删除字符（127）
        token = token.replaceAll("[\\x00-\\x1F\\x7F]", "");
        // 移除所有空白字符
        token = token.replaceAll("\\s+", "");
        // 只保留Base64URL字符和点号
        token = token.replaceAll("[^A-Za-z0-9\\-_.]", "");
        
        return token;
    }
}

