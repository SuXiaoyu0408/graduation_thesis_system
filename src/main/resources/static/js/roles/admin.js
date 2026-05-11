// ================== Admin Role Page Renderers ==================
// Data for these pages is loaded into global variables (userListData, adminNoticesData) in main.js

function renderUserManagementPage() {
    const container = document.getElementById('contentArea');
    container.innerHTML = `
        <div class="card p-8">
            <div class="flex justify-between items-center mb-6">
                <h3 class="text-xl font-bold text-slate-900">用户管理</h3>
                <button id="add-user-btn" class="px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm font-bold hover:bg-indigo-700">新增用户</button>
            </div>
            <table class="w-full text-left text-sm">
                <thead>
                    <tr class="text-slate-500 border-b">
                        <th class="py-3">姓名</th><th>角色</th><th>账号</th><th>手机号</th><th>操作</th>
                    </tr>
                </thead>
                <tbody>
                    ${(userListData || []).map((u, index) => {
                        // 确保用户ID存在
                        const userId = u.id || u.userId || null;
                        console.log(`[renderUserManagementPage] 渲染用户 ${index}:`, {
                            name: u.name,
                            id: u.id,
                            userId: u.userId,
                            finalUserId: userId
                        });
                        return `
                        <tr class="border-b border-slate-50 last:border-0">
                            <td class="py-3 font-medium text-slate-800">${u.name || '未知'}</td>
                            <td>${u.role || '未分配'}</td>
                            <td>${u.account || '未知'}</td>
                            <td>${u.phone || ''}</td>
                            <td class="py-2">
                                <div class="flex gap-2">
                                    <button class="edit-user-btn text-xs px-2 py-1 border border-slate-300 rounded hover:bg-slate-50 transition-colors cursor-pointer" data-user-index="${index}">编辑</button>
                                    <button class="delete-user-btn text-xs px-2 py-1 bg-rose-50 text-rose-600 rounded hover:bg-rose-100 transition-colors cursor-pointer" data-user-id="${userId}" ${!userId ? 'disabled title="用户ID缺失"' : ''}>删除</button>
                                </div>
                            </td>
                        </tr>
                    `;
                    }).join('')}
                </tbody>
            </table>
        </div>`;

    document.getElementById('add-user-btn').addEventListener('click', () => showUserModal());
    
    // 为编辑按钮添加事件监听器
    container.querySelectorAll('.edit-user-btn').forEach(btn => {
        btn.addEventListener('click', async function() {
            const index = parseInt(this.getAttribute('data-user-index'), 10);
            const user = userListData[index];
            if (user && user.id) {
                try {
                    // 获取用户的完整信息（包括所有角色）
                    const userDetail = await fetchWithAuth(`/api/admin/users/${user.id}`);
                    if (userDetail) {
                        // 将后端返回的完整用户信息传递给模态框
                        const userForModal = {
                            id: userDetail.userId,
                            name: userDetail.realName || userDetail.username,
                            account: userDetail.username,
                            phone: userDetail.phone || '',
                            email: userDetail.email || '',
                            role: userDetail.roles && userDetail.roles.length > 0 
                                ? userDetail.roles[0].roleName 
                                : '未分配',
                            roles: userDetail.roles || []
                        };
                        showUserModal(userForModal);
                    } else {
                        showUserModal(user);
                    }
                } catch (error) {
                    console.error('Failed to load user detail:', error);
                    mockAction('获取用户详情失败，使用基本信息');
                    showUserModal(user);
                }
            }
        });
    });
    
    // 为删除按钮添加事件监听器
    const deleteButtons = container.querySelectorAll('.delete-user-btn');
    console.log(`[renderUserManagementPage] 找到 ${deleteButtons.length} 个删除按钮`);
    
    deleteButtons.forEach((btn, index) => {
        const userId = btn.getAttribute('data-user-id');
        console.log(`[renderUserManagementPage] 删除按钮 ${index}: data-user-id="${userId}"`);
        
        btn.addEventListener('click', function(e) {
            console.log('[删除按钮点击事件] 按钮被点击', {
                userId: this.getAttribute('data-user-id'),
                button: this,
                event: e
            });
            
            e.preventDefault();
            e.stopPropagation();
            
            const userIdStr = this.getAttribute('data-user-id');
            console.log('[删除按钮点击事件] 原始userId字符串:', userIdStr, typeof userIdStr);
            
            const userId = parseInt(userIdStr, 10);
            console.log('[删除按钮点击事件] 解析后的userId:', userId, 'isNaN:', isNaN(userId));
            
            if (userId && !isNaN(userId)) {
                console.log('[删除按钮点击事件] 调用 handleDeleteUser，userId:', userId);
                handleDeleteUser(userId);
            } else {
                console.error('[删除按钮点击事件] 删除按钮缺少有效的用户ID:', {
                    userIdStr: userIdStr,
                    userId: userId,
                    button: this,
                    html: this.outerHTML
                });
                mockAction('删除失败：无法获取用户ID');
            }
        });
        
        // 添加鼠标悬停事件用于调试
        btn.addEventListener('mouseenter', function() {
            console.log('[删除按钮] 鼠标悬停，data-user-id:', this.getAttribute('data-user-id'));
        });
    });
}

