package com.sxy.umlmyself.service.impl;

import com.sxy.umlmyself.common.BusinessException;
import com.sxy.umlmyself.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * 文件服务实现类
 */
@Slf4j
@Service
public class FileServiceImpl implements FileService {
    
    @Value("${file.upload.dir:./uploads}")
    private String uploadDir;
    
    /**
     * 允许的文件扩展名列表（支持PDF和Word文档）
     */
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
        ".pdf", ".doc", ".docx"
    );
    
    @Override
    public String uploadFile(MultipartFile file, Long processId, String materialType, Integer uploaderId) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        
        // 创建上传目录结构：uploads/processId/materialType/
        String relativeDir = String.format("%s/%d/%s", uploadDir, processId, materialType);
        Path uploadPath = Paths.get(relativeDir);
        
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // 生成唯一文件名：UUID_原始文件名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new BusinessException("文件名不能为空");
        }
        
        // 验证文件格式
        String fileExtension = "";
        int lastDotIndex = originalFilename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            fileExtension = originalFilename.substring(lastDotIndex).toLowerCase(Locale.ROOT);
        }
        
        // 检查文件扩展名是否在允许列表中
        if (fileExtension.isEmpty() || !ALLOWED_EXTENSIONS.contains(fileExtension)) {
            throw new BusinessException("不支持的文件格式。仅支持PDF和Word文档（.pdf, .doc, .docx）");
        }
        
        String uniqueFileName = UUID.randomUUID().toString() + "_" + System.currentTimeMillis() + fileExtension;
        Path filePath = uploadPath.resolve(uniqueFileName);
        
        // 保存文件
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // 返回相对路径（用于数据库存储）
        String relativePath = String.format("%s/%d/%s/%s", uploadDir, processId, materialType, uniqueFileName);
        log.info("文件上传成功: {}, 原始文件名: {}", relativePath, originalFilename);
        
        return relativePath;
    }
    
    @Override
    public Resource downloadFile(String filePath) throws IOException {
        // Resolve path to absolute path to avoid CWD issues
        Path path = Paths.get(filePath).toAbsolutePath().normalize();
        log.info("Attempting to download file from absolute path: {}", path);

        if (!Files.exists(path)) {
            throw new BusinessException("文件不存在: " + path);
        }

        Resource resource = new UrlResource(path.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            throw new BusinessException("文件无法读取: " + path);
        }

        return resource;
    }
    
    @Override
    public Resource previewFile(String filePath) throws IOException {
        // 预览和下载使用相同的逻辑
        return downloadFile(filePath);
    }
    
    @Override
    public boolean deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                Files.delete(path);
                log.info("文件删除成功: {}", filePath);
                return true;
            }
            return false;
        } catch (IOException e) {
            log.error("删除文件失败: {}", filePath, e);
            return false;
        }
    }
    
    @Override
    public String getOriginalFileName(String filePath) {
        // 从文件路径中提取原始文件名
        // 格式：uploads/processId/materialType/UUID_timestamp.extension
        Path path = Paths.get(filePath);
        String fileName = path.getFileName().toString();
        
        // 如果文件名包含UUID和时间戳前缀，尝试提取原始文件名
        // 这里简化处理，直接返回文件名
        // 实际应用中，可以在上传时单独存储原始文件名
        return fileName;
    }
}

