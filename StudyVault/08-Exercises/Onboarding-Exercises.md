---
module: exercises
path: 08-Exercises
keywords: practice, onboarding, trace, config, debug, extend
---

# 入门练习

#practice #onboarding

## 相关模块
- [[System-Architecture]]
- [[Request-Flow]]
- [[JWT-Authorization]]
- [[Infrastructure]]
- [[API-Surface]]
- [[Business-Logic]]
- [[Data-Model]]

---

## 练习 1 — 代码阅读：追踪登录流程 [trace]
> 从 `POST /login` 请求开始，追踪代码执行路径，列出每一步涉及的文件和方法。

> [!answer]- 查看答案
> 1. `LoginController.login()` — 接收 `LoginRequestDTO`，调用 `userService.login()`
> 2. `UserServiceImpl.login()` — `userRepository.findByUsername()` 查用户 → `passwordEncoder.matches()` 验密码 → 检查 `status` → `roleRepository.findRolesByUserId()` 查角色 → 组装 `LoginResponseDTO`
> 3. Controller 包装 `ApiResponse.success("登录成功，请选择角色", loginResponse)` 返回
> 4. 前端收到角色列表，用户选择一个角色，调用 `POST /login/confirm-role`
> 5. `UserServiceImpl.confirmRole()` — 查用户 → `roleRepository.findByRoleCode()` → `userRoleRepository.existsByUserIdAndRoleId()` → `jwtUtil.generateToken()` → 清理 Token → 返回 `ConfirmRoleResponseDTO`

---

## 练习 2 — 代码阅读：追踪权限校验 [trace]
> 当请求 `GET /api/student/thesis/process` 时，权限校验切面是如何工作的？列出 `RoleCheckAspect.checkRole()` 的完整执行步骤。

> [!answer]- 查看答案
> 1. 从 `RequestContextHolder` 获取当前 HTTP 请求
> 2. 提取 `Authorization` header，验证是否以 `Bearer ` 开头
> 3. 剥离 `Bearer ` 前缀 → `jwtUtil.cleanToken()` 清理控制字符和非法Base64URL字符
> 4. `jwtUtil.getUserIdFromToken()` 解析 JWT 获取 userId
> 5. 读取方法 `getMyThesisProcess()` 的注解：无方法级 → 读取类 `StudentThesisController` 的 `@RequireRole("STUDENT")`
> 6. `roleRepository.findRolesByUserId(userId)` 获取用户所有角色 → 检查是否包含 `STUDENT`
> 7. 有 → 放行；无 → `throw BusinessException("无权限访问...")`

---

## 练习 3 — 配置：如何添加一个新的角色？ [config]
> 如果要添加一个"教务处处长"角色（`DEAN`），需要修改哪些文件？

> [!answer]- 查看答案
> - **数据库**: 在 `role` 表中插入新记录（`role_code='DEAN'`, `role_name='教务处处长'`）
> - **注解**: 在对应的 Controller 或方法上添加 `@RequireRole("DEAN")`
> - **AOP 切面**: 无需修改。`RoleCheckAspect` 从数据库动态查询角色，自动支持新角色
> - **前端**: 在 `login_page.html` 的角色选择逻辑中显示新角色
> - **注意**: 如果需要给用户分配此角色，在 `user_role` 表中插入关联记录

---

## 练习 4 — 配置：如何切换数据库环境？ [config]
>  当前数据库连接指向 `172.20.10.4:3306`，如何切换为本地开发环境？

> [!answer]- 查看答案
> - **文件**: `src/main/resources/application.properties`
> - **修改项**:
>   ```
>   spring.datasource.url=jdbc:mysql://localhost:3306/graduation_thesis_system_myself_b?serverTimezone=UTC
>   spring.datasource.username=root
>   spring.datasource.password=<你的本地密码>
>   ```
> - **更佳方案**: 创建 `application-dev.properties`，使用 `spring.profiles.active=dev` 切换环境
> - **关联**: 确保本地 MySQL 已创建同名数据库并执行了建表脚本

---

## 练习 5 — 调试：Token 解析失败怎么排查？ [debug]
> 前端收到 401 错误，日志显示 "无效的认证令牌"。请描述排查步骤。

> [!answer]- 查看答案
> 1. 检查 `RoleCheckAspect` 的日志输出：查看 raw token bytes 和 cleaned token bytes
> 2. 确认前端传的 `Authorization` header 值正确：应为 `Bearer <token>`（注意空格）
> 3. 确认 Token 中无换行符或控制字符（`cleanToken()` 会清理，但可对比原始值）
> 4. 检查 JWT 密钥配置 `jwt.secret` 是否与生成时一致
> 5. 检查 Token 是否过期：`jwtUtil.isTokenExpired(token)` 或查看 `jwt.expiration` 配置
> 6. 检查 `application.properties` 中 `jwt.secret` 的字符串是否被意外截断或修改
> 7. 前端调试：`console.log(token)` 输出实际传递的 token 值

