---
module: services
path: 05-Services
keywords: service, business-logic, thesis, user, file
---

# 业务逻辑层 (重要性: ★★★)

#module-services #pattern-service-impl

## 概述
Service 层封装核心业务逻辑，采用接口-实现分离模式（`Service` 接口 + `ServiceImpl` 实现类）。每个 Service 对应一个业务领域，通过 Repository 访问数据，向 Controller 返回 DTO。

## 服务清单

| 接口 | 实现类 | 职责 |
|------|--------|------|
| `UserService` | `UserServiceImpl` | 登录(2阶段)、用户信息CRUD |
| `AdminService` | `AdminServiceImpl` | 用户管理、归档统计与导出 |
| `StudentThesisService` | `StudentThesisServiceImpl` | 材料上传、流程查询、答辩安排 |
| `SupervisorThesisService` | `SupervisorThesisServiceImpl` | 审核材料、评分、指导的学生管理 |
| `ReviewerThesisService` | `ReviewerThesisServiceImpl` | 评阅表上传、评分、统计 |
| `MajorLeaderThesisService` | `MajorLeaderThesisServiceImpl` | 选题/中期审批、成绩查看 |
| `CollegeLeaderThesisService` | `CollegeLeaderThesisServiceImpl` | 专业列表、进度监控 |
| `DefenseTeamThesisService` | `DefenseTeamThesisServiceImpl` | 答辩评分、小组管理 |
| `NoticeService` | `NoticeServiceImpl` | 通知CRUD |
| `PasswordService` | `PasswordServiceImpl` | 短信验证码、密码重置 |
| `FileService` | `FileServiceImpl` | 文件保存到 uploads/、文件读取 |
| `GradeService` | `GradeServiceImpl` | 分数计算、最终成绩汇总 |

## 关键流程

### 材料上传与审批流
```text
StudentThesisService.uploadMaterial(processId, materialType, file)
  ├─ 验证 processId 属于当前学生
  ├─ 检查流程状态是否允许上传此类型材料
  ├─ FileService.saveFile(file) → uploads/{timestamp}_{filename}
  ├─ materialHistoryRepository.save(history)
  └─ thesisProcessRepository.save(updatedProcess)   ← 更新状态

  ↓ (后续由审批角色触发)

SupervisorThesisService.approveMaterial(dto, token)
  ├─ 从 token 解析审批人信息
  ├─ 校验审批权限 (processId 关联到该导师)
  ├─ 根据 materialType 和 approve 结果更新对应状态字段
  │  例: topic_selection + approved → topic_supervisor_approved=true
  └─ 检查多级审批是否全部完成 → 更新主状态
```

### 成绩计算流程
```text
GradeService
  ├─ 指导教师评分 (SupervisorScoreRequestDTO)
  ├─ 评阅教师评分 (ReviewerScoreRequestDTO)
  ├─ 答辩评分 (DefenseScoreRequestDTO)
  └─ 汇总 → FinalGrade (加权计算最终成绩)

ScoreSheet 实体存储每次评分记录
FinalGrade 实体存储最终汇总成绩
```

### 验证码流程 (Redis)
```text
PasswordService.sendSmsCode(phone)
  ├─ 生成6位随机码
  ├─ redisTemplate.opsForValue().set("sms:" + phone, code, 5, TimeUnit.MINUTES)
  └─ 调用短信网关发送

PasswordService.verifyCode(phone, code)
  └─ redisTemplate.opsForValue().get("sms:" + phone) 比较

PasswordService.resetPassword(phone, newPassword, confirmPassword)
  ├─ 校验两次密码一致
  ├─ userRepository.findByPhone()
  └─ BCryptPasswordEncoder.encode(newPassword) → 更新
```

## 事务管理
Spring Data JPA 默认提供方法级事务。Service 方法中的多步数据库操作在同一事务中执行。

## 相关笔记
- [[API-Surface]]
- [[Data-Model]]
- [[JWT-Authorization]]
- [[System-Architecture]]
