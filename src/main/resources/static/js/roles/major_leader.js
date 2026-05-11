// ================== Major Leader Role Page Renderers ==================

async function renderTopicApprovalPage() {
    // 从API获取待审核的课题列表
    let topics = [];
    try {
        // 使用正确的API端点 /api/major-leader/thesis/topics
        topics = await fetchWithAuth('/api/major-leader/thesis/topics') || [];
    } catch (error) {
        console.error('Failed to load topics for approval:', error);
        topics = [];
    }

    const container = document.getElementById('contentArea');
    container.innerHTML = `
        <div class="card p-8">
            <h3 class="text-xl font-bold text-slate-900 mb-6">指导老师课题申报审核</h3>
            <table class="w-full text-left text-sm">
                <thead>
                    <tr class="text-slate-500 border-b">
                        <th class="py-3">课题名称</th>
                        <th class="py-3">申报老师</th>
                        <th class="py-3">提交时间</th>
                        <th class="py-3">状态</th>
                        <th class="py-3">操作</th>
                    </tr>
                </thead>
                <tbody>
                    ${topics.length === 0 ? '<tr><td colspan="5" class="py-8 text-center text-slate-400">暂无待审核课题</td></tr>' : topics.map(topic => {
                        const statusChip = topic.status === 'approved' || topic.statusName === '已通过' ? '<span class="text-xs font-bold text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded">已通过</span>' :
                            topic.status === 'rejected' || topic.statusName === '已驳回' ? '<span class="text-xs font-bold text-rose-600 bg-rose-50 px-2 py-0.5 rounded">已驳回</span>' :
                            '<span class="text-xs font-bold text-amber-600 bg-amber-50 px-2 py-0.5 rounded">待审核</span>';
                        const submittedAt = topic.submittedAt ? new Date(topic.submittedAt).toLocaleString('zh-CN') : '';
                        const title = topic.thesisTitle || topic.title || '未确定课题';
                        const supervisorName = topic.supervisorName || '未知老师';
                        const processId = topic.processId || topic.id;
                        return `
                        <tr class="border-b border-slate-50 last:border-0">
                            <td class="py-3 font-medium max-w-[300px] truncate" title="${title.replace(/"/g, '&quot;')}">${title}</td>
                            <td>${supervisorName}</td>
                            <td>${submittedAt}</td>
                            <td>${statusChip}</td>
                            <td class="flex gap-2 py-2">
                                ${(topic.status === 'pending' || !topic.status || topic.status === 'none') && processId ? `
                                <button class="px-2 py-1 text-xs bg-emerald-50 text-emerald-600 rounded hover:bg-emerald-100" onclick="handleTopicApproval(${processId}, true)">通过</button>
                                <button class="px-2 py-1 text-xs bg-rose-50 text-rose-600 rounded hover:bg-rose-100" onclick="handleTopicApproval(${processId}, false)">驳回</button>
                                ` : ''}
                            </td>
                        </tr>
                    `;
                    }).join('')}
                </tbody>
            </table>
        </div>
    `;
}


async function renderTopicSelectionReviewPage() {
    // 从API获取待审核的选题列表
    let submissions = [];
    try {
        submissions = await fetchWithAuth('/api/major-leader/thesis/topics');
    } catch (error) {
        console.error('Failed to load topic submissions:', error);
        submissions = [];
    }
    const container = document.getElementById('contentArea');
    container.innerHTML = `
        <div class="card p-8">
            <h3 class="text-xl font-bold text-slate-900 mb-6">学生选题审核</h3>
            <table class="w-full text-left text-sm">
                <thead>
                    <tr class="text-slate-500 border-b">
                        <th class="py-3">学生姓名</th><th>学号</th><th>课题名称</th><th>指导老师</th><th>提交时间</th><th>状态</th><th>操作</th>
                    </tr>
                </thead>
                <tbody>
                    ${submissions.length === 0 ? '<tr><td colspan="7" class="py-8 text-center text-slate-400">暂无待审核选题</td></tr>' : submissions.map(s => {
                        const statusChip = s.status === 'approved' ? '<span class="text-xs font-bold text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded">已通过</span>' :
                            s.status === 'rejected' ? '<span class="text-xs font-bold text-rose-600 bg-rose-50 px-2 py-0.5 rounded">已驳回</span>' :
                            '<span class="text-xs font-bold text-amber-600 bg-amber-50 px-2 py-0.5 rounded">待审核</span>';
                        const submittedAt = s.submittedAt ? new Date(s.submittedAt).toLocaleString('zh-CN') : '';
                        return `
                        <tr class="border-b border-slate-50 last:border-0">
                            <td class="py-3 font-medium">${s.studentName || ''}</td>
                            <td>${s.studentNo || ''}</td>
                            <td class="max-w-[300px] truncate" title="${s.thesisTitle || ''}">${s.thesisTitle || ''}</td>
                            <td>${s.supervisorName || ''}</td>
                            <td>${submittedAt}</td>
                            <td>${statusChip}</td>
                            <td class="flex gap-2 py-2">
                                <button class="px-2 py-1 text-xs border rounded hover:bg-slate-50" onclick="previewMajorLeaderMaterial(${s.processId}, 'TOPIC_SELECTION')">预览</button>
                                ${s.status === 'pending' ? `
                                <button class="px-2 py-1 text-xs bg-emerald-50 text-emerald-600 rounded hover:bg-emerald-100" onclick="approveMajorLeaderTopic(${s.processId})">通过</button>
                                <button class="px-2 py-1 text-xs bg-rose-50 text-rose-600 rounded hover:bg-rose-100" onclick="handleMajorLeaderReject(${s.processId}, 'TOPIC_SELECTION', '${s.studentName || ''}')">驳回</button>
                                ` : ''}
                            </td>
                        </tr>
                    `;
                    }).join('')}
                </tbody>
            </table>
        </div>
    `;
}

