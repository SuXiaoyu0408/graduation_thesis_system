package com.sxy.umlmyself.repository;

import com.sxy.umlmyself.entity.FinalGrade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FinalGradeRepository extends JpaRepository<FinalGrade, Long> {

    Optional<FinalGrade> findByProcessId(Long processId);
}

