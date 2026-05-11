// ================= API & Auth Helper =================
const API_BASE_URL = 'http://localhost:8080';

/**
 * 获取token，清除空白字符和不可见控制字符
 * 与后端JwtUtil.cleanToken()保持一致，只保留JWT有效字符（Base64URL字符和点号）
 */
function getCleanToken() {
    const token = localStorage.getItem('token');
    if (!token || typeof token !== 'string') {
        return '';
    }
    // 去除首尾空白字符
    let cleaned = token.trim();
    // 移除Bearer前缀（如果有）
    if (cleaned.startsWith('Bearer ')) {
        cleaned = cleaned.substring(7);
    }
    // 与后端保持一致：只保留JWT有效字符（Base64URL字符：A-Za-z0-9\-_.）
    // 移除所有其他字符（包括空白字符、控制字符等）
    cleaned = cleaned.replace(/[^A-Za-z0-9\-_.]/g, '');
    return cleaned;
}

async function fetchWithAuth(endpoint, options = {}) {
    const cleanToken = getCleanToken();
    
    // 先合并 options.headers，确保 Authorization 不会被覆盖
    const headers = {
        ...(options.headers || {}),
    };

    // 对于DELETE和GET请求，如果没有body，不应该设置Content-Type
    // 对于POST和PUT请求，如果有body且不是FormData，才设置Content-Type
    const method = (options.method || 'GET').toUpperCase();
    const hasBody = options.body !== undefined && options.body !== null;
    const isFormData = options.body instanceof FormData;
    
    if (!headers['Content-Type'] && !headers['content-type']) {
        // 只有POST/PUT/PATCH请求且有body且不是FormData时才设置Content-Type
        if ((method === 'POST' || method === 'PUT' || method === 'PATCH') && hasBody && !isFormData) {
            headers['Content-Type'] = 'application/json';
        }
        // DELETE和GET请求不设置Content-Type
    }

    // 最后设置 Authorization，确保不会被 options.headers 覆盖
    if (cleanToken) {
        headers['Authorization'] = `Bearer ${cleanToken}`;
    }

    try {
        console.log(`[fetchWithAuth] ${method} ${endpoint}`, { headers, hasBody });
        
        // 注意：先展开 options，再覆盖 headers，确保 headers 优先级最高
        const response = await fetch(`${API_BASE_URL}${endpoint}`, {
            ...options,
            headers: headers
        });

        console.log(`[fetchWithAuth] Response status: ${response.status} for ${method} ${endpoint}`);

        if (response.status === 401) {
            // Token is invalid or expired, redirect to login
            mockAction('登录已过期，请重新登录');
            setTimeout(() => logout(), 1500);
            throw new Error('Unauthorized');
        }

        if (!response.ok) {
            // Handle other HTTP errors (e.g., 404, 500)
            let errorMessage = `HTTP error! status: ${response.status}`;
            try {
                const errorData = await response.json();
                errorMessage = errorData.message || errorData.msg || errorMessage;
                console.error(`[fetchWithAuth] Error response:`, errorData);
            } catch (e) {
                // 如果响应不是JSON，尝试读取文本
                try {
                    const text = await response.text();
                    console.error(`[fetchWithAuth] Error response text:`, text);
                    if (text) errorMessage = text;
                } catch (e2) {
                    console.error(`[fetchWithAuth] Failed to read error response`);
                }
            }
            throw new Error(errorMessage);
        }

        // For 204 No Content, response.json() will fail
        if (response.status === 204) {
            return null;
        }

        const data = await response.json();
        if (data.code !== 200) {
            throw new Error(data.message || data.msg || 'API returned a non-200 code');
        }
        return data.data; // Directly return the 'data' payload

    } catch (error) {
        console.error(`[fetchWithAuth] API Fetch Error for ${method} ${endpoint}:`, error);
        mockAction(`请求失败: ${error.message}`);
        throw error; // Re-throw the error to be caught by the caller
    }
}

// ================= Global Variables & Configuration =================
let isEditing = false; // 个人信息编辑状态
let currentUser = {
    account: "",
    name: "",
    roleId: "",
    phone: "",
    email: "",
    supervisor: "",
    college: "",
    major: ""
};
let notices = [];
let userListData = [];
let adminNoticesData = [];

// 学生数据（从API获取）
let myTopicData = {}, myProposalData = {}, myMidtermData = {}, myThesisData = {}, myDefenseData = {};
let currentProcessId = null; // 当前论文流程ID

