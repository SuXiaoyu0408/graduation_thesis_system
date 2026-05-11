-- 为thesis_process表添加评阅老师和答辩小组字段
-- 用于权限校验，确保评阅老师和答辩小组只能为分配的学生操作

ALTER TABLE thesis_process 
ADD COLUMN reviewer_id INT COMMENT '评阅老师ID（用于权限校验）',
ADD COLUMN defense_team_id INT COMMENT '答辩小组ID（用于权限校验）';

-- 添加索引以提高查询性能
CREATE INDEX idx_reviewer_id ON thesis_process(reviewer_id);
CREATE INDEX idx_defense_team_id ON thesis_process(defense_team_id);

-- 注意：
-- 1. 这两个字段可以为NULL，以兼容现有数据
-- 2. 建议管理员在分配评阅老师和答辩小组时设置这些字段
-- 3. 如果字段为NULL，系统允许访问（兼容旧数据），但建议尽快分配

