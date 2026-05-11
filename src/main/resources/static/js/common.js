// =================== Common Renderers (Shared) ===================

async function deleteHistoryFile(historyId, onSuccess) {
    if (!confirm('确定要删除这条历史记录吗？删除后不可恢复。')) return;
    try {
        await fetchWithAuth(`/api/student/thesis/material/history/${historyId}`, { method: 'DELETE' });
        mockAction('删除成功');
        if (onSuccess) {
            onSuccess();
        } else {
            renderHistoryPage();
        }
    } catch (error) {
        mockAction(`删除失败: ${error.message}`);
    }
}

async function renderHistoryPage() {
    const container = document.getElementById('contentArea');
    container.innerHTML = '<div class="card p-8"><h3 class="text-xl font-bold text-slate-900 mb-6">历史记录</h3><p class="text-slate-400 text-center py-8">正在加载历史记录...</p></div>';
    
    try {
        // 根据角色获取历史记录
        let history = [];
        
        if (currentUser.roleId === 'STUDENT') {
            // 学生查看自己的历史记录
            if (currentProcessId) {
                const [topicHistory, proposalHistory, midtermHistory, thesisHistory] = await Promise.all([
                    fetchWithAuth(`/api/student/thesis/material/${currentProcessId}/TOPIC_SELECTION/history`).catch(e => []),
                    fetchWithAuth(`/api/student/thesis/material/${currentProcessId}/OPENING_REPORT/history`).catch(e => []),
                    fetchWithAuth(`/api/student/thesis/material/${currentProcessId}/MID_TERM_REPORT/history`).catch(e => []),
                    fetchWithAuth(`/api/student/thesis/material/${currentProcessId}/FINAL_PAPER/history`).catch(e => [])
                ]);
                
                history = [
                    ...(topicHistory || []).map(h => ({ ...h, material: '选题申报表', name: currentUser.name || '我' })),
                    ...(proposalHistory || []).map(h => ({ ...h, material: '开题报告', name: currentUser.name || '我' })),
                    ...(midtermHistory || []).map(h => ({ ...h, material: '中期报告', name: currentUser.name || '我' })),
                    ...(thesisHistory || []).map(h => ({ ...h, material: '论文终稿', name: currentUser.name || '我' }))
                ];
            }
        }
        // 其他角色的历史记录可以根据需要添加
        
        const getChip = (s) => {
            if (s === 'approved' || s === true) return '<span class="text-xs font-bold text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded">已通过</span>';
            if (s === 'pending') return '<span class="text-xs font-bold text-amber-600 bg-amber-50 px-2 py-0.5 rounded">待审核</span>';
            if (s === 'rejected' || s === false) return '<span class="text-xs font-bold text-rose-600 bg-rose-50 px-2 py-0.5 rounded">已驳回</span>';
            return '<span class="text-xs font-bold text-slate-400 bg-slate-100 px-2 py-0.5 rounded">未知</span>';
        };
        
        if (history.length === 0) {
            container.innerHTML = '<div class="card p-8"><h3 class="text-xl font-bold text-slate-900 mb-6">历史记录</h3><p class="text-slate-400 text-center py-8">暂无历史记录</p></div>';
        } else {
            container.innerHTML = `<div class="card p-8"><h3 class="text-xl font-bold text-slate-900 mb-6">历史记录</h3><table class="w-full text-left text-sm"><thead><tr class="border-b text-slate-500"><th class="py-3">人员</th><th>材料类型</th><th>提交时间</th><th>状态</th><th>操作</th></tr></thead><tbody>${history.map(h=>`<tr class="border-b last:border-0"><td class="py-3 font-medium">${h.name || h.uploaderName || '未知'}</td><td>${h.material || h.materialType || '未知'}</td><td>${h.time || h.uploadedAt || '未知'}</td><td>${getChip(h.status || h.isLatest)}</td><td class="py-2"><button class='text-xs px-2 py-1 border rounded hover:bg-slate-50' onclick='downloadHistoryFile(${h.historyId}, ${JSON.stringify(h.originalFilename || '')})'>下载</button><button class='text-xs px-2 py-1 border rounded hover:bg-red-50 text-red-500 ml-1' onclick='event.stopPropagation();deleteHistoryFile(${h.historyId})'>删除</button></td></tr>`).join('')}</tbody></table></div>`;
        }
    } catch (error) {
        console.error('Failed to load history:', error);
        container.innerHTML = '<div class="card p-8"><h3 class="text-xl font-bold text-slate-900 mb-6">历史记录</h3><p class="text-rose-500 text-center py-8">加载历史记录失败</p></div>';
    }
}

