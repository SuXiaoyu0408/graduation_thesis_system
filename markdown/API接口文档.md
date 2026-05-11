# 毕业论文管理系统 - API接口文档

> 文档版本：v1.0.0  
> 最后更新：2025-01-XX  
> 基础URL：`http://localhost:8080`

---

## 📋 目录

1. [通用说明](#通用说明)
2. [认证与授权](#认证与授权)
3. [用户管理](#用户管理)
4. [登录与密码](#登录与密码)
5. [学生模块](#学生模块)
6. [指导老师模块](#指导老师模块)
7. [专业负责人模块](#专业负责人模块)
8. [学院领导模块](#学院领导模块)
9. [评阅老师模块](#评阅老师模块)
10. [答辩小组模块](#答辩小组模块)
11. [通知管理](#通知管理)
12. [管理员模块](#管理员模块)

---

## 通用说明

### 统一响应格式

所有接口返回统一的JSON格式：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

**响应字段说明：**
- `code`: 状态码（200=成功，400=客户端错误，500=服务器错误）
- `message`: 提示信息
- `data`: 响应数据（根据接口不同而变化）

### 认证方式

大部分接口需要在请求头中携带JWT Token：

```
Authorization: Bearer {token}
```

### 材料类型枚举（MaterialType）

| 值 | 说明 |
|---|---|
| `TOPIC_SELECTION` | 选题申报表 |
| `TASK_ASSIGNMENT` | 课题任务书 |
| `OPENING_REPORT` | 开题报告 |
| `MID_TERM_REPORT` | 中期报告 |
| `FINAL_PAPER` | 论文终稿 |
| `REVIEW_FORM` | 审阅表 |
| `EVALUATION_FORM` | 评阅表 |
| `DEFENSE_REVIEW_FORM` | 答辩评审表 |

### 错误码说明

| 状态码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| 400 | 客户端错误（参数错误、业务逻辑错误等） |
| 401 | 未授权（Token无效或过期） |
| 403 | 无权限访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 认证与授权

### 1. 用户登录（阶段一：身份认证）

**接口地址：** `POST /login`

**接口说明：** 校验用户名和密码，返回用户的所有角色（不生成Token）

**请求参数：**

```json
{
  "username": "string",
  "password": "string"
}
```

**参数说明：**
- `username`: 用户名（必填）
- `password`: 密码（必填）

**响应示例：**

```json
{
  "code": 200,
  "message": "登录成功，请选择角色",
  "data": {
    "userId": 1,
    "username": "student001",
    "roles": [
      {
        "roleId": 1,
        "roleCode": "STUDENT",
        "roleName": "学生"
      }
    ]
  }
}
```

---

### 2. 确认角色（阶段二：生成Token）

**接口地址：** `POST /login/confirm-role`

**接口说明：** 校验用户ID和角色ID的关联关系，生成JWT Token

**请求参数：**

```json
{
  "userId": 1,
  "roleId": 1
}
```

**参数说明：**
- `userId`: 用户ID（必填）
- `roleId`: 角色ID（必填）

**响应示例：**

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": 1,
    "username": "student001",
    "roleId": 1,
    "roleCode": "STUDENT"
  }
}
```

**响应头：**
- `Authorization: Bearer {token}` - Token也会设置在响应头中

---

### 3. 获取当前用户信息

**接口地址：** `GET /api/current-user`

**接口说明：** 获取当前登录用户的详细信息

**请求头：**
- `Authorization: Bearer {token}` - 必填

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "userId": 1,
    "username": "student001",
    "realName": "张三",
    "phone": "13800138000",
    "email": "zhangsan@example.com",
    "collegeId": 1,
    "collegeName": "计算机学院",
    "majorId": 1,
    "majorName": "计算机科学与技术",
    "roles": [
      {
        "roleId": 1,
        "roleCode": "STUDENT",
        "roleName": "学生"
      }
    ]
  }
}
```

---

## 登录与密码

### 1. 发送验证码

**接口地址：** `POST /password/sms-code`

**接口说明：** 发送手机验证码（用于密码找回）

**请求参数：**

```json
{
  "phone": "13800138000"
}
```

**参数说明：**
- `phone`: 手机号（必填，11位手机号）

**响应示例：**

```json
{
  "code": 200,
  "message": "验证码已发送",
  "data": null
}
```

---

### 2. 校验验证码

**接口地址：** `POST /password/verify-code`

**接口说明：** 校验手机验证码是否正确

**请求参数：**

```json
{
  "phone": "13800138000",
  "code": "123456"
}
```

**参数说明：**
- `phone`: 手机号（必填）
- `code`: 验证码（必填）

**响应示例：**

```json
{
  "code": 200,
  "message": "验证码校验成功",
  "data": null
}
```

---

### 3. 重置密码

**接口地址：** `POST /password/reset`

**接口说明：** 通过手机验证码重置密码

**请求参数：**

```json
{
  "phone": "13800138000",
  "newPassword": "newpass123",
  "confirmPassword": "newpass123"
}
```

**参数说明：**
- `phone`: 手机号（必填，11位手机号）
- `newPassword`: 新密码（必填，至少6位）
- `confirmPassword`: 确认密码（必填，必须与新密码一致）

**响应示例：**

```json
{
  "code": 200,
  "message": "密码重置成功",
  "data": null
}
```

---

## 学生模块

**基础路径：** `/api/student/thesis`

**权限要求：** 需要学生角色，且只能操作自己的论文流程

---

### 1. 上传材料

**接口地址：** `POST /api/student/thesis/material/{processId}/{materialType}`

**接口说明：** 上传论文材料（选题申报表、开题报告、中期报告、论文终稿）

**路径参数：**
- `processId`: 论文流程ID（必填）
- `materialType`: 材料类型（必填，可选值：`TOPIC_SELECTION`、`OPENING_REPORT`、`MID_TERM_REPORT`、`FINAL_PAPER`）

**请求头：**
- `Authorization: Bearer {token}` - 必填

**请求体：**
- `file`: 文件（multipart/form-data，必填）

**响应示例：**

```json
{
  "code": 200,
  "message": "材料上传成功",
  "data": null
}
```

---

### 2. 预览最新材料

**接口地址：** `GET /api/student/thesis/material/{processId}/{materialType}/preview`

**接口说明：** 在线预览当前最新版本的材料

**路径参数：**
- `processId`: 论文流程ID（必填）
- `materialType`: 材料类型（必填）

**请求头：**
- `Authorization: Bearer {token}` - 必填

**响应：** 文件流（Content-Type: application/octet-stream）

---

### 3. 下载最新材料

**接口地址：** `GET /api/student/thesis/material/{processId}/{materialType}/download`

**接口说明：** 下载当前最新版本的材料

**路径参数：**
- `processId`: 论文流程ID（必填）
- `materialType`: 材料类型（必填）

**请求头：**
- `Authorization: Bearer {token}` - 必填

**响应：** 文件流（Content-Disposition: attachment）

---

### 4. 查看材料历史版本列表

**接口地址：** `GET /api/student/thesis/material/{processId}/{materialType}/history`

**接口说明：** 获取指定材料的所有历史版本列表

**路径参数：**
- `processId`: 论文流程ID（必填）
- `materialType`: 材料类型（必填）

**请求头：**
- `Authorization: Bearer {token}` - 必填

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "historyId": 1,
      "materialType": "TOPIC_SELECTION",
      "filePath": "/upload/2025/01/01/topic_1_v1.pdf",
      "version": 1,
      "uploaderName": "张三",
      "isLatest": true,
      "rejectedReason": null,
      "uploadedAt": "2025-01-01T10:00:00"
    }
  ]
}
```

---

### 5. 预览历史版本材料

**接口地址：** `GET /api/student/thesis/material/history/{historyId}/preview`

**接口说明：** 预览指定历史版本的材料

**路径参数：**
- `historyId`: 历史记录ID（必填）

**请求头：**
- `Authorization: Bearer {token}` - 必填

**响应：** 文件流

---

### 6. 下载历史版本材料

**接口地址：** `GET /api/student/thesis/material/history/{historyId}/download`

**接口说明：** 下载指定历史版本的材料

**路径参数：**
- `historyId`: 历史记录ID（必填）

**请求头：**
- `Authorization: Bearer {token}` - 必填

**响应：** 文件流

---

### 7. 查看驳回原因

**接口地址：** `GET /api/student/thesis/material/{processId}/{materialType}/rejected-reason`

**接口说明：** 查看材料被驳回的原因

**路径参数：**
- `processId`: 论文流程ID（必填）
- `materialType`: 材料类型（必填）

**请求头：**
- `Authorization: Bearer {token}` - 必填

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": "选题不够新颖，请重新选择"
}
```

---

### 8. 获取我的论文流程信息

**接口地址：** `GET /api/student/thesis/process`

**接口说明：** 获取当前学生的论文流程状态和基本信息

**请求头：**
- `Authorization: Bearer {token}` - 必填

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "title": "基于Spring Boot的毕业论文管理系统设计与实现",
    "status": "TOPIC_APPROVED",
    "supervisor": "李老师"
  }
}
```

---

### 9. 预览课题任务书

**接口地址：** `GET /api/student/thesis/task-assignment/{processId}/preview`

**接口说明：** 预览课题任务书

**路径参数：**
- `processId`: 论文流程ID（必填）

**请求头：**
- `Authorization: Bearer {token}` - 必填

**响应：** 文件流

---

### 10. 下载课题任务书

**接口地址：** `GET /api/student/thesis/task-assignment/{processId}/download`

**接口说明：** 下载课题任务书

**路径参数：**
- `processId`: 论文流程ID（必填）

**请求头：**
- `Authorization: Bearer {token}` - 必填

**响应：** 文件流

---

### 11. 获取系统通知列表

**接口地址：** `GET /api/student/thesis/notices`

**接口说明：** 获取系统通知列表

**请求头：**
- `Authorization: Bearer {token}` - 必填

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "noticeId": 1,
      "title": "关于论文提交的通知",
      "content": "请各位同学在3月1日前提交论文终稿",
      "createdAt": "2025-01-01T10:00:00"
    }
  ]
}
```

---

## 指导老师模块

**基础路径：** `/api/supervisor/thesis`

**权限要求：** 需要指导老师角色，且只能操作分配给自己的学生

---

### 1. 审核材料

**接口地址：** `POST /api/supervisor/thesis/approve`

**接口说明：** 审核学生提交的材料（选题、开题、中期、终稿）

**请求头：**
- `Authorization: Bearer {token}` - 必填

**请求参数：**

```json
{
  "processId": 1,
  "materialType": "TOPIC_SELECTION",
  "pass": true,
  "reason": "选题合理，同意通过"
}
```

**参数说明：**
- `processId`: 论文流程ID（必填）
- `materialType`: 材料类型（必填）
- `pass`: 是否通过（必填，true=通过，false=驳回）
- `reason`: 驳回原因（当pass=false时建议填写）

**响应示例：**

```json
{
  "code": 200,
  "message": "审核操作成功",
  "data": null
}
```

---

### 2. 上传材料

**接口地址：** `POST /api/supervisor/thesis/material/{processId}/{materialType}`

**接口说明：** 上传材料（课题任务书、评阅意见表等）

**路径参数：**
- `processId`: 论文流程ID（必填）
- `materialType`: 材料类型（必填，可选值：`TASK_ASSIGNMENT`、`REVIEW_FORM`等）

**请求头：**
- `Authorization: Bearer {token}` - 必填

**请求体：**
- `file`: 文件（multipart/form-data，必填）

**响应示例：**

```json
{
  "code": 200,
  "message": "材料上传成功",
  "data": null
}
```

---

### 3. 提交评分

**接口地址：** `POST /api/supervisor/thesis/score`

**接口说明：** 提交指导老师评分（结构化评分）

**请求头：**
- `Authorization: Bearer {token}` - 必填

**请求参数：**

```json
{
  "processId": 1,
  "topicReview": 18,
  "innovation": 9,
  "theoryKnowledge": 32,
  "attitudeAndWriting": 33
}
```

**参数说明：**
- `processId`: 论文流程ID（必填）
- `topicReview`: 选题与文献综述（0-20分，必填）
- `innovation`: 创新性（0-10分，必填）
- `theoryKnowledge`: 基础理论与专业知识（0-35分，必填）
- `attitudeAndWriting`: 态度、写作水平、规范、综合能力（0-35分，必填）

**响应示例：**

```json
{
  "code": 200,
  "message": "评分提交成功",
  "data": null
}
```

---

### 4. 预览学生材料

**接口地址：** `GET /api/supervisor/thesis/student/material/{processId}/{materialType}/preview`

**接口说明：** 预览学生提交的材料

**路径参数：**
- `processId`: 论文流程ID（必填）
- `materialType`: 材料类型（必填）

**请求头：**
- `Authorization: Bearer {token}` - 必填

**响应：** 文件流

---

### 5. 下载学生材料

**接口地址：** `GET /api/supervisor/thesis/student/material/{processId}/{materialType}/download`

**接口说明：** 下载学生提交的材料

**路径参数：**
- `processId`: 论文流程ID（必填）
- `materialType`: 材料类型（必填）

**请求头：**
- `Authorization: Bearer {token}` - 必填

**响应：** 文件流

---

### 6. 查看学生材料历史版本列表

**接口地址：** `GET /api/supervisor/thesis/student/material/{processId}/{materialType}/history`

**接口说明：** 查看学生材料的历史版本列表

**路径参数：**
- `processId`: 论文流程ID（必填）
- `materialType`: 材料类型（必填）

**请求头：**
- `Authorization: Bearer {token}` - 必填

**响应示例：** 同学生模块的"查看材料历史版本列表"

---

### 7. 预览历史版本材料

**接口地址：** `GET /api/supervisor/thesis/material/history/{historyId}/preview`

**接口说明：** 预览历史版本材料

**路径参数：**
- `historyId`: 历史记录ID（必填）

**请求头：**
- `Authorization: Bearer {token}` - 必填

**响应：** 文件流

---

### 8. 下载历史版本材料

**接口地址：** `GET /api/supervisor/thesis/material/history/{historyId}/download`

**接口说明：** 下载历史版本材料

**路径参数：**
- `historyId`: 历史记录ID（必填）

**请求头：**
- `Authorization: Bearer {token}` - 必填

**响应：** 文件流

---

## 专业负责人模块

**基础路径：** `/api/major-leader/thesis`

**权限要求：** 需要专业负责人角色

---

### 1. 审核材料

**接口地址：** `POST /api/major-leader/thesis/approve`

**接口说明：** 审核选题、课题任务书等

**请求头：**
- `Authorization: Bearer {token}` - 必填

**请求参数：** 同指导老师模块的"审核材料"

**响应示例：**

```json
{
  "code": 200,
  "message": "审核操作成功",
  "data": null
}
```

---

### 2. 预览材料

**接口地址：** `GET /api/major-leader/thesis/material/{processId}/{materialType}/preview`

**接口说明：** 预览材料

**路径参数：**
- `processId`: 论文流程ID（必填）
- `materialType`: 材料类型（必填）

**请求头：**
- `Authorization: Bearer {token}` - 必填

**响应：** 文件流

---

### 3. 下载材料

**接口地址：** `GET /api/major-leader/thesis/material/{processId}/{materialType}/download`

**接口说明：** 下载材料

**路径参数：**
- `processId`: 论文流程ID（必填）
- `materialType`: 材料类型（必填）

**请求头：**
- `Authorization: Bearer {token}` - 必填

**响应：** 文件流

---

### 4. 查看材料历史版本列表

**接口地址：** `GET /api/major-leader/thesis/material/{processId}/{materialType}/history`

**接口说明：** 查看材料历史版本列表

**路径参数：**
- `processId`: 论文流程ID（必填）
- `materialType`: 材料类型（必填）

**请求头：**
- `Authorization: Bearer {token}` - 必填

**响应示例：** 同学生模块的"查看材料历史版本列表"

---

### 5. 预览历史版本材料

**接口地址：** `GET /api/major-leader/thesis/material/history/{historyId}/preview`

**接口说明：** 预览历史版本材料

**路径参数：**
- `historyId`: 历史记录ID（必填）

**请求头：**
- `Authorization: Bearer {token}` - 必填

**响应：** 文件流

---

### 6. 下载历史版本材料

**接口地址：** `GET /api/major-leader/thesis/material/history/{historyId}/download`

**接口说明：** 下载历史版本材料

**路径参数：**
- `historyId`: 历史记录ID（必填）

**请求头：**
- `Authorization: Bearer {token}` - 必填

**响应：** 文件流

---

## 学院领导模块

**基础路径：** `/api/college-leader/thesis`

**权限要求：** 需要学院领导角色

---

### 1. 审核材料

**接口地址：** `POST /api/college-leader/thesis/approve`

**接口说明：** 审核选题、课题任务书等

**请求头：**
- `Authorization: Bearer {token}` - 必填

**请求参数：** 同指导老师模块的"审核材料"

**响应示例：**

```json
{
  "code": 200,
  "message": "审核操作成功",
  "data": null
}
```

---

### 2. 上传正式材料

**接口地址：** `POST /api/college-leader/thesis/official-material/{processId}/{materialType}`

**接口说明：** 上传正式材料

**路径参数：**
- `processId`: 论文流程ID（必填）
- `materialType`: 材料类型（必填）

**请求头：**
- `Authorization: Bearer {token}` - 必填

**请求体：**
- `file`: 文件（multipart/form-data，必填）

**响应示例：**

```json
{
  "code": 200,
  "message": "官方材料上传成功",
  "data": null
}
```

---

### 3. 预览材料

**接口地址：** `GET /api/college-leader/thesis/material/{processId}/{materialType}/preview`

**接口说明：** 预览材料

**路径参数：**
- `processId`: 论文流程ID（必填）
- `materialType`: 材料类型（必填）

**请求头：**
- `Authorization: Bearer {token}` - 必填

**响应：** 文件流

---

### 4. 下载材料

**接口地址：** `GET /api/college-leader/thesis/material/{processId}/{materialType}/download`

**接口说明：** 下载材料

**路径参数：**
- `processId`: 论文流程ID（必填）
- `materialType`: 材料类型（必填）

**请求头：**
- `Authorization: Bearer {token}` - 必填

**响应：** 文件流

---

### 5. 查看材料历史版本列表

**接口地址：** `GET /api/college-leader/thesis/material/{processId}/{materialType}/history`

**接口说明：** 查看材料历史版本列表

**路径参数：**
- `processId`: 论文流程ID（必填）
- `materialType`: 材料类型（必填）

**请求头：**
- `Authorization: Bearer {token}` - 必填

**响应示例：** 同学生模块的"查看材料历史版本列表"

---

### 6. 预览历史版本材料

**接口地址：** `GET /api/college-leader/thesis/material/history/{historyId}/preview`

**接口说明：** 预览历史版本材料

**路径参数：**
- `historyId`: 历史记录ID（必填）

**请求头：**
- `Authorization: Bearer {token}` - 必填

**响应：** 文件流

---

### 7. 下载历史版本材料

**接口地址：** `GET /api/college-leader/thesis/material/history/{historyId}/download`

**接口说明：** 下载历史版本材料

**路径参数：**
- `historyId`: 历史记录ID（必填）

**请求头：**
- `Authorization: Bearer {token}` - 必填

**响应：** 文件流

---

## 评阅老师模块

**基础路径：** `/api/reviewer/thesis`

**权限要求：** 需要评阅老师角色，且只能操作分配给自己的学生（通过`reviewerId`校验）

---

### 1. 上传评阅表

**接口地址：** `POST /api/reviewer/thesis/evaluation-form/{processId}`

**接口说明：** 上传评阅意见表

**路径参数：**
- `processId`: 论文流程ID（必填）

**请求头：**
- `Authorization: Bearer {token}` - 必填

**请求体：**
- `file`: 文件（multipart/form-data，必填）

**响应示例：**

```json
{
  "code": 200,
  "message": "评阅表上传成功",
  "data": null
}
```

---

### 2. 提交评分

**接口地址：** `POST /api/reviewer/thesis/score`

**接口说明：** 提交评阅老师评分（结构化评分）

**请求头：**
- `Authorization: Bearer {token}` - 必填

**请求参数：**

```json
{
  "processId": 1,
  "topicReview": 18,
  "innovation": 9,
  "theoryKnowledge": 38,
  "writingSkill": 28
}
```

**参数说明：**
- `processId`: 论文流程ID（必填）
- `topicReview`: 选题与文献综述（0-20分，必填）
- `innovation`: 创新性（0-10分，必填）
- `theoryKnowledge`: 基础理论与专业知识（0-40分，必填）
- `writingSkill`: 写作水平与综合能力（0-30分，必填）

**响应示例：**

```json
{
  "code": 200,
  "message": "评分提交成功",
  "data": null
}
```

---

### 3. 预览评阅表

**接口地址：** `GET /api/reviewer/thesis/evaluation-form/{processId}/preview`

**接口说明：** 预览评阅意见表

**路径参数：**
- `processId`: 论文流程ID（必填）

**请求头：**
- `Authorization: Bearer {token}` - 必填

**响应：** 文件流

---

### 4. 下载评阅表

**接口地址：** `GET /api/reviewer/thesis/evaluation-form/{processId}/download`

**接口说明：** 下载评阅意见表

**路径参数：**
- `processId`: 论文流程ID（必填）

**请求头：**
- `Authorization: Bearer {token}` - 必填

**响应：** 文件流

---

### 5. 预览学生材料

**接口地址：** `GET /api/reviewer/thesis/student/material/{processId}/{materialType}/preview`

**接口说明：** 预览学生材料（选题申报表、开题报告、中期报告、论文终稿）

**路径参数：**
- `processId`: 论文流程ID（必填）
- `materialType`: 材料类型（必填，可选值：`TOPIC_SELECTION`、`OPENING_REPORT`、`MID_TERM_REPORT`、`FINAL_PAPER`）

**请求头：**
- `Authorization: Bearer {token}` - 必填

**响应：** 文件流

---

### 6. 下载学生材料

**接口地址：** `GET /api/reviewer/thesis/student/material/{processId}/{materialType}/download`

**接口说明：** 下载学生材料

**路径参数：**
- `processId`: 论文流程ID（必填）
- `materialType`: 材料类型（必填）

**请求头：**
- `Authorization: Bearer {token}` - 必填

**响应：** 文件流

---

## 答辩小组模块

**基础路径：** `/api/defense-team/thesis`

**权限要求：** 需要答辩小组成员角色，且只能操作分配给自己的学生（通过`defenseTeamId`校验）

---

### 1. 上传答辩评审表

**接口地址：** `POST /api/defense-team/thesis/defense-review-form/{processId}`

**接口说明：** 上传答辩评审表

**路径参数：**
- `processId`: 论文流程ID（必填）

**请求头：**
- `Authorization: Bearer {token}` - 必填

**请求体：**
- `file`: 文件（multipart/form-data，必填）

**响应示例：**

```json
{
  "code": 200,
  "message": "答辩评审表上传成功",
  "data": null
}
```

---

### 2. 提交评分

**接口地址：** `POST /api/defense-team/thesis/score`

**接口说明：** 提交答辩小组评分（结构化评分）

**请求头：**
- `Authorization: Bearer {token}` - 必填

**请求参数：**

```json
{
  "processId": 1,
  "reportContent": 38,
  "reportProcess": 9,
  "defensePerformance": 48
}
```

**参数说明：**
- `processId`: 论文流程ID（必填）
- `reportContent`: 报告内容（0-40分，必填）
- `reportProcess`: 报告过程（0-10分，必填）
- `defensePerformance`: 答辩表现（0-50分，必填）

**响应示例：**

```json
{
  "code": 200,
  "message": "答辩评分提交成功",
  "data": null
}
```

---

### 3. 预览答辩评审表

**接口地址：** `GET /api/defense-team/thesis/defense-review-form/{processId}/preview`

**接口说明：** 预览答辩评审表

**路径参数：**
- `processId`: 论文流程ID（必填）

**请求头：**
- `Authorization: Bearer {token}` - 必填

**响应：** 文件流

---

### 4. 下载答辩评审表

**接口地址：** `GET /api/defense-team/thesis/defense-review-form/{processId}/download`

**接口说明：** 下载答辩评审表

**路径参数：**
- `processId`: 论文流程ID（必填）

**请求头：**
- `Authorization: Bearer {token}` - 必填

**响应：** 文件流

---

### 5. 预览论文终稿

**接口地址：** `GET /api/defense-team/thesis/final-paper/{processId}/preview`

**接口说明：** 预览论文终稿

**路径参数：**
- `processId`: 论文流程ID（必填）

**请求头：**
- `Authorization: Bearer {token}` - 必填

**响应：** 文件流

---

### 6. 下载论文终稿

**接口地址：** `GET /api/defense-team/thesis/final-paper/{processId}/download`

**接口说明：** 下载论文终稿

**路径参数：**
- `processId`: 论文流程ID（必填）

**请求头：**
- `Authorization: Bearer {token}` - 必填

**响应：** 文件流

---

## 通知管理

**基础路径：** `/api/notice`

---

### 1. 获取最新通知列表（公开接口）

**接口地址：** `GET /api/notice/latest`

**接口说明：** 获取最新通知列表（无需认证）

**查询参数：**
- `limit`: 返回数量（可选，默认5）

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "noticeId": 1,
      "title": "关于论文提交的通知",
      "content": "请各位同学在3月1日前提交论文终稿",
      "createdAt": "2025-01-01T10:00:00"
    }
  ]
}
```

---

### 2. 创建通知（管理员接口）

**接口地址：** `POST /api/notice/admin`

**接口说明：** 管理员发布通知

**权限要求：** 需要管理员权限

**请求头：**
- `Authorization: Bearer {token}` - 必填

**请求参数：**

```json
{
  "title": "关于论文提交的通知",
  "content": "请各位同学在3月1日前提交论文终稿"
}
```

**参数说明：**
- `title`: 通知标题（必填）
- `content`: 通知内容（必填）

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "noticeId": 1,
    "title": "关于论文提交的通知",
    "content": "请各位同学在3月1日前提交论文终稿",
    "createdAt": "2025-01-01T10:00:00"
  }
}
```

---

### 3. 更新通知（管理员接口）

**接口地址：** `PUT /api/notice/admin/{noticeId}`

**接口说明：** 管理员更新通知

**权限要求：** 需要管理员权限

**路径参数：**
- `noticeId`: 通知ID（必填）

**请求头：**
- `Authorization: Bearer {token}` - 必填

**请求参数：**

```json
{
  "title": "关于论文提交的通知（更新）",
  "content": "请各位同学在3月5日前提交论文终稿"
}
```

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "noticeId": 1,
    "title": "关于论文提交的通知（更新）",
    "content": "请各位同学在3月5日前提交论文终稿",
    "createdAt": "2025-01-01T10:00:00"
  }
}
```

---

### 4. 删除通知（管理员接口）

**接口地址：** `DELETE /api/notice/admin/{noticeId}`

**接口说明：** 管理员删除通知

**权限要求：** 需要管理员权限

**路径参数：**
- `noticeId`: 通知ID（必填）

**请求头：**
- `Authorization: Bearer {token}` - 必填

**响应示例：**

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

### 5. 分页查询所有通知（管理员接口）

**接口地址：** `GET /api/notice/admin`

**接口说明：** 管理员分页查询所有通知

**权限要求：** 需要管理员权限

**查询参数：**
- `page`: 页码（可选，默认1）
- `size`: 每页大小（可选，默认10）

**请求头：**
- `Authorization: Bearer {token}` - 必填

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "noticeId": 1,
      "title": "关于论文提交的通知",
      "content": "请各位同学在3月1日前提交论文终稿",
      "createdAt": "2025-01-01T10:00:00"
    }
  ]
}
```

