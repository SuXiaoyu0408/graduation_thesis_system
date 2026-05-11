// ================== Reviewer Role Helper Functions ==================

// 存储当前论文列表，用于数据传递
let currentReviewerPapersList = [];

async function uploadEvaluationForm(processId) {
    if (!processId) {
        mockAction('缺少 processId');
        return;
    }
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
            mockAction(`正在上传评阅表...`);
            const fd = new FormData();
            fd.append('file', file);
            await fetchWithAuth(`/api/reviewer/thesis/evaluation-form/${processId}`, {
                method: 'POST',
                body: fd
            });
            mockAction('评阅表上传成功！');
            // Here you might want to re-render the component to show the uploaded status
        } catch (e) {
            console.error('Upload failed:', e);
        } finally {
            document.body.removeChild(fileInput);
        }
    };
    fileInput.click();
}

// ================== Reviewer Role Page Renderers ==================

async function renderReviewerPaperListPage() {
    // 从API获取待评阅的论文列表
    let papersForReview = [];
    try {
        papersForReview = await fetchWithAuth('/api/reviewer/thesis/papers') || [];
        console.log('Papers loaded from API:', papersForReview);
    } catch (error) {
        console.error('Failed to load papers for review:', error);
        papersForReview = [];
    }
    // 存储到全局变量，用于后续数据传递
    currentReviewerPapersList = papersForReview;
    console.log('Stored papers list:', currentReviewerPapersList);
    const container = document.getElementById('contentArea');
    container.innerHTML = `
        <div class="card p-8">
            <h3 class="text-xl font-bold text-slate-900 mb-6">待评阅论文列表</h3>
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
                    ${papersForReview.length === 0 ? `
                        <tr>
                            <td colspan="4" class="py-8 text-center text-slate-400">暂无待评阅论文</td>
                        </tr>
                    ` : papersForReview.map((p, index) => {
                        // 映射后端字段到前端期望的字段
                        const paperData = {
                            processId: p.processId,
                            id: p.processId,
                            student: p.studentName || '未知',
                            supervisor: p.supervisorName || '未分配',
                            topic: p.thesisTitle || '未确定',
                            studentName: p.studentName,
                            supervisorName: p.supervisorName,
                            thesisTitle: p.thesisTitle,
                            hasEvaluationForm: p.hasEvaluationForm,
                            hasScore: p.hasScore
                        };
                        return `
                        <tr class="border-b border-slate-100 text-sm">
                            <td class="py-4 font-medium text-slate-800">${paperData.student}</td>
                            <td>${paperData.supervisor}</td>
                            <td class="max-w-[300px] truncate">${paperData.topic}</td>
                            <td>
                                <button class="reviewer-grade-btn text-indigo-600 hover:underline font-semibold" 
                                        data-index="${index}">
                                    ${paperData.hasScore ? '查看/修改评阅' : '开始评阅'}
                                </button>
                            </td>
                        </tr>
                    `;
                    }).join('')}
                </tbody>
            </table>
        </div>
    `;
    
    // 为所有"开始评阅"按钮添加事件监听器
    container.querySelectorAll('.reviewer-grade-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            try {
                console.log('Button clicked, currentReviewerPapersList:', currentReviewerPapersList);
                const index = parseInt(this.getAttribute('data-index'), 10);
                console.log('Index from button:', index);
                
                if (isNaN(index)) {
                    console.error('Invalid index (NaN):', this.getAttribute('data-index'));
                    mockAction('数据错误，请刷新页面重试');
                    return;
                }
                
                if (index < 0 || index >= currentReviewerPapersList.length) {
                    console.error('Index out of range:', index, 'List length:', currentReviewerPapersList.length);
                    mockAction('数据错误，请刷新页面重试');
                    return;
                }
                
                // 从存储的论文列表中获取数据
                const p = currentReviewerPapersList[index];
                console.log('Paper data from list:', p);
                
                if (!p) {
                    console.error('Paper data is null or undefined at index:', index);
                    mockAction('数据错误，请刷新页面重试');
                    return;
                }
                
                if (!p.processId) {
                    console.error('processId is missing in paper data:', p);
                    mockAction('数据错误：缺少processId，请刷新页面重试');
                    return;
                }
                
                const paperData = {
                    processId: p.processId,
                    id: p.processId,
                    student: p.studentName || '未知',
                    supervisor: p.supervisorName || '未分配',
                    topic: p.thesisTitle || '未确定',
                    studentName: p.studentName,
                    supervisorName: p.supervisorName,
                    thesisTitle: p.thesisTitle,
                    hasEvaluationForm: p.hasEvaluationForm,
                    hasScore: p.hasScore
                };
                
                console.log('Paper data for grading page:', paperData);
                console.log('Calling switchPage with:', paperData);
                switchPage('成绩评定', paperData);
            } catch (error) {
                console.error('Failed to get paper data:', error);
                console.error('Error stack:', error.stack);
                mockAction('数据获取错误，请刷新页面重试');
            }
        });
    });
}

