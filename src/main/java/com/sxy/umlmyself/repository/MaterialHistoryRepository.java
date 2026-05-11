package com.sxy.umlmyself.repository;

import com.sxy.umlmyself.entity.MaterialHistory;
import com.sxy.umlmyself.enums.MaterialType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaterialHistoryRepository extends JpaRepository<MaterialHistory, Long> {

    List<MaterialHistory> findByProcessIdAndMaterialTypeOrderByVersionDesc(Long processId, MaterialType materialType);

    List<MaterialHistory> findByProcessIdOrderByUploadedAtDesc(Long processId);
}

