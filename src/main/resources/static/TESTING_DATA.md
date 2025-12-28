# 毕业论文管理系统 - 测试数据

## 登录功能测试数据

### 学生角色
- 用户名：`student123`
- 密码：任意密码
- 登录后跳转：`management_system.html?role=STUDENT`

### 指导老师角色
- 用户名：`instructor123`
- 密码：任意密码
- 登录后跳转：`management_system.html?role=TEACHER`

### 专业负责人角色
- 用户名：`major_responsible123`
- 密码：任意密码
- 登录后跳转：`management_system.html?role=PRO_LEADER`

### 评阅老师角色
- 用户名：`reviewer123`
- 密码：任意密码
- 登录后跳转：`management_system.html?role=REVIEWER`

### 二级学院领导角色
- 用户名：`college_leader123`
- 密码：任意密码
- 登录后跳转：`management_system.html?role=DEAN`

### 管理员角色
- 用户名：`admin123`
- 密码：任意密码
- 登录后跳转：`management_system.html?role=ADMIN`

### 答辩小组成员角色
- 用户名：`defense_member123`
- 密码：任意密码
- 登录后跳转：`management_system.html?role=DEFENSE_MEMBER`

### 答辩小组组长角色
- 用户名：`defense_leader123`
- 密码：任意密码
- 登录后跳转：`management_system.html?role=DEFENSE_CHAIRMAN`

## 忘记密码功能测试

### 测试手机号
- 任意格式正确的手机号（如：13800138000）都可以通过验证
- 验证码：自动发送，无需输入
- 重置密码后返回登录页面

## 通用说明

1. 登录功能中，用户名必须是上述之一，密码可以是任意值
2. 忘记密码功能中，输入任意格式正确的手机号即可测试
3. 所有角色都跳转到统一的 management_system.html 页面，通过 URL 参数传递角色信息
4. 系统会根据用户名自动判断用户角色并跳转到对应页面