async function renderReviewerGradingPage(data = {}) {
    console.log('renderReviewerGradingPage called with data:', data);
    console.log('Data type:', typeof data);
    console.log('Data keys:', Object.keys(data || {}));
    console.log('Data.processId:', data?.processId);
    console.log('Data.id:', data?.id);
    
    const studentName = data.student || data.studentName || '未知学生';
    // 确保processId是数字类型，并处理各种可能的输入
    let processId = data.processId || data.id || null;
    console.log('Initial processId:', processId, 'Type:', typeof processId);
    if (processId) {
        processId = parseInt(processId, 10);
        if (isNaN(processId)) {
            console.error('Invalid processId:', data.processId);
            processId = null;
        }
    }
    const topic = data.topic || data.thesisTitle || '未知课题';
    
    console.log('Parsed processId:', processId);
    
    // 如果processId无效，尝试从全局变量中获取，或者显示错误
    if (!processId) {
        console.error('processId is missing or invalid. Data received:', data);
        console.log('Attempting to use currentReviewerPapersList:', currentReviewerPapersList);
        
        // 如果全局变量中有数据，显示选择列表
        // 如果全局变量为空，尝试重新加载
        if ((!currentReviewerPapersList || currentReviewerPapersList.length === 0)) {
            console.log('currentReviewerPapersList is empty, attempting to reload...');
            // 尝试重新加载论文列表
            try {
                const papers = await fetchWithAuth('/api/reviewer/thesis/papers') || [];
                currentReviewerPapersList = papers;
                console.log('Reloaded papers:', currentReviewerPapersList);
            } catch (error) {
                console.error('Failed to reload papers:', error);
            }
        }
        
        if (currentReviewerPapersList && currentReviewerPapersList.length > 0) {
            console.log('Showing paper selection list as fallback');
            const container = document.getElementById('contentArea');
            container.innerHTML = `
                <div class="card p-8">
                    <h3 class="text-xl font-bold text-slate-900 mb-4">选择要评阅的论文</h3>
                    <p class="text-slate-600 mb-6">请从列表中选择要评阅的论文：</p>
                    <div class="space-y-3" id="fallback-paper-list">
                        ${currentReviewerPapersList.map((p, index) => {
                            return `
                                <div class="p-4 border border-slate-200 rounded-lg hover:bg-slate-50 cursor-pointer fallback-paper-item" 
                                     data-index="${index}">
                                    <div class="flex justify-between items-center">
                                        <div>
                                            <p class="font-semibold text-slate-900">${p.studentName || '未知'}</p>
                                            <p class="text-sm text-slate-600">${p.thesisTitle || '未确定'}</p>
                                        </div>
                                        <button class="px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700">
                                            ${p.hasScore ? '查看/修改' : '开始评阅'}
                                        </button>
                                    </div>
                                </div>
                            `;
                        }).join('')}
                    </div>
                </div>
            `;
            
            // 为备用列表添加事件监听器
            container.querySelectorAll('.fallback-paper-item').forEach((item, index) => {
                item.addEventListener('click', function() {
                    const p = currentReviewerPapersList[index];
                    if (!p || !p.processId) {
                        console.error('Invalid paper data at index:', index);
                        mockAction('数据错误，请刷新页面重试');
                        return;
                    }
                    const paperData = {
                        processId: p.processId,
                        id: p.processId,
                        student: p.studentName || '未知',
                        supervisor: p.supervisorName || '未分配',
                        topic: p.thesisTitle || '未确定',
                        studentName: p.studentName,
                        supervisorName: p.supervisorName,
                        thesisTitle: p.thesisTitle,
                        hasEvaluationForm: p.hasEvaluationForm,
                        hasScore: p.hasScore
                    };
                    console.log('Fallback: Paper data for grading page:', paperData);
                    switchPage('成绩评定', paperData);
                });
            });
            return;
        }
        
        // 如果没有数据，显示友好提示
        const container = document.getElementById('contentArea');
        container.innerHTML = `
            <div class="card p-8">
                <div class="text-center py-8">
                    <i class="fas fa-inbox text-6xl text-slate-300 mb-4"></i>
                    <h3 class="text-xl font-bold text-slate-900 mb-2">暂无待评阅论文</h3>
                    <p class="text-slate-600 mb-6">您当前没有被分配任何待评阅的论文。</p>
                    <p class="text-sm text-slate-400 mb-6">如果您认为应该有论文需要评阅，请联系管理员进行分配。</p>
                    <div class="flex gap-3 justify-center">
                        <button onclick="switchPage('论文评阅')" class="px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700">
                            查看论文列表
                        </button>
                        <button onclick="switchPage('评阅统计')" class="px-4 py-2 border border-slate-300 rounded-lg hover:bg-slate-50">
                            查看评阅统计
                        </button>
                    </div>
                </div>
            </div>
        `;
        return;
    }
    const scoreItems = [
        { key: 'literatureReview', label: '选题与文献综述', max: 20, apiKey: 'topicReview' },
        { key: 'innovation', label: '创新性', max: 10, apiKey: 'innovation' },
        { key: 'theoryKnowledge', label: '基础理论和专业知识', max: 40, apiKey: 'theoryKnowledge' },
        { key: 'writingAbility', label: '写作水平、写作规范、综合能力', max: 30, apiKey: 'writingSkill' },
    ];
    
    // 尝试加载已评分数据
    let existingScore = null;
    if (processId) {
        try {
            existingScore = await fetchWithAuth(`/api/reviewer/thesis/score/${processId}`) || null;
        } catch (error) {
            console.log('未找到已评分数据，将显示空白表单');
        }
    }
    // 根据processId获取材料列表（从API获取）
    const documents = [
        { name: '选题申报表', type: 'TOPIC_SELECTION', icon: 'fa-lightbulb', processId: processId },
        { name: '开题报告', type: 'OPENING_REPORT', icon: 'fa-book-open', processId: processId },
        { name: '中期报告', type: 'MID_TERM_REPORT', icon: 'fa-tasks', processId: processId },
        { name: '论文终稿', type: 'FINAL_PAPER', icon: 'fa-file-invoice', processId: processId },
    ];

    const container = document.getElementById('contentArea');
    container.innerHTML = `
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div class="lg:col-span-2 card p-8">
            <div class="flex justify-between items-center mb-6">
                 <div>
                    <h3 class="text-xl font-bold text-slate-900">评阅老师评分 - ${studentName}</h3>
                    <p class="text-sm text-slate-500 mt-1">${topic}</p>
                </div>
                 <button onclick="switchPage('论文评阅')" class="text-sm text-indigo-600 font-semibold hover:underline"><i class="fas fa-arrow-left mr-1"></i>返回列表</button>
            </div>
            
            <div class="space-y-5 pt-6 border-t">
                ${scoreItems.map(item => {
                    const existingValue = existingScore ? (existingScore[item.apiKey] || 0) : 0;
                    return `
                    <div class="flex items-center gap-4">
                        <label class="w-2/5 text-sm font-medium text-slate-700">${item.label}</label>
                        <input type="number" data-key="${item.key}" data-api-key="${item.apiKey}" max="${item.max}" min="0" 
                               value="${existingValue}" class="score-input w-24 border-slate-200 rounded-md text-center font-bold" 
                               placeholder="0" oninput="updateTotalScore()" />
                        <span class="text-sm text-slate-400">/ ${item.max}分</span>
                    </div>
                `;
                }).join('')}
                <div class="flex items-center gap-4 pt-5 border-t">
                     <label class="w-2/5 text-sm font-bold text-slate-800">总分</label>
                     <p id="totalScore" class="font-bold text-2xl text-indigo-600">0</p>
                     <span class="text-sm text-slate-400">/ 100分</span>
                </div>
            </div>
            <div class="text-right mt-8">
                <button id="submit-reviewer-score-btn" class="px-8 py-3 bg-indigo-600 text-white rounded-lg font-bold shadow-lg shadow-indigo-100">提交评分</button>
            </div>
        </div>
        <div class="space-y-6 self-start">
            <div class="card p-6">
                <h4 class="font-bold text-slate-900 mb-4">相关材料下载</h4>
                <div class="space-y-3">
                    ${documents.map(doc => `
                        <div class="p-3 bg-slate-50 rounded-lg flex items-center justify-between hover:bg-slate-100">
                            <div class="flex items-center gap-3">
                                <i class="fas ${doc.icon} text-indigo-500"></i>
                                <span class="text-sm font-medium text-slate-700">${doc.name}</span>
                            </div>
                            <div>
                                <button onclick="previewStudentMaterial(${doc.processId}, '${doc.type}')" class="text-slate-400 hover:text-indigo-600 w-6 h-6"><i class="fas fa-eye"></i></button>
                                <button onclick="downloadStudentMaterial(${doc.processId}, '${doc.type}')" class="text-slate-400 hover:text-indigo-600 w-6 h-6"><i class="fas fa-download"></i></button>
                            </div>
                        </div>
                    `).join('')}
                </div>
            </div>
            <div class="card p-6">
                <h4 class="font-bold text-slate-900 mb-4">上传评阅表</h4>
                <div id="evaluation-upload-zone" class="border-2 border-dashed border-slate-200 rounded-xl p-6 text-center flex flex-col items-center justify-center hover:border-indigo-300 cursor-pointer transition-colors group bg-slate-50/30">
                    <i class="fas fa-cloud-upload-alt text-3xl text-slate-300 group-hover:text-indigo-400 mb-2"></i>
                    <p id="evaluation-upload-text" class="text-xs font-medium text-slate-500">点击或拖拽文件上传</p>
                    <p class="text-[10px] text-slate-400 mt-1">支持 PDF, DOC, DOCX 格式，最大 10MB</p>
                    <input type="file" id="evaluation-file-input" class="hidden" accept=".pdf,.doc,.docx">
                </div>
                <button id="upload-evaluation-btn" class="w-full mt-3 py-2 bg-indigo-600 text-white rounded-lg text-sm font-bold hover:bg-indigo-700">上传评阅表</button>
            </div>
        </div>
    </div>
    `;

    // 实现评阅表上传功能
    const evaluationUploadZone = document.getElementById('evaluation-upload-zone');
    const evaluationFileInput = document.getElementById('evaluation-file-input');
    const evaluationUploadText = document.getElementById('evaluation-upload-text');
    const uploadEvaluationBtn = document.getElementById('upload-evaluation-btn');
    let evaluationFile = null;

    evaluationUploadZone.addEventListener('click', () => evaluationFileInput.click());

    evaluationFileInput.addEventListener('change', () => {
        if (evaluationFileInput.files.length > 0) {
            evaluationFile = evaluationFileInput.files[0];
            evaluationUploadText.textContent = `已选择: ${evaluationFile.name}`;
            evaluationUploadZone.classList.add('border-indigo-400');
        }
    });

    // 拖拽上传
    ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
        evaluationUploadZone.addEventListener(eventName, (e) => {
            e.preventDefault();
            e.stopPropagation();
        }, false);
    });
    ['dragenter', 'dragover'].forEach(eventName => {
        evaluationUploadZone.addEventListener(eventName, () => evaluationUploadZone.classList.add('border-indigo-400'), false);
    });
    ['dragleave', 'drop'].forEach(eventName => {
        evaluationUploadZone.addEventListener(eventName, () => evaluationUploadZone.classList.remove('border-indigo-400'), false);
    });

    evaluationUploadZone.addEventListener('drop', (e) => {
        if (e.dataTransfer.files.length > 0) {
            evaluationFile = e.dataTransfer.files[0];
            evaluationFileInput.files = e.dataTransfer.files;
            evaluationUploadText.textContent = `已选择: ${evaluationFile.name}`;
        }
    });

    uploadEvaluationBtn.addEventListener('click', async () => {
        // 确保processId可用 - 从闭包中获取
        const currentProcessId = processId;
        console.log('Upload evaluation form clicked, processId:', currentProcessId);
        if (!currentProcessId) {
            mockAction('缺少必要参数（processId）');
            console.error('processId is missing when uploading evaluation form. Available processId:', processId);
            return;
        }
        
        if (!evaluationFile) {
            mockAction('请先选择文件');
            return;
        }

        // 文件类型和大小校验
        const allowedTypes = ['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'];
        if (!allowedTypes.includes(evaluationFile.type)) {
            mockAction('仅支持 PDF 或 Word 文件');
            return;
        }
        if (evaluationFile.size > 10 * 1024 * 1024) {
            mockAction('文件大小不能超过 10MB');
            return;
        }

        try {
            uploadEvaluationBtn.disabled = true;
            uploadEvaluationBtn.classList.add('opacity-60', 'cursor-not-allowed');
            mockAction('正在上传评阅表...');

            const formData = new FormData();
            formData.append('file', evaluationFile);

            await fetchWithAuth(`/api/reviewer/thesis/evaluation-form/${currentProcessId}`, {
                method: 'POST',
                body: formData
            });

            mockAction('评阅表上传成功！');
            // 重置
            evaluationFile = null;
            evaluationFileInput.value = '';
            evaluationUploadText.textContent = '点击或拖拽文件上传';
            evaluationUploadZone.classList.remove('border-indigo-400');
        } catch (error) {
            console.error('Upload failed:', error);
            mockAction(`上传失败: ${error.message}`);
        } finally {
            uploadEvaluationBtn.disabled = false;
            uploadEvaluationBtn.classList.remove('opacity-60', 'cursor-not-allowed');
        }
    });

    // 初始化总分显示
    if (existingScore) {
        updateTotalScore();
    }
    
    document.getElementById('submit-reviewer-score-btn').addEventListener('click', async () => {
        // 确保processId可用 - 从闭包中获取
        const currentProcessId = processId;
        console.log('Submit score clicked, processId:', currentProcessId);
        if (!currentProcessId) {
            mockAction('缺少必要参数（processId）');
            console.error('processId is missing when submitting score. Available processId:', processId);
            return;
        }
        
        const scores = {};
        const inputs = document.querySelectorAll('.score-input');
        let totalScore = 0;
        inputs.forEach(input => {
            const score = parseInt(input.value, 10) || 0;
            const apiKey = input.dataset.apiKey || input.dataset.key;
            scores[apiKey] = score;
            totalScore += score;
        });

        if (totalScore <= 0) {
            mockAction('请填写评分');
            return;
        }

        try {
            mockAction('正在提交评分...');
            // 根据API文档，使用正确的接口和参数名
            await fetchWithAuth('/api/reviewer/thesis/score', {
                method: 'POST',
                body: JSON.stringify({
                    processId: currentProcessId,
                    topicReview: scores.topicReview || 0,
                    innovation: scores.innovation || 0,
                    theoryKnowledge: scores.theoryKnowledge || 0,
                    writingSkill: scores.writingSkill || 0
                })
            });
            mockAction('评分提交成功！');
            await loadUserData();
            switchPage('论文评阅');
        } catch (error) {
            console.error('Score submission failed:', error);
            mockAction(`评分提交失败: ${error.message}`);
        }
    });
}