---

## 管理员模块

**基础路径：** `/api/admin`

**权限要求：** 所有接口都需要管理员权限

---

### 1. 分页查询用户列表

**接口地址：** `GET /api/admin/users`

**接口说明：** 分页查询所有用户，支持按角色、学院、专业筛选

**请求头：**
- `Authorization: Bearer {token}` - 必填

**查询参数：**
- `page`: 页码（可选，默认1）
- `size`: 每页大小（可选，默认10）
- `username`: 用户名（可选，模糊查询）
- `realName`: 真实姓名（可选，模糊查询）
- `roleId`: 角色ID（可选）
- `collegeId`: 学院ID（可选）
- `majorId`: 专业ID（可选）
- `status`: 状态（可选，1=启用，0=禁用）

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "users": [
      {
        "userId": 1,
        "username": "student001",
        "realName": "张三",
        "phone": "13800138000",
        "email": "zhangsan@example.com",
        "collegeId": 1,
        "collegeName": "计算机学院",
        "majorId": 1,
        "majorName": "计算机科学与技术",
        "status": 1,
        "roles": [
          {
            "roleId": 1,
            "roleCode": "STUDENT",
            "roleName": "学生"
          }
        ]
      }
    ],
    "total": 100,
    "page": 1,
    "size": 10,
    "totalPages": 10
  }
}
```

---

### 2. 查询用户详情

**接口地址：** `GET /api/admin/users/{userId}`

**接口说明：** 根据用户ID查询用户详细信息

**路径参数：**
- `userId`: 用户ID（必填）

**请求头：**
- `Authorization: Bearer {token}` - 必填

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "userId": 1,
    "username": "student001",
    "realName": "张三",
    "phone": "13800138000",
    "email": "zhangsan@example.com",
    "collegeId": 1,
    "collegeName": "计算机学院",
    "majorId": 1,
    "majorName": "计算机科学与技术",
    "status": 1,
    "roles": [
      {
        "roleId": 1,
        "roleCode": "STUDENT",
        "roleName": "学生"
      }
    ]
  }
}
```