const ROLES = {
    STUDENT: { id: 'STUDENT', name: '学生', desc: '查看选题进展，提交论文材料', menu: ['我的选题', '提交开题报告', '中期检查', '论文终稿', '答辩安排', '历史记录','个人信息'] },
    TEACHER: { id: 'SUPERVISOR', name: '指导老师', desc: '审核学生进度，评定论文成绩', menu: ['题目申报', '任务书下达', '进度批阅', '论文评阅', '成绩报送', '历史记录', '个人信息'] },
    PRO_LEADER: { id: 'MAJOR_LEADER', name: '专业负责人', desc: '管理专业课题，审核学生选题', menu: ['课题审核', '选题审核', '中期检查管理', '成绩管理', '答辩安排', '历史记录', '个人信息'] },
    REVIEWER: { id: 'REVIEWER', name: '评阅老师', desc: '评阅学生论文，给出评阅意见', menu: ['论文评阅', '成绩评定', '评阅统计', '历史记录', '个人信息'] },
    DEAN: { id: 'COLLEGE_LEADER', name: '二级学院领导', desc: '管理专业流程，查看统计信息', menu: ['专业管理', '进度监控', '统计报表', '答辩安排', '历史记录', '个人信息'] },
    ADMIN: { id: 'ADMIN', name: '管理员', desc: '系统管理，用户管理，数据维护', menu: ['用户管理', '通知管理', '权限配置', '数据维护', '系统监控', '历史记录', '个人信息'] },
    DEFENSE_MEMBER: { id: 'DEFENSE_MEMBER', name: '答辩小组成员', desc: '参与答辩评审，评分', menu: ['答辩安排', '答辩评分', '答辩记录', '历史记录', '个人信息'] },
    DEFENSE_CHAIRMAN: { id: 'DEFENSE_LEADER', name: '答辩小组组长', desc: '组织答辩，汇总成绩', menu: ['答辩组织', '成绩汇总', '答辩安排', '历史记录', '个人信息'] }
};

const PLACEHOLDER_PAGES = [
    '选题审核', '中期检查管理', '成绩管理',
    '评阅统计',
    '专业管理', '进度监控', '统计报表',
    '权限配置', '数据维护', '系统监控'
];

// ================= Core Application Logic =================
window.onload = function () {
    const urlParams = new URLSearchParams(window.location.search);
    const roleCode = urlParams.get('role');
    let roleId = 'STUDENT'; // Default role

    if (roleCode) {
        const upperRoleCode = roleCode.toUpperCase();
        // 查找ROLES对象中是否有某个条目的id与URL中的roleCode匹配
        const matchedRoleKey = Object.keys(ROLES).find(key => ROLES[key].id === upperRoleCode);
        if (matchedRoleKey) {
            // 如果找到匹配，则使用该角色的id
            roleId = ROLES[matchedRoleKey].id;
        } else {
            // 如果没有找到，则保持默认值'STUDENT'并打印警告
            console.warn(`URL parameter "role=${roleCode}" does not match any role ID in ROLES object. Defaulting to STUDENT.`);
        }
    } else {
        console.warn('No role parameter found in URL. Defaulting to STUDENT.');
    }

    // ================= DEBUG LOG =================
    console.log('URL role param:', roleCode);
    console.log('Matched roleId:', roleId);
    // ===========================================

    currentUser.roleId = roleId;

    loadUserData().then(success => {
        if (success) renderDashboard();
    });
    updateTime();
    setInterval(updateTime, 1000);
};

async function loadUserData() {
    try {
        // 1. Fetch current user's basic info
        const userInfo = await fetchWithAuth('/api/user/profile');
        // 使用后端返回的真实角色ID，确保显示正确的角色
        if (userInfo && userInfo.roleId) {
            currentUser.roleId = userInfo.roleId;
        }
        const { roleId: fetchedRoleId, roleCode: fetchedRoleCode, ...restInfo } = userInfo || {};
        currentUser = { ...currentUser, ...restInfo };

        // 2. Based on role, fetch role-specific data
        switch (currentUser.roleId) {
            case 'STUDENT':
                await loadStudentData();
                break;
            case 'ADMIN':
                await loadAdminData();
                break;
            case 'SUPERVISOR':
            case 'SUPERVISOR':
                await loadTeacherData();
                break;
            case 'REVIEWER':
                // 评阅老师数据加载
                break;
            case 'DEFENSE_MEMBER':
            case 'DEFENSE_LEADER':
                await loadDefenseTeamData();
                break;
            case 'PRO_LEADER':
            case 'MAJOR_LEADER':
                // 专业负责人数据加载
                break;
            case 'DEAN':
                // 学院领导数据加载
                break;
            default:
                console.log(`No specific data loader for role: ${currentUser.roleId}`);
        }

        // 3. Fetch common data like notices
        try {
            notices = await fetchWithAuth('/api/notice/latest?limit=5') || [];
        } catch (error) {
            console.error('Failed to load notices:', error);
            notices = [];
        }

        return true; // Indicate success

    } catch (error) {
        console.error('Failed to load user data:', error);
        // The fetchWithAuth function already shows a toast message
        return false; // Indicate failure
    }
}

