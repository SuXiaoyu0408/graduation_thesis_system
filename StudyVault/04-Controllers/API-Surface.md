---
module: controllers
path: 04-Controllers
keywords: API, REST, endpoints, controller
---

# API 接口全景 (重要性: ★★★)

#api-all #module-controllers

## 概述
系统共 10 个 Controller，按角色和功能域划分。每个 Controller 对应一组相关的 REST 端点。除 LoginController 和 PasswordController 外，其余均通过 `@RequireRole` 进行权限控制。

## 接口汇总

### 认证相关 (LoginController)
| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| POST | `/login` | 无 | 阶段一：验证用户名密码，返回可用角色列表 |
| POST | `/login/confirm-role` | 无 | 阶段二：确认角色，返回 JWT Token |

### 个人信息 (UserController)
| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | `/api/user/profile` | Bearer Token | 获取当前登录用户个人信息 |
| PUT | `/api/user/profile` | Bearer Token | 更新个人信息(姓名/手机/邮箱) |

### 管理员 (AdminController) — `@RequireRole(requireAdmin=true)`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/users` | 用户列表(分页+筛选) |
| GET | `/api/admin/users/{userId}` | 用户详情 |
| POST | `/api/admin/users` | 创建用户 |
| PUT | `/api/admin/users/{userId}` | 更新用户 |
| DELETE | `/api/admin/users/{userId}` | 删除用户 |
| GET | `/api/admin/archive/statistics` | 归档统计 |
| POST | `/api/admin/archive/export` | 导出归档材料(ZIP) |

### 学生 (StudentThesisController) — `@RequireRole("STUDENT")`
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/student/thesis/material/{processId}/{materialType}` | 上传材料 |
| GET | `/api/student/thesis/material/{processId}/{materialType}/history` | 材料历史 |
| GET | `/api/student/thesis/material/{processId}/{materialType}/rejected-reason` | 驳回原因 |
| GET | `/api/student/thesis/material/history/{historyId}/download` | 下载历史文件 |
| GET | `/api/student/thesis/process` | 查看我的论文流程 |
| GET | `/api/student/thesis/defense/{processId}/arrangement` | 查看答辩安排 |

### 指导教师 (SupervisorThesisController) — `@RequireRole("SUPERVISOR")`
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/supervisor/thesis/approve` | 审核材料(通过/驳回) |
| POST | `/api/supervisor/thesis/material/{processId}/{materialType}` | 上传材料 |
| POST | `/api/supervisor/thesis/score` | 提交评分 |
| GET | `/api/supervisor/thesis/student/material/{processId}/{materialType}/preview` | 预览学生材料 |
| GET | `/api/supervisor/thesis/students` | 获取指导的学生列表 |

### 评阅教师 (ReviewerThesisController) — `@RequireRole("REVIEWER")`
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/reviewer/thesis/evaluation-form/{processId}` | 上传评阅表 |
| POST | `/api/reviewer/thesis/score` | 提交评分 |
| GET | `/api/reviewer/thesis/papers` | 待评阅论文列表 |
| GET | `/api/reviewer/thesis/student/material/{processId}/{materialType}/preview` | 预览学生材料 |
| GET | `/api/reviewer/thesis/student/material/{processId}/{materialType}/download` | 下载学生材料 |
| GET | `/api/reviewer/thesis/score/{processId}` | 获取已提交的评分 |
| GET | `/api/reviewer/thesis/statistics` | 评阅统计 |

### 专业负责人 (MajorLeaderThesisController) — `@RequireRole("MAJOR_LEADER")`
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/major-leader/thesis/approve` | 审核材料 |
| GET | `/api/major-leader/thesis/material/{processId}/{materialType}/preview` | 预览材料 |
| GET | `/api/major-leader/thesis/topics` | 待审批选题列表 |
| GET | `/api/major-leader/thesis/midterm-reports` | 中期报告列表 |
| GET | `/api/major-leader/thesis/grades` | 成绩汇总 |

### 学院负责人 (CollegeLeaderThesisController) — `@RequireRole("COLLEGE_LEADER")`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/college-leader/thesis/majors` | 获取下属专业列表 |
| GET | `/api/college-leader/thesis/progress` | 进度监控 |

### 答辩小组 (DefenseTeamThesisController) — `@RequireRole({"DEFENSE_MEMBER","DEFENSE_LEADER"})`
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/defense-team/thesis/score` | 提交答辩评分 |
| GET | `/api/defense-team/thesis/students` | 答辩学生列表 |
| GET | `/api/defense-team/thesis/grades` | 成绩汇总 |
| GET | `/api/defense-team/thesis/teams` | 答辩小组列表 |

### 通知公告 (NoticeController)
| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | `/api/notice/latest` | 无 | 公开：最新N条通知 |
| POST | `/api/notice/admin` | ADMIN | 创建通知 |
| PUT | `/api/notice/admin/{noticeId}` | ADMIN | 更新通知 |
| DELETE | `/api/notice/admin/{noticeId}` | ADMIN | 删除通知 |
| GET | `/api/notice/admin` | ADMIN | 分页查询所有通知 |

### 密码管理 (PasswordController)
| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| POST | `/password/sms-code` | 无 | 发送短信验证码(存Redis, TTL 5分钟) |
| POST | `/password/verify-code` | 无 | 校验验证码 |
| POST | `/password/reset` | 无 | 重置密码 |

## 路径变量与参数约定
- `{processId}`: 论文流程ID (Long)
- `{materialType}`: 材料类型枚举 → `StringToMaterialTypeConverter` 自动转换
- `{userId}`: 用户ID (Integer)
- 分页: `page`(默认1), `size`(默认10), `limit`(默认5)
- Token 传递: `Authorization: Bearer <token>` header

## 相关笔记
- [[JWT-Authorization]]
- [[Infrastructure]]
- [[Business-Logic]]
- [[Data-Model]]