---

### 3. 创建用户

**接口地址：** `POST /api/admin/users`

**接口说明：** 创建新用户（学生、老师等）

**请求头：**
- `Authorization: Bearer {token}` - 必填

**请求参数：**

```json
{
  "username": "student002",
  "password": "123456",
  "realName": "李四",
  "phone": "13900139000",
  "email": "lisi@example.com",
  "collegeId": 1,
  "majorId": 1,
  "roleIds": [1],
  "status": 1
}
```

**参数说明：**
- `username`: 用户名（必填，唯一）
- `password`: 密码（必填，至少6位）
- `realName`: 真实姓名（可选）
- `phone`: 手机号（可选，11位手机号格式）
- `email`: 邮箱（可选，邮箱格式）
- `collegeId`: 学院ID（可选）
- `majorId`: 专业ID（可选）
- `roleIds`: 角色ID列表（必填，至少一个角色）
- `status`: 账号状态（可选，1=启用，0=禁用，默认1）

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "userId": 2,
    "username": "student002",
    "realName": "李四",
    "phone": "13900139000",
    "email": "lisi@example.com",
    "collegeId": 1,
    "collegeName": "计算机学院",
    "majorId": 1,
    "majorName": "计算机科学与技术",
    "status": 1,
    "roles": [
      {
        "roleId": 1,
        "roleCode": "STUDENT",
        "roleName": "学生"
      }
    ]
  }
}
```

---

### 4. 更新用户信息

**接口地址：** `PUT /api/admin/users/{userId}`

**接口说明：** 更新用户信息（基本信息、角色分配）

**路径参数：**
- `userId`: 用户ID（必填）

**请求头：**
- `Authorization: Bearer {token}` - 必填

**请求参数：**

```json
{
  "realName": "李四（更新）",
  "phone": "13900139001",
  "email": "lisi_new@example.com",
  "collegeId": 1,
  "majorId": 1,
  "roleIds": [1, 2],
  "status": 1
}
```

**参数说明：** 所有字段都是可选的，只更新提供的字段

**响应示例：** 同"查询用户详情"

---

### 5. 启用/禁用用户账号

**接口地址：** `PUT /api/admin/users/{userId}/status`

**接口说明：** 启用或禁用用户账号

**路径参数：**
- `userId`: 用户ID（必填）

**查询参数：**
- `status`: 状态（必填，1=启用，0=禁用）

**请求头：**
- `Authorization: Bearer {token}` - 必填

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

---

### 6. 删除用户（软删除）

**接口地址：** `DELETE /api/admin/users/{userId}`

**接口说明：** 删除用户（软删除，将状态设为0）

**路径参数：**
- `userId`: 用户ID（必填）

**请求头：**
- `Authorization: Bearer {token}` - 必填

**响应示例：**

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

### 7. 分页查询已归档论文列表

**接口地址：** `GET /api/admin/archive`

**接口说明：** 查询所有已完成（COMPLETED）的论文流程

**请求头：**
- `Authorization: Bearer {token}` - 必填

**查询参数：**
- `page`: 页码（可选，默认1）
- `size`: 每页大小（可选，默认10）
- `collegeId`: 学院ID（可选）
- `majorId`: 专业ID（可选）
- `studentName`: 学生姓名（可选，模糊查询）
- `thesisTitle`: 论文标题（可选，模糊查询）

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "archives": [
      {
        "processId": 1,
        "studentId": 1,
        "studentName": "张三",
        "studentNo": "2021001",
        "collegeId": 1,
        "collegeName": "计算机学院",
        "majorId": 1,
        "majorName": "计算机科学与技术",
        "supervisorId": 10,
        "supervisorName": "李老师",
        "thesisTitle": "基于Spring Boot的毕业论文管理系统设计与实现",
        "finalScore": 85.5,
        "gradeLevel": "良好",
        "completedAt": "2025-01-15T10:00:00"
      }
    ],
    "total": 50,
    "page": 1,
    "size": 10,
    "totalPages": 5
  }
}
```