async function loadStudentData() {
    console.log("Loading student-specific data...");
    try {
        console.log('RAW localStorage token:', JSON.stringify(localStorage.getItem('token')));
        const token = getCleanToken();
        console.log('CLEAN token:', JSON.stringify(token));
        // console.log(
        //     [...cleanToken].map(c => c.charCodeAt(0))
        // );
        //获取论文流程信息
        let processInfo = null;
        try {
            processInfo = await fetchWithAuth('/api/student/thesis/process');
        } catch (error) {
            if (error.message.includes('未找到资料')) {
                console.log('No thesis process found for student, initializing empty state.');
            } else {
                // For other errors, re-throw to be caught by the outer catch block
                throw error;
            }
        }
        
        let baseData = null;
        let topicHistory = [], proposalHistory = [], midtermHistory = [], thesisHistory = [];
        let topicRejected = null, proposalRejected = null, midtermRejected = null, thesisRejected = null;

        if (processInfo && processInfo.processId) {
            // 从processInfo中获取processId
            //console.log('lunwen',lunwen = processInfo.processId);
            currentProcessId = processInfo.processId || null;
            
            // 根据API文档，processInfo包含title, status, supervisor
            const rawProcessStatus = processInfo.status; // 保存原始状态用于判断
            baseData = {
                title: processInfo.title || '未确定',
                supervisor: processInfo.supervisor || '未分配',
                status: mapThesisStatusToLocal(processInfo.status),
                processId: processInfo.processId
            };
            
            // 如果processId存在，获取各材料的历史版本和驳回原因
            if (currentProcessId) {
                [topicHistory, proposalHistory, midtermHistory, thesisHistory, topicRejected, proposalRejected, midtermRejected, thesisRejected] = await Promise.all([
                    fetchWithAuth(`/api/student/thesis/material/${currentProcessId}/TOPIC_SELECTION/history`).catch(e => []),
                    fetchWithAuth(`/api/student/thesis/material/${currentProcessId}/OPENING_REPORT/history`).catch(e => []),
                    fetchWithAuth(`/api/student/thesis/material/${currentProcessId}/MID_TERM_REPORT/history`).catch(e => []),
                    fetchWithAuth(`/api/student/thesis/material/${currentProcessId}/FINAL_PAPER/history`).catch(e => []),
                    fetchWithAuth(`/api/student/thesis/material/${currentProcessId}/TOPIC_SELECTION/rejected-reason`).catch(e => null),
                    fetchWithAuth(`/api/student/thesis/material/${currentProcessId}/OPENING_REPORT/rejected-reason`).catch(e => null),
                    fetchWithAuth(`/api/student/thesis/material/${currentProcessId}/MID_TERM_REPORT/rejected-reason`).catch(e => null),
                    fetchWithAuth(`/api/student/thesis/material/${currentProcessId}/FINAL_PAPER/rejected-reason`).catch(e => null)
                ]);
            
                // 检查是否有被驳回的版本
                const topicLatest = topicHistory && topicHistory.length > 0 ? topicHistory[0] : null;
                const proposalLatest = proposalHistory && proposalHistory.length > 0 ? proposalHistory[0] : null;
                const midtermLatest = midtermHistory && midtermHistory.length > 0 ? midtermHistory[0] : null;
                const thesisLatest = thesisHistory && thesisHistory.length > 0 ? thesisHistory[0] : null;
                
                // 根据流程状态和材料类型判断状态
                let topicStatus;
                if (!topicHistory || topicHistory.length === 0) {
                    topicStatus = 'none'; // No submission history, so status is 'none'
                } else if (topicRejected || (topicLatest && topicLatest.rejectedReason)) {
                    topicStatus = 'rejected'; // Has history and is rejected
                } else {
                    // 根据流程状态判断选题报告状态
                    if (rawProcessStatus === 'topic_submitted') {
                        topicStatus = 'pending';
                    } else if (rawProcessStatus && ['topic_approved', 'opening_submitted', 'opening_approved', 'midterm_submitted', 'midterm_approved', 'final_submitted', 'final_approved', 'defense_scored', 'completed'].includes(rawProcessStatus)) {
                        topicStatus = 'approved';
                    } else {
                        topicStatus = 'pending'; // 默认待审核
                    }
                }
                myTopicData = {
                    ...baseData,
                    history: (topicHistory || []).map(h => ({
                        originalFilename: h.originalFilename || '文件名未知',
                        time: h.uploadedAt || '',
                        size: '未知',
                        historyId: h.historyId
                    })),
                    rejectReason: topicRejected || (topicLatest && topicLatest.rejectedReason) || null,
                    status: topicStatus
                };
                
                let proposalStatus;
                if (!proposalHistory || proposalHistory.length === 0) {
                    proposalStatus = 'none'; // No submission history, so status is 'none'
                } else if (proposalRejected || (proposalLatest && proposalLatest.rejectedReason)) {
                    proposalStatus = 'rejected'; // Has history and is rejected
                } else {
                    // 根据流程状态判断开题报告状态
                    if (rawProcessStatus === 'opening_submitted') {
                        proposalStatus = 'pending';
                    } else if (rawProcessStatus && ['opening_approved', 'midterm_submitted', 'midterm_approved', 'final_submitted', 'final_approved', 'defense_scored', 'completed'].includes(rawProcessStatus)) {
                        proposalStatus = 'approved';
                    } else {
                        proposalStatus = 'pending'; // 默认待审核
                    }
                }
                myProposalData = {
                    ...baseData,
                    history: (proposalHistory || []).map(h => ({
                        originalFilename: h.originalFilename || '文件名未知',
                        time: h.uploadedAt || '',
                        size: '未知',
                        historyId: h.historyId
                    })),
                    rejectReason: proposalRejected || (proposalLatest && proposalLatest.rejectedReason) || null,
                    status: proposalStatus
                };
                
                let midtermStatus;
                if (!midtermHistory || midtermHistory.length === 0) {
                    midtermStatus = 'none';
                } else if (midtermRejected || (midtermLatest && midtermLatest.rejectedReason)) {
                    midtermStatus = 'rejected';
                } else {
                    // 根据流程状态判断中期报告状态
                    if (rawProcessStatus === 'midterm_submitted') {
                        midtermStatus = 'pending';
                    } else if (rawProcessStatus && ['midterm_approved', 'final_submitted', 'final_approved', 'defense_scored', 'completed'].includes(rawProcessStatus)) {
                        midtermStatus = 'approved';
                    } else {
                        midtermStatus = 'pending'; // 默认待审核
                    }
                }
                myMidtermData = {
                    ...baseData,
                    history: (midtermHistory || []).map(h => ({
                        originalFilename: h.originalFilename || '文件名未知',
                        time: h.uploadedAt || '',
                        size: '未知',
                        historyId: h.historyId
                    })),
                    rejectReason: midtermRejected || (midtermLatest && midtermLatest.rejectedReason) || null,
                    status: midtermStatus
                };
                
                let thesisStatus;
                if (!thesisHistory || thesisHistory.length === 0) {
                    thesisStatus = 'none';
                } else if (thesisRejected || (thesisLatest && thesisLatest.rejectedReason)) {
                    thesisStatus = 'rejected';
                } else {
                    // 根据流程状态判断论文终稿状态
                    if (rawProcessStatus === 'final_submitted') {
                        thesisStatus = 'pending';
                    } else if (rawProcessStatus && ['final_approved', 'defense_scored', 'completed'].includes(rawProcessStatus)) {
                        thesisStatus = 'approved';
                    } else {
                        thesisStatus = 'pending'; // 默认待审核
                    }
                }
                myThesisData = {
                    ...baseData,
                    history: (thesisHistory || []).map(h => ({
                        originalFilename: h.originalFilename || '文件名未知',
                        time: h.uploadedAt || '',
                        size: '未知',
                        historyId: h.historyId
                    })),
                    rejectReason: thesisRejected || (thesisLatest && thesisLatest.rejectedReason) || null,
                    status: thesisStatus
                };
            } else {
                // 如果没有processId，只设置基本信息
                myTopicData = { ...baseData, history: [], rejectReason: null };
                myProposalData = { ...baseData, history: [], rejectReason: null };
                myMidtermData = { ...baseData, history: [], rejectReason: null };
                myThesisData = { ...baseData, history: [], rejectReason: null };
            }
        } else {
            // 如果没有流程信息，初始化空数据
            myTopicData = { status: 'none', history: [], title: '未确定', supervisor: '未分配' };
            myProposalData = { status: 'none', history: [], title: '未确定', supervisor: '未分配' };
            myMidtermData = { status: 'none', history: [], title: '未确定', supervisor: '未分配' };
            myThesisData = { status: 'none', history: [], title: '未确定', supervisor: '未分配' };
        }
        
        myDefenseData = { hasQualification: false };
        
        console.log("--- STUDENT DATA DIAGNOSTICS ---");
        console.log("1. Raw Process Info from API:", JSON.parse(JSON.stringify(processInfo)));
        console.log("2. Derived Base Data:", JSON.parse(JSON.stringify(baseData)));
        console.log("3. Raw Topic History from API:", JSON.parse(JSON.stringify(topicHistory)));
        console.log("4. Raw Topic Rejected Reason from API:", topicRejected);
        console.log("5. Final myTopicData object:", JSON.parse(JSON.stringify(myTopicData)));
        console.log("--- END DIAGNOSTICS ---");

        console.log("Student data loaded successfully.");
        console.log("myProposalData:", myProposalData);
        

    } catch (error) {
        console.error("An error occurred during parallel student data fetch:", error);
    }
}

