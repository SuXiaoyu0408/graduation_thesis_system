package com.sxy.umlmyself.repository;

import com.sxy.umlmyself.entity.Major;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 专业数据访问接口
 */
@Repository
public interface MajorRepository extends JpaRepository<Major, Integer> {
    
    /**
     * 根据学院ID查询专业列表
     */
    List<Major> findByCollegeId(Integer collegeId);
}

