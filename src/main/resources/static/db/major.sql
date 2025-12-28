CREATE TABLE major (
    major_id    INT AUTO_INCREMENT PRIMARY KEY,
    major_name  VARCHAR(100),
    major_code  VARCHAR(50),
    college_id  INT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
INSERT INTO major (major_name, major_code, college_id) VALUES
('软件工程', 'SE', 1),
('计算机科学与技术', 'CS', 1),
('网络工程', 'NE', 1);
INSERT INTO major (major_name, major_code, college_id) VALUES
('海洋科学', 'OS', 2),
('海洋技术', 'OT', 2);
INSERT INTO major (major_name, major_code, college_id) VALUES
('数学与应用数学', 'MATH', 3),
('物理学', 'PHYS', 3),
('化学', 'CHEM', 3);