// 将后端状态映射到前端状态
function mapThesisStatusToLocal(status) {
    if (!status) return 'none';
    const statusMap = {
        'init': 'none',
        'topic_submitted': 'pending',
        'topic_approved': 'approved',
        'opening_submitted': 'pending',
        'opening_approved': 'approved',
        'midterm_submitted': 'pending',
        'midterm_approved': 'approved',
        'final_submitted': 'pending',
        'final_approved': 'approved'
    };
    return statusMap[status] || 'none';
}

async function loadAdminData() {
    try {
        // 获取用户列表
        const userListResponse = await fetchWithAuth('/api/admin/users?page=1&size=100');
        if (userListResponse && userListResponse.users) {
            userListData = userListResponse.users.map(u => ({
                id: u.userId,
                name: u.realName || u.username,
                account: u.username,
                phone: u.phone || '',
                role: u.roles && u.roles.length > 0 ? u.roles.map(r => r.roleName).join(', ') : '未分配'
            }));
        } else {
            userListData = [];
        }
        
        // 获取通知列表
        const noticesResponse = await fetchWithAuth('/api/notice/admin?page=1&size=100');
        if (noticesResponse && Array.isArray(noticesResponse)) {
            adminNoticesData = noticesResponse;
        } else {
            adminNoticesData = [];
        }
    } catch (error) {
        console.error('Failed to load admin data:', error);
        userListData = [];
        adminNoticesData = [];
    }
}