async function handleDeleteUser(userId) {
    console.log('[handleDeleteUser] ========== 开始删除流程 ==========');
    console.log('[handleDeleteUser] 接收到的userId:', userId, '类型:', typeof userId);
    
    if (!userId || isNaN(userId)) {
        console.error('[handleDeleteUser] ❌ 无效的用户ID:', userId);
        mockAction('删除失败：无效的用户ID');
        return;
    }
    
    console.log('[handleDeleteUser] ✅ 用户ID验证通过:', userId);
    
    const confirmed = confirm('确定要删除该用户吗？此操作不可撤销。');
    console.log('[handleDeleteUser] 用户确认结果:', confirmed);
    
    if (!confirmed) {
        console.log('[handleDeleteUser] 用户取消了删除操作');
        return;
    }

    try {
        mockAction('正在删除用户...');
        console.log('[handleDeleteUser] 📤 准备发送DELETE请求，userId:', userId);
        console.log('[handleDeleteUser] 请求URL:', `/api/admin/users/${userId}`);
        
        // DELETE请求不需要body，也不应该设置Content-Type
        const deleteOptions = { 
            method: 'DELETE',
            // 明确不设置body
            body: undefined
        };
        
        console.log('[handleDeleteUser] 请求选项:', JSON.stringify(deleteOptions));
        console.log('[handleDeleteUser] 当前token:', localStorage.getItem('token') ? '存在' : '不存在');
        
        const startTime = Date.now();
        const response = await fetchWithAuth(`/api/admin/users/${userId}`, deleteOptions);
        const endTime = Date.now();
        
        console.log('[handleDeleteUser] ✅ DELETE请求完成，耗时:', endTime - startTime, 'ms');
        console.log('[handleDeleteUser] 删除响应:', response);
        console.log('[handleDeleteUser] 响应类型:', typeof response);
        
        mockAction('用户删除成功！');
        
        // 等待一小段时间确保后端事务已提交
        console.log('[handleDeleteUser] 等待500ms以确保后端事务提交...');
        await new Promise(resolve => setTimeout(resolve, 500));
        
        // 重新加载数据并刷新页面
        try {
            console.log('[handleDeleteUser] 🔄 开始重新加载管理员数据...');
            await loadAdminData(); // Reload data from server
            console.log('[handleDeleteUser] ✅ 数据重新加载完成');
            console.log('[handleDeleteUser] 当前用户列表长度:', userListData?.length || 0);
            console.log('[handleDeleteUser] 用户列表数据:', userListData);
            
            renderUserManagementPage(); // Re-render page with new data
            console.log('[handleDeleteUser] ✅ 页面重新渲染完成');
            console.log('[handleDeleteUser] ========== 删除流程完成 ==========');
        } catch (reloadError) {
            console.error('[handleDeleteUser] ❌ 重新加载数据失败:', reloadError);
            console.error('[handleDeleteUser] 重新加载错误堆栈:', reloadError.stack);
            mockAction('删除成功，但刷新数据失败，请手动刷新页面');
        }
    } catch (error) {
        console.error('[handleDeleteUser] ❌ ========== 删除失败 ==========');
        console.error('[handleDeleteUser] 错误对象:', error);
        console.error('[handleDeleteUser] 错误消息:', error.message);
        console.error('[handleDeleteUser] 错误堆栈:', error.stack);
        console.error('[handleDeleteUser] 错误名称:', error.name);
        
        const errorMessage = error.message || '未知错误';
        console.error('[handleDeleteUser] 最终错误消息:', errorMessage);
        
        mockAction(`删除失败: ${errorMessage}`);
        
        // 如果是权限错误，提示用户
        if (errorMessage.includes('权限') || errorMessage.includes('Unauthorized') || errorMessage.includes('无权限')) {
            console.error('[handleDeleteUser] 检测到权限错误');
            mockAction('删除失败：权限不足，请确认您有管理员权限');
        }
        
        // 如果是网络错误
        if (errorMessage.includes('Failed to fetch') || errorMessage.includes('NetworkError')) {
            console.error('[handleDeleteUser] 检测到网络错误');
            mockAction('删除失败：网络连接错误，请检查网络连接');
        }
    }
}

