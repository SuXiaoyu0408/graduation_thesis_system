package com.sxy.umlmyself.dto;

import lombok.Data;

import java.util.List;

/**
 * 用户列表查询响应DTO
 */
@Data
public class UserListResponseDTO {
    
    /**
     * 用户列表
     */
    private List<UserDetailDTO> users;
    
    /**
     * 总记录数
     */
    private Long total;
    
    /**
     * 当前页码
     */
    private Integer page;
    
    /**
     * 每页大小
     */
    private Integer size;
    
    /**
     * 总页数
     */
    private Integer totalPages;
}

