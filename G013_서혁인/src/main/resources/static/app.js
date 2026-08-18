const $ = (id) => document.getElementById(id);
const ROLES = ["PM", "FRONTEND", "BACKEND", "AI", "INFRA"];

const state = {
  currentUserId: localStorage.getItem("tm_currentUserId") || null,
  members: [],
  projects: [],
  serviceUp: true,
};

async function api(path, options = {}) {
  const res = await fetch(path, {
    headers: { "Content-Type": "application/json" },
    ...options,
  });
  if (res.status === 204) return null;
  const body = await res.json().catch(() => null);
  if (!res.ok) {
    throw new Error((body && body.message) || `요청 실패 (HTTP ${res.status})`);
  }
  return body;
}

function currentMember() {
  return state.members.find((m) => String(m.id) === String(state.currentUserId));
}

// ---------- 헬스 배너 ----------
async function loadHealth() {
  try {
    const res = await fetch("/actuator/health");
    const body = await res.json();
    state.serviceUp = body.status === "UP";
    const banner = $("healthBanner");
    if (!state.serviceUp) {
      const detail = body.components?.activeProjects?.details?.reason || "";
      banner.textContent = `🚧 서비스 점검 중입니다 (${detail}). 신규 가입/프로젝트 등록이 제한됩니다.`;
      banner.classList.remove("hidden");
    } else {
      banner.classList.add("hidden");
    }
    const suBtn = $("su-submitBtn");
    if (suBtn) suBtn.disabled = !state.serviceUp;
    const cpBtn = $("openCreateProjectBtn");
    if (cpBtn) cpBtn.disabled = !state.serviceUp;
  } catch (e) {
    console.warn("헬스체크 실패", e);
  }
}

// ---------- 사용자 선택 ----------
async function loadMembers() {
  state.members = await api("/api/members");
  const select = $("currentUserSelect");
  select.innerHTML = state.members
    .map((m) => `<option value="${m.id}">${m.nickname} (${m.email})</option>`)
    .join("");
  if (!state.currentUserId && state.members.length > 0) {
    state.currentUserId = String(state.members[0].id);
  }
  select.value = state.currentUserId;
  localStorage.setItem("tm_currentUserId", state.currentUserId);
}

function onUserChanged() {
  state.currentUserId = $("currentUserSelect").value;
  localStorage.setItem("tm_currentUserId", state.currentUserId);
  fitNavToUserText();
  refreshActiveTab();
  fillProfileForm();
}

// ---------- 사이드바 폭을 "현재 사용자" 텍스트 길이에 맞춘다 ----------
// .app-nav 안에 select 를 겹겹이 감싼 실제 박스모델 여백(아래 두 줄은 style.css 값과 맞춰야 한다).
const NAV_MIN_WIDTH = 232;
const NAV_MAX_WIDTH = 420;
const NAV_PADDING_H = 32; // .app-nav padding: 24px 16px → 좌우 16*2
const NAV_BORDER_H = 1; // .app-nav border-right: 1px
const USER_BOX_PADDING_H = 24; // .user-box padding: 12px → 좌우 12*2
const USER_BOX_BORDER_H = 2; // .user-box border: 1px solid → 좌우 1*2
const BOX_MODEL_CHROME = NAV_PADDING_H + NAV_BORDER_H + USER_BOX_PADDING_H + USER_BOX_BORDER_H; // = 59
// select 의 padding-right(20px, style.css)만으로는 부족했다 — 네이티브 드롭다운 화살표는
// 브라우저가 자체적으로 그리는 영역이라 CSS padding을 그대로 반영하지 않고, canvas 측정치도
// 실제 select 렌더링 폭과 정확히 일치하지 않는다(실사용 중 실측: 최소 여유가 필요한 값이
// 계산치보다 훨씬 컸다). 정확한 공식 대신 넉넉한 여유값으로 흡수한다.
const SELECT_NATIVE_CHROME = 60;
const NAV_CHROME = BOX_MODEL_CHROME + SELECT_NATIVE_CHROME;
let textMeasureCanvas = null;

