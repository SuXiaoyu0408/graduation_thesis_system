CREATE TABLE user (
    user_id    INT AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(50),
    password   VARCHAR(255),
    real_name  VARCHAR(50),
    role_id    INT,
    phone      VARCHAR(20),
    email      VARCHAR(100),
    major_id   INT,
    college_id INT,
    status     INT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
