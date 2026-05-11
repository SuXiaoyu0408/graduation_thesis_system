---
module: auth
path: 02-Auth
keywords: JWT, authentication, authorization, role, aspect, token
---

# JWT 认证与授权 (重要性: ★★★)

#module-auth #arch-aop #pattern-jwt

## 概述
系统采用 JWT (JSON Web Token) 实现无状态认证，通过 Spring AOP 切面 + `@RequireRole` 注解实现声明式角色授权。登录流程分为两个阶段：凭证验证和角色确认。

## 关键文件
| 文件 | 作用 |
|------|------|
| `util/JwtUtil.java` | JWT 生成、解析、验证、Token清理 |
| `aspect/RoleCheckAspect.java` | AOP 切面，拦截 @RequireRole 进行权限校验 |
| `common/RequireRole.java` | 权限注解，支持 `value`(角色码列表) 和 `requireAdmin` |
| `controller/LoginController.java` | 登录接口 (2阶段) |
| `service/impl/UserServiceImpl.java` | 登录逻辑实现、BCrypt 密码校验 |

## 公开接口
| 导出 | 类型 | 描述 |
|------|------|------|
| `JwtUtil.generateToken(userId, roleId)` | 方法 | 生成 JWT，包含 userId 和 roleId claims |
| `JwtUtil.getUserIdFromToken(token)` | 方法 | 从 Token 解析 userId |
| `JwtUtil.getRoleIdFromToken(token)` | 方法 | 从 Token 解析 roleId |
| `JwtUtil.cleanToken(token)` | 方法 | 清除空白字符和控制字符 |
| `JwtUtil.validateToken(token)` | 方法 | 校验 Token 是否有效(未过期) |
| `@RequireRole("ROLE_CODE")` | 注解 | 声明方法需要特定角色 |
| `@RequireRole(requireAdmin=true)` | 注解 | 声明方法需要 ADMIN 角色 |
| `POST /login` | 端点 | 阶段一：凭证验证 |
| `POST /login/confirm-role` | 端点 | 阶段二：角色确认+签发Token |

## 内部流程

### 权限校验切面流程
```text
请求到达带有 @RequireRole 的方法
  │
  ▼
RoleCheckAspect.checkRole()
  │
  ├─ 1. 从 RequestContextHolder 获取 HttpServletRequest
  │     └─ 失败 → BusinessException("无法获取请求信息")
  │
  ├─ 2. 提取 Authorization header
  │     └─ 非 Bearer 格式 → BusinessException("认证头格式不正确")
  │
  ├─ 3. jwtUtil.cleanToken(rawToken)
  │     └─ 去除 Bearer 前缀 → trim → 只保留 [A-Za-z0-9\-_.]
  │
  ├─ 4. jwtUtil.getUserIdFromToken(cleanedToken)
  │     └─ 解析失败 → BusinessException("无效的认证令牌")
  │
  ├─ 5. 读取 @RequireRole 注解（优先方法级别 → 类级别）
  │
  ├─ 6a. requireAdmin=true → checkAdminRole()
  │     └─ roleRepository.findRolesByUserId() → 检查是否含 "ADMIN"
  │
  └─ 6b. value={"ROLE_A","ROLE_B"} → checkSpecificRoles()
        └─ roleRepository.findRolesByUserId() → 检查是否含任一角色
           └─ 无 → BusinessException("无权限访问，需要以下角色之一: [...]")
```

### cleanToken() 细节
```text
输入: "Bearer eyJhbG...xyz\n"  (可能含换行符、控制字符)
  → trim()
  → 去 "Bearer " 前缀
  → trim()
  → replaceAll("[^A-Za-z0-9\\-_.]", "")  ← 只保留 Base64URL 字符
输出: "eyJhbG...xyz"
```

> [!warning] Token 清理至关重要
> 前端传过来的 Token 可能包含不可见字符(换行、空格、控制字符)。`cleanToken()` 必须在解析前调用，否则 JWT 库会拒绝解析。`RoleCheckAspect` 和 `UserServiceImpl.confirmRole()` 都做了清理。

## 依赖
| 方向 | 模块/服务 | 方式 |
|------|-----------|------|
| **使用** | `RoleRepository` | 查询用户角色 |
| **使用** | `UserRoleRepository` | 校验用户-角色关联 |
| **使用** | `UserRepository` | 查询用户信息 |
| **被使用** | 所有 Controller | @RequireRole 注解触发 AOP |
| **被使用** | `UserController` | 手动调用 JwtUtil 解析 Token |

## 配置
| 配置项 | 用途 | 默认值 |
|--------|------|--------|
| `jwt.secret` | HMAC-SHA256 签名密钥 | `UMLmyselfSecretKeyForJWTTokenGeneration2024` |
| `jwt.expiration` | Token 过期时间(毫秒) | `604800000` (7天) |

## 角色列表
| 角色代码 | 角色名称 | 典型权限 |
|----------|----------|----------|
| `STUDENT` | 学生 | 上传材料、查看自己的论文流程 |
| `SUPERVISOR` | 指导教师 | 审核材料、评分、查看指导的学生 |
| `REVIEWER` | 评阅教师 | 评阅论文、评分、查看统计 |
| `MAJOR_LEADER` | 专业负责人 | 选题审批、中期报告审批、查看成绩 |
| `COLLEGE_LEADER` | 学院负责人 | 进度监控、查看专业列表 |
| `DEFENSE_MEMBER` | 答辩成员 | 答辩评分 |
| `DEFENSE_LEADER` | 答辩组长 | 答辩评分、查看小组 |
| `ADMIN` | 管理员 | 用户管理、归档导出 |

## 相关笔记
- [[System-Architecture]]
- [[Request-Flow]]
- [[Infrastructure]]
- [[API-Surface]]