function showUserModal(user = null) {
    const isEditing = user !== null;
    const modalTitle = isEditing ? '编辑用户' : '新增用户';
    const roles = ['学生', '指导老师', '评阅老师', '专业负责人', '二级学院领导', '管理员'];

    const modal = document.createElement('div');
    modal.className = 'fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50';
    modal.innerHTML = `
        <div class="bg-white rounded-lg shadow-xl p-8 w-1/3">
            <h3 class="text-xl font-bold text-slate-900 mb-6">${modalTitle}</h3>
            <div class="space-y-4">
                <div>
                    <label class="block text-xs font-bold text-slate-500 mb-1.5">姓名</label>
                    <input id="user-name" type="text" class="w-full px-4 py-2.5 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent" value="${user?.name || ''}" placeholder="请输入姓名">
                </div>
                <div>
                    <label class="block text-xs font-bold text-slate-500 mb-1.5">账号</label>
                    <input id="user-account" type="text" class="w-full px-4 py-2.5 ${isEditing ? 'bg-slate-50' : 'bg-white'} border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent" value="${user?.account || ''}" ${isEditing ? 'readonly' : ''} placeholder="请输入账号">
                </div>
                <div>
                    <label class="block text-xs font-bold text-slate-500 mb-1.5">手机号</label>
                    <input id="user-phone" type="text" class="w-full px-4 py-2.5 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent" value="${user?.phone || ''}" placeholder="请输入手机号（可选）">
                </div>
                <div>
                    <label class="block text-xs font-bold text-slate-500 mb-1.5">角色</label>
                    <select id="user-role" class="w-full px-4 py-2.5 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent">
                        ${roles.map(r => `<option value="${r}" ${user?.role === r ? 'selected' : ''}>${r}</option>`).join('')}
                    </select>
                </div>
            </div>
            <div class="flex justify-end gap-2 mt-6">
                <button id="cancel-user-modal" class="px-4 py-2 border border-slate-300 rounded-md hover:bg-slate-50 transition-colors">取消</button>
                <button id="save-user-modal" class="px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 transition-colors">保存</button>
            </div>
        </div>
    `;
    document.body.appendChild(modal);

    const closeModal = () => {
        if (document.body.contains(modal)) {
            document.body.removeChild(modal);
        }
    };
    
    // 点击模态框背景关闭
    modal.addEventListener('click', (e) => {
        if (e.target === modal) {
            closeModal();
        }
    });
    
    document.getElementById('cancel-user-modal').addEventListener('click', closeModal);

    document.getElementById('save-user-modal').addEventListener('click', async () => {
        const userData = {
            name: document.getElementById('user-name').value.trim(),
            account: document.getElementById('user-account').value.trim(),
            phone: document.getElementById('user-phone').value.trim(),
            role: document.getElementById('user-role').value,
        };

        // 前端验证
        if (!isEditing) {
            // 新增用户时，账号和姓名是必填的
            if (!userData.account) {
                mockAction('账号不能为空');
                return;
            }
            if (!userData.name) {
                mockAction('姓名不能为空');
                return;
            }
        } else {
            // 编辑用户时，姓名是必填的
            if (!userData.name) {
                mockAction('姓名不能为空');
                return;
            }
        }

        // 手机号格式验证（如果填写了手机号）
        if (userData.phone && !/^1[3-9]\d{9}$/.test(userData.phone)) {
            mockAction('手机号格式不正确，请输入11位有效手机号');
            return;
        }

        try {
            mockAction('正在保存用户...');
            // 根据API文档，需要转换角色名称为角色ID
            const roleMap = {
                '学生': 1,
                '指导老师': 2,
                '评阅老师': 3,
                '专业负责人': 4,
                '二级学院领导': 5,
                '管理员': 6
            };
            
            const requestData = {
                realName: userData.name,
                roleIds: [roleMap[userData.role] || 1],
                status: 1
            };
            
            // 只有当手机号不为空时才添加到请求中
            if (userData.phone) {
                requestData.phone = userData.phone;
            }
            
            // 编辑时不需要传递 username 和 password
            let response;
            if (isEditing) {
                // 编辑用户时，只更新提供的字段
                console.log('Updating user:', user.id, requestData);
                response = await fetchWithAuth(`/api/admin/users/${user.id}`, {
                    method: 'PUT',
                    body: JSON.stringify(requestData)
                });
                console.log('Update response:', response);
            } else {
                // 新建用户时需要 username 和 password
                requestData.username = userData.account;
                requestData.password = '123456'; // 默认密码
                console.log('Creating user:', requestData);
                response = await fetchWithAuth('/api/admin/users', {
                    method: 'POST',
                    body: JSON.stringify(requestData)
                });
                console.log('Create response:', response);
            }
            
            mockAction('用户保存成功！');
            closeModal();
            
            // 等待一小段时间确保后端事务已提交
            await new Promise(resolve => setTimeout(resolve, 300));
            
            // 重新加载数据并刷新页面
            try {
                console.log('Reloading admin data...');
                await loadAdminData(); // Reload data from server
                console.log('Admin data reloaded, userListData:', userListData);
                renderUserManagementPage(); // Re-render page with new data
                console.log('User management page re-rendered');
            } catch (reloadError) {
                console.error('Failed to reload data:', reloadError);
                mockAction('保存成功，但刷新数据失败，请手动刷新页面');
            }
        } catch (error) {
            console.error('Failed to save user:', error);
            mockAction(`保存失败: ${error.message}`);
        }
    });
}

