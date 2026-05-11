// ================== Defense Team Role Page Renderers ==================

async function renderDefenseGradingPage(data = {}) {
    const studentToGrade = data.student;

    if (studentToGrade) {
        const scoreItems = [
            { key: 'reportContent', label: '报告内容', max: 40 },
            { key: 'reportProcess', label: '报告过程', max: 10 },
            { key: 'defensePerformance', label: '答辩', max: 50 },
        ];
        const container = document.getElementById('contentArea');
        container.innerHTML = `
        <div class="card p-8">
            <div class="flex justify-between items-center mb-6">
                <div>
                    <h3 class="text-xl font-bold text-slate-900">答辩评分 - ${studentToGrade.name}</h3>
                    <p class="text-sm text-slate-500 mt-1">课题: ${studentToGrade.topic}</p>
                </div>
                <button onclick="switchPage('答辩评分')" class="text-sm text-indigo-600 font-semibold hover:underline"><i class="fas fa-arrow-left mr-1"></i>返回列表</button>
            </div>
            <div class="space-y-5 pt-6 border-t">
                ${scoreItems.map(item => `
                    <div class="flex items-center gap-4">
                        <label class="w-1/3 text-sm font-medium text-slate-700">${item.label}</label>
                        <input type="number" data-key="${item.key}" max="${item.max}" min="0" class="score-input w-24 border-slate-200 rounded-md text-center font-bold" placeholder="0" oninput="updateTotalScore()" />
                        <span class="text-sm text-slate-400">/ ${item.max}分</span>
                    </div>
                `).join('')}
                <div class="flex items-center gap-4 pt-5 border-t">
                     <label class="w-1/3 text-sm font-bold text-slate-800">总分</label>
                     <p id="totalScore" class="font-bold text-2xl text-indigo-600">0</p>
                     <span class="text-sm text-slate-400">/ 100分</span>
                </div>
            </div>
            <div class="text-right mt-8">
                <button id="submit-defense-score-btn" class="px-8 py-3 bg-indigo-600 text-white rounded-lg font-bold shadow-lg shadow-indigo-100">提交评分</button>
            </div>
        </div>
        `;
        
        document.getElementById('submit-defense-score-btn').addEventListener('click', async () => {
            const scores = {};
            const inputs = document.querySelectorAll('.score-input');
            let totalScore = 0;
            inputs.forEach(input => {
                const score = parseInt(input.value, 10) || 0;
                scores[input.dataset.key] = score;
                totalScore += score;
            });

            if (totalScore <= 0) {
                mockAction('请填写评分');
                return;
            }

            try {
                // 获取processId
                const processId = studentToGrade.processId || studentToGrade.id;
                if (!processId) {
                    mockAction('缺少必要参数');
                    return;
                }
                
                mockAction('正在提交答辩评分...');
                // 根据API文档，使用正确的接口和参数名
                await fetchWithAuth('/api/defense-team/thesis/score', {
                    method: 'POST',
                    body: JSON.stringify({
                        processId: processId,
                        reportContent: scores.reportContent || 0,
                        reportProcess: scores.reportProcess || 0,
                        defensePerformance: scores.defensePerformance || 0
                    })
                });
                mockAction('答辩评分提交成功！');
                await loadUserData();
                switchPage('答辩评分');
            } catch (error) {
                console.error('Defense score submission failed:', error);
                mockAction(`评分提交失败: ${error.message}`);
            }
        });
        return;
    }

    // 从API获取待答辩的学生列表
    let studentsForDefense = [];
    try {
        // 使用正确的API端点获取待答辩学生列表
        studentsForDefense = await fetchWithAuth('/api/defense-team/thesis/students').catch(e => []);
    } catch (error) {
        console.error('Failed to load students for defense:', error);
        studentsForDefense = [];
    }
    const container = document.getElementById('contentArea');
    container.innerHTML = `
        <div class="card p-8">
            <h3 class="text-xl font-bold text-slate-900 mb-6">待评分答辩列表</h3>
            <table class="w-full text-left">
                <thead>
                    <tr class="border-b border-slate-200 text-sm text-slate-500">
                        <th class="py-3">学生姓名</th>
                        <th>指导老师</th>
                        <th>毕业课题</th>
                        <th>操作</th>
                    </tr>
                </thead>
                <tbody>
                    ${studentsForDefense.length === 0 ? '<tr><td colspan="4" class="py-8 text-center text-slate-400">暂无待答辩学生</td></tr>' : studentsForDefense.map(s => {
                        const studentName = s.studentName || s.name || '未知学生';
                        const supervisorName = s.supervisorName || s.supervisor || '未知老师';
                        const thesisTitle = s.thesisTitle || s.topic || '未确定课题';
                        const processId = s.processId || s.id;
                        return `
                        <tr class="border-b border-slate-100 text-sm">
                            <td class="py-4 font-medium text-slate-800">${studentName}</td>
                            <td>${supervisorName}</td>
                            <td class="max-w-[300px] truncate" title="${thesisTitle.replace(/"/g, '&quot;')}">${thesisTitle}</td>
                            <td>
                                ${processId ? `
                                    <button onclick='switchPage("答辩评分", { student: { name: "${studentName.replace(/"/g, '&quot;').replace(/'/g, "\\'")}", topic: "${thesisTitle.replace(/"/g, '&quot;').replace(/'/g, "\\'")}", processId: ${processId} } })' class="text-indigo-600 hover:underline font-semibold">进入评分</button>
                                ` : '<span class="text-xs text-slate-400">暂无流程ID</span>'}
                            </td>
                        </tr>
                    `;
                    }).join('')}
                </tbody>
            </table>
        </div>
    `;
}