---

### 8. 获取归档统计信息

**接口地址：** `GET /api/admin/archive/statistics`

**接口说明：** 统计各学院、专业的论文完成情况

**请求头：**
- `Authorization: Bearer {token}` - 必填

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "totalCompleted": 100,
    "collegeStatistics": [
      {
        "collegeId": 1,
        "collegeName": "计算机学院",
        "completedCount": 50
      }
    ],
    "majorStatistics": [
      {
        "majorId": 1,
        "majorName": "计算机科学与技术",
        "collegeId": 1,
        "collegeName": "计算机学院",
        "completedCount": 30
      }
    ]
  }
}
```

---

### 9. 批量导出已归档论文材料

**接口地址：** `POST /api/admin/archive/export`

**接口说明：** 批量导出已归档论文的材料（ZIP格式）

**请求头：**
- `Authorization: Bearer {token}` - 必填

**请求参数：**

```json
[1, 2, 3]
```

**参数说明：**
- 流程ID列表（可选，如果为空或null，则导出所有已归档论文）
- 如果提供流程ID列表，只导出指定的论文材料

**响应：** ZIP文件流
- `Content-Type: application/zip`
- `Content-Disposition: attachment; filename=archive_materials.zip`

**文件结构：**
```
archive_materials.zip
├── 计算机学院_计算机科学与技术_2021001_张三/
│   ├── 选题申报表_v1.pdf
│   ├── 开题报告_v1.pdf
│   ├── 中期报告_v1.pdf
│   └── 论文终稿_v1.pdf
└── 计算机学院_软件工程_2021002_李四/
    └── ...