async function loadTeacherData() {
    try {
        // 指导老师数据加载逻辑
        // 可以根据需要加载指导学生的列表等
        console.log("Teacher data loaded");
    } catch (error) {
        console.error('Failed to load teacher data:', error);
    }
}

async function loadDefenseTeamData() {
    try {
        // 答辩小组数据加载逻辑
        console.log("Defense team data loaded");
    } catch (error) {
        console.error('Failed to load defense team data:', error);
    }
}

function switchPage(pageName, extraData = {}) {
        const roleKey = Object.keys(ROLES).find(key => ROLES[key].id === currentUser.roleId);
    const roleInfo = roleKey ? ROLES[roleKey] : ROLES.STUDENT;
    document.getElementById('viewTitle').innerText = pageName === '首页看板' ? `${roleInfo.name}门户` : pageName;

    const links = document.querySelectorAll('.sidebar-link');
    links.forEach(link => {
        const span = link.querySelector('.sidebar-text');
        if (span && span.innerText === pageName) {
            link.classList.add('active');
        } else {
            link.classList.remove('active');
        }
    });

    isEditing = false; // Reset editing state on page switch

    // Routing logic
    if (pageName === '首页看板') {
        renderContent(roleInfo);
    } else if (pageName === '个人信息') {
        renderProfilePage();
    } 
    // Student pages
    else if (pageName === '我的选题') { renderMyTopicPage(); }
    else if (pageName === '提交开题报告') { renderProposalPage(); }
    else if (pageName === '中期检查') { renderMidtermPage(); }
    else if (pageName === '论文终稿') { renderThesisPage(); }
    // Supervisor pages
    else if (pageName === '题目申报') { renderTopicDeclarationPage(); }
    else if (pageName === '任务书下达') { renderTaskBookPage(); }
    else if (pageName === '进度批阅') { renderProgressReviewPage(); }
    // Reviewer/Supervisor shared page
    else if (pageName === '论文评阅') {
                if (currentUser.roleId === 'SUPERVISOR') {
            renderSupervisorPaperReviewPage();
        } else if (currentUser.roleId === 'REVIEWER') {
            renderReviewerPaperListPage();
        }
    }
    // Supervisor/Reviewer Grading
    else if (pageName === '成绩报送') { renderScoreSubmitPage(extraData); }
    else if (pageName === '成绩评定') { 
        console.log('switchPage: 成绩评定, extraData:', extraData);
        // 如果extraData为空或没有processId，先显示论文列表让用户选择
        if (!extraData || !extraData.processId) {
            if (currentUser.roleId === 'REVIEWER') {
                renderReviewerPaperListPage();
            } else {
                renderReviewerGradingPage(extraData);
            }
        } else {
            renderReviewerGradingPage(extraData);
        }
    }
    else if (pageName === '评阅统计') { renderReviewStatisticsPage(); }
    // Major Leader pages
    else if (pageName === '课题审核') { renderTopicApprovalPage(); }
    else if (pageName === '选题审核') { renderTopicSelectionReviewPage(); }
    else if (pageName === '中期检查管理') { renderMidtermCheckManagementPage(); }
    else if (pageName === '成绩管理') { renderGradeManagementPage(); }
    // Admin pages
    else if (pageName === '用户管理') { renderUserManagementPage(); }
    else if (pageName === '通知管理') { renderNoticeManagementPage(); }
    else if (pageName === '权限配置') { renderPermissionConfigPage(); }
    else if (pageName === '数据维护') { renderDataMaintenancePage(); }
    else if (pageName === '系统监控') { renderSystemMonitoringPage(); }
    // Common history page
    else if (pageName === '历史记录') { 
        if (typeof renderHistoryPage === 'function') {
            renderHistoryPage(); 
        } else {
            renderGenericPlaceholder('历史记录');
        }
    }
    // Defense pages
    else if (pageName === '答辩评分') { renderDefenseGradingPage(extraData); }
    else if (pageName === '答辩组织') { renderDefenseOrganizationPage(); }
    else if (pageName === '成绩汇总') { renderGradeSummaryPage(); }
    else if (pageName === '答辩记录') { renderDefenseRecordPage(); }
    else if (pageName === '答辩安排') { 
        // 学生角色使用专门的函数，其他角色使用通用函数
        if (currentUser.roleId === 'STUDENT') {
            renderDefensePage();
        } else {
            renderDefenseArrangementPage();
        }
    }
    // Placeholder for all other pages
    else if (PLACEHOLDER_PAGES.includes(pageName)) {
        renderGenericPlaceholder(pageName);
    } else {
        renderSubPage(pageName); // Fallback for any unhandled pages
    }
}

