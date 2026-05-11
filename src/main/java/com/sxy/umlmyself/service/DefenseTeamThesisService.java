package com.sxy.umlmyself.service;

import com.sxy.umlmyself.dto.DefenseScoreRequestDTO;
import com.sxy.umlmyself.dto.DefenseStudentDTO;
import com.sxy.umlmyself.dto.DefenseTeamDTO;
import com.sxy.umlmyself.dto.GradeSummaryDTO;

import java.util.List;

public interface DefenseTeamThesisService {

    void submitScore(DefenseScoreRequestDTO dto, String token);

    List<DefenseStudentDTO> getStudentsForDefense(String token);

    List<GradeSummaryDTO> getGrades(String token);

    List<DefenseTeamDTO> getDefenseTeams(String token);
}
