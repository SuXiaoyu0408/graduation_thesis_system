package com.sxy.umlmyself.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 文件服务接口
 * 统一处理文件的上传、下载、预览等功能
 */
public interface FileService {
    
    /**
     * 上传文件
     * 
     * @param file 文件对象
     * @param processId 论文流程ID
     * @param materialType 材料类型
     * @param uploaderId 上传者ID
     * @return 文件存储路径
     * @throws IOException 文件操作异常
     */
    String uploadFile(MultipartFile file, Long processId, String materialType, Integer uploaderId) throws IOException;
    
    /**
     * 下载文件
     * 
     * @param filePath 文件路径
     * @return 文件资源
     * @throws IOException 文件操作异常
     */
    Resource downloadFile(String filePath) throws IOException;
    
    /**
     * 预览文件（返回文件资源，用于在线预览）
     * 
     * @param filePath 文件路径
     * @return 文件资源
     * @throws IOException 文件操作异常
     */
    Resource previewFile(String filePath) throws IOException;
    
    /**
     * 删除文件
     * 
     * @param filePath 文件路径
     * @return 是否删除成功
     */
    boolean deleteFile(String filePath);
    
    /**
     * 获取文件的原始文件名
     * 
     * @param filePath 文件路径
     * @return 原始文件名
     */
    String getOriginalFileName(String filePath);
}