function renderDashboard() {
    // Find the role info object by matching the id property, not by key
    const roleKey = Object.keys(ROLES).find(key => ROLES[key].id === currentUser.roleId);
    const roleInfo = roleKey ? ROLES[roleKey] : ROLES.STUDENT;
    document.getElementById('userName').innerText = currentUser.name || '测试用户';
    document.getElementById('userInitial').innerText = (currentUser.name || '用').charAt(0);
    document.getElementById('roleBadge').innerText = roleInfo.name;
    document.getElementById('viewTitle').innerText = `${roleInfo.name}门户`;
    document.getElementById('viewDesc').innerText = roleInfo.desc;

    const nav = document.getElementById('sidebarNav');
    nav.innerHTML = `<a onclick="switchPage('首页看板')" class="sidebar-link active"><i class="fas fa-th-large"></i><span class="sidebar-text ml-2 font-medium">首页看板</span></a>`;
    roleInfo.menu.forEach(item => {
        // A simple icon mapping, can be expanded
        const icon = item === '个人信息' ? 'fa-user-cog' : 'fa-file-alt';
        nav.innerHTML += `<a onclick="switchPage('${item}')" class="sidebar-link"><i class="fas ${icon}"></i><span class="sidebar-text ml-2">${item}</span></a>`;
    });
    renderContent(roleInfo);
}

function renderContent(role) {
    const container = document.getElementById('contentArea');
    const displayMenu = role.menu.filter(m => m !== '个人信息');

    container.innerHTML = `
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div class="lg:col-span-2 card p-8">
            <h3 class="font-bold text-lg text-slate-900 mb-6">${role.name}工作流</h3>
            <div class="space-y-6">
                ${displayMenu.map((m, i) => {
                    // 根据实际数据判断状态（简化处理）
                    let stepStatus = "暂未进行";
                    let statusClass = "text-slate-400";
                    let circleClass = "bg-slate-100 text-slate-400";
                    
                    // 可以根据实际数据判断状态，这里简化处理
                    if (currentUser.roleId === 'STUDENT') {
                        // 学生角色可以根据myTopicData等判断
                        if (i === 0 && myTopicData && myTopicData.status === 'approved') {
                            stepStatus = "已完成";
                            statusClass = "text-emerald-500";
                            circleClass = "bg-emerald-50 text-emerald-500";
                        } else if (i === 0 && myTopicData && myTopicData.status === 'pending') {
                            stepStatus = "进行中";
                            statusClass = "text-amber-500";
                            circleClass = "bg-amber-50 text-amber-500";
                        }
                    }

                    return `
                        <div class="flex items-center gap-4 cursor-pointer hover:bg-slate-50 p-2 rounded-lg transition-colors group" onclick="switchPage('${m}')">
                            <div class="w-8 h-8 rounded-full ${circleClass} flex items-center justify-center text-xs font-bold transition-colors">0${i + 1}</div>
                            <div class="flex-1 border-b border-slate-100 pb-4 flex justify-between items-center">
                                <p class="font-bold text-slate-800">${m}</p>
                                <span class="text-xs font-bold ${statusClass}">${stepStatus}</span>
                            </div>
                        </div>
                    `;
                }).join('')}
            </div>
        </div>
        <div class="space-y-6">
            <div class="card p-6">
                <h4 class="font-bold text-slate-900 mb-4">公告栏</h4>
                <div class="space-y-3">
                    ${notices && notices.length > 0 ? notices.map(n => `
                        <div class="text-xs p-3 bg-indigo-50 text-indigo-700 rounded-lg">
                            <p class="font-bold mb-1">${n.title || '通知'}</p>
                            <p class="text-indigo-600">${n.content || ''}</p>
                            <p class="text-indigo-500 text-[10px] mt-1">${n.createdAt || ''}</p>
                        </div>
                    `).join('') : '<div class="text-xs p-3 bg-indigo-50 text-indigo-700 rounded-lg">暂无新通知。</div>'}
                </div>
            </div>
        </div>
    </div>`;
}