function measureTextWidth(text, font) {
  textMeasureCanvas = textMeasureCanvas || document.createElement("canvas");
  const ctx = textMeasureCanvas.getContext("2d");
  ctx.font = font;
  return ctx.measureText(text).width;
}

function fitNavToUserText() {
  const nav = document.querySelector(".app-nav");
  // 900px 이하에서는 사이드바가 상단 가로 탭바로 바뀌어 폭이 100%로 고정되므로 건드리지 않는다
  if (window.matchMedia("(max-width: 900px)").matches) {
    nav.style.width = "";
    return;
  }

  const select = $("currentUserSelect");
  const option = select.options[select.selectedIndex];
  if (!option) return;

  const font = getComputedStyle(select).font;
  const requiredNavWidth = measureTextWidth(option.text, font) + NAV_CHROME;
  const navWidth = Math.min(NAV_MAX_WIDTH, Math.max(NAV_MIN_WIDTH, Math.ceil(requiredNavWidth)));
  nav.style.width = `${navWidth}px`;
}

window.addEventListener("resize", () => fitNavToUserText());

// ---------- 탭 전환 ----------
function switchTab(tabName) {
  document.querySelectorAll(".tab-btn").forEach((b) => b.classList.toggle("active", b.dataset.tab === tabName));
  document.querySelectorAll(".tab-panel").forEach((p) => p.classList.toggle("hidden", p.id !== `tab-${tabName}`));
  refreshActiveTab();
}

function activeTabName() {
  return document.querySelector(".tab-btn.active")?.dataset.tab || "projects";
}

function refreshActiveTab() {
  const tab = activeTabName();
  if (tab === "projects") loadProjects();
  else if (tab === "leaderApplications") loadLeaderApplications();
  else if (tab === "myApplications") loadMyApplications();
  else if (tab === "chat") loadChatRooms();
  else if (tab === "statistics") loadStatistics();
}

// ---------- 프로젝트 목록 ----------
function statusLabel(status) {
  return { RECRUITING: "모집중", CLOSED: "모집마감", COMPLETED: "완료" }[status] || status;
}

async function loadProjects() {
  const status = $("statusFilter").value;
  state.projects = await api(`/api/projects${status ? `?status=${status}` : ""}`);
  const container = $("projectList");
  if (state.projects.length === 0) {
    container.innerHTML = `<p class="empty">등록된 프로젝트가 없습니다.</p>`;
    return;
  }
  container.innerHTML = state.projects.map(renderProjectCard).join("");

  container.querySelectorAll("[data-apply]").forEach((btn) => {
    btn.addEventListener("click", () => openApplyModal(Number(btn.dataset.apply)));
  });
}

function renderProjectCard(p) {
  const isLeader = String(p.leaderId) === String(state.currentUserId);
  const openSlots = p.slots.filter((s) => s.filledCount < s.capacity);
  const canApply = p.status === "RECRUITING" && !isLeader && openSlots.length > 0;

  const slotsHtml = p.slots
    .map((s) => `<span class="pill ${s.filledCount >= s.capacity ? "full" : ""}">${s.role} ${s.filledCount}/${s.capacity}</span>`)
    .join("");
  const techHtml = (p.techStacks || []).map((t) => `<span class="pill tech">${t}</span>`).join("");

  return `
    <div class="card">
      <div class="card-head">
        <h3>${p.title}</h3>
        <span class="status-badge status-${p.status}">${statusLabel(p.status)}</span>
      </div>
      <p class="desc">${p.description || ""}</p>
      <div class="meta">리더: ${p.leaderNickname} · 팀원 ${p.memberCount}명 · 마감일 ${p.deadline || "-"}</div>
      <div class="pill-row">${slotsHtml}</div>
      <div class="pill-row">${techHtml}</div>
      <div class="card-actions">
        ${canApply ? `<button data-apply="${p.id}" class="primary">지원하기</button>` : ""}
        ${isLeader ? `<span class="hint">내가 만든 프로젝트</span>` : ""}
      </div>
    </div>`;
}

