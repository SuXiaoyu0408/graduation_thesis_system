// ================== Teacher Role Page Renderers ==================

async function handleApproval(processId, materialType, action, studentName) {
    // 确保processId是数字类型
    const processIdNum = parseInt(processId, 10);
    if (!processIdNum || isNaN(processIdNum)) {
        mockAction('无效的流程ID');
        console.error('Invalid processId:', processId);
        return;
    }
    
    // 确保materialType是小写格式（后端期望的格式）
    const normalizedMaterialType = materialType.toLowerCase();
    
    let reason = '';
    if (action === 'reject') {
        // This relies on a modified showRejectModal that returns a promise
        reason = await showRejectModal(studentName, materialType);
        if (reason === null) { // User cancelled
            mockAction('操作已取消');
            return; 
        }
    }

    try {
        mockAction('正在提交审批...');
        // 根据API文档，使用正确的接口和格式
        await fetchWithAuth('/api/supervisor/thesis/approve', {
            method: 'POST',
            body: JSON.stringify({
                processId: processIdNum,
                materialType: normalizedMaterialType, // 使用小写格式
                pass: action === 'approve', // true=通过，false=驳回
                reason: reason || null
            })
        });
        mockAction('审批成功！');
        // After a successful action, reload data and re-render the view
        await loadSupervisedStudents(); 
        await renderProgressReviewPage();
    } catch (error) {
        console.error('Approval failed:', error);
        mockAction(`审批失败: ${error.message || '未知错误'}`);
    }
}

