# 找回密码/重置密码接口文档

## 接口概述

本文档描述了找回密码和重置密码功能的 RESTful API 接口。

**基础URL**: `http://localhost:8080`

---

## 1. 发送验证码接口

### 1.1 接口信息

- **接口路径**: `/password/sms-code`
- **请求方法**: `POST`
- **接口描述**: 向指定手机号发送验证码，用于找回密码

### 1.2 请求信息

#### 请求头（Request Headers）

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Content-Type | String | 是 | application/json |

#### 请求体（Request Body）

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| phone | String | 是 | 手机号 |

**请求体示例**:
```json
{
  "phone": "13800000001"
}
```

### 1.3 响应信息

#### 响应体（Response Body）

**成功响应**:
```json
{
  "code": 200,
  "message": "验证码已发送",
  "data": null
}
```

**错误响应示例**:
```json
{
  "code": 400,
  "message": "该手机号未注册",
  "data": null
}
```

### 1.4 状态码说明

| 状态码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| 400 | 业务错误（手机号未注册等） |
| 500 | 系统错误 |

### 1.5 注意事项

- 验证码有效期为 5 分钟
- 验证码为 6 位随机数字
- 验证码存储在 Redis 中，Key 格式：`sms:reset:{phone}`
- 实际生产环境需要集成短信服务发送验证码

---

## 2. 校验验证码接口

### 2.1 接口信息

- **接口路径**: `/password/verify-code`
- **请求方法**: `POST`
- **接口描述**: 校验用户输入的验证码是否正确

### 2.2 请求信息

#### 请求头（Request Headers）

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Content-Type | String | 是 | application/json |

#### 请求体（Request Body）

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| phone | String | 是 | 手机号 |
| code | String | 是 | 验证码（6位数字） |

**请求体示例**:
```json
{
  "phone": "13800000001",
  "code": "123456"
}
```

### 2.3 响应信息

#### 响应体（Response Body）

**成功响应**:
```json
{
  "code": 200,
  "message": "验证码校验成功",
  "data": null
}
```

**错误响应示例**:

验证码不存在或已过期:
```json
{
  "code": 400,
  "message": "验证码不存在或已过期",
  "data": null
}
```

验证码错误:
```json
{
  "code": 400,
  "message": "验证码错误",
  "data": null
}
```

### 2.4 状态码说明

| 状态码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| 400 | 业务错误（验证码错误、已过期等） |
| 500 | 系统错误 |

### 2.5 注意事项

- 验证码校验成功后，会设置校验通过标记（有效期 10 分钟）
- 验证码校验成功后会被删除，防止重复使用
- 只有校验通过后才能进行密码重置操作

---

## 3. 重置密码接口

### 3.1 接口信息

- **接口路径**: `/password/reset`
- **请求方法**: `POST`
- **接口描述**: 重置用户密码，需要先校验验证码

### 3.2 请求信息

#### 请求头（Request Headers）

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Content-Type | String | 是 | application/json |

#### 请求体（Request Body）

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| phone | String | 是 | 手机号 |
| newPassword | String | 是 | 新密码 |
| confirmPassword | String | 是 | 确认密码 |

**请求体示例**:
```json
{
  "phone": "13800000001",
  "newPassword": "newPassword123",
  "confirmPassword": "newPassword123"
}
```

### 3.3 响应信息

#### 响应体（Response Body）

**成功响应**:
```json
{
  "code": 200,
  "message": "密码重置成功",
  "data": null
}
```

**错误响应示例**:

两次密码不一致:
```json
{
  "code": 400,
  "message": "两次输入的密码不一致",
  "data": null
}
```

验证码未校验:
```json
{
  "code": 400,
  "message": "请先校验验证码",
  "data": null
}
```

用户不存在:
```json
{
  "code": 400,
  "message": "用户不存在",
  "data": null
}
```

### 3.4 状态码说明

| 状态码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| 400 | 业务错误（密码不一致、验证码未校验等） |
| 500 | 系统错误 |