async function renderMidtermCheckManagementPage() {
    // 从API获取中期检查报告列表
    let reports = [];
    try {
        reports = await fetchWithAuth('/api/major-leader/thesis/midterm-reports') || [];
    } catch (error) {
        console.error('Failed to load midterm reports:', error);
        reports = [];
    }
    const getStatusChip = s=> s==='approved'?'<span class="text-xs font-bold text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded">已通过</span>':s==='pending'?'<span class="text-xs font-bold text-amber-600 bg-amber-50 px-2 py-0.5 rounded">待审核</span>':'<span class="text-xs font-bold text-rose-600 bg-rose-50 px-2 py-0.5 rounded">被驳回</span>';
    const container=document.getElementById('contentArea');
    container.innerHTML=`
        <div class="card p-8">
            <h3 class="text-xl font-bold text-slate-900 mb-6">中期检查管理</h3>
            <table class="w-full text-left text-sm">
                <thead><tr class="text-slate-500 border-b"><th class="py-3">学生</th><th>学号</th><th>课题名称</th><th>指导老师</th><th>提交时间</th><th>状态</th><th>操作</th></tr></thead>
                <tbody>
                    ${reports.length === 0 ? '<tr><td colspan="7" class="py-8 text-center text-slate-400">暂无中期检查报告</td></tr>' : reports.map(r=>{
                        const submittedAt = r.submittedAt ? new Date(r.submittedAt).toLocaleString('zh-CN') : '';
                        return `<tr class="border-b border-slate-50 last:border-0">
                        <td class="py-3 font-medium">${r.studentName || ''}</td>
                        <td>${r.studentNo || ''}</td>
                        <td class="max-w-[250px] truncate" title="${r.thesisTitle || ''}">${r.thesisTitle || ''}</td>
                        <td>${r.supervisorName || ''}</td>
                        <td>${submittedAt}</td>
                        <td>${getStatusChip(r.status)}</td>
                        <td class="flex gap-2 py-2">
                            <button class="px-2 py-1 text-xs border rounded hover:bg-slate-50" onclick="previewMajorLeaderMaterial(${r.processId}, 'MID_TERM_REPORT')">预览</button>
                            ${r.status==='pending'?`<button class="px-2 py-1 text-xs bg-emerald-50 text-emerald-600 rounded hover:bg-emerald-100" onclick="approveMajorLeaderMidterm(${r.processId})">通过</button>
                            <button class="px-2 py-1 text-xs bg-rose-50 text-rose-600 rounded hover:bg-rose-100" onclick="handleMajorLeaderReject(${r.processId}, 'MID_TERM_REPORT', '${r.studentName || ''}')">驳回</button>`:''}
                        </td>
                    </tr>`;
                    }).join('')}
                </tbody>
            </table>
        </div>`;
}

// 处理专业负责人驳回操作
async function handleMajorLeaderReject(processId, materialType, studentName) {
    const reason = await showRejectModal(studentName, materialType);
    if (reason === null) {
        mockAction('操作已取消');
        return;
    }

    try {
        mockAction('正在提交驳回...');
        await fetchWithAuth(`/api/major-leader/thesis/approve`, {
            method: 'POST',
            body: JSON.stringify({
                processId: processId,
                materialType: materialType,
                pass: false,
                reason: reason
            })
        });
        mockAction('驳回成功');
        // 重新加载页面
        if (materialType === 'MID_TERM_REPORT') {
            renderMidtermCheckManagementPage();
        } else if (materialType === 'TOPIC_SELECTION') {
            renderTopicSelectionReviewPage();
        }
    } catch (error) {
        console.error('Reject error:', error);
        mockAction(`驳回失败: ${error.message}`);
    }
}

