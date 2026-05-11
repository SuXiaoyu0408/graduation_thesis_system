package com.sxy.umlmyself.service;

import com.sxy.umlmyself.dto.CreateNoticeRequestDTO;
import com.sxy.umlmyself.dto.NoticeDTO;
import com.sxy.umlmyself.dto.UpdateNoticeRequestDTO;
import java.util.List;

public interface NoticeService {
    /**
     * 获取最新通知列表
     */
    List<NoticeDTO> getLatestNotices(int limit);
    
    /**
     * 创建通知
     * 
     * @param request 创建通知请求DTO
     * @param creatorId 创建者ID
     * @return 通知DTO
     */
    NoticeDTO createNotice(CreateNoticeRequestDTO request, Integer creatorId);
    
    /**
     * 更新通知
     * 
     * @param noticeId 通知ID
     * @param request 更新通知请求DTO
     * @return 通知DTO
     */
    NoticeDTO updateNotice(Integer noticeId, UpdateNoticeRequestDTO request);
    
    /**
     * 删除通知
     * 
     * @param noticeId 通知ID
     */
    void deleteNotice(Integer noticeId);
    
    /**
     * 分页查询所有通知（管理员视图）
     * 
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 通知列表
     */
    List<NoticeDTO> getAllNotices(Integer page, Integer size);
}

