package com.sxy.umlmyself.service.impl;

import com.sxy.umlmyself.common.BusinessException;
import com.sxy.umlmyself.service.FileService;
import jakarta.annotation.PostConstruct;
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

@Slf4j
@Service
public class FileServiceImpl implements FileService {

    @Value("${file.upload.dir:./uploads}")
    private String uploadDir;

    private Path resolvedUploadDir;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
        ".pdf", ".doc", ".docx"
    );

    @PostConstruct
    public void init() {
        this.resolvedUploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        log.info("文件上传目录(绝对路径): {}", resolvedUploadDir);
    }

    @Override
    public String uploadFile(MultipartFile file, Long processId, String materialType, Integer uploaderId) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        // 清洗原始文件名：某些浏览器会发送完整客户端路径(如 C:\Users\xxx\Desktop\file.docx)，只提取纯文件名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new BusinessException("文件名不能为空");
        }
        originalFilename = Paths.get(originalFilename).getFileName().toString();

        // 验证文件格式
        String fileExtension = "";
        int lastDotIndex = originalFilename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            fileExtension = originalFilename.substring(lastDotIndex).toLowerCase(Locale.ROOT);
        }

        if (fileExtension.isEmpty() || !ALLOWED_EXTENSIONS.contains(fileExtension)) {
            throw new BusinessException("不支持的文件格式。仅支持PDF和Word文档（.pdf, .doc, .docx）");
        }

        // 使用绝对路径创建上传目录
        Path uploadPath = resolvedUploadDir.resolve(processId + "/" + materialType);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String uniqueFileName = UUID.randomUUID().toString() + "_" + System.currentTimeMillis() + fileExtension;
        Path filePath = uploadPath.resolve(uniqueFileName);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // 存储绝对路径，消除 CWD 依赖
        String absolutePath = filePath.toAbsolutePath().normalize().toString();
        log.info("文件上传成功: {}, 原始文件名: {}", absolutePath, originalFilename);

        return absolutePath;
    }

    @Override
    public Resource downloadFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        // 兼容旧数据（相对路径如 ./uploads/... 或 uploads/...）
        if (!path.isAbsolute()) {
            path = resolvedUploadDir.getParent().resolve(path).normalize();
        } else {
            path = path.toAbsolutePath().normalize();
        }
        log.info("解析后文件路径: {}", path);

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
        return downloadFile(filePath);
    }

    @Override
    public boolean deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (!path.isAbsolute()) {
                path = resolvedUploadDir.getParent().resolve(path).normalize();
            }
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
        Path path = Paths.get(filePath);
        String fileName = path.getFileName().toString();
        return fileName;
    }
}