// 预览学生材料
async function previewStudentMaterial(processId, materialType) {
    // 确保processId是数字类型
    const processIdNum = parseInt(processId, 10);
    if (!processIdNum || isNaN(processIdNum)) {
        console.error('Invalid processId for preview:', processId);
        mockAction('无效的参数');
        return;
    }
    try {
        const url = `${API_BASE_URL}/api/reviewer/thesis/student/material/${processIdNum}/${materialType}/preview`;
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
            mockAction('预览失败');
        }
    } catch (error) {
        console.error('Preview failed:', error);
        mockAction('预览失败');
    }
}

// 下载学生材料
async function downloadStudentMaterial(processId, materialType) {
    // 确保processId是数字类型
    const processIdNum = parseInt(processId, 10);
    if (!processIdNum || isNaN(processIdNum)) {
        console.error('Invalid processId for download:', processId);
        mockAction('无效的参数');
        return;
    }
    try {
        const url = `${API_BASE_URL}/api/reviewer/thesis/student/material/${processIdNum}/${materialType}/download`;
        const cleanToken = getCleanToken();
        const response = await fetch(url, {
            headers: {
                'Authorization': `Bearer ${cleanToken}`
            }
        });
        
        if (response.ok) {
            const blob = await response.blob();
            const downloadUrl = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = downloadUrl;
            a.download = `${materialType}_${processId}`;
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            window.URL.revokeObjectURL(downloadUrl);
            mockAction('下载成功');
        } else {
            mockAction('下载失败');
        }
    } catch (error) {
        console.error('Download failed:', error);
        mockAction('下载失败');
    }
}