function renderNoticeManagementPage() {
    const container = document.getElementById('contentArea');
    container.innerHTML = `
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div class="lg:col-span-2 space-y-6">
            <div class="card p-8">
                <h3 class="text-xl font-bold text-slate-900 mb-6">已发布通知</h3>
                <div class="space-y-3">
                    ${(adminNoticesData || []).length === 0 ? `
                        <div class="text-center py-8 text-slate-400">
                            <i class="fas fa-inbox text-4xl mb-2"></i>
                            <p>暂无通知</p>
                        </div>
                    ` : (adminNoticesData || []).map((n, index) => `
                        <div class="p-4 border rounded-lg flex justify-between items-center notice-item" data-notice-id="${n.noticeId || n.id}">
                            <div class="flex-1">
                                <p class="font-semibold text-slate-800">${n.title || '无标题'}</p>
                                <p class="text-sm text-slate-600 mt-1 line-clamp-2">${n.content || ''}</p>
                                <p class="text-xs text-slate-400 mt-1">发布于: ${n.createdAt || '未知时间'}</p>
                            </div>
                            <div class="flex gap-2 ml-4">
                                <button class="edit-notice-btn text-xs px-2 py-1 border rounded hover:bg-slate-50" data-index="${index}">编辑</button>
                                <button class="delete-notice-btn text-xs px-2 py-1 bg-rose-50 text-rose-600 rounded hover:bg-rose-100" data-notice-id="${n.noticeId || n.id}">删除</button>
                            </div>
                        </div>
                    `).join('')}
                </div>
            </div>
        </div>
        <div class="space-y-6">
            <div class="card p-8">
                <h3 class="text-xl font-bold text-slate-900 mb-6" id="notice-form-title">发布新通知</h3>
                <div class="space-y-4">
                    <div>
                        <label class="block text-xs font-bold text-slate-500 mb-1.5">通知标题</label>
                        <input id="notice-title" type="text" class="w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-lg text-sm" placeholder="请输入通知标题">
                    </div>
                    <div>
                        <label class="block text-xs font-bold text-slate-500 mb-1.5">通知内容</label>
                        <textarea id="notice-content" class="w-full h-32 px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-lg text-sm" placeholder="请输入详细通知内容..."></textarea>
                    </div>
                    <button id="publish-notice-btn" class="w-full mt-2 py-3 bg-indigo-600 text-white rounded-lg font-bold text-sm hover:bg-indigo-700 transition-all shadow-md">立即发布</button>
                    <button id="cancel-edit-notice-btn" class="w-full mt-2 py-3 border border-slate-300 text-slate-700 rounded-lg font-bold text-sm hover:bg-slate-50 transition-all" style="display: none;">取消编辑</button>
                </div>
            </div>
        </div>
    </div>
    `;

    let editingNoticeId = null;

    // 发布/更新通知
    document.getElementById('publish-notice-btn').addEventListener('click', async () => {
        const title = document.getElementById('notice-title').value.trim();
        const content = document.getElementById('notice-content').value.trim();

        if (!title || !content) {
            mockAction('标题和内容均不能为空');
            return;
        }

        try {
            mockAction(editingNoticeId ? '正在更新通知...' : '正在发布通知...');
            
            if (editingNoticeId) {
                // 更新通知
                await fetchWithAuth(`/api/notice/admin/${editingNoticeId}`, {
                    method: 'PUT',
                    body: JSON.stringify({ title, content })
                });
                mockAction('通知更新成功！');
            } else {
                // 创建通知
                await fetchWithAuth('/api/notice/admin', {
                    method: 'POST',
                    body: JSON.stringify({ title, content })
                });
                mockAction('通知发布成功！');
            }
            
            // 重置表单
            document.getElementById('notice-title').value = '';
            document.getElementById('notice-content').value = '';
            editingNoticeId = null;
            document.getElementById('notice-form-title').textContent = '发布新通知';
            document.getElementById('cancel-edit-notice-btn').style.display = 'none';
            
            await loadAdminData(); // Reload data
            renderNoticeManagementPage(); // Re-render page
        } catch (error) {
            console.error('Failed to save notice:', error);
            mockAction(`操作失败: ${error.message}`);
        }
    });

    // 取消编辑
    document.getElementById('cancel-edit-notice-btn').addEventListener('click', () => {
        document.getElementById('notice-title').value = '';
        document.getElementById('notice-content').value = '';
        editingNoticeId = null;
        document.getElementById('notice-form-title').textContent = '发布新通知';
        document.getElementById('cancel-edit-notice-btn').style.display = 'none';
    });

    // 编辑通知
    container.querySelectorAll('.edit-notice-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            const index = parseInt(this.getAttribute('data-index'), 10);
            const notice = adminNoticesData[index];
            if (notice) {
                editingNoticeId = notice.noticeId || notice.id;
                document.getElementById('notice-title').value = notice.title || '';
                document.getElementById('notice-content').value = notice.content || '';
                document.getElementById('notice-form-title').textContent = '编辑通知';
                document.getElementById('cancel-edit-notice-btn').style.display = 'block';
                document.getElementById('publish-notice-btn').textContent = '更新通知';
            }
        });
    });

    // 删除通知
    container.querySelectorAll('.delete-notice-btn').forEach(btn => {
        btn.addEventListener('click', async function() {
            const noticeId = this.getAttribute('data-notice-id');
            if (!confirm('确定要删除这条通知吗？此操作不可撤销。')) {
                return;
            }

            try {
                mockAction('正在删除通知...');
                await fetchWithAuth(`/api/notice/admin/${noticeId}`, {
                    method: 'DELETE'
                });
                mockAction('通知删除成功！');
                await loadAdminData(); // Reload data
                renderNoticeManagementPage(); // Re-render page
            } catch (error) {
                console.error('Failed to delete notice:', error);
                mockAction(`删除失败: ${error.message}`);
            }
        });
    });
}

