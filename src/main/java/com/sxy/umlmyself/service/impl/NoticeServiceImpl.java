package com.sxy.umlmyself.service.impl;

import com.sxy.umlmyself.common.BusinessException;
import com.sxy.umlmyself.dto.CreateNoticeRequestDTO;
import com.sxy.umlmyself.dto.NoticeDTO;
import com.sxy.umlmyself.dto.UpdateNoticeRequestDTO;
import com.sxy.umlmyself.entity.Notice;
import com.sxy.umlmyself.repository.NoticeRepository;
import com.sxy.umlmyself.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private final NoticeRepository noticeRepository;

    @Override
    public List<NoticeDTO> getLatestNotices(int limit) {
        List<Notice> notices = noticeRepository.findLatestNotices();
        
        return notices.stream()
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public NoticeDTO createNotice(CreateNoticeRequestDTO request, Integer creatorId) {
        Notice notice = new Notice();
        notice.setTitle(request.getTitle());
        notice.setContent(request.getContent());
        notice.setCreatorId(creatorId);
        notice.setCreateTime(LocalDateTime.now());
        
        notice = noticeRepository.save(notice);
        return convertToDTO(notice);
    }
    
    @Override
    @Transactional
    public NoticeDTO updateNotice(Integer noticeId, UpdateNoticeRequestDTO request) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BusinessException("通知不存在"));
        
        notice.setTitle(request.getTitle());
        notice.setContent(request.getContent());
        
        notice = noticeRepository.save(notice);
        return convertToDTO(notice);
    }
    
    @Override
    @Transactional
    public void deleteNotice(Integer noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BusinessException("通知不存在"));
        
        noticeRepository.delete(notice);
    }
    
    @Override
    public List<NoticeDTO> getAllNotices(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(
                page - 1, 
                size,
                Sort.by(Sort.Direction.DESC, "createTime")
        );
        
        Page<Notice> noticePage = noticeRepository.findAll(pageable);
        
        return noticePage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 将Notice实体转换为NoticeDTO
     */
    private NoticeDTO convertToDTO(Notice notice) {
        NoticeDTO dto = new NoticeDTO();
        dto.setNoticeId(notice.getNoticeId());
        dto.setTitle(notice.getTitle());
        dto.setContent(notice.getContent());
        dto.setCreateTime(notice.getCreateTime());
        return dto;
    }
}