// ---------- 지원하기 모달 ----------
let applyProjectId = null;

function openApplyModal(projectId) {
  applyProjectId = projectId;
  const project = state.projects.find((p) => p.id === projectId);
  $("applyModalTitle").textContent = `"${project.title}" 지원하기`;
  const openSlots = project.slots.filter((s) => s.filledCount < s.capacity);
  $("apply-role").innerHTML = openSlots.map((s) => `<option value="${s.role}">${s.role} (${s.filledCount}/${s.capacity})</option>`).join("");
  $("apply-message").value = "";
  $("apply-message-status").textContent = "";
  $("applyModal").classList.remove("hidden");
}

function closeApplyModal() {
  $("applyModal").classList.add("hidden");
  applyProjectId = null;
}

async function submitApply() {
  try {
    await api(`/api/projects/${applyProjectId}/applications`, {
      method: "POST",
      body: JSON.stringify({
        applicantId: Number(state.currentUserId),
        role: $("apply-role").value,
        message: $("apply-message").value,
      }),
    });
    $("apply-message-status").textContent = "✅ 지원이 완료되었습니다.";
    setTimeout(() => {
      closeApplyModal();
      loadProjects();
    }, 700);
  } catch (e) {
    $("apply-message-status").textContent = "❌ " + e.message;
  }
}

// ---------- 프로젝트 생성 ----------
function addSlotRow(role = "BACKEND", capacity = 1) {
  const row = document.createElement("div");
  row.className = "slot-row";
  row.innerHTML = `
    <select class="cp-slot-role">${ROLES.map((r) => `<option value="${r}" ${r === role ? "selected" : ""}>${r}</option>`).join("")}</select>
    <input type="number" class="cp-slot-capacity" min="1" value="${capacity}" style="width:60px">
    <button type="button" class="remove-slot">삭제</button>`;
  row.querySelector(".remove-slot").addEventListener("click", () => row.remove());
  $("cp-slots").appendChild(row);
}

function resetCreateProjectForm() {
  $("cp-title").value = "";
  $("cp-description").value = "";
  $("cp-deadline").value = "";
  $("cp-techstacks").value = "";
  $("cp-slots").innerHTML = "";
  $("cp-message").textContent = "";
  addSlotRow("FRONTEND", 1);
  addSlotRow("BACKEND", 1);
}

async function submitCreateProject() {
  const slots = Array.from($("cp-slots").querySelectorAll(".slot-row")).map((row) => ({
    role: row.querySelector(".cp-slot-role").value,
    capacity: Number(row.querySelector(".cp-slot-capacity").value),
  }));
  const techStacks = $("cp-techstacks").value.split(",").map((t) => t.trim()).filter(Boolean);

  try {
    await api("/api/projects", {
      method: "POST",
      body: JSON.stringify({
        leaderId: Number(state.currentUserId),
        leaderRole: $("cp-leaderRole").value,
        title: $("cp-title").value,
        description: $("cp-description").value,
        deadline: $("cp-deadline").value || null,
        slots,
        techStacks,
      }),
    });
    $("createProjectForm").classList.add("hidden");
    loadProjects();
  } catch (e) {
    $("cp-message").textContent = "❌ " + e.message;
  }
}

