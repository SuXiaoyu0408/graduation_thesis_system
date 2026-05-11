// ================= Student Role Page Renderers =================



function getStatusInfo(status) {
    let label = "未提交", color = "text-slate-400 bg-slate-100";
    if (status === 'pending') { label = "待审核"; color = "text-amber-500 bg-amber-50"; }
    else if (status === 'approved') { label = "已通过"; color = "text-emerald-500 bg-emerald-50"; }
    else if (status === 'rejected') { label = "被驳回"; color = "text-rose-500 bg-rose-50"; }
    return { label, color };
}

function renderMyTopicPage() {
    const { label: statusLabel, color: statusColor } = getStatusInfo(myTopicData.status);
    const container = document.getElementById('contentArea');
    // Keep the original detailed HTML structure from management_system.html
    container.innerHTML = `
        <div class="space-y-6">
            <div class="card p-8">
                <div class="flex justify-between items-start mb-6">
                    <div class="space-y-2 flex-1">
                        <div class="flex items-center gap-3 mb-2">
                            <h3 class="text-xl font-bold text-slate-900">选题报告管理</h3>
                            <span class="px-3 py-1 rounded-full text-xs font-bold ${statusColor}">${statusLabel}</span>
                        </div>
                        <div class="grid grid-cols-1 md:grid-cols-2 gap-x-12 gap-y-3 pt-2">
                            <div class="flex items-start gap-2">
                                <span class="text-sm text-slate-400 whitespace-nowrap">课题名称：</span>
                                <span class="text-sm font-semibold text-slate-700">${myTopicData.title || '未确定'}</span>
                            </div>
                            <div class="flex items-center gap-2">
                                <span class="text-sm text-slate-400">指导老师：</span>
                                <span class="text-sm font-semibold text-slate-700">${myTopicData.supervisor || '未分配'}</span>
                            </div>
                        </div>
                    </div>
                </div>
                ${myTopicData.status === 'rejected' ? `
                    <div class="mb-8 p-4 bg-rose-50 border-l-4 border-rose-500 rounded-r-lg">
                        <p class="text-xs font-bold text-rose-600 uppercase mb-1"><i class="fas fa-exclamation-circle mr-1"></i> 指导老师驳回原因</p>
                        <p class="text-sm text-rose-800 leading-relaxed">${myTopicData.rejectReason}</p>
                    </div>
                ` : ''}
                <div class="flex flex-col md:flex-row gap-6">
                    <div id="drop-zone" class="flex-1 border-2 border-dashed border-slate-200 rounded-xl p-10 text-center flex flex-col items-center justify-center hover:border-indigo-300 transition-colors group bg-slate-50/30">
                        <i class="fas fa-cloud-upload-alt text-4xl text-slate-300 group-hover:text-indigo-400 mb-3"></i>
                        <p id="drop-zone-text" class="text-sm font-medium text-slate-600">点击或拖拽新版本 PDF 或 Word 报告进行提交</p>
                        <p class="text-xs text-slate-400 mt-2">文件限制：支持 PDF, DOC, DOCX 格式，最大 10MB</p>
                        <input type="file" id="file-input" class="hidden" accept=".pdf,.doc,.docx">
                    </div>
                    <div class="md:w-56 flex flex-col justify-center">
                        <button id="upload-button" class="w-full py-4 bg-indigo-600 text-white rounded-lg font-bold text-sm hover:bg-indigo-700 transition-all shadow-lg shadow-indigo-100">提交新版本</button>
                        <p class="text-[10px] text-slate-400 text-center mt-3 leading-relaxed">提交后需等待指导老师<br>重新审核</p>
                    </div>
                </div>
            </div>
            <div class="card p-8">
                <h3 class="text-lg font-bold text-slate-800 mb-4">提交历史</h3>
                <div id="history-list" class="space-y-1">
                    ${(myTopicData.history && myTopicData.history.length > 0) ? myTopicData.history.map(file => `
                        <div class="flex items-center justify-between p-4 bg-slate-50 rounded-xl">
                            <div class="min-w-0 flex-1">
                                <p class="text-sm font-bold text-slate-700 truncate">${file.originalFilename}</p>
                                <p class="text-xs text-slate-400">${file.time} · ${file.size}</p>
                            </div>
                            <button onclick='downloadHistoryFile(${file.historyId}, ${JSON.stringify(file.originalFilename || '')})' class="text-indigo-600 hover:text-indigo-800 text-sm font-bold"><i class="fas fa-download mr-1"></i>下载</button>
                            <button onclick='event.stopPropagation();deleteHistoryFile(${file.historyId}, async () => { await loadUserData(); renderMyTopicPage(); })' class="text-rose-500 hover:text-rose-700 text-sm font-bold ml-3"><i class="fas fa-trash-alt mr-1"></i>删除</button>
                        </div>
                    `).join('') : '<p class="text-slate-400 text-sm text-center py-4 italic">暂无历史提交记录</p>'}
                </div>
            </div>
        </div>
    `;

    // --- Event Listeners & File Handling Logic ---
    const dropZone = document.getElementById('drop-zone');
    const fileInput = document.getElementById('file-input');
    const uploadButton = document.getElementById('upload-button');
    const dropZoneText = document.getElementById('drop-zone-text');
    let selectedFile = null;

    dropZone.addEventListener('click', () => fileInput.click());

    fileInput.addEventListener('change', () => {
        if (fileInput.files.length > 0) {
            selectedFile = fileInput.files[0];
            dropZoneText.textContent = `已选择文件: ${selectedFile.name}`;
            dropZone.classList.add('border-indigo-400');
        }
    });

    // Drag and Drop functionality
    ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
        dropZone.addEventListener(eventName, (e) => {
            e.preventDefault();
            e.stopPropagation();
        }, false);
    });
    ['dragenter', 'dragover'].forEach(eventName => {
        dropZone.addEventListener(eventName, () => dropZone.classList.add('border-indigo-400'), false);
    });
    ['dragleave', 'drop'].forEach(eventName => {
        dropZone.addEventListener(eventName, () => dropZone.classList.remove('border-indigo-400'), false);
    });

    dropZone.addEventListener('drop', (e) => {
        const dt = e.dataTransfer;
        if (dt.files.length > 0) {
            selectedFile = dt.files[0];
            fileInput.files = dt.files; // Sync with file input
            dropZoneText.textContent = `已选择文件: ${selectedFile.name}`;
        }
    });

    uploadButton.addEventListener('click', async (event) => {
        event.preventDefault(); // 明确阻止任何默认行为
        event.stopPropagation();

        if (!selectedFile) {
            mockAction('请先选择一个文件');
            return;
        }

        // 前端校验文件类型与大小 (<=10MB, PDF / Word)
        const allowedTypes = ['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'];
        if (!allowedTypes.includes(selectedFile.type)) {
            mockAction('仅支持 PDF 或 Word 文件');
            return;
        }
        if (selectedFile.size > 10 * 1024 * 1024) { // 10MB
            mockAction('文件大小不能超过 10MB');
            return;
        }

        const formData = new FormData();
        formData.append('file', selectedFile);

        try {
            if (!currentProcessId) {
                mockAction('未找到论文流程，请先创建流程');
                return;
            }

            // 禁用按钮避免重复点击
            uploadButton.disabled = true;
            uploadButton.classList.add('opacity-60', 'cursor-not-allowed');
            mockAction('正在上传...');

            await fetchWithAuth(`/api/student/thesis/material/${currentProcessId}/TOPIC_SELECTION`, {
                method: 'POST',
                body: formData
            });

            mockAction('上传成功！');
            // 重置选择
            selectedFile = null;
            fileInput.value = '';
            dropZoneText.textContent = '点击或拖拽新版本 PDF 或 Word 报告进行提交';

            // 重新加载数据并刷新页面
            await loadUserData();
            renderMyTopicPage();
        } catch (error) {
            mockAction(`上传失败: ${error.message}`);
            console.error('Upload failed:', error);
        } finally {
            uploadButton.disabled = false;
            uploadButton.classList.remove('opacity-60', 'cursor-not-allowed');
        }
    });
}



