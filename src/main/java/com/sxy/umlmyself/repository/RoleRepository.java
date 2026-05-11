package com.sxy.umlmyself.repository;

import com.sxy.umlmyself.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 角色数据访问接口
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    
    /**
     * 根据用户ID查询该用户拥有的所有角色
     * 
     * @param userId 用户ID
     * @return 角色列表
     */
    @Query("SELECT r FROM Role r, UserRole ur WHERE r.roleId = ur.roleId AND ur.userId = :userId")
    List<Role> findRolesByUserId(@Param("userId") Integer userId);
    
    /**
     * 根据角色ID查询角色信息
     * 
     * @param roleId 角色ID
     * @return 角色信息（如果存在）
     */
    Role findByRoleId(Integer roleId);

    /**
     * 根据角色代码查询角色信息
     *
     * @param roleCode 角色代码
     * @return 角色信息（如果存在）
     */
    Role findByRoleCode(String roleCode);
}