// ---------- 지원자 관리 (핵심 트랜잭션: 수락/거절) ----------
async function loadLeaderApplications() {
  const container = $("leaderApplicationList");
  container.innerHTML = "불러오는 중...";

  const allProjects = await api("/api/projects");
  const myProjects = allProjects.filter((p) => String(p.leaderId) === String(state.currentUserId));

  if (myProjects.length === 0) {
    container.innerHTML = `<p class="empty">내가 리더인 프로젝트가 없습니다.</p>`;
    return;
  }

  const blocks = [];
  for (const project of myProjects) {
    const applications = await api(`/api/projects/${project.id}/applications`);
    const rows = applications
      .map((a) => {
        const actions =
          a.status === "PENDING"
            ? `<button data-decide="${a.id}" data-decision="ACCEPT" class="primary">수락</button>
               <button data-decide="${a.id}" data-decision="REJECT" class="danger">거절</button>`
            : `<span class="status-badge status-${a.status}">${a.status}</span>`;
        return `<div class="app-row">
            <div>${a.applicantNickname} · ${a.role} · <span class="hint">${a.message || ""}</span></div>
            <div class="app-actions">${actions}</div>
          </div>`;
      })
      .join("");
    blocks.push(`<div class="card"><h3>${project.title}</h3>${rows || `<p class="empty">지원자가 없습니다.</p>`}</div>`);
  }
  container.innerHTML = blocks.join("");

  container.querySelectorAll("[data-decide]").forEach((btn) => {
    btn.addEventListener("click", () => decideApplication(Number(btn.dataset.decide), btn.dataset.decision));
  });
}

async function decideApplication(applicationId, decision) {
  try {
    await api(`/api/applications/${applicationId}/decision`, {
      method: "POST",
      body: JSON.stringify({ actorMemberId: Number(state.currentUserId), decision }),
    });
    loadLeaderApplications();
  } catch (e) {
    $("leaderApplicationList").insertAdjacentHTML("afterbegin", `<p class="message">❌ 처리 실패: ${e.message}</p>`);
  }
}

// ---------- 내 지원 현황 ----------
async function loadMyApplications() {
  const container = $("myApplicationList");
  const applications = await api(`/api/members/${state.currentUserId}/applications`);
  if (applications.length === 0) {
    container.innerHTML = `<p class="empty">지원 내역이 없습니다.</p>`;
    return;
  }
  container.innerHTML = applications
    .map(
      (a) => `<div class="card">
        <div class="card-head"><h3>${a.projectTitle}</h3><span class="status-badge status-${a.status}">${a.status}</span></div>
        <div class="meta">역할: ${a.role} · 지원일: ${(a.appliedAt || "").slice(0, 10)}</div>
        <p class="desc">${a.message || ""}</p>
      </div>`
    )
    .join("");
}

// ---------- 채팅방 ----------
async function loadChatRooms() {
  const container = $("chatRoomList");
  container.innerHTML = "불러오는 중...";
  const allProjects = await api("/api/projects");
  const rooms = [];
  for (const project of allProjects) {
    try {
      const room = await api(`/api/projects/${project.id}/chatroom`);
      if (room.members.some((m) => String(m.id) === String(state.currentUserId))) {
        rooms.push(room);
      }
    } catch (e) {
      // 아직 채팅방이 없는 프로젝트는 건너뛴다
    }
  }
  if (rooms.length === 0) {
    container.innerHTML = `<p class="empty">참여 중인 채팅방이 없습니다. 프로젝트를 만들거나, 지원이 수락되면 자동으로 합류돼요.</p>`;
    return;
  }
  container.innerHTML = rooms
    .map(
      (r) => `<div class="card">
        <h3>💬 ${r.projectTitle}</h3>
        <div class="pill-row">${r.members.map((m) => `<span class="pill tech">${m.nickname}</span>`).join("")}</div>
      </div>`
    )
    .join("");
}

// ---------- 통계 ----------
async function loadStatistics() {
  const techStacks = await api("/api/statistics/tech-stacks/top10");
  $("techStackTable").innerHTML =
    `<tr><th>#</th><th>기술 스택</th><th>수요(프로젝트 수)</th></tr>` +
    techStacks.map((t, i) => `<tr><td>${i + 1}</td><td>${t.techName}</td><td>${t.demandCount}</td></tr>`).join("");

  const ranking = await api("/api/statistics/users/top-completed");
  $("rankingTable").innerHTML =
    `<tr><th>#</th><th>닉네임</th><th>완료 프로젝트 수</th></tr>` +
    ranking.map((r, i) => `<tr><td>${i + 1}</td><td>${r.nickname}</td><td>${r.completedCount}</td></tr>`).join("");
}

