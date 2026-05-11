package com.sxy.umlmyself.service;

import com.sxy.umlmyself.dto.MajorDTO;
import com.sxy.umlmyself.dto.ProgressMonitoringDTO;

import java.util.List;

public interface CollegeLeaderThesisService {

    List<MajorDTO> getMajors(String token);

    List<ProgressMonitoringDTO> getProgress(String token);
}
