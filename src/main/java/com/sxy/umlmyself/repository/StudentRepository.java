package com.sxy.umlmyself.repository;

import com.sxy.umlmyself.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Integer> {
    Optional<Student> findByUser_UserId(Integer userId);
    
    /**
     * 根据用户ID查询学生信息，同时加载学院和专业信息
     */
    @Query("SELECT s FROM Student s LEFT JOIN FETCH s.college LEFT JOIN FETCH s.major WHERE s.user.userId = :userId")
    Optional<Student> findByUser_UserIdWithCollegeAndMajor(@Param("userId") Integer userId);
    
    /**
     * 根据指导老师ID查询学生列表
     */
    @Query("SELECT s FROM Student s LEFT JOIN FETCH s.college LEFT JOIN FETCH s.major WHERE s.teaSupervisorId = :supervisorId")
    List<Student> findByTeaSupervisorId(@Param("supervisorId") Integer supervisorId);
}

