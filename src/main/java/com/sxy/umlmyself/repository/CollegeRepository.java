package com.sxy.umlmyself.repository;

import com.sxy.umlmyself.entity.College;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 学院数据访问接口
 */
@Repository
public interface CollegeRepository extends JpaRepository<College, Integer> {
}