async function renderTopicDeclarationPage() {
    // 加载指导学生的数据
    await loadSupervisedStudents();
    
    // 获取已申报的题目列表（从指导的学生中获取）
    const getStatusChip = (status) => {
        if (status === 'approved') return '<span class="text-xs font-bold text-emerald-500 bg-emerald-50 px-2 py-0.5 rounded">已通过</span>';
        if (status === 'pending') return '<span class="text-xs font-bold text-amber-500 bg-amber-50 px-2 py-0.5 rounded">待审核</span>';
        if (status === 'rejected') return '<span class="text-xs font-bold text-rose-500 bg-rose-50 px-2 py-0.5 rounded">被驳回</span>';
        return '<span class="text-xs font-bold text-slate-400 bg-slate-100 px-2 py-0.5 rounded">未提交</span>';
    };

    // 获取每个学生的选题申报表状态
    const studentsWithTopics = supervisedStudentsData.map(student => {
        const topicMaterial = student.materials?.find(m => 
            m.materialType === 'TOPIC_SELECTION' || 
            m.materialType === 'topic_selection' ||
            m.materialType?.toLowerCase() === 'topic_selection'
        );
        return {
            studentName: student.studentName,
            topic: student.thesisTitle || '未确定课题',
            status: topicMaterial?.status || 'none',
            rejectReason: topicMaterial?.rejectReason,
            processId: student.processId
        };
    });

    const container = document.getElementById('contentArea');
    container.innerHTML = `
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div class="lg:col-span-2 space-y-6">
            <div class="card p-8">
                <h3 class="text-xl font-bold text-slate-900 mb-6">学生选题申报状态</h3>
                <div class="space-y-4">
                    ${studentsWithTopics.length === 0 ? '<p class="text-slate-400 text-sm text-center py-8">暂无指导学生</p>' : studentsWithTopics.map(item => `
                        <div class="p-4 border rounded-lg hover:shadow-sm transition-shadow">
                            <div class="flex justify-between items-start">
                                <div class="flex-1">
                                    <p class="font-bold text-slate-800">${item.studentName}</p>
                                    <p class="text-sm text-slate-600 mt-1">${item.topic}</p>
                                    <p class="text-sm mt-2">${getStatusChip(item.status)}</p>
                                </div>
                                ${item.status === 'none' ? `
                                    <button onclick="submitTopicForStudent(${item.processId}, '${item.studentName}')" class="text-xs px-3 py-1.5 bg-indigo-600 text-white rounded hover:bg-indigo-700">提交选题申报表</button>
                                ` : ''}
                            </div>
                            ${item.status === 'rejected' && item.rejectReason ? `<div class="mt-3 pt-3 border-t border-slate-100 text-xs text-rose-700"><b>驳回原因:</b> ${item.rejectReason}</div>` : ''}
                        </div>
                    `).join('')}
                </div>
            </div>
        </div>
        <div class="space-y-6">
            <div class="card p-8">
                <h3 class="text-xl font-bold text-slate-900 mb-6">为学生提交选题申报表</h3>
                <p class="text-sm text-slate-500 mb-4">请从左侧列表中选择学生，或直接点击"提交选题申报表"按钮。</p>
                <div class="space-y-4">
                    <div>
                        <label class="block text-xs font-bold text-slate-500 mb-1.5">选择学生</label>
                        <select id="student-select" class="w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-lg text-sm">
                            <option value="">请选择学生</option>
                            ${supervisedStudentsData.map(s => `<option value="${s.processId}" data-name="${s.studentName}">${s.studentName} - ${s.thesisTitle || '未确定课题'}</option>`).join('')}
                        </select>
                    </div>
                    <div>
                        <label class="block text-xs font-bold text-slate-500 mb-1.5">上传选题申报表</label>
                        <div id="topic-upload-zone" class="border-2 border-dashed border-slate-200 rounded-xl p-6 text-center cursor-pointer hover:border-indigo-300 transition-colors group bg-slate-50/30">
                            <i class="fas fa-cloud-upload-alt text-3xl text-slate-300 group-hover:text-indigo-400 mb-2"></i>
                            <p id="topic-upload-text" class="text-xs font-medium text-slate-500">点击或拖拽文件上传</p>
                            <p class="text-[10px] text-slate-400 mt-1">支持 PDF, DOC, DOCX 格式，最大 10MB</p>
                            <input type="file" id="topic-file-input" class="hidden" accept=".pdf,.doc,.docx">
                        </div>
                    </div>
                    <button id="submit-topic-btn" class="w-full mt-2 py-3 bg-indigo-600 text-white rounded-lg font-bold text-sm hover:bg-indigo-700 transition-all shadow-md">提交选题申报表</button>
                </div>
            </div>
        </div>
    </div>
    `;

    // 实现文件上传功能
    const uploadZone = document.getElementById('topic-upload-zone');
    const fileInput = document.getElementById('topic-file-input');
    const uploadText = document.getElementById('topic-upload-text');
    const submitBtn = document.getElementById('submit-topic-btn');
    const studentSelect = document.getElementById('student-select');
    let selectedFile = null;

    uploadZone.addEventListener('click', () => fileInput.click());

    fileInput.addEventListener('change', () => {
        if (fileInput.files.length > 0) {
            selectedFile = fileInput.files[0];
            uploadText.textContent = `已选择: ${selectedFile.name}`;
            uploadZone.classList.add('border-indigo-400');
        }
    });

    // 拖拽上传
    ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
        uploadZone.addEventListener(eventName, (e) => {
            e.preventDefault();
            e.stopPropagation();
        }, false);
    });
    ['dragenter', 'dragover'].forEach(eventName => {
        uploadZone.addEventListener(eventName, () => uploadZone.classList.add('border-indigo-400'), false);
    });
    ['dragleave', 'drop'].forEach(eventName => {
        uploadZone.addEventListener(eventName, () => uploadZone.classList.remove('border-indigo-400'), false);
    });

    uploadZone.addEventListener('drop', (e) => {
        if (e.dataTransfer.files.length > 0) {
            selectedFile = e.dataTransfer.files[0];
            fileInput.files = e.dataTransfer.files;
            uploadText.textContent = `已选择: ${selectedFile.name}`;
        }
    });

    submitBtn.addEventListener('click', async () => {
        const processId = studentSelect.value;
        
        if (!processId) {
            mockAction('请选择学生');
            return;
        }

        if (!selectedFile) {
            mockAction('请上传选题申报表');
            return;
        }

        // 文件类型和大小校验
        const allowedTypes = ['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'];
        if (!allowedTypes.includes(selectedFile.type)) {
            mockAction('仅支持 PDF 或 Word 文件');
            return;
        }
        if (selectedFile.size > 10 * 1024 * 1024) {
            mockAction('文件大小不能超过 10MB');
            return;
        }

        try {
            submitBtn.disabled = true;
            submitBtn.classList.add('opacity-60', 'cursor-not-allowed');
            mockAction('正在提交选题申报表...');

            // 使用现有的上传材料接口
            const formData = new FormData();
            formData.append('file', selectedFile);

            await fetchWithAuth(`/api/supervisor/thesis/material/${processId}/topic_selection`, {
                method: 'POST',
                body: formData
            });

            mockAction('选题申报表提交成功！');
            // 重置表单
            studentSelect.value = '';
            selectedFile = null;
            fileInput.value = '';
            uploadText.textContent = '点击或拖拽文件上传';
            uploadZone.classList.remove('border-indigo-400');

            // 重新加载数据
            await renderTopicDeclarationPage();
        } catch (error) {
            console.error('Topic declaration failed:', error);
            mockAction(`提交失败: ${error.message || '未知错误'}`);
        } finally {
            submitBtn.disabled = false;
            submitBtn.classList.remove('opacity-60', 'cursor-not-allowed');
        }
    });
}

