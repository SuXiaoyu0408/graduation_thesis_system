package com.sxy.umlmyself.repository;

import com.sxy.umlmyself.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Integer> {
    
    /**
     * 查询最新的通知（按创建时间倒序）
     */
    @Query("SELECT n FROM Notice n ORDER BY n.createTime DESC")
    List<Notice> findLatestNotices();
}