function renderPermissionConfigPage() {
    const roles = ['学生', '指导老师', '评阅老师', '专业负责人', '二级学院领导', '管理员'];
    const modules = ['选题申报', '开题报告', '中期检查', '论文终稿', '答辩'];
    const container = document.getElementById('contentArea');
    container.innerHTML = `
        <div class="card p-8">
            <h3 class="text-xl font-bold text-slate-900 mb-6">权限配置</h3>
            <p class="text-sm text-slate-500 mb-4">勾选模块，即可授予该角色访问/操作权限（仅示例 UI，无保存逻辑）。</p>
            <table class="w-full text-center text-sm">
                <thead>
                    <tr class="border-b text-slate-500">
                        <th class="py-3 w-32 text-left pl-4">角色</th>
                        ${modules.map(m=>`<th>${m}</th>`).join('')}
                    </tr>
                </thead>
                <tbody>
                    ${roles.map(r=>`<tr class="border-b last:border-0"><td class="py-3 text-left pl-4 font-medium text-slate-800">${r}</td>${modules.map(()=>`<td><input type='checkbox' class='accent-indigo-600'></td>`).join('')}</tr>`).join('')}
                </tbody>
            </table>
            <div class="text-right mt-6"><button class="px-6 py-2 bg-indigo-600 text-white rounded-lg font-bold hover:bg-indigo-700" onclick="mockAction('权限配置已保存！')">保存配置</button></div>
        </div>`;
}

