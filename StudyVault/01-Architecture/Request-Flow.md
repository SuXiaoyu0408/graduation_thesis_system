---
module: architecture
path: 01-Architecture
keywords: request-flow, http, jwt, aop, lifecycle
---

# 请求流程 (重要性: ★★★)

#arch-request-flow #pattern-aop

## 典型请求追踪: 学生上传选题材料

```text
1. 浏览器
   POST /api/student/thesis/material/{processId}/topic_selection
   Header: Authorization: Bearer <jwt_token>
   Body: multipart/form-data (file)
   │
2. Filter Chain / DispatcherServlet
   │
3. RoleCheckAspect.checkRole()          ← @Before 切点触发
   ├─ 从 RequestContextHolder 获取 HttpServletRequest
   ├─ 提取 Authorization header, 剥离 "Bearer " 前缀
   ├─ jwtUtil.cleanToken(rawToken)       ← 清除控制字符/空白
   ├─ jwtUtil.getUserIdFromToken()       ← 解析 JWT Claims → userId
   ├─ 读取方法上的 @RequireRole("STUDENT")
   └─ roleRepository.findRolesByUserId(userId)
      └─ 检查用户是否拥有 STUDENT 角色
         ├─ 有 → 放行
         └─ 无 → throw BusinessException("无权限访问")
   │
4. StudentThesisController.uploadMaterial()
   ├─ @PathVariable Long processId
   ├─ @PathVariable MaterialType materialType  ← StringToMaterialTypeConverter 转换
   ├─ @RequestPart MultipartFile file
   └─ 调用 studentThesisService.uploadMaterial(...)
   │
5. StudentThesisServiceImpl.uploadMaterial()
   ├─ 校验 processId 属于当前登录学生 (从 SecurityContext 获取 userId)
   ├─ 校验论文流程状态是否允许上传
   ├─ FileService 保存文件到 uploads/ 目录
   ├─ 创建 MaterialHistory 记录
   ├─ 更新 ThesisProcess 状态
   └─ 返回
   │
6. Controller 包装 ApiResponse.success("材料上传成功")
   │
7. 浏览器收到 JSON: {"code":200, "message":"材料上传成功", "data":null}
```

## 登录流程 (2阶段)

```text
阶段一: POST /login
  Client → {username, password}
  Server → UserServiceImpl.login()
    ├─ userRepository.findByUsername()
    ├─ passwordEncoder.matches()
    ├─ roleRepository.findRolesByUserId()
    └─ 返回 LoginResponseDTO {userId, username, roles[]}
            (尚未生成 Token)

阶段二: POST /login/confirm-role
  Client → {userId, roleCode}
  Server → UserServiceImpl.confirmRole()
    ├─ roleRepository.findByRoleCode()
    ├─ userRoleRepository.existsByUserIdAndRoleId()
    ├─ jwtUtil.generateToken(userId, roleId)
    ├─ 清理 Token 中的非法字符
    └─ 返回 ConfirmRoleResponseDTO {token, userId, username, roleId, roleCode}
  Response Header: Authorization: Bearer <token>
```

## JWT Token 生命周期

```text
生成: jwtUtil.generateToken(userId, roleId)
  → Claims: {userId, roleId}
  → 签名: HMAC-SHA256 (secret from application.properties)
  → 过期: now + jwt.expiration (默认7天)

传输: HTTP Header → Authorization: Bearer <token>

验证 (每次请求):
  1. 提取 Bearer token
  2. cleanToken(): 去除控制字符、空白、非法Base64URL字符
  3. Jwts.parser().verifyWith(secretKey).build().parseSignedClaims()
  4. 提取 userId, roleId
  5. 检查过期时间
```

## 论文流程状态机

```text
  init
    │ 学生提交选题申报表
    ▼
  topic_submitted
    │ 导师/专业负责人/学院负责人 审核通过
    ▼
  topic_approved ────(驳回)──→ topic_rejected
    │ 学生提交开题报告
    ▼
  opening_submitted
    │ 导师/专业负责人 审核通过
    ▼
  opening_approved ──(驳回)──→ opening_rejected
    │ 学生提交中期报告
    ▼
  midterm_submitted
    │ 审核通过
    ▼
  midterm_approved ──(驳回)──→ midterm_rejected
    │ 学生提交终稿
    ▼
  final_submitted
    │ 审核通过
    ▼
  final_approved ────(驳回)──→ final_rejected
    │ 答辩评分完成
    ▼
  defense_scored
    │
    ▼
  completed
```

> [!important] 多级审批
> 选题审批涉及三个独立字段: `topic_supervisor_approved`、`topic_major_leader_approved`、`topic_college_leader_approved`。三个字段全部为 `true` 后，状态才变为 `topic_approved`。

## 相关笔记
- [[System-Architecture]]
- [[JWT-Authorization]]
- [[API-Surface]]
- [[Data-Model]]
