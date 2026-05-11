/*
 Navicat Premium Dump SQL

 Source Server         : lunwenguanli
 Source Server Type    : MySQL
 Source Server Version : 50726 (5.7.26)
 Source Host           : localhost:3306
 Source Schema         : graduation_thesis_system_myself_b

 Target Server Type    : MySQL
 Target Server Version : 50726 (5.7.26)
 File Encoding         : 65001

 Date: 31/12/2025 05:41:30
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for college
-- ----------------------------
DROP TABLE IF EXISTS `college`;
CREATE TABLE `college`  (
  `college_id` int(11) NOT NULL AUTO_INCREMENT,
  `college_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `college_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`college_id`) USING BTREE,
  UNIQUE INDEX `uk_college_code`(`college_code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of college
-- ----------------------------
INSERT INTO `college` VALUES (1, '计算机科学与技术学院', 'CS');
INSERT INTO `college` VALUES (2, '海洋科学与技术学院', 'OST');
INSERT INTO `college` VALUES (3, '理学院', 'SCI');

-- ----------------------------
-- Table structure for final_grade
-- ----------------------------
DROP TABLE IF EXISTS `final_grade`;
CREATE TABLE `final_grade`  (
  `grade_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `process_id` bigint(20) NOT NULL COMMENT '关联ThesisProcess主键',
  `teacher_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '指导老师评分',
  `reviewer_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '评阅老师评分',
  `defense_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '答辩评分',
  `final_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '最终总分',
  `grade_level` enum('excellent','good','average','pass','fail') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '成绩等级',
  `calculated_at` datetime(6) NULL DEFAULT NULL COMMENT '计算时间',
  PRIMARY KEY (`grade_id`) USING BTREE,
  UNIQUE INDEX `uk_fg_process`(`process_id`) USING BTREE,
  CONSTRAINT `fk_fg_process` FOREIGN KEY (`process_id`) REFERENCES `thesis_process` (`process_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '最终成绩表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of final_grade
-- ----------------------------

-- ----------------------------
-- Table structure for major
-- ----------------------------
DROP TABLE IF EXISTS `major`;
CREATE TABLE `major`  (
  `major_id` int(11) NOT NULL AUTO_INCREMENT,
  `major_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `major_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `college_id` int(11) NULL DEFAULT NULL,
  PRIMARY KEY (`major_id`) USING BTREE,
  UNIQUE INDEX `uk_major_name_college`(`major_name`, `college_id`) USING BTREE,
  INDEX `fk_major_college`(`college_id`) USING BTREE,
  CONSTRAINT `fk_major_college` FOREIGN KEY (`college_id`) REFERENCES `college` (`college_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of major
-- ----------------------------
INSERT INTO `major` VALUES (1, '软件工程', 'SE', 1);
INSERT INTO `major` VALUES (2, '计算机科学与技术', 'CS', 1);
INSERT INTO `major` VALUES (3, '网络工程', 'NE', 1);
INSERT INTO `major` VALUES (4, '海洋科学', 'OS', 2);
INSERT INTO `major` VALUES (5, '海洋技术', 'OT', 2);
INSERT INTO `major` VALUES (6, '数学与应用数学', 'MATH', 3);
INSERT INTO `major` VALUES (7, '物理学', 'PHYS', 3);
INSERT INTO `major` VALUES (8, '化学', 'CHEM', 3);

-- ----------------------------
-- Table structure for material_history
-- ----------------------------
DROP TABLE IF EXISTS `material_history`;
CREATE TABLE `material_history`  (
  `history_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `process_id` bigint(20) NOT NULL COMMENT '关联ThesisProcess主键',
  `material_type` enum('topic_selection','task_assignment','opening_report','mid_term_report','final_paper','review_form','evaluation_form','defense_review_form') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '材料类型',
  `file_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件路径',
  `original_filename` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '原始文件名',
  `version` int(11) NOT NULL DEFAULT 1 COMMENT '版本号',
  `uploader_id` int(11) NOT NULL COMMENT '上传者ID',
  `is_latest` bit(1) NOT NULL DEFAULT b'1' COMMENT '是否最新版本',
  `rejected_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '拒绝原因',
  `uploaded_at` datetime(6) NULL DEFAULT NULL COMMENT '上传时间',
  PRIMARY KEY (`history_id`) USING BTREE,
  INDEX `fk_mh_process`(`process_id`) USING BTREE,
  CONSTRAINT `fk_mh_process` FOREIGN KEY (`process_id`) REFERENCES `thesis_process` (`process_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 39 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of material_history
-- ----------------------------
INSERT INTO `material_history` VALUES (2, 6, 'topic_selection', './uploads/6/TOPIC_SELECTION/a2fbc3a6-330e-48f8-9c28-8bfa4dc0defe_1767089074103.docx', '新建 Microsoft Word 文档.docx', 1, 1, b'0', NULL, '2025-12-30 10:04:34.109505');
INSERT INTO `material_history` VALUES (3, 6, 'topic_selection', './uploads/6/topic_selection/0d051d7a-89cf-46ad-a9dd-cac466625c6a_1767090612735.docx', '新建 Microsoft Word 文档.docx', 2, 1, b'0', NULL, '2025-12-30 10:30:12.773716');
INSERT INTO `material_history` VALUES (4, 6, 'opening_report', './uploads/6/opening_report/f367014c-8ede-4f51-a049-e9f66041c10d_1767090685799.docx', '新建 Microsoft Word 文档 (2).docx', 1, 1, b'0', NULL, '2025-12-30 10:31:25.803531');
INSERT INTO `material_history` VALUES (5, 6, 'mid_term_report', './uploads/6/mid_term_report/63b0c20f-8ce5-47d0-a447-c50caf659615_1767090715930.docx', '新建 Microsoft Word 文档.docx', 1, 1, b'0', NULL, '2025-12-30 10:31:55.934001');
INSERT INTO `material_history` VALUES (6, 6, 'topic_selection', './uploads/6/topic_selection/a423f4f6-3bf3-4585-88a0-aedc9768234b_1767109223567.docx', '新建 Microsoft Word 文档.docx', 3, 1, b'0', NULL, '2025-12-30 15:40:23.591371');
INSERT INTO `material_history` VALUES (7, 6, 'topic_selection', './uploads/6/topic_selection/f7d708a9-7999-4b29-b06d-00e7ba2b7bf1_1767113902441.docx', '第三个实验.docx', 4, 1, b'0', NULL, '2025-12-30 16:58:22.476727');
INSERT INTO `material_history` VALUES (8, 6, 'topic_selection', './uploads/6/topic_selection/216b3ef2-54a1-4744-88d0-500f9f676733_1767114351697.docx', '232650120_吴叶卓.docx', 5, 1, b'0', NULL, '2025-12-30 17:05:51.699954');
INSERT INTO `material_history` VALUES (9, 6, 'topic_selection', './uploads/6/topic_selection/2b684615-3c10-4068-b85c-357a34293ea4_1767114507703.docx', '232650120_吴叶卓.docx', 6, 1, b'0', NULL, '2025-12-30 17:08:27.733628');
INSERT INTO `material_history` VALUES (10, 6, 'topic_selection', './uploads/6/topic_selection/60696b02-0b5e-48fb-93af-075824bd2d12_1767114643753.doc', '232650120_吴叶卓.docx.doc', 7, 1, b'0', NULL, '2025-12-30 17:10:43.757348');
INSERT INTO `material_history` VALUES (11, 6, 'topic_selection', './uploads/6/topic_selection/77d773d5-d1db-40d3-91ae-1af2513b11dc_1767114906842.docx', '新建 Microsoft Word 文档.docx', 8, 1, b'0', NULL, '2025-12-30 17:15:06.845860');
INSERT INTO `material_history` VALUES (12, 6, 'topic_selection', './uploads/6/topic_selection/ac7da22c-4bcb-48e2-9c08-04e7f3fed0db_1767114921137.docx', '新建 Microsoft Word 文档.docx', 9, 1, b'0', NULL, '2025-12-30 17:15:21.140429');
INSERT INTO `material_history` VALUES (13, 6, 'topic_selection', './uploads/6/topic_selection/5d6f5e38-f938-451c-85a3-754c8f52ad76_1767115039098.doc', '232650120_吴叶卓.docx.doc', 10, 1, b'0', NULL, '2025-12-30 17:17:19.104744');
INSERT INTO `material_history` VALUES (14, 6, 'topic_selection', './uploads/6/topic_selection/8883ce96-2c6d-4f64-a472-6cebc7aeed51_1767115049280.docx', '新建 Microsoft Word 文档.docx', 11, 1, b'0', NULL, '2025-12-30 17:17:29.282581');
INSERT INTO `material_history` VALUES (15, 6, 'topic_selection', './uploads/6/topic_selection/50c17b33-cfd8-43eb-93d0-a180b25641dc_1767118413467.docx', '232650120-吴叶卓.docx', 12, 1, b'0', NULL, '2025-12-30 18:13:33.499533');
INSERT INTO `material_history` VALUES (16, 6, 'topic_selection', './uploads/6/topic_selection/d150751e-7204-4973-b3b1-482ba863b8be_1767118920985.docx', '232650120_吴叶卓.docx', 13, 1, b'0', NULL, '2025-12-30 18:22:00.995572');
INSERT INTO `material_history` VALUES (17, 6, 'topic_selection', './uploads/6/topic_selection/e9bad1dc-e248-47e2-8713-b8efb77e3344_1767118961230.docx', '232650120_吴叶卓.docx', 14, 1, b'0', NULL, '2025-12-30 18:22:41.258743');
INSERT INTO `material_history` VALUES (18, 6, 'topic_selection', './uploads/6/topic_selection/839bacb0-3c91-414b-b9ed-24e750762c20_1767122369099.doc', '论文模板（命名格式为：学号+姓名）.doc', 15, 1, b'0', NULL, '2025-12-30 19:19:29.144391');
INSERT INTO `material_history` VALUES (19, 6, 'topic_selection', './uploads/6/topic_selection/4ea2a63c-cf86-46c1-b771-2fa205b66e0a_1767122615886.docx', '计操基本版.docx', 16, 1, b'0', NULL, '2025-12-30 19:23:35.921631');
INSERT INTO `material_history` VALUES (20, 6, 'topic_selection', './uploads/6/topic_selection/6d4708bd-fd31-4d9e-b5e4-7488b07b3e18_1767122658310.docx', '计操无答案版.docx', 17, 1, b'0', NULL, '2025-12-30 19:24:18.316370');
INSERT INTO `material_history` VALUES (21, 6, 'topic_selection', './uploads/6/topic_selection/2395d144-43a0-4833-8313-13f2933d4598_1767123097003.docx', '计操无答案版.docx', 18, 1, b'0', NULL, '2025-12-30 19:31:37.060302');
INSERT INTO `material_history` VALUES (22, 6, 'topic_selection', './uploads/6/topic_selection/9cd14dc6-ac43-4894-b0ff-03f4b2fc3bb6_1767123123827.docx', '新建 Microsoft Word 文档.docx', 19, 1, b'0', NULL, '2025-12-30 19:32:03.833813');
INSERT INTO `material_history` VALUES (23, 6, 'topic_selection', './uploads/6/topic_selection/0b4e9781-5053-4e22-bddb-f9f4ef4f70f5_1767123532323.docx', '通知附件1-6 .docx', 20, 1, b'0', NULL, '2025-12-30 19:38:52.399926');
INSERT INTO `material_history` VALUES (24, 6, 'topic_selection', './uploads/6/topic_selection/e7bb31a0-5fff-4ed7-ada5-174e5dc8aa28_1767123577903.docx', '通知附件1-6 .docx', 21, 1, b'0', NULL, '2025-12-30 19:39:37.913722');
INSERT INTO `material_history` VALUES (25, 6, 'topic_selection', './uploads/6/topic_selection/d5fec696-f377-487f-8795-f1ac2cda3f66_1767124436456.docx', '计操无答案版.docx', 22, 1, b'0', NULL, '2025-12-30 19:53:56.492869');
INSERT INTO `material_history` VALUES (26, 6, 'opening_report', './uploads/6/opening_report/77c3d5be-abcf-4758-815b-498229dd56b7_1767124445869.docx', '通知附件1-6 .docx', 2, 1, b'0', NULL, '2025-12-30 19:54:05.876319');
INSERT INTO `material_history` VALUES (31, 6, 'opening_report', './uploads/6/opening_report/31a56ff5-588f-4e2b-bf44-234862051bf7_1767125219514.docx', '新建 Microsoft Word 文档.docx', 3, 1, b'1', '111111', '2025-12-30 20:06:59.579507');
INSERT INTO `material_history` VALUES (32, 6, 'mid_term_report', './uploads/6/mid_term_report/1b61e4e2-78ba-450f-ad14-c206afa718ef_1767125238512.docx', '新建 Microsoft Word 文档.docx', 2, 1, b'1', NULL, '2025-12-30 20:07:18.524072');
INSERT INTO `material_history` VALUES (33, 6, 'final_paper', './uploads/6/final_paper/340fb357-43b1-44b6-a92a-a1c0b4cd7e04_1767125249433.docx', '新建 Microsoft Word 文档.docx', 1, 1, b'1', NULL, '2025-12-30 20:07:29.443731');
INSERT INTO `material_history` VALUES (34, 6, 'topic_selection', './uploads/6/topic_selection/cd703338-6642-41f8-94ed-8f5672e5694c_1767126717364.docx', '新建 Microsoft Word 文档.docx', 23, 2, b'0', NULL, '2025-12-30 20:31:57.410382');
INSERT INTO `material_history` VALUES (35, 6, 'task_assignment', './uploads/6/task_assignment/5c9261ef-f440-4d7d-bfb3-34740128b330_1767126727208.docx', '新建 Microsoft Word 文档.docx', 1, 2, b'0', NULL, '2025-12-30 20:32:07.213943');
INSERT INTO `material_history` VALUES (36, 6, 'task_assignment', './uploads/6/task_assignment/985792f1-02c9-4b6c-85e9-b6e5683a95cb_1767127183696.docx', '新建 Microsoft Word 文档.docx', 2, 2, b'0', NULL, '2025-12-30 20:39:43.700894');
INSERT INTO `material_history` VALUES (37, 6, 'task_assignment', './uploads/6/task_assignment/1a78c871-917f-4b69-b918-5e058dc1d92a_1767127293939.docx', '新建 Microsoft Word 文档.docx', 3, 2, b'1', NULL, '2025-12-30 20:41:33.944584');
INSERT INTO `material_history` VALUES (38, 6, 'topic_selection', './uploads/6/topic_selection/100c634a-a3e9-48da-9126-f76a60d0492d_1767127304019.docx', '新建 Microsoft Word 文档.docx', 24, 2, b'1', NULL, '2025-12-30 20:41:44.026300');

-- ----------------------------
-- Table structure for notice
-- ----------------------------
DROP TABLE IF EXISTS `notice`;
CREATE TABLE `notice`  (
  `notice_id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '通知标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '通知内容',
  `creator_id` int(11) NOT NULL COMMENT '发布者(管理员)用户ID',
  `create_time` datetime(6) NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`notice_id`) USING BTREE,
  INDEX `fk_notice_creator`(`creator_id`) USING BTREE,
  CONSTRAINT `fk_notice_creator` FOREIGN KEY (`creator_id`) REFERENCES `user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '通知公告表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of notice
-- ----------------------------
INSERT INTO `notice` VALUES (2, '关于寒假放假安排', '全校师生寒假时间：1 月 20 日至 2 月 28 日。祝大家假期愉快！', 6, '2025-01-15 09:30:00.000000');
INSERT INTO `notice` VALUES (3, '1111', '11111', 6, '2025-12-30 21:13:02.501015');

-- ----------------------------
-- Table structure for role
-- ----------------------------
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role`  (
  `role_id` int(11) NOT NULL AUTO_INCREMENT,
  `role_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`role_id`) USING BTREE,
  UNIQUE INDEX `uk_role_code`(`role_code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of role
-- ----------------------------
INSERT INTO `role` VALUES (1, 'STUDENT', '学生');
INSERT INTO `role` VALUES (2, 'SUPERVISOR', '指导老师');
INSERT INTO `role` VALUES (3, 'MAJOR_LEADER', '专业负责人');
INSERT INTO `role` VALUES (4, 'COLLEGE_LEADER', '二级学院领导');
INSERT INTO `role` VALUES (5, 'REVIEWER', '评阅老师');
INSERT INTO `role` VALUES (6, 'ADMIN', '管理员');
INSERT INTO `role` VALUES (7, 'DEFENSE_LEADER', '答辩小组组长');
INSERT INTO `role` VALUES (8, 'DEFENSE_MEMBER', '答辩小组组员');

-- ----------------------------
-- Table structure for score_sheet
-- ----------------------------
DROP TABLE IF EXISTS `score_sheet`;
CREATE TABLE `score_sheet`  (
  `score_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `process_id` bigint(20) NOT NULL COMMENT '关联ThesisProcess主键',
  `scorer_role` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '评分者角色：SUPERVISOR / REVIEWER / DEFENSE',
  `scorer_user_id` int(11) NOT NULL COMMENT '评分者用户ID',
  `score_item1` decimal(5, 2) NULL DEFAULT NULL COMMENT '评分项1',
  `score_item2` decimal(5, 2) NULL DEFAULT NULL COMMENT '评分项2',
  `score_item3` decimal(5, 2) NULL DEFAULT NULL COMMENT '评分项3',
  `score_item4` decimal(5, 2) NULL DEFAULT NULL COMMENT '评分项4',
  `total_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '总分',
  `created_at` datetime(6) NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`score_id`) USING BTREE,
  INDEX `fk_ss_process`(`process_id`) USING BTREE,
  INDEX `fk_ss_scorer`(`scorer_user_id`) USING BTREE,
  CONSTRAINT `fk_ss_process` FOREIGN KEY (`process_id`) REFERENCES `thesis_process` (`process_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_ss_scorer` FOREIGN KEY (`scorer_user_id`) REFERENCES `user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '评分记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of score_sheet
-- ----------------------------
INSERT INTO `score_sheet` VALUES (1, 6, 'SUPERVISOR', 2, 10.00, 10.00, 10.00, 10.00, 40.00, '2025-12-30 20:34:50.380582');
INSERT INTO `score_sheet` VALUES (2, 6, 'SUPERVISOR', 2, 10.00, 10.00, 10.00, 10.00, 40.00, '2025-12-30 20:39:30.988201');

-- ----------------------------
-- Table structure for student
-- ----------------------------
DROP TABLE IF EXISTS `student`;
CREATE TABLE `student`  (
  `stu_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '学生id',
  `stu_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `tea_supervisor_id` int(11) NULL DEFAULT NULL COMMENT '指导老师id',
  `user_id` int(11) NULL DEFAULT NULL COMMENT '用户id',
  `college_id` int(11) NULL DEFAULT NULL COMMENT '学院id',
  `major_id` int(11) NULL DEFAULT NULL COMMENT '专业id',
  PRIMARY KEY (`stu_id`) USING BTREE,
  INDEX `idx_student_user`(`user_id`) USING BTREE,
  INDEX `idx_student_supervisor`(`tea_supervisor_id`) USING BTREE,
  INDEX `idx_student_college`(`college_id`) USING BTREE,
  INDEX `idx_student_major`(`major_id`) USING BTREE,
  CONSTRAINT `fk_student_college` FOREIGN KEY (`college_id`) REFERENCES `college` (`college_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_student_major` FOREIGN KEY (`major_id`) REFERENCES `major` (`major_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_student_supervisor` FOREIGN KEY (`tea_supervisor_id`) REFERENCES `teacher` (`tea_id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_student_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学生表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of student
-- ----------------------------
INSERT INTO `student` VALUES (1, '学生一', 1, 1, 1, 1);
INSERT INTO `student` VALUES (2, '被禁用学生', 7, 9, 1, 1);

-- ----------------------------
-- Table structure for teacher
-- ----------------------------
DROP TABLE IF EXISTS `teacher`;
CREATE TABLE `teacher`  (
  `tea_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '教师id',
  `tea_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `user_id` int(11) NULL DEFAULT NULL COMMENT '用户id',
  `college_id` int(11) NULL DEFAULT NULL COMMENT '学院id',
  PRIMARY KEY (`tea_id`) USING BTREE,
  INDEX `idx_teacher_user`(`user_id`) USING BTREE,
  INDEX `idx_teacher_college`(`college_id`) USING BTREE,
  CONSTRAINT `fk_teacher_college` FOREIGN KEY (`college_id`) REFERENCES `college` (`college_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_teacher_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '教师基础表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of teacher
-- ----------------------------
INSERT INTO `teacher` VALUES (1, '指导老师一', NULL, 2, 1);
INSERT INTO `teacher` VALUES (2, '专业负责人一', NULL, 3, 1);
INSERT INTO `teacher` VALUES (3, '学院领导一', NULL, 4, 2);
INSERT INTO `teacher` VALUES (4, '评阅老师一', NULL, 5, 3);
INSERT INTO `teacher` VALUES (5, '答辩组长一', NULL, 7, 1);
INSERT INTO `teacher` VALUES (6, '答辩组员一', NULL, 8, 1);
INSERT INTO `teacher` VALUES (7, '指导老师二', NULL, 10, 1);

-- ----------------------------
-- Table structure for teacher_role_rel
-- ----------------------------
DROP TABLE IF EXISTS `teacher_role_rel`;
CREATE TABLE `teacher_role_rel`  (
  `tea_role_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '教师角色关系id',
  `tea_id` int(11) NULL DEFAULT NULL COMMENT '教师id',
  `role_id` int(11) NULL DEFAULT NULL COMMENT '角色id',
  PRIMARY KEY (`tea_role_id`) USING BTREE,
  UNIQUE INDEX `uk_trr_teacher_role`(`tea_id`, `role_id`) USING BTREE,
  INDEX `idx_trr_teacher`(`tea_id`) USING BTREE,
  INDEX `idx_trr_role`(`role_id`) USING BTREE,
  CONSTRAINT `fk_trr_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`role_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_trr_teacher` FOREIGN KEY (`tea_id`) REFERENCES `teacher` (`tea_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '教师与角色关系表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of teacher_role_rel
-- ----------------------------
INSERT INTO `teacher_role_rel` VALUES (1, 1, 2);
INSERT INTO `teacher_role_rel` VALUES (2, 1, 3);
INSERT INTO `teacher_role_rel` VALUES (3, 2, 3);
INSERT INTO `teacher_role_rel` VALUES (4, 3, 4);
INSERT INTO `teacher_role_rel` VALUES (5, 4, 5);
INSERT INTO `teacher_role_rel` VALUES (6, 5, 7);
INSERT INTO `teacher_role_rel` VALUES (7, 6, 8);
INSERT INTO `teacher_role_rel` VALUES (8, 7, 2);

-- ----------------------------
-- Table structure for thesis_process
-- ----------------------------
DROP TABLE IF EXISTS `thesis_process`;
CREATE TABLE `thesis_process`  (
  `process_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `student_id` int(11) NOT NULL COMMENT '学生ID',
  `supervisor_id` int(11) NULL DEFAULT NULL COMMENT '指导老师ID',
  `reviewer_id` int(11) NULL DEFAULT NULL COMMENT '评阅老师ID（用于权限校验）',
  `defense_team_id` int(11) NULL DEFAULT NULL COMMENT '答辩小组ID（用于权限校验）',
  `thesis_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '论文题目',
  `status` enum('init','topic_submitted','topic_approved','opening_submitted','opening_approved','midterm_submitted','midterm_approved','final_submitted','final_approved','defense_scored','completed') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'init' COMMENT '状态',
  `topic_supervisor_approved` bit(1) NULL DEFAULT b'0' COMMENT '选题-指导老师审批',
  `topic_major_leader_approved` bit(1) NULL DEFAULT b'0' COMMENT '选题-专业负责人审批',
  `topic_college_leader_approved` bit(1) NULL DEFAULT b'0' COMMENT '选题-学院领导审批',
  `task_major_leader_approved` bit(1) NULL DEFAULT b'0' COMMENT '任务书-专业负责人审批',
  `task_college_leader_approved` bit(1) NULL DEFAULT b'0' COMMENT '任务书-学院领导审批',
  `opening_supervisor_approved` bit(1) NULL DEFAULT b'0' COMMENT '开题-指导老师审批',
  `opening_major_leader_approved` bit(1) NULL DEFAULT b'0' COMMENT '开题-专业负责人审批',
  `created_at` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `topic_selection_file_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '选题申报表文件路径',
  `opening_report_file_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '开题报告文件路径',
  `mid_term_report_file_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '中期报告文件路径',
  `final_paper_file_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '论文终稿文件路径',
  PRIMARY KEY (`process_id`) USING BTREE,
  INDEX `fk_tp_student`(`student_id`) USING BTREE,
  CONSTRAINT `fk_tp_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`stu_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '毕业流程材料与审核表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of thesis_process
-- ----------------------------
INSERT INTO `thesis_process` VALUES (6, 1, 2, 4, NULL, '计操无答案版', 'final_submitted', b'1', b'0', b'0', b'0', b'0', b'0', b'0', '2025-12-30 10:04:24.839047', '2025-12-30 20:39:11.237016', NULL, './uploads/6/opening_report/31a56ff5-588f-4e2b-bf44-234862051bf7_1767125219514.docx', './uploads/6/mid_term_report/1b61e4e2-78ba-450f-ad14-c206afa718ef_1767125238512.docx', './uploads/6/final_paper/340fb357-43b1-44b6-a92a-a1c0b4cd7e04_1767125249433.docx');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `user_id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '密码（已加密）',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '邮箱',
  `major_id` int(11) NULL DEFAULT NULL COMMENT '专业ID',
  `college_id` int(11) NULL DEFAULT NULL COMMENT '学院ID',
  `status` int(11) NULL DEFAULT NULL COMMENT '账号状态（1 表示正常，0 表示禁用）',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `uk_user_username`(`username`) USING BTREE,
  UNIQUE INDEX `uk_user_phone`(`phone`) USING BTREE,
  INDEX `fk_user_college`(`college_id`) USING BTREE,
  INDEX `fk_user_major`(`major_id`) USING BTREE,
  CONSTRAINT `fk_user_college` FOREIGN KEY (`college_id`) REFERENCES `college` (`college_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_user_major` FOREIGN KEY (`major_id`) REFERENCES `major` (`major_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'student01', '$2a$10$LoYLofmb/4DqLzlLc8GzvugTX5/BHwN07jkRDcy9DRFkIpJIelJUS', '学生一', '13800000001', 'student01@test.com', 1, 1, 1);
INSERT INTO `user` VALUES (2, 'supervisor01', '$2a$10$DBmplVjMwapJ5DWuuOCWreXSDoDrvDitmTHmdGDu3IzoNc47V.F9G', '指导老师一', '13800000002', 'supervisor01@test.com', NULL, 1, 1);
INSERT INTO `user` VALUES (3, 'majorleader01', '$2a$10$DBmplVjMwapJ5DWuuOCWreXSDoDrvDitmTHmdGDu3IzoNc47V.F9G', '专业负责人一', '13800000003', 'majorleader01@test.com', NULL, 1, 1);
INSERT INTO `user` VALUES (4, 'collegeleader01', '$2a$10$DBmplVjMwapJ5DWuuOCWreXSDoDrvDitmTHmdGDu3IzoNc47V.F9G', '学院领导一', '13800000004', 'collegeleader01@test.com', NULL, 2, 1);
INSERT INTO `user` VALUES (5, 'reviewer01', '$2a$10$DBmplVjMwapJ5DWuuOCWreXSDoDrvDitmTHmdGDu3IzoNc47V.F9G', '评阅老师一', '13800000005', 'reviewer01@test.com', NULL, 3, 1);
INSERT INTO `user` VALUES (6, 'admin01', '$2a$10$DBmplVjMwapJ5DWuuOCWreXSDoDrvDitmTHmdGDu3IzoNc47V.F9G', '系统管理员', '13800000006', 'admin@test.com', NULL, NULL, 1);
INSERT INTO `user` VALUES (7, 'defenseleader01', '$2a$10$DBmplVjMwapJ5DWuuOCWreXSDoDrvDitmTHmdGDu3IzoNc47V.F9G', '答辩组长一', '13800000007', 'defenseleader01@test.com', NULL, 1, 1);
INSERT INTO `user` VALUES (8, 'defensemember01', '$2a$10$DBmplVjMwapJ5DWuuOCWreXSDoDrvDitmTHmdGDu3IzoNc47V.F9G', '答辩组员一', '13800000008', 'defensemember01@test.com', NULL, 1, 1);
INSERT INTO `user` VALUES (9, 'student_disabled', '$2a$10$DBmplVjMwapJ5DWuuOCWreXSDoDrvDitmTHmdGDu3IzoNc47V.F9G', '被禁用学生', '13800000009', 'disabled@test.com', 1, 1, 0);
INSERT INTO `user` VALUES (10, 'supervisor02', '$2a$10$DBmplVjMwapJ5DWuuOCWreXSDoDrvDitmTHmdGDu3IzoNc47V.F9G', '指导老师二', '13800000011', 'supervisor02@test.com', NULL, 1, 1);
INSERT INTO `user` VALUES (11, 'test', '$2a$10$/aa/cpJD00H3d166WVQKc.jkFysamUzDcimqevEnkaCDnjOm4ppaS', '测试', '15155024455', NULL, NULL, NULL, 0);

-- ----------------------------
-- Table structure for user_role
-- ----------------------------
DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role`  (
  `user_role_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `role_id` int(11) NOT NULL,
  PRIMARY KEY (`user_role_id`) USING BTREE,
  UNIQUE INDEX `uk_user_role`(`user_id`, `role_id`) USING BTREE,
  INDEX `idx_user_role_user`(`user_id`) USING BTREE,
  INDEX `idx_user_role_role`(`role_id`) USING BTREE,
  CONSTRAINT `fk_user_role_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`role_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_user_role_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_role
-- ----------------------------
INSERT INTO `user_role` VALUES (1, 1, 1);
INSERT INTO `user_role` VALUES (8, 2, 2);
INSERT INTO `user_role` VALUES (9, 2, 3);
INSERT INTO `user_role` VALUES (11, 3, 3);
INSERT INTO `user_role` VALUES (2, 4, 4);
INSERT INTO `user_role` VALUES (3, 5, 5);
INSERT INTO `user_role` VALUES (4, 6, 6);
INSERT INTO `user_role` VALUES (5, 7, 7);
INSERT INTO `user_role` VALUES (6, 8, 8);
INSERT INTO `user_role` VALUES (7, 9, 1);
INSERT INTO `user_role` VALUES (14, 10, 2);
INSERT INTO `user_role` VALUES (15, 11, 1);

SET FOREIGN_KEY_CHECKS = 1;
