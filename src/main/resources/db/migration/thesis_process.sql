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

 Date: 31/12/2025 03:26:47
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

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
INSERT INTO `thesis_process` VALUES (6, 1, 2, NULL, NULL, NULL, 'topic_submitted', b'0', b'0', b'0', b'0', b'0', b'0', b'0', '2025-12-30 10:04:24.839047', '2025-12-30 14:02:53.351262', NULL, NULL, NULL, NULL);

SET FOREIGN_KEY_CHECKS = 1;