async function renderDataMaintenancePage() {
    // 从API获取数据维护信息
    let datasets = [];
    try {
        // 获取归档统计信息
        const archiveStats = await fetchWithAuth('/api/admin/archive/statistics') || {};
        
        // 获取用户列表以获取用户总数
        const userListResponse = await fetchWithAuth('/api/admin/users?page=1&size=1') || {};
        const totalUsers = userListResponse.total || 0;
        
        // 构建数据集信息
        datasets = [
            { name: '用户表', rows: totalUsers, last: '实时', table: 'users' },
            { name: '论文流程表', rows: archiveStats.totalCompleted || 0, last: '实时', table: 'thesis_process' },
            { name: '已归档论文', rows: archiveStats.totalCompleted || 0, last: '从未备份', table: 'archive' }
        ];
    } catch (error) {
        console.error('Failed to load data maintenance info:', error);
        datasets = [
            { name: '用户表', rows: 0, last: '未知', table: 'users' },
            { name: '论文流程表', rows: 0, last: '未知', table: 'thesis_process' },
            { name: '已归档论文', rows: 0, last: '从未备份', table: 'archive' }
        ];
    }
    
    const container = document.getElementById('contentArea');
    container.innerHTML = `
    <div class="card p-8">
        <h3 class="text-xl font-bold text-slate-900 mb-6">数据维护</h3>
        <div class="mb-6">
            <p class="text-sm text-slate-600 mb-4">数据维护功能用于管理系统的数据，包括数据导出、清理和备份操作。</p>
        </div>
        <table class="w-full text-left text-sm">
            <thead>
                <tr class="border-b text-slate-500">
                    <th class="py-3">数据集</th>
                    <th>记录数</th>
                    <th>最后备份</th>
                    <th>操作</th>
                </tr>
            </thead>
            <tbody>
                ${datasets.map(d => `
                    <tr class="border-b last:border-0">
                        <td class="py-3 font-medium">${d.name}</td>
                        <td>${d.rows.toLocaleString()}</td>
                        <td>${d.last}</td>
                        <td class="flex gap-2 py-2">
                            <button class="export-data-btn px-2 py-1 text-xs border rounded hover:bg-slate-50" data-table="${d.table}">导出</button>
                            ${d.table !== 'archive' ? `<button class="clean-data-btn px-2 py-1 text-xs border rounded hover:bg-slate-50" data-table="${d.table}">清理</button>` : ''}
                        </td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
        <div class="flex justify-between items-center mt-6">
            <div class="text-sm text-slate-500">
                <p>提示：导出功能将生成数据文件，清理功能将删除过期数据，请谨慎操作。</p>
            </div>
            <button id="backup-all-btn" class="px-6 py-2 bg-indigo-600 text-white rounded-lg font-bold hover:bg-indigo-700">
                一键备份
            </button>
        </div>
    </div>`;
    
    // 导出数据
    container.querySelectorAll('.export-data-btn').forEach(btn => {
        btn.addEventListener('click', async function() {
            const table = this.getAttribute('data-table');
            mockAction(`导出 ${table} 数据功能开发中...`);
            // TODO: 实现数据导出功能
        });
    });
    
    // 清理数据
    container.querySelectorAll('.clean-data-btn').forEach(btn => {
        btn.addEventListener('click', async function() {
            const table = this.getAttribute('data-table');
            if (!confirm(`确定要清理 ${table} 表中的过期数据吗？此操作不可撤销。`)) {
                return;
            }
            mockAction(`清理 ${table} 数据功能开发中...`);
            // TODO: 实现数据清理功能
        });
    });
    
    // 一键备份
    document.getElementById('backup-all-btn').addEventListener('click', async () => {
        if (!confirm('确定要执行全库备份吗？这将导出所有已归档的论文材料。')) {
            return;
        }
        try {
            mockAction('正在备份数据...');
            // 调用备份API
            const response = await fetch(`${API_BASE_URL}/api/admin/archive/export`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${getCleanToken()}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify([]) // 空数组表示导出所有
            });
            
            if (response.ok) {
                const blob = await response.blob();
                const url = window.URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = `archive_backup_${new Date().toISOString().split('T')[0]}.zip`;
                document.body.appendChild(a);
                a.click();
                document.body.removeChild(a);
                window.URL.revokeObjectURL(url);
                mockAction('备份完成！文件已下载');
            } else {
                throw new Error('备份失败');
            }
        } catch (error) {
            console.error('Failed to backup:', error);
            mockAction(`备份失败: ${error.message}`);
        }
    });
}

async function renderSystemMonitoringPage() {
    // 从API获取系统监控数据
    let metrics = [];
    try {
        // 获取归档统计信息作为系统指标
        const archiveStats = await fetchWithAuth('/api/admin/archive/statistics') || {};
        
        // 获取用户列表以获取用户总数
        const userListResponse = await fetchWithAuth('/api/admin/users?page=1&size=1') || {};
        const totalUsers = userListResponse.total || 0;
        
        // 计算学院和专业数量
        const collegeCount = archiveStats.collegeStatistics ? archiveStats.collegeStatistics.length : 0;
        const majorCount = archiveStats.majorStatistics ? archiveStats.majorStatistics.length : 0;
        
        // 构建系统指标
        metrics = [
            { name: '总用户数', value: totalUsers, unit: '', color: 'indigo' },
            { name: '已完成论文', value: archiveStats.totalCompleted || 0, unit: '', color: 'emerald' },
            { name: '学院数量', value: collegeCount, unit: '', color: 'amber' },
            { name: '专业数量', value: majorCount, unit: '', color: 'blue' }
        ];
    } catch (error) {
        console.error('Failed to load system monitoring data:', error);
        metrics = [
            { name: '总用户数', value: 0, unit: '', color: 'indigo' },
            { name: '已完成论文', value: 0, unit: '', color: 'emerald' },
            { name: '学院数量', value: 0, unit: '', color: 'amber' },
            { name: '专业数量', value: 0, unit: '', color: 'blue' }
        ];
    }
    
    const colorClasses = {
        indigo: 'bg-indigo-50 text-indigo-600',
        emerald: 'bg-emerald-50 text-emerald-600',
        amber: 'bg-amber-50 text-amber-600',
        blue: 'bg-blue-50 text-blue-600',
        purple: 'bg-purple-50 text-purple-600'
    };
    
    const container = document.getElementById('contentArea');
    container.innerHTML = `
    <div class="card p-8">
        <div class="flex justify-between items-center mb-6">
            <h3 class="text-xl font-bold text-slate-900">系统监控</h3>
            <button id="refresh-monitoring-btn" class="px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm font-bold hover:bg-indigo-700">
                <i class="fas fa-sync-alt mr-2"></i>刷新数据
            </button>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
            ${metrics.map(m => `
                <div class="p-6 ${colorClasses[m.color] || 'bg-slate-50 text-slate-600'} rounded-xl text-center">
                    <p class="text-3xl font-bold mb-1">${m.value.toLocaleString()}${m.unit}</p>
                    <p class="text-sm font-medium">${m.name}</p>
                </div>
            `).join('')}
        </div>
        <div class="border-t pt-6">
            <h4 class="text-lg font-bold text-slate-900 mb-4">系统信息</h4>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
                <div class="p-4 bg-slate-50 rounded-lg">
                    <p class="text-slate-500 mb-1">系统状态</p>
                    <p class="font-semibold text-emerald-600">运行正常</p>
                </div>
                <div class="p-4 bg-slate-50 rounded-lg">
                    <p class="text-slate-500 mb-1">最后备份时间</p>
                    <p class="font-semibold text-slate-800">${metrics.find(m => m.name === '已完成论文')?.value > 0 ? '有已完成论文可备份' : '暂无已完成论文'}</p>
                </div>
            </div>
        </div>
    </div>`;
    
    // 刷新按钮
    document.getElementById('refresh-monitoring-btn').addEventListener('click', async () => {
        mockAction('正在刷新数据...');
        await renderSystemMonitoringPage();
        mockAction('数据已刷新');
    });
}
