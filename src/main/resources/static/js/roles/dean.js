// ================== Dean Role Page Renderers ==================

async function renderMajorManagementPage() {
    // 从API获取专业列表
    let majors = [];
    try {
        majors = await fetchWithAuth('/api/college-leader/thesis/majors');
    } catch (error) {
        console.error('Failed to load majors:', error);
        majors = [];
    }
    const container = document.getElementById('contentArea');
    container.innerHTML = `
        <div class="card p-8">
            <h3 class="text-xl font-bold text-slate-900 mb-6">专业管理</h3>
            <table class="w-full text-left text-sm">
                <thead>
                    <tr class="text-slate-500 border-b">
                        <th class="py-3">专业名称</th><th>所属学院</th><th>学生人数</th><th>课题数量</th><th>操作</th>
                    </tr>
                </thead>
                <tbody>
                    ${majors.length === 0 ? '<tr><td colspan="5" class="py-8 text-center text-slate-400">暂无专业数据</td></tr>' : majors.map(m => `
                        <tr class="border-b border-slate-50 last:border-0">
                            <td class="py-3 font-medium">${m.majorName || ''}</td>
                            <td>${m.collegeName || ''}</td>
                            <td>${m.studentCount || 0}</td>
                            <td>${m.topicCount || 0}</td>
                            <td class="py-2"><button class="px-2 py-1 text-xs border rounded hover:bg-slate-50" onclick="mockAction('查看 ${m.majorName || ''} 详情')">查看详情</button></td>
                        </tr>
                    `).join('')}
                </tbody>
            </table>
        </div>
    `;
}

async function renderProgressMonitoringPage() {
    // 从API获取进度监控数据
    let progress = [];
    try {
        progress = await fetchWithAuth('/api/college-leader/thesis/progress');
    } catch (error) {
        console.error('Failed to load progress data:', error);
        progress = [];
    }
    const container = document.getElementById('contentArea');
    container.innerHTML = `
        <div class="card p-8">
            <h3 class="text-xl font-bold text-slate-900 mb-6">全院毕业设计进度监控</h3>
            <div class="space-y-4">
                ${progress.length === 0 ? '<p class="text-slate-400 text-center py-8">暂无进度数据</p>' : progress.map(p => `
                    <div>
                        <div class="flex justify-between items-center mb-1">
                            <p class="font-semibold text-slate-700">${p.stage || ''}</p>
                            <p class="text-sm font-bold text-indigo-600">${p.percentage || 0}%</p>
                        </div>
                        <div class="w-full bg-slate-100 rounded-full h-2.5">
                            <div class="bg-indigo-500 h-2.5 rounded-full" style="width: ${p.percentage || 0}%"></div>
                        </div>
                        <p class="text-xs text-slate-400 text-right mt-1">${p.completed || 0} / ${p.total || 0} 人完成</p>
                    </div>
                `).join('')}
            </div>
        </div>
    `;
}

function renderStatisticsReportPage() {
    const container = document.getElementById('contentArea');
    container.innerHTML = `
        <div class="card p-8">
            <h3 class="text-xl font-bold text-slate-900 mb-6">统计报表</h3>
            <p class="text-slate-500">此区域用于展示各类统计图表，例如各专业成绩分布、优秀论文比例等。</p>
            <div class="mt-6 p-12 bg-slate-50 rounded-lg text-center text-slate-400">
                <i class="fas fa-chart-pie text-4xl"></i>
                <p class="mt-2">图表区域</p>
            </div>
        </div>
    `;
}

function renderDeanDefenseArrangementPage() {
    const container = document.getElementById('contentArea');
    container.innerHTML = `
        <div class="card p-8">
            <h3 class="text-xl font-bold text-slate-900 mb-6">答辩安排（院级）</h3>
            <p class="text-slate-500">此页面用于查看和管理全院的答辩小组、时间及地点安排。</p>
             <div class="mt-6 p-12 bg-slate-50 rounded-lg text-center text-slate-400">
                <i class="fas fa-calendar-alt text-4xl"></i>
                <p class="mt-2">答辩安排概览区域</p>
            </div>
        </div>
    `;
}

