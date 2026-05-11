---
module: entities
path: 06-Entities
keywords: JPA, entity, data-model, table, relationship
---

# 数据模型 (重要性: ★★★)

#module-entities #data-model

## 概述
系统使用 JPA 实体映射 MySQL 数据库表。关键实体包括 User、Role、ThesisProcess、Student、Teacher，以及多对多关联表 UserRole。

## 实体关系图

```text
┌──────────┐     ┌───────────┐     ┌────────┐
│   Role   │     │  UserRole │     │  User  │
│ role_id  │←───│ role_id   │    │ user_id│
│ role_code│     │ user_id   │───→│username│
│ role_name│     └───────────┘     │password│
└──────────┘                       │real_name│
                                   │phone   │
┌────────────┐                     │email   │
│   College  │←────────────────────│college_id│
│ college_id │                     │major_id│──→┌────────┐
│college_name│                     │status  │   │ Major  │
└────────────┘                     └────────┘   │major_id│
                                                │major_name│
┌──────────┐   1:1    ┌──────────┐              └────────┘
│  Student │←────────→│   User   │
│  stu_id  │          └──────────┘
│ stu_name │              1:1
│tea_supervisor_id        │
│ college_id              │
│ major_id  │             │
└──────────┘      ┌───────┴───┐
                  │  Teacher  │
                  │  tea_id   │
                  │ tea_name  │
                  │  title    │
                  │ college_id│
                  └───────────┘

┌──────────────────┐      ┌──────────────┐
│  ThesisProcess   │      │MaterialHistory│
│  process_id      │←────│ process_id   │
│  student_id      │      │material_type │
│  supervisor_id   │      │file_path     │
│  reviewer_id     │      │uploaded_by   │
│  defense_team_id │      │approved      │
│  thesis_title    │      │rejected_reason│
│  status (enum)   │      └──────────────┘
│  *_approved(bool)│
└──────────────────┘      ┌──────────────┐
                          │  ScoreSheet  │
┌──────────────┐          │  process_id  │
│  FinalGrade  │          │scorer_type   │
│  process_id  │          │  score_items │
│  total_score │          └──────────────┘
│  grade_level │
└──────────────┘      ┌──────────────┐
                      │   Notice     │
                      │  notice_id   │
                      │  title       │
                      │  content     │
                      │ created_by   │
                      └──────────────┘
```

## 实体清单

| 实体 | 表名 | 主键 | 关键字段 |
|------|------|------|----------|
| `User` | `user` | `user_id` (自增) | `username`, `password`(BCrypt), `real_name`, `phone`, `email`, `major_id`, `college_id`, `status` |
| `Role` | `role` | `role_id` (自增) | `role_code`, `role_name` |
| `UserRole` | `user_role` | 复合主键 | `user_id` + `role_id` (多对多) |
| `Student` | `student` | `stu_id` (自增) | `stu_name`, `tea_supervisor_id`, `user_id`(1:1), `college_id`, `major_id` |
| `Teacher` | `teacher` | `tea_id` (自增) | `tea_name`, `title`, `user_id`(1:1), `college_id` |
| `College` | `college` | `college_id` | `college_name` |
| `Major` | `major` | `major_id` | `major_name`, `college_id` |
| `ThesisProcess` | `thesis_process` | `process_id` (自增) | `student_id`, `supervisor_id`, `reviewer_id`, `defense_team_id`, `thesis_title`, `status`(枚举), 7个 `*_approved` 布尔字段, `created_at`, `updated_at` |
| `MaterialHistory` | `material_history` | 自增ID | `process_id`, `material_type`, `file_path`, `uploaded_by`, `approved`, `rejected_reason` |
| `ScoreSheet` | `score_sheet` | 自增ID | `process_id`, `scorer_type`, 评分明细字段 |
| `FinalGrade` | `final_grade` | 自增ID | `process_id`, `total_score`, `grade_level` |
| `Notice` | `notice` | `notice_id` (自增) | `title`, `content`, `created_by`, `created_at` |

> [!important] User 与 Student/Teacher 是 1:1 关系
> `User` 存储登录信息，`Student`/`Teacher` 存储扩展信息。它们通过 `user_id` 一对一关联。获取学生信息时需要 JOIN 三张表。

## 枚举类型
| 枚举 | 值 | 说明 |
|------|-----|------|
| `ThesisStatus` | `init` → `topic_submitted` → `topic_approved` → `topic_rejected` → `opening_submitted` → `opening_approved` → `opening_rejected` → `midterm_submitted` → `midterm_approved` → `midterm_rejected` → `final_submitted` → `final_approved` → `final_rejected` → `defense_scored` → `completed` | 论文流程16个状态 |
| `MaterialType` | `topic_selection` `task_assignment` `opening_report` `mid_term_report` `final_paper` `review_form` `evaluation_form` `defense_review_form` | 8种材料类型 |
| `GradeLevel` | (需确认) | 成绩等级(优/良/中/及格/不及格) |

## 配置
| 配置项 | 值 | 说明 |
|--------|-----|------|
| `spring.jpa.hibernate.ddl-auto` | `validate` | 只验证表结构不修改 |
| `spring.jpa.show-sql` | `true` | 打印SQL |
| `spring.jpa.properties.hibernate.format_sql` | `true` | 格式化SQL输出 |

> [!warning] DDL策略为 validate
> 不会自动建表/改表。部署前必须手动执行 `src/main/resources/db/` 下的建表脚本。

## 相关笔记
- [[Business-Logic]]
- [[API-Surface]]
- [[System-Architecture]]
