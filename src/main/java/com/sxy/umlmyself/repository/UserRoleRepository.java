package com.sxy.umlmyself.repository;

import com.sxy.umlmyself.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户角色关联数据访问接口
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Integer> {
    
    /**
     * 根据用户ID查询该用户的所有角色关联
     * 
     * @param userId 用户ID
     * @return 用户角色关联列表
     */
    List<UserRole> findByUserId(Integer userId);
    
    /**
     * 根据用户ID和角色ID查询关联关系
     * 
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 用户角色关联（如果存在）
     */
    UserRole findByUserIdAndRoleId(Integer userId, Integer roleId);
    
    /**
     * 判断用户是否拥有指定角色
     * 
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return true 表示拥有该角色，false 表示不拥有
     */
    boolean existsByUserIdAndRoleId(Integer userId, Integer roleId);
}

