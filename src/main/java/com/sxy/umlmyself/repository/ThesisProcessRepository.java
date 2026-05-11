package com.sxy.umlmyself.repository;

import com.sxy.umlmyself.entity.ThesisProcess;
import com.sxy.umlmyself.enums.ThesisStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ThesisProcessRepository extends JpaRepository<ThesisProcess, Long>, JpaSpecificationExecutor<ThesisProcess> {

    // 获取指定学生最新创建的论文流程记录（按 process_id 倒序取第一条）
    Optional<ThesisProcess> findFirstByStudentIdOrderByProcessIdDesc(Integer studentId);

    // 旧方法可能返回多条导致 NonUniqueResultException，已弃用
    @Deprecated
    Optional<ThesisProcess> findByStudentId(Integer studentId);
    
    /**
     * 查询所有已完成的论文流程
     */
    List<ThesisProcess> findByStatus(ThesisStatus status);
    
    /**
     * 分页查询已完成的论文流程
     */
    Page<ThesisProcess> findByStatus(ThesisStatus status, Pageable pageable);
    
    /**
     * 统计已完成论文数量
     */
    @Query("SELECT COUNT(t) FROM ThesisProcess t WHERE t.status = :status")
    Long countByStatus(@Param("status") ThesisStatus status);
    
    /**
     * 按学院统计已完成论文数量
     * 注意：由于ThesisProcess和Student没有JPA关联，这里需要通过原生查询或Service层实现
     */
    @Query(value = "SELECT s.college_id, c.college_name, COUNT(t.process_id) " +
           "FROM thesis_process t " +
           "JOIN student s ON t.student_id = s.stu_id " +
           "LEFT JOIN college c ON s.college_id = c.college_id " +
           "WHERE t.status = :status " +
           "GROUP BY s.college_id, c.college_name", nativeQuery = true)
    List<Object[]> countByStatusGroupByCollege(@Param("status") String status);
    
    /**
     * 按专业统计已完成论文数量
     */
    @Query(value = "SELECT s.major_id, m.major_name, s.college_id, c.college_name, COUNT(t.process_id) " +
           "FROM thesis_process t " +
           "JOIN student s ON t.student_id = s.stu_id " +
           "LEFT JOIN major m ON s.major_id = m.major_id " +
           "LEFT JOIN college c ON s.college_id = c.college_id " +
           "WHERE t.status = :status " +
           "GROUP BY s.major_id, m.major_name, s.college_id, c.college_name", nativeQuery = true)
    List<Object[]> countByStatusGroupByMajor(@Param("status") String status);
    
    /**
     * 根据指导老师ID查询论文流程列表
     */
    List<ThesisProcess> findBySupervisorId(Integer supervisorId);
    
    /**
     * 根据评阅老师ID查询论文流程列表
     */
    List<ThesisProcess> findByReviewerId(Integer reviewerId);
    
    /**
     * 根据答辩小组ID查询论文流程列表
     */
    List<ThesisProcess> findByDefenseTeamId(Integer defenseTeamId);
}
