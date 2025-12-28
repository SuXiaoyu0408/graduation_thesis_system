CREATE TABLE college (
    college_id   INT AUTO_INCREMENT PRIMARY KEY,
    college_name VARCHAR(100),
    college_code VARCHAR(50)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
INSERT INTO college (college_name, college_code) VALUES
('计算机科学与技术学院', 'CS'),
('海洋科学与技术学院', 'OST'),
('理学院', 'SCI');