// 为学生提交选题申报表的辅助函数
async function submitTopicForStudent(processId, studentName) {
    // 设置选择框的值
    const studentSelect = document.getElementById('student-select');
    if (studentSelect) {
        studentSelect.value = processId;
    }
    
    // 触发文件选择
    const fileInput = document.getElementById('topic-file-input');
    if (fileInput) {
        fileInput.click();
        fileInput.onchange = async () => {
            const file = fileInput.files[0];
            if (!file) return;
            
            // 文件类型和大小校验
            const allowedTypes = ['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'];
            if (!allowedTypes.includes(file.type)) {
                mockAction('仅支持 PDF 或 Word 文件');
                return;
            }
            if (file.size > 10 * 1024 * 1024) {
                mockAction('文件大小不能超过 10MB');
                return;
            }
            
            try {
                mockAction(`正在为 ${studentName} 提交选题申报表...`);
                const formData = new FormData();
                formData.append('file', file);
                
                await fetchWithAuth(`/api/supervisor/thesis/material/${processId}/topic_selection`, {
                    method: 'POST',
                    body: formData
                });
                
                mockAction('选题申报表提交成功！');
                await renderTopicDeclarationPage();
            } catch (error) {
                console.error('Submit topic failed:', error);
                mockAction(`提交失败: ${error.message || '未知错误'}`);
            }
        };
    }
}