function renderProposalPage() {
    const { label: statusLabel, color: statusColor } = getStatusInfo(myProposalData.status);
    const container = document.getElementById('contentArea');
    container.innerHTML = `
        <div class="space-y-6">
            <div class="card p-8">
                <div class="flex justify-between items-start mb-6">
                    <div class="space-y-2 flex-1">
                        <div class="flex items-center gap-3 mb-2">
                            <h3 class="text-xl font-bold text-slate-900">开题报告提交</h3>
                            <span class="px-3 py-1 rounded-full text-xs font-bold ${statusColor}">${statusLabel}</span>
                        </div>
                        <div class="grid grid-cols-1 md:grid-cols-2 gap-x-12 gap-y-3 pt-2">
                            <div class="flex items-start gap-2">
                                <span class="text-sm text-slate-400 whitespace-nowrap">课题名称：</span>
                                <span class="text-sm font-semibold text-slate-700">${myProposalData.title || '未确定'}</span>
                            </div>
                            <div class="flex items-center gap-2">
                                <span class="text-sm text-slate-400">指导老师：</span>
                                <span class="text-sm font-semibold text-slate-700">${myProposalData.supervisor || '未分配'}</span>
                            </div>
                        </div>
                    </div>
                </div>
                ${myProposalData.status === 'rejected' ? `
                    <div class="mb-8 p-4 bg-rose-50 border-l-4 border-rose-500 rounded-r-lg">
                        <p class="text-xs font-bold text-rose-600 uppercase mb-1"><i class="fas fa-exclamation-circle mr-1"></i> 驳回原因</p>
                        <p class="text-sm text-rose-800 leading-relaxed">${myProposalData.rejectReason}</p>
                    </div>
                ` : ''}
                <div class="flex flex-col md:flex-row gap-6">
                    <div id="drop-zone-proposal" class="flex-1 border-2 border-dashed border-slate-200 rounded-xl p-10 text-center flex flex-col items-center justify-center hover:border-indigo-300 transition-colors group bg-slate-50/30">
                        <i class="fas fa-cloud-upload-alt text-4xl text-slate-300 group-hover:text-indigo-400 mb-3"></i>
                        <p id="drop-zone-text-proposal" class="text-sm font-medium text-slate-600">点击或拖拽 PDF 或 Word 开题报告进行提交</p>
                        <p class="text-xs text-slate-400 mt-2">文件限制：支持 PDF, DOC, DOCX 格式，最大 15MB</p>
                        <input type="file" id="file-input-proposal" class="hidden" accept=".pdf,.doc,.docx">
                    </div>
                    <div class="md:w-56 flex flex-col justify-center">
                        <button id="upload-button-proposal" class="w-full py-4 bg-indigo-600 text-white rounded-lg font-bold text-sm hover:bg-indigo-700 transition-all shadow-lg shadow-indigo-100">立即提交</button>
                    </div>
                </div>
            </div>
            <div class="card p-8">
                <h3 class="text-lg font-bold text-slate-800 mb-4">提交历史</h3>
                <div class="space-y-1">
                    ${(myProposalData.history && myProposalData.history.length > 0) ? myProposalData.history.map(file => `
                        <div class="flex items-center justify-between p-4 bg-slate-50 rounded-xl">
                            <div class="min-w-0 flex-1">
                                <p class="text-sm font-bold text-slate-700 truncate">${file.originalFilename}</p>
                                <p class="text-xs text-slate-400">${file.time} · ${file.size}</p>
                            </div>
                            <button onclick='downloadHistoryFile(${file.historyId}, ${JSON.stringify(file.originalFilename || '')})' class="text-indigo-600 hover:text-indigo-800 text-sm font-bold"><i class="fas fa-download mr-1"></i>下载</button>
                            <button onclick='event.stopPropagation();deleteHistoryFile(${file.historyId}, async () => { await loadUserData(); renderProposalPage(); })' class="text-rose-500 hover:text-rose-700 text-sm font-bold ml-3"><i class="fas fa-trash-alt mr-1"></i>删除</button>
                        </div>
                    `).join('') : '<p class="text-slate-400 text-sm text-center py-4 italic">暂无历史提交记录</p>'}
                </div>
            </div>
        </div>
    `;

    // --- Event Listeners & File Handling Logic ---
    const dropZone = document.getElementById('drop-zone-proposal');
    const fileInput = document.getElementById('file-input-proposal');
    const uploadButton = document.getElementById('upload-button-proposal');
    const dropZoneText = document.getElementById('drop-zone-text-proposal');
    let selectedFile = null;

    dropZone.addEventListener('click', () => fileInput.click());

    fileInput.addEventListener('change', () => {
        if (fileInput.files.length > 0) {
            selectedFile = fileInput.files[0];
            dropZoneText.textContent = `已选择文件: ${selectedFile.name}`;
            dropZone.classList.add('border-indigo-400');
        }
    });

    ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
        dropZone.addEventListener(eventName, (e) => { e.preventDefault(); e.stopPropagation(); }, false);
    });
    ['dragenter', 'dragover'].forEach(eventName => {
        dropZone.addEventListener(eventName, () => dropZone.classList.add('border-indigo-400'), false);
    });
    ['dragleave', 'drop'].forEach(eventName => {
        dropZone.addEventListener(eventName, () => dropZone.classList.remove('border-indigo-400'), false);
    });

    dropZone.addEventListener('drop', (e) => {
        if (e.dataTransfer.files.length > 0) {
            selectedFile = e.dataTransfer.files[0];
            fileInput.files = e.dataTransfer.files;
            dropZoneText.textContent = `已选择文件: ${selectedFile.name}`;
        }
    });

    uploadButton.addEventListener('click', async (event) => {
        event.preventDefault(); // 明确阻止任何默认行为
        if (!selectedFile) {
            mockAction('请先选择一个文件');
            return;
        }
        const formData = new FormData();
        formData.append('file', selectedFile);
        try {
            // 获取当前流程ID
            if (!currentProcessId) {
                mockAction('未找到论文流程，请先创建流程');
                return;
            }
            
            mockAction('正在上传开题报告...');
            await fetchWithAuth(`/api/student/thesis/material/${currentProcessId}/OPENING_REPORT`, {
                method: 'POST',
                body: formData
            });
            
            mockAction('上传成功！');
            await loadUserData();
            renderProposalPage();
        } catch (error) {
            mockAction(`上传失败: ${error.message}`);
            console.error('Upload failed:', error);
        }
    });
}