// ================= Utility Functions =================
function toggleSidebar() {
    document.getElementById('app').classList.toggle('sidebar-collapsed');
}

function updateTime() {
    const now = new Date();
    document.getElementById('currentTime').innerText = now.toLocaleString('zh-CN');
}

function logout() {
    localStorage.clear();
    window.location.href = 'login_page.html';
}

function mockAction(msg) {
    const toast = document.createElement('div');
    toast.className = "fixed bottom-8 left-1/2 -translate-x-1/2 px-6 py-3 bg-slate-800 text-white text-sm rounded-full shadow-2xl z-50 animate-bounce";
    toast.innerText = msg;
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 2500);
}

function showRejectModal(studentName, materialType) {
    return new Promise((resolve) => {
        const modal = document.createElement('div');
        modal.className = 'fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50';
        
        const modalContent = `
            <div class="bg-white rounded-lg shadow-xl p-8 w-1/3">
                <h4 class="font-bold text-lg mb-4">驳回原因 - ${studentName} (${materialType})</h4>
                <textarea id="rejection-reason-input" class="w-full h-32 border-slate-200 rounded-md p-2" placeholder="请输入详细的驳回原因..."></textarea>
                <div class="flex justify-end gap-2 mt-4">
                    <button id="cancel-rejection" class="px-4 py-2 border rounded-md">取消</button>
                    <button id="confirm-rejection" class="px-4 py-2 bg-rose-500 text-white rounded-md">确认驳回</button>
                </div>
            </div>
        `;
        modal.innerHTML = modalContent;
        document.body.appendChild(modal);

        const confirmBtn = document.getElementById('confirm-rejection');
        const cancelBtn = document.getElementById('cancel-rejection');
        const reasonInput = document.getElementById('rejection-reason-input');

        const closeModal = () => document.body.removeChild(modal);

        confirmBtn.onclick = () => {
            const reason = reasonInput.value.trim();
            if (!reason) {
                mockAction('驳回原因不能为空');
                return;
            }
            closeModal();
            resolve(reason);
        };

        cancelBtn.onclick = () => {
            closeModal();
            resolve(null); // Resolve with null if cancelled
        };
    });
}