async function renderReviewStatisticsPage() {
    // 从API获取评阅统计信息
    let stats = {
        totalReviewed: 0,
        avgScore: 0,
        pending: 0,
        excellent: 0,
    };
    try {
        stats = await fetchWithAuth('/api/reviewer/thesis/statistics') || stats;
    } catch (error) {
        console.error('Failed to load review statistics:', error);
        mockAction('加载统计信息失败');
    }
    const container = document.getElementById('contentArea');
    container.innerHTML = `
        <div class="card p-8">
            <h3 class="text-xl font-bold text-slate-900 mb-6">评阅统计</h3>
            <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
                <div class="p-6 bg-indigo-50 rounded-xl text-center">
                    <p class="text-3xl font-bold text-indigo-600 mb-1">${stats.totalReviewed}</p>
                    <p class="text-sm text-slate-600">已评阅论文数</p>
                </div>
                <div class="p-6 bg-emerald-50 rounded-xl text-center">
                    <p class="text-3xl font-bold text-emerald-600 mb-1">${stats.avgScore ? stats.avgScore.toFixed(1) : '0.0'}</p>
                    <p class="text-sm text-slate-600">平均分</p>
                </div>
                <div class="p-6 bg-amber-50 rounded-xl text-center">
                    <p class="text-3xl font-bold text-amber-600 mb-1">${stats.pending}</p>
                    <p class="text-sm text-slate-600">待评阅</p>
                </div>
                <div class="p-6 bg-rose-50 rounded-xl text-center">
                    <p class="text-3xl font-bold text-rose-600 mb-1">${stats.excellent}</p>
                    <p class="text-sm text-slate-600">优秀论文</p>
                </div>
            </div>
        </div>
    `;
}