function renderMidtermPage() {
    const { label: statusLabel, color: statusColor } = getStatusInfo(myMidtermData.status);
    const container = document.getElementById('contentArea');
    container.innerHTML = `
        <div class="space-y-6">
            <div class="card p-8">
                <div class="flex justify-between items-start mb-6">
                    <div class="space-y-2 flex-1">
                        <div class="flex items-center gap-3 mb-2">
                            <h3 class="text-xl font-bold text-slate-900">中期检查提交</h3>
                            <span class="px-3 py-1 rounded-full text-xs font-bold ${statusColor}">${statusLabel}</span>
                        </div>
                        <div class="grid grid-cols-1 md:grid-cols-2 gap-x-12 gap-y-3 pt-2">
                            <div class="flex items-start gap-2">
                                <span class="text-sm text-slate-400 whitespace-nowrap">课题名称：</span>
                                <span class="text-sm font-semibold text-slate-700">${myMidtermData.title || '未确定'}</span>
                            </div>
                            <div class="flex items-center gap-2">
                                <span class="text-sm text-slate-400">指导老师：</span>
                                <span class="text-sm font-semibold text-slate-700">${myMidtermData.supervisor || '未分配'}</span>
                            </div>
                        </div>
                    </div>
                </div>
                ${myMidtermData.status === 'rejected' ? `
                    <div class="mb-8 p-4 bg-rose-50 border-l-4 border-rose-500 rounded-r-lg">
                        <p class="text-xs font-bold text-rose-600 uppercase mb-1"><i class="fas fa-exclamation-circle mr-1"></i> 驳回原因</p>
                        <p class="text-sm text-rose-800 leading-relaxed">${myMidtermData.rejectReason}</p>
                    </div>
                ` : ''}
                <div class="flex flex-col md:flex-row gap-6">
                    <div id="drop-zone-midterm" class="flex-1 border-2 border-dashed border-slate-200 rounded-xl p-10 text-center flex flex-col items-center justify-center hover:border-indigo-300 transition-colors group bg-slate-50/30">
                        <i class="fas fa-cloud-upload-alt text-4xl text-slate-300 group-hover:text-indigo-400 mb-3"></i>
                        <p id="drop-zone-text-midterm" class="text-sm font-medium text-slate-600">点击或拖拽 PDF 或 Word 中期检查报告进行提交</p>
                        <p class="text-xs text-slate-400 mt-2">文件限制：支持 PDF, DOC, DOCX 格式，最大 15MB</p>
                        <input type="file" id="file-input-midterm" class="hidden" accept=".pdf,.doc,.docx">
                    </div>
                    <div class="md:w-56 flex flex-col justify-center">
                        <button id="upload-button-midterm" class="w-full py-4 bg-indigo-600 text-white rounded-lg font-bold text-sm hover:bg-indigo-700 transition-all shadow-lg shadow-indigo-100">立即提交</button>
                    </div>
                </div>
            </div>
            <div class="card p-8">
                <h3 class="text-lg font-bold text-slate-800 mb-4">提交历史</h3>
                <div class="space-y-1">
                    ${(myMidtermData.history && myMidtermData.history.length > 0) ? myMidtermData.history.map(file => `
                        <div class="flex items-center justify-between p-4 bg-slate-50 rounded-xl">
                            <div class="min-w-0 flex-1">
                                <p class="text-sm font-bold text-slate-700 truncate">${file.originalFilename}</p>
                                <p class="text-xs text-slate-400">${file.time} · ${file.size}</p>
                            </div>
                            <button onclick='downloadHistoryFile(${file.historyId}, ${JSON.stringify(file.originalFilename || '')})' class="text-indigo-600 hover:text-indigo-800 text-sm font-bold"><i class="fas fa-download mr-1"></i>下载</button>
                            <button onclick='event.stopPropagation();deleteHistoryFile(${file.historyId}, async () => { await loadUserData(); renderMidtermPage(); })' class="text-rose-500 hover:text-rose-700 text-sm font-bold ml-3"><i class="fas fa-trash-alt mr-1"></i>删除</button>
                        </div>
                    `).join('') : '<p class="text-slate-400 text-sm text-center py-4 italic">暂无历史提交记录</p>'}
                </div>
            </div>
        </div>
    `;

    // --- Event Listeners & File Handling Logic ---
    const dropZone = document.getElementById('drop-zone-midterm');
    const fileInput = document.getElementById('file-input-midterm');
    const uploadButton = document.getElementById('upload-button-midterm');
    const dropZoneText = document.getElementById('drop-zone-text-midterm');
    let selectedFile = null;

    dropZone.addEventListener('click', () => fileInput.click());

    fileInput.addEventListener('change', () => {
        if (fileInput.files.length > 0) {
            selectedFile = fileInput.files[0];
            dropZoneText.textContent = `已选择文件: ${selectedFile.name}`;
            dropZone.classList.add('border-indigo-400');
        }
    });

    ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
        dropZone.addEventListener(eventName, (e) => { e.preventDefault(); e.stopPropagation(); }, false);
    });
    ['dragenter', 'dragover'].forEach(eventName => {
        dropZone.addEventListener(eventName, () => dropZone.classList.add('border-indigo-400'), false);
    });
    ['dragleave', 'drop'].forEach(eventName => {
        dropZone.addEventListener(eventName, () => dropZone.classList.remove('border-indigo-400'), false);
    });

    dropZone.addEventListener('drop', (e) => {
        if (e.dataTransfer.files.length > 0) {
            selectedFile = e.dataTransfer.files[0];
            fileInput.files = e.dataTransfer.files;
            dropZoneText.textContent = `已选择文件: ${selectedFile.name}`;
        }
    });

    uploadButton.addEventListener('click', async (event) => {
        event.preventDefault(); // 明确阻止任何默认行为
        if (!selectedFile) {
            mockAction('请先选择一个文件');
            return;
        }
        const formData = new FormData();
        formData.append('file', selectedFile);
        try {
            // 获取当前流程ID
            if (!currentProcessId) {
                mockAction('未找到论文流程，请先创建流程');
                return;
            }
            
            mockAction('正在上传中期报告...');
            await fetchWithAuth(`/api/student/thesis/material/${currentProcessId}/MID_TERM_REPORT`, {
                method: 'POST',
                body: formData
            });
            
            mockAction('上传成功！');
            await loadUserData();
            renderMidtermPage();
        } catch (error) {
            mockAction(`上传失败: ${error.message}`);
            console.error('Upload failed:', error);
        }
    });
}

