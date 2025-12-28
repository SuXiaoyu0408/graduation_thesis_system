CREATE TABLE role (
    role_id   INT AUTO_INCREMENT PRIMARY KEY,
    role_code VARCHAR(50),
    role_name VARCHAR(50)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
INSERT INTO role (role_code, role_name) VALUES
('STUDENT', '学生'),
('SUPERVISOR', '指导老师'),
('MAJOR_LEADER', '专业负责人'),
('COLLEGE_LEADER', '二级学院领导'),
('REVIEWER', '评阅老师'),
('ADMIN', '管理员'),
('DEFENSE_LEADER', '答辩小组组长'),
('DEFENSE_MEMBER', '答辩小组组员');
