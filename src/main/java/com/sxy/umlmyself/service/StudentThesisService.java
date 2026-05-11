package com.sxy.umlmyself.service;

import com.sxy.umlmyself.dto.DefenseArrangementDTO;
import com.sxy.umlmyself.dto.MaterialHistoryDTO;
import com.sxy.umlmyself.dto.ThesisProcessDTO;
import com.sxy.umlmyself.enums.MaterialType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface StudentThesisService {

    void uploadMaterial(Long processId, MaterialType materialType, MultipartFile file);

    Map<String, Object> downloadHistoryMaterial(Long historyId);

    List<MaterialHistoryDTO> getMaterialHistory(Long processId, MaterialType materialType);

    String getRejectedReason(Long processId, MaterialType materialType);

    ThesisProcessDTO getMyThesisProcess();

    DefenseArrangementDTO getDefenseArrangement(Long processId);

    void deleteHistoryRecord(Long historyId);
}