// 下载历史文件
async function downloadHistoryFile(historyId, knownFilename) {
    mockAction('正在准备下载...');
    try {
        const url = `${API_BASE_URL}/api/student/thesis/material/history/${historyId}/download`;
        const cleanToken = getCleanToken();
        const response = await fetch(url, {
            headers: {
                'Authorization': `Bearer ${cleanToken}`
            }
        });

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ message: '下载失败' }));
            throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
        }

        // 确定文件名优先级：传入的原始文件名 > Content-Disposition头 > 默认名
        let filename = knownFilename || null;

        if (!filename) {
            const disposition = response.headers.get('Content-Disposition');
            if (disposition) {
                const utf8FilenameMatch = disposition.match(/filename\*=UTF-8''([^;]+)/);
                if (utf8FilenameMatch && utf8FilenameMatch[1]) {
                    filename = decodeURIComponent(utf8FilenameMatch[1]);
                } else {
                    const asciiFilenameMatch = disposition.match(/filename="([^"]+)"/);
                    if (asciiFilenameMatch && asciiFilenameMatch[1]) {
                        filename = asciiFilenameMatch[1];
                    }
                }
            }
        }

        if (!filename) {
            filename = `文件_${historyId}.tmp`;
        }

        const blob = await response.blob();
        const downloadUrl = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.style.display = 'none';
        a.href = downloadUrl;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(downloadUrl);
        mockAction('下载已开始');

    } catch (error) {
        console.error('Download failed:', error);
        mockAction(`下载失败: ${error.message}`);
    }
}