async function renderTaskBookPage() {
    // 从API获取指导学生的任务书状态
    try {
        // 加载指导学生的数据
        await loadSupervisedStudents();
        
        // 为每个学生获取任务书状态
        const studentsWithTaskBook = supervisedStudentsData.map(s => {
            // 从materials数组中查找任务书状态
            let taskBookStatus = 'none';
            if (s.materials && Array.isArray(s.materials)) {
                const taskBookMaterial = s.materials.find(m => {
                    const mType = m.materialType || m.material_type || '';
                    return mType.toLowerCase() === 'task_assignment' ||
                           mType === 'TASK_ASSIGNMENT';
                });
                if (taskBookMaterial) {
                    // 如果材料存在，根据状态判断
                    if (taskBookMaterial.status === 'approved' || taskBookMaterial.status === 'pending') {
                        taskBookStatus = 'uploaded';
                    } else {
                        taskBookStatus = taskBookMaterial.status || 'none';
                    }
                }
            }
            
            return {
                name: s.studentName || s.name || '未知学生',
                topic: s.thesisTitle || s.topic || '未确定课题',
                taskBookStatus: taskBookStatus,
                processId: s.processId
            };
        });
        
        const container = document.getElementById('contentArea');
        container.innerHTML = `
            <div class="card p-8">
                <h3 class="text-xl font-bold text-slate-900 mb-6">课题任务书管理</h3>
                <p class="text-sm text-slate-500 mb-6">为您的指导学生上传和管理课题任务书。</p>
                <div class="space-y-4">
                    ${studentsWithTaskBook.length === 0 ? '<p class="text-slate-400 text-center py-8">暂无指导学生</p>' : studentsWithTaskBook.map(student => {
                        const nameEscaped = (student.name || '').replace(/'/g, "\\'").replace(/"/g, '&quot;');
                        return `
                        <div class="p-4 border rounded-lg flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
                            <div class="flex-1">
                                <p class="font-bold text-slate-800">${student.name}</p>
                                <p class="text-sm text-slate-500 mt-1 truncate" title="${(student.topic || '').replace(/"/g, '&quot;')}">${student.topic}</p>
                            </div>
                            <div class="w-full md:w-auto flex-shrink-0">
                                ${student.taskBookStatus === 'uploaded' || student.taskBookStatus === 'approved' || student.taskBookStatus === 'pending' ? `
                                    <div class="flex items-center gap-2 justify-end">
                                        <span class="text-sm font-medium text-emerald-600"><i class="fas fa-check-circle mr-1"></i>已上传</span>
                                        <button onclick="uploadTaskBook(${student.processId}, '${nameEscaped}')" class="text-xs px-2 py-1 bg-indigo-50 text-indigo-600 rounded hover:bg-indigo-100">替换</button>
                                    </div>
                                ` : `
                                    <div class="flex items-center gap-3 justify-end">
                                        <span class="text-sm font-medium text-amber-600"><i class="fas fa-exclamation-circle mr-1"></i>待上传</span>
                                        <button onclick="uploadTaskBook(${student.processId}, '${nameEscaped}')" class="text-sm px-4 py-2 bg-indigo-600 text-white rounded-lg font-bold hover:bg-indigo-700">上传</button>
                                    </div>
                                `}
                            </div>
                        </div>
                    `;
                    }).join('')}
                </div>
            </div>
        `;
    } catch (error) {
        console.error('Failed to load task book data:', error);
        const container = document.getElementById('contentArea');
        container.innerHTML = `
            <div class="card p-8">
                <h3 class="text-xl font-bold text-slate-900 mb-6">课题任务书管理</h3>
                <p class="text-slate-500 text-center py-8">加载失败，请刷新页面重试</p>
            </div>
        `;
    }
}

// 指导学生的数据（从API获取）
let supervisedStudentsData = [];

// 加载指导学生的数据
// ====================== Supervisor Helper Functions ======================
/**
 * 上传课题任务书
 * @param {number} processId 论文流程ID
 * @param {string} studentName 学生姓名（仅用于提示）
 */
async function uploadTaskBook(processId, studentName = '') {
    if (!processId) {
        mockAction('缺少 processId');
        return;
    }
    // 创建隐藏文件输入
    const fileInput = document.createElement('input');
    fileInput.type = 'file';
    fileInput.accept = '.pdf,.doc,.docx';
    fileInput.style.display = 'none';
    document.body.appendChild(fileInput);

    fileInput.onchange = async () => {
        const file = fileInput.files[0];
        if (!file) {
            document.body.removeChild(fileInput);
            return;
        }
        try {
            mockAction(`正在上传任务书...`);
            const fd = new FormData();
            fd.append('file', file);
            await fetchWithAuth(`/api/supervisor/thesis/material/${processId}/task_assignment`, {
                method: 'POST',
                body: fd
            });
            mockAction('上传成功！');
            await loadSupervisedStudents();
            renderTaskBookPage();
        } catch (e) {
            console.error('Upload failed:', e);
        } finally {
            document.body.removeChild(fileInput);
        }
    };
    // 触发点击
    fileInput.click();
}