---

## 练习 6 — 扩展：如何添加一个新的文件上传接口？ [extend]
> 假设需要让指导教师上传"修改意见书"（`revision_note`），需要修改哪些文件？

> [!answer]- 查看答案
> 1. **枚举**: `enums/MaterialType.java` — 添加 `revision_note("revision_note", "修改意见书")`
> 2. **Controller**: `SupervisorThesisController` — 添加 `POST /api/supervisor/thesis/material/{processId}/revision_note` 端点
> 3. **Service**: `SupervisorThesisService` 接口 + `SupervisorThesisServiceImpl` — 添加 `uploadRevisionNote()` 方法，调用 `FileService.saveFile()` 和 `materialHistoryRepository.save()`
> 4. **数据库**: `material_history` 表已有 `material_type` 字段（字符串），新枚举值自动支持
> 5. **权限**: Controller 类已有 `@RequireRole("SUPERVISOR")`，无需额外配置
> 6. **前端**: 在指导教师的页面添加文件上传表单

---

## 练习 7 — 扩展：如何添加论文流程的新状态？ [extend]
> 如果要在答辩评分后加入"最终审核通过"状态（`final_review_approved`），需要改哪些地方？

> [!answer]- 查看答案
> 1. **枚举**: `ThesisStatus.java` — 在 `defense_scored` 和 `completed` 之间插入新值
> 2. **Service**: 修改使用状态机逻辑的 Service（如 `SupervisorThesisServiceImpl`、`DefenseTeamThesisServiceImpl`），调整状态转换条件
> 3. **前端**: 更新流程进度条的显示，映射新状态到对应的 UI 状态
> 4. **数据库**: `thesis_process` 表的 `status` 字段是字符串，枚举值变更自动兼容（但现有数据需手动迁移）
> 5. **测试**: 更新相关测试用例中的状态断言

---

## 练习 8 — 代码阅读：追踪材料审核流程 [trace]
> 学生提交选题后，指导教师审批通过。追踪 `POST /api/supervisor/thesis/approve` 的完整处理过程。

> [!answer]- 查看答案
> 1. `RoleCheckAspect.checkRole()` — 验证 Token 和 `SUPERVISOR` 角色
> 2. `SupervisorThesisController.approveMaterial()` — 接收 `ApproveMaterialRequestDTO` (含 processId, materialType, approved, rejectReason)
> 3. `SupervisorThesisServiceImpl.approveMaterial()`:
>    - 从 Token 获取当前导师 userId
>    - 查询 `ThesisProcess`，确认 `supervisor_id` 匹配
>    - 根据 `materialType` 找到对应的 `MaterialHistory` 记录
>    - 设置 `approved=true` 或 `rejected=true` + `rejected_reason`
>    - 更新 `ThesisProcess` 的对应审批字段（如 `topic_supervisor_approved`）
>    - 检查其他审批字段是否也通过 → 更新 `status`（如 `topic_approved`）
> 4. 返回 `ApiResponse.success("审核操作成功")`

---

## 练习 9 — 调试：文件上传失败怎么排查？ [debug]
> 学生调用上传接口返回错误，请描述排查流程。

> [!answer]- 查看答案
> 1. 检查 `uploads/` 目录是否存在且应用有写权限
> 2. 检查请求格式：必须是 `multipart/form-data`，文件参数名必须是 `file`
> 3. 检查 URL 路径：`/api/student/thesis/material/{processId}/{materialType}`
>    - `processId` 必须是当前学生关联的有效流程
>    - `materialType` 必须是 `MaterialType` 枚举的有效值
> 4. 检查 `ThesisProcess` 的 `status`：是否允许在当前状态下上传该类型材料
> 5. 查看日志中的 `GlobalExceptionHandler` 输出
> 6. 如返回 401，检查 Authorization header 和 Token 有效性

---

## 练习 10 — 扩展：如何实现通知的"已读"功能？ [extend]
> 当前通知没有已读/未读标记。如何为每个用户添加通知已读状态？

> [!answer]- 查看答案
> 1. **Entity**: 新建 `NoticeReadStatus` 实体，映射 `notice_read_status` 表 (notice_id, user_id, read_at)
> 2. **Repository**: 新建 `NoticeReadStatusRepository`
> 3. **Service**: `NoticeService` 添加 `markAsRead(noticeId, userId)` 方法
> 4. **Controller**: `NoticeController` 添加 `POST /api/notice/{noticeId}/read` 端点
> 5. **前端**: 在通知列表中对未读通知高亮，点击时调用已读接口
> 6. **权限**: 需要登录用户身份（Token 中获取 userId）

---