async function renderGradeManagementPage() {
    // 从API获取成绩列表
    let grades = [];
    try {
        grades = await fetchWithAuth('/api/major-leader/thesis/grades');
    } catch (error) {
        console.error('Failed to load grades:', error);
        grades = [];
    }
    const container=document.getElementById('contentArea');
    container.innerHTML=`
    <div class="card p-8">
        <h3 class="text-xl font-bold text-slate-900 mb-6">成绩管理</h3>
        <table class="w-full text-left text-sm">
            <thead>
                <tr class="text-slate-500 border-b"><th class="py-3">学生</th><th>学号</th><th>课题名称</th><th>指导老师评分</th><th>评阅老师评分</th><th>答辩小组评分</th><th>最终成绩</th><th>等级</th></tr>
            </thead>
            <tbody>
                ${grades.length === 0 ? '<tr><td colspan="8" class="py-8 text-center text-slate-400">暂无成绩数据</td></tr>' : grades.map(g=>{
                    const supervisorScore = g.supervisorScore || 0;
                    const reviewerScore = g.reviewerScore || 0;
                    const defenseScore = g.defenseScore || 0;
                    const finalScore = (supervisorScore * 0.4 + reviewerScore * 0.2 + defenseScore * 0.4).toFixed(1);
                    let level = g.gradeLevel || '不合格';
                    if(finalScore>=90) level='优秀'; else if(finalScore>=80) level='良好'; else if(finalScore>=70) level='中等'; else if(finalScore>=60) level='合格';
                    return `<tr class="border-b border-slate-50 last:border-0">
                        <td class="py-3 font-medium">${g.studentName || ''}</td>
                        <td>${g.studentNo || ''}</td>
                        <td class="max-w-[200px] truncate" title="${g.thesisTitle || ''}">${g.thesisTitle || ''}</td>
                        <td>${supervisorScore}</td>
                        <td>${reviewerScore}</td>
                        <td>${defenseScore}</td>
                        <td class="font-bold">${finalScore}</td>
                        <td>${level}</td>
                    </tr>`;
                }).join('')}
            </tbody>
        </table>
    </div>`;
}

// 辅助函数：预览专业负责人材料
async function previewMajorLeaderMaterial(processId, materialType) {
    try {
        const url = `${API_BASE_URL}/api/major-leader/thesis/material/${processId}/${materialType}/preview`;
        const cleanToken = getCleanToken();
        const response = await fetch(url, {
            headers: {
                'Authorization': `Bearer ${cleanToken}`
            }
        });
        if (response.ok) {
            const blob = await response.blob();
            const url = window.URL.createObjectURL(blob);
            window.open(url, '_blank');
        } else {
            mockAction('预览失败');
        }
    } catch (error) {
        console.error('Preview error:', error);
        mockAction('预览失败');
    }
}

// 辅助函数：通过选题
async function approveMajorLeaderTopic(processId) {
    try {
        await fetchWithAuth(`/api/major-leader/thesis/approve`, {
            method: 'POST',
            body: JSON.stringify({
                processId: processId,
                materialType: 'TOPIC_SELECTION',
                pass: true
            })
        });
        mockAction('选题审核通过');
        renderTopicSelectionReviewPage();
    } catch (error) {
        console.error('Approve error:', error);
        mockAction('审核失败');
    }
}

// 辅助函数：处理课题审核
async function handleTopicApproval(processId, isApproved) {
    // 确保processId是数字类型
    const processIdNum = parseInt(processId, 10);
    if (!processIdNum || isNaN(processIdNum)) {
        mockAction('无效的流程ID');
        console.error('Invalid processId:', processId);
        return;
    }
    
    let reason = '';
    if (!isApproved) {
        reason = await showRejectModal('该课题', '课题申报');
        if (reason === null) {
            mockAction('操作已取消');
            return;
        }
    }

    try {
        mockAction('正在提交审核...');
        // 使用正确的API端点，传递processId和materialType
        await fetchWithAuth('/api/major-leader/thesis/approve', {
            method: 'POST',
            body: JSON.stringify({
                processId: processIdNum,
                materialType: 'topic_selection', // 课题审核对应选题申报表
                pass: isApproved,
                reason: reason || null
            })
        });
        mockAction('审核操作成功');
        await renderTopicApprovalPage(); // Refresh the page
    } catch (error) {
        console.error('Topic approval error:', error);
        mockAction(`审核失败: ${error.message || '未知错误'}`);
    }
}

// 辅助函数：通过中期检查
async function approveMajorLeaderMidterm(processId) {
    try {
        await fetchWithAuth(`/api/major-leader/thesis/approve`, {
            method: 'POST',
            body: JSON.stringify({
                processId: processId,
                materialType: 'MID_TERM_REPORT',
                pass: true
            })
        });
        mockAction('中期检查通过');
        renderMidtermCheckManagementPage();
    } catch (error) {
        console.error('Approve error:', error);
        mockAction('审核失败');
    }
}