function renderThesisPage() {
    const { label: statusLabel, color: statusColor } = getStatusInfo(myThesisData.status);
    const container = document.getElementById('contentArea');
    container.innerHTML = `
        <div class="space-y-6">
            <div class="card p-8">
                <div class="flex justify-between items-start mb-6">
                    <div class="space-y-2 flex-1">
                        <div class="flex items-center gap-3 mb-2">
                            <h3 class="text-xl font-bold text-slate-900">论文终稿提交</h3>
                            <span class="px-3 py-1 rounded-full text-xs font-bold ${statusColor}">${statusLabel}</span>
                        </div>
                        <div class="grid grid-cols-1 md:grid-cols-2 gap-x-12 gap-y-3 pt-2">
                            <div class="flex items-start gap-2">
                                <span class="text-sm text-slate-400 whitespace-nowrap">课题名称：</span>
                                <span class="text-sm font-semibold text-slate-700">${myThesisData.title || '未确定'}</span>
                            </div>
                            <div class="flex items-center gap-2">
                                <span class="text-sm text-slate-400">指导老师：</span>
                                <span class="text-sm font-semibold text-slate-700">${myThesisData.supervisor || '未分配'}</span>
                            </div>
                        </div>
                    </div>
                </div>
                ${myThesisData.status === 'rejected' ? `
                    <div class="mb-8 p-4 bg-rose-50 border-l-4 border-rose-500 rounded-r-lg">
                        <p class="text-xs font-bold text-rose-600 uppercase mb-1"><i class="fas fa-exclamation-circle mr-1"></i> 驳回原因</p>
                        <p class="text-sm text-rose-800 leading-relaxed">${myThesisData.rejectReason}</p>
                    </div>
                ` : ''}
                <div class="flex flex-col md:flex-row gap-6">
                    <div id="drop-zone-thesis" class="flex-1 border-2 border-dashed border-slate-200 rounded-xl p-10 text-center flex flex-col items-center justify-center hover:border-indigo-300 transition-colors group bg-slate-50/30">
                        <i class="fas fa-cloud-upload-alt text-4xl text-slate-300 group-hover:text-indigo-400 mb-3"></i>
                        <p id="drop-zone-text-thesis" class="text-sm font-medium text-slate-600">点击或拖拽 PDF 或 Word 论文终稿进行提交</p>
                        <p class="text-xs text-slate-400 mt-2">文件限制：支持 PDF, DOC, DOCX 格式，最大 20MB</p>
                        <input type="file" id="file-input-thesis" class="hidden" accept=".pdf,.doc,.docx">
                    </div>
                    <div class="md:w-56 flex flex-col justify-center">
                        <button id="upload-button-thesis" class="w-full py-4 bg-indigo-600 text-white rounded-lg font-bold text-sm hover:bg-indigo-700 transition-all shadow-lg shadow-indigo-100">立即提交</button>
                    </div>
                </div>
            </div>
            <div class="card p-8">
                <h3 class="text-lg font-bold text-slate-800 mb-4">提交历史</h3>
                <div class="space-y-1">
                    ${(myThesisData.history && myThesisData.history.length > 0) ? myThesisData.history.map(file => `
                        <div class="flex items-center justify-between p-4 bg-slate-50 rounded-xl">
                            <div class="min-w-0 flex-1">
                                <p class="text-sm font-bold text-slate-700 truncate">${file.originalFilename}</p>
                                <p class="text-xs text-slate-400">${file.time} · ${file.size}</p>
                            </div>
                            <button onclick='downloadHistoryFile(${file.historyId}, ${JSON.stringify(file.originalFilename || '')})' class="text-indigo-600 hover:text-indigo-800 text-sm font-bold"><i class="fas fa-download mr-1"></i>下载</button>
                            <button onclick='event.stopPropagation();deleteHistoryFile(${file.historyId}, async () => { await loadUserData(); renderThesisPage(); })' class="text-rose-500 hover:text-rose-700 text-sm font-bold ml-3"><i class="fas fa-trash-alt mr-1"></i>删除</button>
                        </div>
                    `).join('') : '<p class="text-slate-400 text-sm text-center py-4 italic">暂无历史提交记录</p>'}
                </div>
            </div>
        </div>
    `;

    // --- Event Listeners & File Handling Logic ---
    const dropZone = document.getElementById('drop-zone-thesis');
    const fileInput = document.getElementById('file-input-thesis');
    const uploadButton = document.getElementById('upload-button-thesis');
    const dropZoneText = document.getElementById('drop-zone-text-thesis');
    let selectedFile = null;

    dropZone.addEventListener('click', () => fileInput.click());

    fileInput.addEventListener('change', () => {
        if (fileInput.files.length > 0) {
            selectedFile = fileInput.files[0];
            dropZoneText.textContent = `已选择文件: ${selectedFile.name}`;
            dropZone.classList.add('border-indigo-400');
        }
    });

    ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
        dropZone.addEventListener(eventName, (e) => { e.preventDefault(); e.stopPropagation(); }, false);
    });
    ['dragenter', 'dragover'].forEach(eventName => {
        dropZone.addEventListener(eventName, () => dropZone.classList.add('border-indigo-400'), false);
    });
    ['dragleave', 'drop'].forEach(eventName => {
        dropZone.addEventListener(eventName, () => dropZone.classList.remove('border-indigo-400'), false);
    });

    dropZone.addEventListener('drop', (e) => {
        if (e.dataTransfer.files.length > 0) {
            selectedFile = e.dataTransfer.files[0];
            fileInput.files = e.dataTransfer.files;
            dropZoneText.textContent = `已选择文件: ${selectedFile.name}`;
        }
    });

    uploadButton.addEventListener('click', async (event) => {
        event.preventDefault(); // 明确阻止任何默认行为
        if (!selectedFile) {
            mockAction('请先选择一个文件');
            return;
        }
        const formData = new FormData();
        formData.append('file', selectedFile);
        try {
            // 获取当前流程ID
            if (!currentProcessId) {
                mockAction('未找到论文流程，请先创建流程');
                return;
            }
            
            mockAction('正在上传论文终稿...');
            await fetchWithAuth(`/api/student/thesis/material/${currentProcessId}/FINAL_PAPER`, {
                method: 'POST',
                body: formData
            });
            
            mockAction('上传成功！');
            await loadUserData();
            renderThesisPage();
        } catch (error) {
            mockAction(`上传失败: ${error.message}`);
            console.error('Upload failed:', error);
        }
    });
}

function renderDefensePage() {
    // 使用统一的答辩安排页面
    renderDefenseArrangementPage();
}