## 练习 11 — 配置：如何关闭 Redis（不使用验证码功能）？ [config]
> 如果本地没有 Redis，如何让应用正常运行？

> [!answer]- 查看答案
> - **文件**: `src/main/resources/application.properties`
> - **方法1**: 注释或删除所有 `spring.data.redis.*` 配置行
> - **方法2**: 在 `pom.xml` 中排除 `spring-boot-starter-data-redis` (但会编译错误，不推荐)
> - **方法3**: 配置 `spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration`
> - **影响**: 密码相关接口 (`/password/*`) 将无法使用验证码功能，但不影响其他功能

---

## 练习 12 — 代码阅读：BCrypt 密码处理 [trace]
> 追踪密码是如何存储和校验的。新用户创建时密码如何处理？

> [!answer]- 查看答案
> 1. **存储**: `AdminServiceImpl.createUser()` 调用 `new BCryptPasswordEncoder().encode(password)` → 存入 `user.password` 字段
> 2. **校验**: `UserServiceImpl.login()` 调用 `passwordEncoder.matches(rawPassword, encodedPassword)` → 明文与密文比对
> 3. **重置**: `PasswordServiceImpl.resetPassword()` 调用 `new BCryptPasswordEncoder().encode(newPassword)` → 更新密码字段
> 4. **关键点**: 密码以 BCrypt 哈希形式存储，即使数据库泄露也无法还原明文

---

## 练习 13 — 调试：服务器启动失败怎么排查？ [debug]
> `./mvnw spring-boot:run` 后应用启动失败，列出最常见的5个原因及排查方法。

> [!answer]- 查看答案
> 1. **端口被占用** (8080) → `netstat -ano | findstr 8080` → 修改 `server.port`
> 2. **数据库连接失败** → 检查 MySQL 是否运行，URL/用户名/密码是否正确
> 3. **数据库表不存在** (DDL=validate) → 执行 `src/main/resources/db/` 的建表脚本
> 4. **Java 版本不匹配** → 确认 `java -version` 输出为 21
> 5. **依赖下载失败** → 检查网络/Maven仓库，尝试 `./mvnw clean compile`

---

## 练习 14 — 代码阅读：DTO 转换模式 [trace]
> 在 `UserServiceImpl.getCurrentUserProfile()` 中，用户实体如何转换为 `UserProfileDTO`？涉及哪些关联表？

> [!answer]- 查看答案
> 1. `userRepository.findById(userId)` → 获取 `User` 实体
> 2. 手动创建 `UserProfileDTO`，设置 `account`、`name`、`phone`、`email`
> 3. `roleRepository.findRolesByUserId()` → 获取角色列表，取第一个角色的 `roleCode`
> 4. `studentRepository.findByUser_UserIdWithCollegeAndMajor()` → JOIN 查询 Student + College + Major
> 5. 从 Student 获取学院名、专业名、导师ID
> 6. 如有导师ID，`teacherRepository.findByTeaId()` → 获取导师姓名
> 7. **模式**: 系统不使用 MapStruct/ModelMapper 等自动映射工具，而是手动编写 DTO 转换逻辑

---

## 练习 15 — 扩展：如何添加一个全局的请求日志记录？ [extend]
> 如何记录每个请求的 URL、方法、耗时，而不修改每个 Controller？

> [!answer]- 查看答案
> 1. **方案**: 使用 Spring AOP 创建 `LoggingAspect`
> 2. **新文件**: `aspect/LoggingAspect.java`
>    ```java
>    @Around("execution(* com.sxy.umlmyself.controller..*(..))")
>    public Object logRequest(ProceedingJoinPoint pjp) throws Throwable {
>        long start = System.currentTimeMillis();
>        Object result = pjp.proceed();
>        long elapsed = System.currentTimeMillis() - start;
>        log.info("{} {} - {}ms", 
>            request.getMethod(), request.getRequestURI(), elapsed);
>        return result;
>    }
>    ```
> 3. **注册**: 使用 `@Aspect` + `@Component`，Spring Boot 自动发现
> 4. **无需修改**: 任何现有的 Controller 代码

---

> [!summary]- 学习要点总结
> | 主题 | 关键收获 |
> |------|----------|
> | 登录流程 | 2阶段设计：凭证校验 → 角色选择 → JWT签发 |
> | 权限控制 | AOP + 注解实现声明式鉴权，角色从DB动态查询 |
> | 响应格式 | ApiResponse<T> 统一封装，code/message/data 三元组 |
> | Token处理 | cleanToken() 是排查JWT问题的首要关注点 |
> | 论文状态机 | 16个状态 + 多级审批布尔字段的复合状态管理 |
> | DDL策略 | validate模式意味着需要手动管理表结构 |
> | DTO转换 | 手动映射模式，未使用自动映射工具 |
> | 配置管理 | 单文件配置，无多环境分离（改进空间） |