### 3.5 注意事项

- 必须先调用校验验证码接口，验证通过后才能重置密码
- 新密码会使用 BCrypt 加密后存储
- 密码重置成功后，验证码校验标记会被删除
- 不返回 password 字段

---

## 4. 前端调用示例

### 4.1 JavaScript (Fetch API)

```javascript
// 发送验证码
async function sendSmsCode(phone) {
  try {
    const response = await fetch('http://localhost:8080/password/sms-code', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        phone: phone
      })
    });
    
    const result = await response.json();
    
    if (result.code === 200) {
      console.log('验证码已发送');
      return true;
    } else {
      console.error('发送失败:', result.message);
      alert(result.message);
      return false;
    }
  } catch (error) {
    console.error('请求错误:', error);
    alert('网络错误，请稍后重试');
    return false;
  }
}

// 校验验证码
async function verifyCode(phone, code) {
  try {
    const response = await fetch('http://localhost:8080/password/verify-code', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        phone: phone,
        code: code
      })
    });
    
    const result = await response.json();
    
    if (result.code === 200) {
      console.log('验证码校验成功');
      return true;
    } else {
      console.error('校验失败:', result.message);
      alert(result.message);
      return false;
    }
  } catch (error) {
    console.error('请求错误:', error);
    alert('网络错误，请稍后重试');
    return false;
  }
}

// 重置密码
async function resetPassword(phone, newPassword, confirmPassword) {
  try {
    const response = await fetch('http://localhost:8080/password/reset', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        phone: phone,
        newPassword: newPassword,
        confirmPassword: confirmPassword
      })
    });
    
    const result = await response.json();
    
    if (result.code === 200) {
      console.log('密码重置成功');
      alert('密码重置成功，请重新登录');
      // 跳转到登录页
      window.location.href = 'login_page.html';
      return true;
    } else {
      console.error('重置失败:', result.message);
      alert(result.message);
      return false;
    }
  } catch (error) {
    console.error('请求错误:', error);
    alert('网络错误，请稍后重试');
    return false;
  }
}
```

### 4.2 JavaScript (Axios)

```javascript
import axios from 'axios';

// 发送验证码
async function sendSmsCode(phone) {
  try {
    const response = await axios.post('http://localhost:8080/password/sms-code', {
      phone: phone
    });
    
    const result = response.data;
    
    if (result.code === 200) {
      console.log('验证码已发送');
      return true;
    } else {
      console.error('发送失败:', result.message);
      alert(result.message);
      return false;
    }
  } catch (error) {
    console.error('请求错误:', error);
    if (error.response) {
      alert(error.response.data.message || '发送失败');
    } else {
      alert('网络错误，请稍后重试');
    }
    return false;
  }
}

// 校验验证码
async function verifyCode(phone, code) {
  try {
    const response = await axios.post('http://localhost:8080/password/verify-code', {
      phone: phone,
      code: code
    });
    
    const result = response.data;
    
    if (result.code === 200) {
      console.log('验证码校验成功');
      return true;
    } else {
      console.error('校验失败:', result.message);
      alert(result.message);
      return false;
    }
  } catch (error) {
    console.error('请求错误:', error);
    if (error.response) {
      alert(error.response.data.message || '校验失败');
    } else {
      alert('网络错误，请稍后重试');
    }
    return false;
  }
}

// 重置密码
async function resetPassword(phone, newPassword, confirmPassword) {
  try {
    const response = await axios.post('http://localhost:8080/password/reset', {
      phone: phone,
      newPassword: newPassword,
      confirmPassword: confirmPassword
    });
    
    const result = response.data;
    
    if (result.code === 200) {
      console.log('密码重置成功');
      alert('密码重置成功，请重新登录');
      window.location.href = 'login_page.html';
      return true;
    } else {
      console.error('重置失败:', result.message);
      alert(result.message);
      return false;
    }
  } catch (error) {
    console.error('请求错误:', error);
    if (error.response) {
      alert(error.response.data.message || '重置失败');
    } else {
      alert('网络错误，请稍后重试');
    }
    return false;
  }
}
```