async function loadSupervisedStudents() {
    try {
        const students = await fetchWithAuth('/api/supervisor/thesis/students');
        supervisedStudentsData = Array.isArray(students) ? students : [];
        console.log("Supervised students data loaded", supervisedStudentsData.length);
    } catch (error) {
        console.error('Failed to load supervised students:', error);
        supervisedStudentsData = [];
    }
}

async function renderProgressReviewPage() {
    // 加载指导学生的数据
    await loadSupervisedStudents();
    const getStatusChip = (status) => {
        if (status === 'approved') return '<span class="text-xs font-bold text-emerald-500 bg-emerald-50 px-2 py-0.5 rounded">已通过</span>';
        if (status === 'pending') return '<span class="text-xs font-bold text-amber-500 bg-amber-50 px-2 py-0.5 rounded">待审核</span>';
        if (status === 'rejected') return '<span class="text-xs font-bold text-rose-500 bg-rose-50 px-2 py-0.5 rounded">被驳回</span>';
        return '<span class="text-xs font-bold text-slate-400 bg-slate-100 px-2 py-0.5 rounded">未提交</span>';
    };

    // 为每个学生准备提交状态数据
    const studentsWithSubmissions = supervisedStudentsData.map(student => {
        const processId = student.processId || student.id;
        // 根据学生的材料状态构建提交列表，使用后端期望的小写格式
        const submissions = [
            { name: '选题申报表', type: 'topic_selection', status: getMaterialStatus(student, 'topic_selection') },
            { name: '开题报告', type: 'opening_report', status: getMaterialStatus(student, 'opening_report') },
            { name: '中期报告', type: 'mid_term_report', status: getMaterialStatus(student, 'mid_term_report') },
            { name: '论文终稿', type: 'final_paper', status: getMaterialStatus(student, 'final_paper') }
        ];
        return { 
            ...student, 
            submissions, 
            processId,
            name: student.studentName || student.name || '未知学生',
            topic: student.thesisTitle || student.topic || '未确定课题',
            major: student.majorName || student.major || ''
        };
    });

    const container = document.getElementById('contentArea');
    container.innerHTML = `
        <div class="card p-8">
            <h3 class="text-xl font-bold text-slate-900 mb-6">学生进度批阅</h3>
            <div class="space-y-6">
                ${studentsWithSubmissions.length === 0 ? '<p class="text-slate-400 text-center py-8">暂无指导学生</p>' : studentsWithSubmissions.map(student => `
                    <div class="card p-6 border border-slate-100 hover:shadow-md transition-shadow">
                        <div class="flex justify-between items-start">
                            <div>
                                <p class="text-lg font-bold text-slate-800">${student.name} - <span class="text-sm font-normal text-slate-500">${student.major || ''}</span></p>
                                <p class="text-sm text-slate-600 mt-1">${student.topic || '未确定课题'}</p>
                            </div>
                            <button onclick="switchPage('历史记录')" class="text-sm text-indigo-600 font-semibold hover:underline">查看历史</button>
                        </div>
                        <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mt-6 pt-6 border-t border-slate-100">
                            ${student.submissions.map(sub => `
                                <div class="text-center">
                                    <p class="text-sm font-semibold text-slate-700 mb-2">${sub.name}</p>
                                    <div>${getStatusChip(sub.status)}</div>
                                    ${sub.status === 'pending' || sub.status === 'rejected' ? `
                                        <div class="mt-3 flex justify-center gap-2">
                                             <button onclick="previewSupervisorMaterial(${student.processId}, '${sub.type}')" class="text-xs px-2 py-1 border rounded hover:bg-slate-50">预览</button>
                                             <button onclick="handleApproval(${student.processId}, '${sub.type}', 'approve', '${student.name}')" class="text-xs px-2 py-1 bg-emerald-50 text-emerald-600 rounded hover:bg-emerald-100">通过</button>
                                             <button onclick="handleApproval(${student.processId}, '${sub.type}', 'reject', '${student.name}')" class="text-xs px-2 py-1 bg-rose-50 text-rose-600 rounded hover:bg-rose-100">驳回</button>
                                        </div>
                                    ` : ''}
                                     ${sub.status === 'approved' ? `
                                        <div class="mt-3 flex justify-center gap-2">
                                             <button onclick="previewSupervisorMaterial(${student.processId}, '${sub.type}')" class="text-xs px-2 py-1 border rounded hover:bg-slate-50">预览</button>
                                        </div>
                                    ` : ''}
                                </div>
                            `).join('')}
                        </div>
                    </div>
                `).join('')}
            </div>
        </div>
    `;
}

