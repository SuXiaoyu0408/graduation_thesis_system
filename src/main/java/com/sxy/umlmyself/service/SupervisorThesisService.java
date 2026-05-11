package com.sxy.umlmyself.service;

import com.sxy.umlmyself.dto.ApproveMaterialRequestDTO;
import com.sxy.umlmyself.dto.SupervisedStudentDTO;
import com.sxy.umlmyself.dto.SupervisorScoreRequestDTO;
import com.sxy.umlmyself.enums.MaterialType;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SupervisorThesisService {

    void approveMaterial(ApproveMaterialRequestDTO dto, String token);

    void uploadMaterial(Long processId, MaterialType materialType, MultipartFile file, String token);

    void submitScore(SupervisorScoreRequestDTO dto, String token);

    Resource previewStudentMaterial(Long processId, MaterialType materialType, String token);

    List<SupervisedStudentDTO> getSupervisedStudents(String token);
}