function renderProfilePage() {
    const container = document.getElementById('contentArea');
    // 使用正确的查找逻辑来匹配角色，与renderDashboard()保持一致
    const roleKey = Object.keys(ROLES).find(key => ROLES[key].id === currentUser.roleId);
    const roleInfo = roleKey ? ROLES[roleKey] : ROLES.STUDENT;
    
    container.innerHTML = `
        <div class="card p-8">
            <div class="flex justify-between items-center mb-6">
                <h3 class="text-xl font-bold text-slate-900">个人信息</h3>
                <button id="edit-profile-btn" class="px-4 py-2 text-sm bg-indigo-600 text-white rounded-lg hover:bg-indigo-700">
                    ${isEditing ? '取消编辑' : '编辑信息'}
                </button>
            </div>
            
            <div class="space-y-6">
                <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div>
                        <label class="block text-sm font-medium text-slate-700 mb-2">账号</label>
                        <input type="text" id="profile-account" value="${currentUser.account || ''}" 
                               class="w-full px-4 py-2 border border-slate-300 rounded-lg ${!isEditing ? 'bg-slate-50' : 'bg-white'}" 
                               ${!isEditing ? 'readonly' : ''} />
                    </div>
                    <div>
                        <label class="block text-sm font-medium text-slate-700 mb-2">姓名</label>
                        <input type="text" id="profile-name" value="${currentUser.name || ''}" 
                               class="w-full px-4 py-2 border border-slate-300 rounded-lg ${!isEditing ? 'bg-slate-50' : 'bg-white'}" 
                               ${!isEditing ? 'readonly' : ''} />
                    </div>
                    <div>
                        <label class="block text-sm font-medium text-slate-700 mb-2">手机号</label>
                        <input type="text" id="profile-phone" value="${currentUser.phone || ''}" 
                               class="w-full px-4 py-2 border border-slate-300 rounded-lg ${!isEditing ? 'bg-slate-50' : 'bg-white'}" 
                               ${!isEditing ? 'readonly' : ''} />
                    </div>
                    <div>
                        <label class="block text-sm font-medium text-slate-700 mb-2">邮箱</label>
                        <input type="email" id="profile-email" value="${currentUser.email || ''}" 
                               class="w-full px-4 py-2 border border-slate-300 rounded-lg ${!isEditing ? 'bg-slate-50' : 'bg-white'}" 
                               ${!isEditing ? 'readonly' : ''} />
                    </div>
                    ${currentUser.roleId === 'STUDENT' ? `
                    <div>
                        <label class="block text-sm font-medium text-slate-700 mb-2">学院</label>
                        <input type="text" id="profile-college" value="${currentUser.college || ''}" 
                               class="w-full px-4 py-2 border border-slate-300 rounded-lg bg-slate-50" readonly />
                    </div>
                    <div>
                        <label class="block text-sm font-medium text-slate-700 mb-2">专业</label>
                        <input type="text" id="profile-major" value="${currentUser.major || ''}" 
                               class="w-full px-4 py-2 border border-slate-300 rounded-lg bg-slate-50" readonly />
                    </div>
                    <div>
                        <label class="block text-sm font-medium text-slate-700 mb-2">指导老师</label>
                        <input type="text" id="profile-supervisor" value="${currentUser.supervisor || ''}" 
                               class="w-full px-4 py-2 border border-slate-300 rounded-lg bg-slate-50" readonly />
                    </div>
                    ` : ''}
                    <div>
                        <label class="block text-sm font-medium text-slate-700 mb-2">角色</label>
                        <input type="text" value="${roleInfo.name || ''}" 
                               class="w-full px-4 py-2 border border-slate-300 rounded-lg bg-slate-50" readonly />
                    </div>
                </div>
                
                ${isEditing ? `
                <div class="flex justify-end gap-3 pt-4 border-t">
                    <button id="cancel-edit-btn" class="px-6 py-2 border border-slate-300 rounded-lg hover:bg-slate-50">取消</button>
                    <button id="save-profile-btn" class="px-6 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700">保存</button>
                </div>
                ` : ''}
            </div>
        </div>
    `;
    
    // 编辑按钮事件
    document.getElementById('edit-profile-btn').addEventListener('click', () => {
        isEditing = !isEditing;
        renderProfilePage();
    });
    
    // 保存按钮事件
    if (isEditing) {
        const saveBtn = document.getElementById('save-profile-btn');
        const cancelBtn = document.getElementById('cancel-edit-btn');
        
        if (saveBtn) {
            saveBtn.addEventListener('click', async () => {
                const updatedData = {
                    name: document.getElementById('profile-name').value,
                    phone: document.getElementById('profile-phone').value,
                    email: document.getElementById('profile-email').value
                };
                
                try {
                    mockAction('正在保存...');
                    await fetchWithAuth('/api/user/profile', {
                        method: 'PUT',
                        body: JSON.stringify(updatedData)
                    });
                    currentUser = { ...currentUser, ...updatedData };
                    isEditing = false;
                    mockAction('保存成功！');
                    renderProfilePage();
                } catch (error) {
                    mockAction('保存失败: ' + error.message);
                    console.error('Failed to update profile:', error);
                }
            });
        }
        
        if (cancelBtn) {
            cancelBtn.addEventListener('click', () => {
                isEditing = false;
                renderProfilePage();
            });
        }
    }
}

function renderGenericPlaceholder(pageName) {
    const container = document.getElementById('contentArea');
    container.innerHTML = `<div class="card p-12 text-center"><h3 class="text-xl font-bold text-slate-900 mb-2">${pageName}</h3><p class="text-slate-500">此功能正在开发中...</p></div>`;
}

function renderSubPage(pageName) {
    const container = document.getElementById('contentArea');
    container.innerHTML = `<div class="card p-12 text-center"><h3 class="text-xl font-bold text-slate-900 mb-2">${pageName} 模块</h3><p class="text-slate-500">数据载入中...</p></div>`;
}

function updateTotalScore() {
    const inputs = document.querySelectorAll('input[type="number"]');
    let total = 0;
    inputs.forEach(input => {
        let value = parseInt(input.value, 10) || 0;
        const max = parseInt(input.max, 10);
        if (value > max) {
            value = max;
            input.value = max;
        }
        if (value < 0) {
            value = 0;
            input.value = 0;
        }
        total += value;
    });
    document.getElementById('totalScore').innerText = total;
}