// 辅助函数：获取材料状态
function getMaterialStatus(student, materialType) {
    // 根据学生的材料状态数组判断材料状态
    if (!student.processId) return 'none';
    
    // 从student.materials数组中查找对应材料的状态
    if (student.materials && Array.isArray(student.materials)) {
        const material = student.materials.find(m => {
            const mType = m.materialType || m.material_type || '';
            // 支持多种格式：大写、小写、混合
            return mType.toLowerCase() === materialType.toLowerCase() ||
                   mType === materialType.toUpperCase() ||
                   mType === materialType;
        });
        
        if (material) {
            return material.status || 'pending';
        }
    }
    
    // 如果没有找到材料，返回未提交
    return 'none';
}

// 预览学生材料
async function previewSupervisorMaterial(processId, materialType) {
    try {
        // 确保materialType是小写格式（后端期望的格式）
        const normalizedMaterialType = materialType.toLowerCase();
        const url = `${API_BASE_URL}/api/supervisor/thesis/student/material/${processId}/${normalizedMaterialType}/preview`;
        const cleanToken = getCleanToken();
        const response = await fetch(url, {
            headers: {
                'Authorization': `Bearer ${cleanToken}`
            }
        });
        
        if (response.ok) {
            const blob = await response.blob();
            const previewUrl = window.URL.createObjectURL(blob);
            window.open(previewUrl, '_blank');
            mockAction('正在预览...');
        } else {
            const errorText = await response.text().catch(() => '预览失败');
            mockAction(`预览失败: ${errorText}`);
        }
    } catch (error) {
        console.error('Preview failed:', error);
        mockAction(`预览失败: ${error.message || '未知错误'}`);
    }
}

async function renderSupervisorPaperReviewPage() {
    // 从API获取需要评阅的学生列表
    let studentsForReview = [];
    try {
        // 加载指导学生的数据
        await loadSupervisedStudents();
        // 根据实际数据转换格式，使用正确的字段名
        studentsForReview = supervisedStudentsData.map(s => {
            // 查找论文终稿的状态
            const finalPaperMaterial = s.materials?.find(m => 
                m.materialType === 'FINAL_PAPER' || 
                m.materialType === 'final_paper' ||
                m.materialType?.toLowerCase() === 'final_paper'
            );
            return {
                name: s.studentName || s.name || '未知学生',
                topic: s.thesisTitle || s.topic || '未确定课题',
                finalPaperStatus: finalPaperMaterial?.status || 'none',
                processId: s.processId
            };
        });
    } catch (error) {
        console.error('Failed to load students for review:', error);
        studentsForReview = [];
    }

    const getStatusLabel = (status) => {
        if (status === 'pending') return '<span class="text-amber-600 font-semibold">待评阅</span>';
        if (status === 'approved') return '<span class="text-emerald-600 font-semibold">已评阅</span>';
        return '<span class="text-slate-500">未提交</span>';
    };

    const container = document.getElementById('contentArea');
    container.innerHTML = `
        <div class="card p-8">
            <h3 class="text-xl font-bold text-slate-900 mb-6">指导老师论文评阅</h3>
            <p class="text-sm text-slate-500 mb-6">在这里预览和下载您指导学生的论文终稿，并为他们评分。</p>
            <table class="w-full text-left text-sm">
                <thead>
                    <tr class="text-slate-500 border-b">
                        <th class="py-3">学生姓名</th>
                        <th class="py-3">课题名称</th>
                        <th class="py-3">状态</th>
                        <th class="py-3">操作</th>
                    </tr>
                </thead>
                <tbody>
                    ${studentsForReview.map(student => `
                        <tr class="border-b border-slate-50 last:border-0">
                            <td class="py-3 font-medium text-slate-800">${student.name}</td>
                            <td class="py-3 max-w-[300px] truncate">${student.topic}</td>
                            <td class="py-3">${getStatusLabel(student.finalPaperStatus)}</td>
                            <td class="py-3">
                                ${student.processId ? `
                                    <div class="flex gap-2">
                                        ${student.finalPaperStatus !== 'none' ? `
                                            <button onclick="previewSupervisorMaterial(${student.processId}, 'final_paper')" class="px-2 py-1 text-xs border rounded hover:bg-slate-50">预览论文</button>
                                            <button onclick="mockAction('下载 ${student.name} 的论文...')" class="px-2 py-1 text-xs border rounded hover:bg-slate-50">下载</button>
                                        ` : ''}
                                        <button onclick="switchPage('成绩报送', { studentName: '${(student.name || '').replace(/'/g, "\\'")}', processId: ${student.processId} })" class="px-2 py-1 text-xs bg-indigo-50 text-indigo-600 rounded hover:bg-indigo-100">${student.finalPaperStatus === 'approved' ? '修改评分' : '去评分'}</button>
                                    </div>
                                ` : '<span class="text-xs text-slate-400">暂无流程ID</span>'}
                            </td>
                        </tr>
                    `).join('')}
                </tbody>
            </table>
        </div>
    `;
}