```

---

## 📝 附录

### 论文流程状态（ThesisStatus）

| 状态码 | 说明 |
|--------|------|
| `INIT` | 未开始 |
| `TOPIC_SUBMITTED` | 已提交选题申报表 |
| `TOPIC_APPROVED` | 选题已通过 |
| `OPENING_SUBMITTED` | 已提交开题报告 |
| `OPENING_APPROVED` | 开题已通过 |
| `MIDTERM_SUBMITTED` | 已提交中期报告 |
| `MIDTERM_APPROVED` | 中期已通过 |
| `FINAL_SUBMITTED` | 已提交终稿 |
| `FINAL_APPROVED` | 终稿已通过 |
| `DEFENSE_SCORED` | 答辩评分已完成 |
| `COMPLETED` | 论文流程已完成 |

### 成绩等级（GradeLevel）

| 等级 | 说明 |
|------|------|
| `EXCELLENT` | 优秀 |
| `GOOD` | 良好 |
| `AVERAGE` | 中等 |
| `PASS` | 合格 |
| `FAIL` | 不合格 |

### 评分权重

- **指导老师评分**：40%
- **评阅老师评分**：20%
- **答辩小组评分**：40%

最终成绩 = 指导老师评分 × 0.4 + 评阅老师评分 × 0.2 + 答辩小组评分 × 0.4

---

## 🔗 相关链接

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **API Docs**: `http://localhost:8080/api-docs`

---

## 📌 注意事项

1. **Token有效期**：JWT Token默认有效期为7天，过期后需要重新登录
2. **文件上传限制**：建议单文件大小不超过50MB
3. **权限校验**：
   - 评阅老师只能为分配给自己的学生评分（通过`reviewerId`校验）
   - 答辩小组只能为分配给自己的学生评分（通过`defenseTeamId`校验）
   - 学生只能操作自己的论文流程
   - 指导老师只能操作分配给自己的学生
4. **数据校验**：所有必填字段都有校验，请确保提交的数据符合要求
5. **错误处理**：所有接口都使用统一的错误响应格式，请根据`code`和`message`判断错误原因

---

**文档结束**