async function renderDefenseOrganizationPage(){
    // 从API获取答辩小组列表
    let teams = [];
    try {
        teams = await fetchWithAuth('/api/defense-team/thesis/teams') || [];
        console.log('答辩小组列表:', teams);
    } catch (error) {
        console.error('Failed to load defense teams:', error);
        teams = [];
    }
    const container = document.getElementById('contentArea');
    container.innerHTML = `
        <div class="card p-8">
            <h3 class="text-xl font-bold text-slate-900 mb-6">答辩组织</h3>
            ${teams.length > 0 ? `
                <table class="w-full text-left text-sm">
                    <thead>
                        <tr class="text-slate-500 border-b">
                            <th class="py-3">小组编号</th>
                            <th>组长</th>
                            <th>成员</th>
                            <th>学生数量</th>
                            <th>答辩教室</th>
                            <th>答辩时间</th>
                            <th>操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${teams.map(t => `
                            <tr class="border-b last:border-0">
                                <td class="py-3 font-medium">第 ${t.teamNumber || t.teamId || 'N/A'} 组</td>
                                <td>${t.chairmanName || '未指定'}</td>
                                <td>${(t.memberNames || []).length > 0 ? t.memberNames.join('、') : '暂无成员'}</td>
                                <td>${t.studentCount || 0}</td>
                                <td>${t.classroom || '待安排'}</td>
                                <td>${t.defenseTime || '待安排'}</td>
                                <td>
                                    <button class='text-xs px-2 py-1 border rounded hover:bg-slate-50' 
                                            onclick='mockAction("调整第 ${t.teamNumber || t.teamId} 组功能开发中...")'>
                                        调整
                                    </button>
                                </td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>
            ` : `
                <div class="text-center py-12">
                    <p class="text-slate-500">暂无答辩小组信息</p>
                </div>
            `}
        </div>
    `;
}

async function renderGradeSummaryPage(){
    // 从API获取成绩汇总列表
    let list = [];
    try {
        list = await fetchWithAuth('/api/defense-team/thesis/grades') || [];
        console.log('成绩汇总列表:', list);
    } catch (error) {
        console.error('Failed to load grade summary:', error);
        list = [];
    }
    const container = document.getElementById('contentArea');
    container.innerHTML = `
        <div class="card p-8">
            <h3 class="text-xl font-bold text-slate-900 mb-6">成绩汇总</h3>
            ${list.length > 0 ? `
                <table class="w-full text-left text-sm">
                    <thead>
                        <tr class="border-b text-slate-500">
                            <th class="py-3">学生姓名</th>
                            <th>学号</th>
                            <th>课题名称</th>
                            <th>指导老师评分</th>
                            <th>评阅老师评分</th>
                            <th>答辩小组评分</th>
                            <th>最终成绩</th>
                            <th>等级</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${list.map(g => {
                            const supervisorScore = g.supervisorScore ? g.supervisorScore.toFixed(1) : '-';
                            const reviewerScore = g.reviewerScore ? g.reviewerScore.toFixed(1) : '-';
                            const defenseScore = g.defenseScore ? g.defenseScore.toFixed(1) : '-';
                            const finalScore = g.finalScore ? g.finalScore.toFixed(1) : '-';
                            const gradeLevel = g.gradeLevel || '未完成';
                            
                            return `
                                <tr class='border-b last:border-0'>
                                    <td class='py-3 font-medium'>${g.studentName || '未知'}</td>
                                    <td>${g.studentNo || '-'}</td>
                                    <td class="max-w-[200px] truncate" title="${(g.thesisTitle || '').replace(/"/g, '&quot;')}">${g.thesisTitle || '-'}</td>
                                    <td>${supervisorScore}</td>
                                    <td>${reviewerScore}</td>
                                    <td>${defenseScore}</td>
                                    <td class='font-bold text-indigo-600'>${finalScore}</td>
                                    <td>
                                        <span class="px-2 py-1 rounded text-xs font-semibold ${
                                            gradeLevel === '优秀' ? 'bg-emerald-50 text-emerald-600' :
                                            gradeLevel === '良好' ? 'bg-blue-50 text-blue-600' :
                                            gradeLevel === '中等' ? 'bg-amber-50 text-amber-600' :
                                            gradeLevel === '合格' ? 'bg-slate-50 text-slate-600' :
                                            'bg-rose-50 text-rose-600'
                                        }">
                                            ${gradeLevel}
                                        </span>
                                    </td>
                                </tr>
                            `;
                        }).join('')}
                    </tbody>
                </table>
            ` : `
                <div class="text-center py-12">
                    <p class="text-slate-500">暂无成绩汇总数据</p>
                    <p class="text-xs text-slate-400 mt-2">请等待学生完成答辩并评分</p>
                </div>
            `}
        </div>
    `;
}

// 答辩安排页面（所有角色通用）
async function renderDefenseArrangementPage() {
    const container = document.getElementById('contentArea');
    const roleInfo = ROLES[currentUser.roleId] || ROLES.STUDENT;
    
    let defenseArrangements = [];
    try {
        // 根据角色获取不同的答辩安排数据
        if (currentUser.roleId === 'STUDENT') {
            // 学生查看自己的答辩安排
            const processInfo = await fetchWithAuth('/api/student/thesis/process').catch(e => null);
            if (processInfo && processInfo.processId) {
                // 获取答辩安排信息
                const arrangement = await fetchWithAuth(`/api/student/thesis/defense/${processInfo.processId}/arrangement`).catch(e => null);
                if (arrangement) {
                    defenseArrangements = [arrangement];
                }
            }
        } else if (currentUser.roleId === 'DEFENSE_MEMBER' || currentUser.roleId === 'DEFENSE_CHAIRMAN' || currentUser.roleId === 'DEFENSE_LEADER') {
            // 答辩小组成员/组长查看小组的答辩安排
            // 使用 /api/defense-team/thesis/students 获取待答辩学生列表
            const students = await fetchWithAuth('/api/defense-team/thesis/students').catch(e => []);
            if (students && students.length > 0) {
                // 将学生列表转换为答辩安排格式（简化处理，实际应该从后端获取完整的答辩安排信息）
                // 这里暂时显示学生列表，表示这些学生需要答辩
                defenseArrangements = [{
                    teamId: 1, // 暂时使用默认值
                    teamNumber: 1,
                    classroom: '待安排',
                    defenseTime: '待安排',
                    chairmanName: '待指定',
                    students: students.map(s => ({
                        studentName: s.studentName || s.name,
                        name: s.studentName || s.name,
                        thesisTitle: s.thesisTitle || s.topic,
                        topic: s.thesisTitle || s.topic
                    }))
                }];
            }
        } else {
            // 其他角色（专业负责人、学院领导等）暂时显示提示信息
            // 如果需要，可以后续添加相应的API端点
            defenseArrangements = [];
        }
    } catch (error) {
        console.error('Failed to load defense arrangements:', error);
        defenseArrangements = [];
    }
    
    container.innerHTML = `
        <div class="card p-8">
            <h3 class="text-xl font-bold text-slate-900 mb-6">答辩安排</h3>
            ${defenseArrangements.length > 0 ? `
                <div class="space-y-4">
                    ${defenseArrangements.map(arr => `
                        <div class="border border-slate-200 rounded-lg p-6 hover:shadow-md transition-shadow">
                            <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
                                <div>
                                    <p class="text-sm text-slate-500 mb-1">答辩小组</p>
                                    <p class="font-semibold text-slate-800">第 ${arr.teamId || arr.teamNumber || 'N/A'} 组</p>
                                </div>
                                <div>
                                    <p class="text-sm text-slate-500 mb-1">答辩教室</p>
                                    <p class="font-semibold text-slate-800">${arr.classroom || arr.room || '未安排'}</p>
                                </div>
                                <div>
                                    <p class="text-sm text-slate-500 mb-1">答辩时间</p>
                                    <p class="font-semibold text-slate-800">${arr.defenseTime || arr.time || '未安排'}</p>
                                </div>
                                <div>
                                    <p class="text-sm text-slate-500 mb-1">小组组长</p>
                                    <p class="font-semibold text-slate-800">${arr.chairmanName || arr.chairman || '未指定'}</p>
                                </div>
                            </div>
                            ${arr.students && arr.students.length > 0 ? `
                                <div class="mt-4 pt-4 border-t">
                                    <p class="text-sm text-slate-500 mb-2">答辩学生</p>
                                    <div class="space-y-2">
                                        ${arr.students.map(s => `
                                            <div class="flex items-center justify-between p-2 bg-slate-50 rounded">
                                                <span class="text-sm font-medium">${s.studentName || s.name}</span>
                                                <span class="text-xs text-slate-500">${s.thesisTitle || s.topic || ''}</span>
                                            </div>
                                        `).join('')}
                                    </div>
                                </div>
                            ` : ''}
                        </div>
                    `).join('')}
                </div>
            ` : `
                <div class="text-center py-12">
                    <p class="text-slate-500 mb-2">暂无答辩安排信息</p>
                    ${currentUser.roleId === 'MAJOR_LEADER' || currentUser.roleId === 'COLLEGE_LEADER' ? 
                        '<p class="text-xs text-slate-400 mt-2">答辩安排功能正在开发中，请稍后再试</p>' : ''}
                </div>
            `}
        </div>
    `;
}

// 答辩记录页面
async function renderDefenseRecordPage() {
    const container = document.getElementById('contentArea');
    
    let defenseRecords = [];
    try {
        // 获取答辩记录
        // 注意：后端可能没有专门的records端点，暂时使用students端点获取已答辩的学生
        if (currentUser.roleId === 'DEFENSE_MEMBER' || currentUser.roleId === 'DEFENSE_CHAIRMAN' || currentUser.roleId === 'DEFENSE_LEADER') {
            // 答辩小组成员/组长可以查看待答辩学生列表（已评分的可以视为已答辩）
            const students = await fetchWithAuth('/api/defense-team/thesis/students').catch(e => []);
            // 过滤出已评分的学生作为答辩记录
            defenseRecords = (students || []).filter(s => s.hasScore === true || s.hasScore === 'true').map(s => ({
                studentName: s.studentName || s.name,
                name: s.studentName || s.name,
                thesisTitle: s.thesisTitle || s.topic,
                topic: s.thesisTitle || s.topic,
                processId: s.processId || s.id,
                defenseScore: '已评分',
                defenseTime: '待补充',
                classroom: '待补充',
                teamId: 1
            }));
        } else {
            // 其他角色暂时显示提示信息
            defenseRecords = [];
        }
    } catch (error) {
        console.error('Failed to load defense records:', error);
        defenseRecords = [];
    }
    
    container.innerHTML = `
        <div class="card p-8">
            <h3 class="text-xl font-bold text-slate-900 mb-6">答辩记录</h3>
            ${defenseRecords.length > 0 ? `
                <table class="w-full text-left text-sm">
                    <thead>
                        <tr class="border-b border-slate-200 text-slate-500">
                            <th class="py-3">学生姓名</th>
                            <th>课题名称</th>
                            <th>答辩时间</th>
                            <th>答辩教室</th>
                            <th>答辩小组</th>
                            <th>答辩成绩</th>
                            <th>操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${defenseRecords.map(record => `
                            <tr class="border-b border-slate-100">
                                <td class="py-4 font-medium text-slate-800">${record.studentName || record.name}</td>
                                <td class="max-w-[300px] truncate">${record.thesisTitle || record.topic || ''}</td>
                                <td>${record.defenseTime || record.time || ''}</td>
                                <td>${record.classroom || record.room || ''}</td>
                                <td>第 ${record.teamId || record.teamNumber || ''} 组</td>
                                <td class="font-semibold">${record.defenseScore || record.score || '未评分'}</td>
                                <td>
                                    <button onclick='viewDefenseDetail(${record.processId || record.id})' 
                                            class="text-indigo-600 hover:underline font-semibold text-xs">
                                        查看详情
                                    </button>
                                </td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>
            ` : `
                <div class="text-center py-12">
                    <p class="text-slate-500">暂无答辩记录</p>
                </div>
            `}
        </div>
    `;
}

// 查看答辩详情
function viewDefenseDetail(processId) {
    // 可以跳转到详情页面或显示详情弹窗
    mockAction('查看答辩详情功能开发中...');
}