async function renderScoreSubmitPage(data = {}) {
    const studentName = data.studentName || '未知学生';
    // 确保processId被正确获取，支持多种可能的参数名
    let processId = data.processId || data.studentId || data.id || null;
    
    // 如果没有提供processId，显示学生列表让用户选择
    if (!processId) {
        // 加载指导学生的数据
        await loadSupervisedStudents();
        
        if (!supervisedStudentsData || supervisedStudentsData.length === 0) {
            const container = document.getElementById('contentArea');
            container.innerHTML = `
                <div class="card p-8">
                    <h3 class="text-xl font-bold text-slate-900 mb-6">成绩报送</h3>
                    <p class="text-slate-500 text-center py-8">暂无指导学生</p>
                </div>
            `;
            return;
        }
        
        // 显示学生列表供选择
        const container = document.getElementById('contentArea');
        container.innerHTML = `
            <div class="card p-8">
                <h3 class="text-xl font-bold text-slate-900 mb-6">成绩报送</h3>
                <p class="text-sm text-slate-500 mb-6">请选择要评分的学生</p>
                <div class="space-y-4">
                    ${supervisedStudentsData.map(student => {
                        const name = (student.studentName || student.name || '未知学生').replace(/'/g, "\\'").replace(/"/g, '&quot;');
                        const topic = (student.thesisTitle || student.topic || '未确定课题').replace(/'/g, "\\'").replace(/"/g, '&quot;');
                        const displayName = student.studentName || student.name || '未知学生';
                        const displayTopic = student.thesisTitle || student.topic || '未确定课题';
                        return `
                        <div class="p-4 border rounded-lg hover:shadow-sm transition-shadow cursor-pointer" 
                             onclick="switchPage('成绩报送', { studentName: '${name}', processId: ${student.processId} })">
                            <div class="flex justify-between items-center">
                                <div>
                                    <p class="font-bold text-slate-800">${displayName}</p>
                                    <p class="text-sm text-slate-600 mt-1">${displayTopic}</p>
                                </div>
                                <i class="fas fa-chevron-right text-slate-400"></i>
                            </div>
                        </div>
                    `;
                    }).join('')}
                </div>
            </div>
        `;
        return;
    }
    
    const scoreItems = [
        { key: 'literatureReview', label: '选题与文献综述', max: 20 },
        { key: 'innovation', label: '创新性', max: 10 },
        { key: 'theoryKnowledge', label: '基础理论和专业知识', max: 35 },
        { key: 'attitudeAndAbility', label: '态度、写作水平、写作规范、综合能力', max: 35 },
    ];

    const container = document.getElementById('contentArea');
    container.innerHTML = `
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
         <div class="lg:col-span-2 card p-8">
            <div class="flex justify-between items-center mb-6">
                <div>
                    <h3 class="text-xl font-bold text-slate-900">指导老师评分 - ${studentName}</h3>
                    <p class="text-sm text-slate-500 mt-1">请根据以下细则为该学生评分</p>
                </div>
                <button onclick="switchPage('论文评阅')" class="text-sm text-indigo-600 font-semibold hover:underline"><i class="fas fa-arrow-left mr-1"></i>返回列表</button>
            </div>
            <div id="score-form" class="space-y-5 pt-6 border-t">
                ${scoreItems.map(item => `
                    <div class="flex items-center gap-4">
                        <label class="w-2/5 text-sm font-medium text-slate-700">${item.label}</label>
                        <input type="number" data-key="${item.key}" max="${item.max}" min="0" class="score-input w-24 border-slate-200 rounded-md text-center font-bold focus:ring-2 focus:ring-indigo-200 focus:border-indigo-400" placeholder="0" oninput="updateTotalScore()" />
                        <span class="text-sm text-slate-400">/ ${item.max}分</span>
                    </div>
                `).join('')}
                <div class="flex items-center gap-4 pt-5 border-t">
                     <label class="w-2/5 text-sm font-bold text-slate-800">总分</label>
                     <p id="totalScore" class="font-bold text-2xl text-indigo-600">0</p>
                     <span class="text-sm text-slate-400">/ 100分</span>
                </div>
            </div>
            <div class="text-right mt-8">
                <button id="submit-score-btn" class="px-8 py-3 bg-indigo-600 text-white rounded-lg font-bold shadow-lg shadow-indigo-100 hover:bg-indigo-700">提交评分</button>
            </div>
        </div>
        <!-- ... scoring criteria ... -->
    </div>
    `;

    // 将processId存储到按钮的data属性中，确保事件监听器可以访问
    const submitBtn = document.getElementById('submit-score-btn');
    if (submitBtn) {
        submitBtn.setAttribute('data-process-id', processId);
    }

    // 移除之前的事件监听器（如果存在），避免重复绑定
    const newSubmitBtn = document.getElementById('submit-score-btn');
    if (newSubmitBtn) {
        // 克隆节点以移除所有事件监听器
        const newBtn = newSubmitBtn.cloneNode(true);
        newSubmitBtn.parentNode.replaceChild(newBtn, newSubmitBtn);
        
        newBtn.addEventListener('click', async () => {
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
                // 从按钮的data属性或闭包中获取processId
                const currentProcessId = newBtn.getAttribute('data-process-id') || processId;
                
                if (!currentProcessId) {
                    mockAction('缺少必要参数：无法获取学生流程ID');
                    console.error('processId is missing in submit score', {
                        buttonData: newBtn.getAttribute('data-process-id'),
                        closureProcessId: processId,
                        originalData: data
                    });
                    return;
                }
                
                mockAction('正在提交评分...');
                // 根据API文档，使用正确的接口和参数名
                await fetchWithAuth('/api/supervisor/thesis/score', {
                    method: 'POST',
                    body: JSON.stringify({
                        processId: parseInt(currentProcessId, 10), // 确保是数字类型
                        topicReview: scores.literatureReview || 0,
                        innovation: scores.innovation || 0,
                        theoryKnowledge: scores.theoryKnowledge || 0,
                        attitudeAndWriting: scores.attitudeAndAbility || 0
                    })
                });
                mockAction('评分提交成功！');
                await loadUserData();
                switchPage('论文评阅');
            } catch (error) {
                console.error('Score submission failed:', error);
                mockAction(`提交失败: ${error.message || '未知错误'}`);
            }
        });
    }
}

