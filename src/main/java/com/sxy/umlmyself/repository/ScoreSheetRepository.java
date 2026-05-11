package com.sxy.umlmyself.repository;

import com.sxy.umlmyself.entity.ScoreSheet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScoreSheetRepository extends JpaRepository<ScoreSheet, Long> {

    List<ScoreSheet> findByProcessIdAndScorerRole(Long processId, String scorerRole);

    List<ScoreSheet> findByProcessId(Long processId);
}
