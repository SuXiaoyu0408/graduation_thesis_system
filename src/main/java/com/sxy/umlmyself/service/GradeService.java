package com.sxy.umlmyself.service;

import com.sxy.umlmyself.dto.FinalGradeDTO;

import java.util.Optional;

public interface GradeService {

    /**
     * 尝试计算并生成最终成绩。
     * 此方法应在每次有新的评分（指导老师、评阅、答辩）提交后调用。
     *
     * @param processId 论文流程ID
     * @return 如果所有分数都已齐全并成功生成最终成绩，则返回FinalGradeDTO；否则返回空的Optional。
     */
    Optional<FinalGradeDTO> tryCalculateFinalGrade(Long processId);

}