### 4.3 完整流程示例

```javascript
// 完整的找回密码流程
async function forgotPasswordFlow() {
  const phone = document.getElementById('phoneInput').value;
  
  // 步骤1: 发送验证码
  const sendSuccess = await sendSmsCode(phone);
  if (!sendSuccess) {
    return;
  }
  
  // 显示验证码输入框
  document.getElementById('codeInput').style.display = 'block';
  
  // 步骤2: 用户输入验证码后，校验验证码
  const code = document.getElementById('codeInput').value;
  const verifySuccess = await verifyCode(phone, code);
  if (!verifySuccess) {
    return;
  }
  
  // 显示密码输入框
  document.getElementById('passwordInput').style.display = 'block';
  
  // 步骤3: 用户输入新密码后，重置密码
  const newPassword = document.getElementById('newPasswordInput').value;
  const confirmPassword = document.getElementById('confirmPasswordInput').value;
  
  if (newPassword !== confirmPassword) {
    alert('两次输入的密码不一致');
    return;
  }
  
  await resetPassword(phone, newPassword, confirmPassword);
}
```

---

## 5. 错误处理建议

### 5.1 常见错误码

| code | message | 处理建议 |
|------|---------|----------|
| 200 | 验证码已发送 | 提示用户查看手机短信 |
| 200 | 验证码校验成功 | 允许用户继续重置密码 |
| 200 | 密码重置成功 | 提示用户重新登录 |
| 400 | 该手机号未注册 | 提示用户检查手机号 |
| 400 | 验证码不存在或已过期 | 提示用户重新获取验证码 |
| 400 | 验证码错误 | 提示用户检查验证码 |
| 400 | 请先校验验证码 | 提示用户先校验验证码 |
| 400 | 两次输入的密码不一致 | 提示用户检查密码输入 |
| 500 | 系统错误 | 提示用户稍后重试或联系技术支持 |

### 5.2 前端错误处理示例

```javascript
function handlePasswordError(result) {
  switch (result.code) {
    case 200:
      // 成功，不需要处理
      break;
    case 400:
      // 业务错误，显示错误信息
      alert(result.message);
      break;
    case 500:
      // 系统错误，记录日志并提示用户
      console.error('系统错误:', result.message);
      alert('系统繁忙，请稍后重试');
      break;
    default:
      alert('未知错误，请稍后重试');
  }
}
```

---

## 6. 安全注意事项

1. **验证码安全**：
   - 验证码有效期为 5 分钟，过期后需要重新获取
   - 验证码校验成功后会被删除，防止重复使用
   - 建议在前端添加验证码输入次数限制（如最多尝试 5 次）

2. **密码安全**：
   - 密码使用 BCrypt 加密存储
   - 建议前端对密码强度进行校验（长度、复杂度等）
   - 重置密码前必须先校验验证码

3. **请求安全**：
   - 所有请求必须使用 `Content-Type: application/json`
   - 建议在生产环境中使用 HTTPS
   - 建议添加请求频率限制（防止暴力破解）

4. **用户体验**：
   - 发送验证码后，建议显示倒计时（如 60 秒内不能重复发送）
   - 验证码输入框建议限制只能输入数字
   - 密码输入框建议显示/隐藏密码功能

---

## 7. 测试数据

### 测试手机号
根据数据库中的测试数据，可以使用以下手机号进行测试：
- `13800000001` - 学生一
- `13800000002` - 指导老师一
- `13800000003` - 专业负责人一
- 等等...

### 测试流程
1. 使用存在的手机号发送验证码
2. 从控制台或 Redis 中获取验证码（开发环境）
3. 使用验证码进行校验
4. 校验成功后重置密码

---

## 8. 联系信息

如有问题，请联系后端开发团队。

**最后更新时间**: 2024年