// ---------- 회원가입 / 프로필 / 파일 업로드 ----------
async function submitSignup() {
  try {
    const member = await api("/api/members", {
      method: "POST",
      body: JSON.stringify({
        email: $("su-email").value,
        password: $("su-password").value,
        nickname: $("su-nickname").value,
        introduction: $("su-introduction").value,
        portfolioUrl: $("su-portfolioUrl").value,
      }),
    });
    $("su-message").textContent = "✅ 가입 완료! 상단에서 현재 사용자로 선택할 수 있습니다.";
    await loadMembers();
    state.currentUserId = String(member.id);
    $("currentUserSelect").value = state.currentUserId;
    localStorage.setItem("tm_currentUserId", state.currentUserId);
    fillProfileForm();
  } catch (e) {
    $("su-message").textContent = "❌ " + e.message;
  }
}

function fillProfileForm() {
  const member = currentMember();
  if (!member) return;
  $("pf-introduction").value = member.introduction || "";
  $("pf-portfolioUrl").value = member.portfolioUrl || "";
  loadPortfolioFiles();
}

async function submitProfile() {
  try {
    await api(`/api/members/${state.currentUserId}/profile`, {
      method: "PUT",
      body: JSON.stringify({
        introduction: $("pf-introduction").value,
        portfolioUrl: $("pf-portfolioUrl").value,
      }),
    });
    $("pf-message").textContent = "✅ 저장되었습니다.";
    await loadMembers();
  } catch (e) {
    $("pf-message").textContent = "❌ " + e.message;
  }
}

async function loadPortfolioFiles() {
  const files = await api(`/api/members/${state.currentUserId}/portfolio-file`);
  $("file-list").innerHTML = files.map((f) => `<li>${f.originalFilename} <span class="hint">(${f.storedPath})</span></li>`).join("");
}

async function uploadPortfolioFile() {
  const input = $("file-input");
  if (!input.files[0]) {
    $("file-message").textContent = "파일을 선택해주세요.";
    return;
  }
  const formData = new FormData();
  formData.append("file", input.files[0]);
  try {
    const res = await fetch(`/api/members/${state.currentUserId}/portfolio-file`, { method: "POST", body: formData });
    const body = await res.json();
    if (!res.ok) throw new Error(body.message);
    $("file-message").textContent = "✅ 업로드 완료: " + body.storedPath;
    input.value = "";
    loadPortfolioFiles();
  } catch (e) {
    $("file-message").textContent = "❌ " + e.message;
  }
}

// ---------- 초기화 ----------
async function init() {
  await loadHealth();
  await loadMembers();
  fitNavToUserText();
  fillProfileForm();

  document.querySelectorAll(".tab-btn").forEach((btn) => btn.addEventListener("click", () => switchTab(btn.dataset.tab)));
  $("currentUserSelect").addEventListener("change", onUserChanged);
  $("statusFilter").addEventListener("change", loadProjects);
  $("refreshProjectsBtn").addEventListener("click", loadProjects);

  $("openCreateProjectBtn").addEventListener("click", () => {
    resetCreateProjectForm();
    const form = $("createProjectForm");
    form.classList.remove("hidden");
    // 프로젝트 목록 아래에 폼이 붙어 있어, 목록이 길면 화면 밖이라 "눌러도 안 열린다"고 오해하기 쉽다
    form.scrollIntoView({ behavior: "smooth", block: "start" });
  });
  $("cp-addSlotBtn").addEventListener("click", () => addSlotRow());
  $("cp-cancelBtn").addEventListener("click", () => $("createProjectForm").classList.add("hidden"));
  $("cp-submitBtn").addEventListener("click", submitCreateProject);

  $("apply-submitBtn").addEventListener("click", submitApply);
  $("apply-cancelBtn").addEventListener("click", closeApplyModal);

  $("su-submitBtn").addEventListener("click", submitSignup);
  $("pf-submitBtn").addEventListener("click", submitProfile);
  $("file-uploadBtn").addEventListener("click", uploadPortfolioFile);

  loadProjects();

  setInterval(loadHealth, 15000);
}

init();
