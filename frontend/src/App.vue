<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import {
  checkinActivity,
  clearAuthToken,
  createActivity,
  createFeedback,
  createForumComment,
  createForumPost,
  getActivities,
  getActivityDetail,
  getArchivedActivities,
  getCategories,
  getCheckins,
  getCurrentUser,
  getDashboardSummary,
  getFeedbacks,
  getForumComments,
  getForumPosts,
  getForumTags,
  getHotPosts,
  getRegistrations,
  getStatistics,
  getStoredToken,
  getUserHistory,
  getUsers,
  login,
  registerActivity,
  setAuthToken,
  toggleForumLike,
  updateActivity
} from './api'

const loginForm = reactive({
  username: 'stu001',
  password: '123456'
})

const navItems = [
  { key: 'dashboard', label: '前台总览' },
  { key: 'activities', label: '活动中心' },
  { key: 'guides', label: '活动指南' },
  { key: 'archive', label: '往期活动' },
  { key: 'my', label: '我的参与' },
  { key: 'admin', label: '管理后台', roles: ['organizer', 'admin'] }
]

const moduleTabs = {
  guides: [
    { key: 'latest', label: '最新指南' },
    { key: 'publish', label: '发布指南' }
  ],
  archive: [
    { key: 'top', label: '评分最高' },
    { key: 'latest', label: '最新评价' },
    { key: 'timeline', label: '活动归档' }
  ],
  admin: [
    { key: 'create', label: '发布活动' },
    { key: 'manage', label: '活动管理' },
    { key: 'checkin', label: '签到管理' }
  ]
}

const currentUser = ref(null)
const currentModule = ref('dashboard')
const currentTab = reactive({
  guides: 'latest',
  archive: 'top',
  admin: 'create'
})

const loading = ref(false)
const authChecking = ref(true)
const errorMessage = ref('')
const successMessage = ref('')

const dashboardSummary = ref({
  totalActivities: 0,
  publishedActivities: 0,
  finishedActivities: 0,
  totalRegistrations: 0,
  totalCheckins: 0,
  averageFeedbackRating: 0
})

const activities = ref([])
const statistics = ref([])
const categories = ref([])
const users = ref([])
const hotPosts = ref([])
const forumPosts = ref([])
const forumTags = ref([])
const archiveActivities = ref([])
const myHistory = ref([])

const selectedActivityId = ref(null)
const activityDetail = ref(null)
const activityFeedbacks = ref([])
const registrations = ref([])
const checkins = ref([])

const selectedDiscussionPostId = ref(null)
const discussionComments = ref([])

const selectedArchiveActivityId = ref(null)
const archiveFeedbacks = ref([])

const createForm = reactive({
  categoryId: '',
  title: '',
  description: '',
  location: '',
  startTime: '',
  endTime: '',
  registrationDeadline: '',
  capacity: 30,
  status: 'published'
})

const editForm = reactive({
  categoryId: '',
  title: '',
  description: '',
  location: '',
  startTime: '',
  endTime: '',
  registrationDeadline: '',
  capacity: 30,
  status: 'published'
})

const registerForm = reactive({
  remark: ''
})

const feedbackForm = reactive({
  rating: 5,
  content: ''
})

const discussionForm = reactive({
  title: '',
  content: '',
  tagIds: []
})

const commentForm = reactive({
  content: ''
})

const checkinForm = reactive({
  userId: '',
  checkinResult: 'manual',
  note: ''
})

const visibleNavItems = computed(() =>
  navItems.filter((item) => !item.roles || item.roles.includes(currentUser.value?.roleCode))
)

const isManager = computed(() =>
  currentUser.value && ['organizer', 'admin'].includes(currentUser.value.roleCode)
)

const summaryCards = computed(() => [
  { label: '活动总数', value: dashboardSummary.value.totalActivities },
  { label: '报名总人次', value: dashboardSummary.value.totalRegistrations },
  { label: '签到总人次', value: dashboardSummary.value.totalCheckins },
  { label: '平均反馈分', value: Number(dashboardSummary.value.averageFeedbackRating || 0).toFixed(2) }
])

const recommendedActivities = computed(() =>
  activities.value
    .filter((item) => item.status === 'published')
    .slice(0, 2)
)

const registerableActivities = computed(() =>
  activities.value.filter((item) => item.status === 'published')
)

const selectedActivity = computed(() =>
  activities.value.find((item) => item.activityId === selectedActivityId.value) || null
)

const featuredActivities = computed(() => registerableActivities.value.slice(0, 2))

const activityPosts = computed(() =>
  forumPosts.value.filter((item) => item.activityId === selectedActivityId.value)
)

const selectedGuide = computed(() =>
  forumPosts.value.find((item) => item.postId === selectedDiscussionPostId.value) || null
)

const guideList = computed(() => forumPosts.value)

const organizerGuides = computed(() =>
  activityPosts.value.filter((item) => ['organizer', 'admin'].includes(item.authorRoleCode))
)

const activityTagChips = computed(() => {
  const map = new Map()
  for (const post of activityPosts.value) {
    for (const tag of post.tags) {
      if (!map.has(tag.tagId)) map.set(tag.tagId, tag)
    }
  }
  return [...map.values()]
})

const activityHotPosts = computed(() =>
  hotPosts.value.filter((item) => item.activityId === selectedActivityId.value).slice(0, 3)
)

const activityCommentCount = computed(() =>
  activityPosts.value.reduce((total, post) => total + Number(post.commentCount || 0), 0)
)

const selectedArchiveActivity = computed(() =>
  archiveActivities.value.find((item) => item.activityId === selectedArchiveActivityId.value) || null
)

const sortedArchiveActivities = computed(() => {
  const list = [...archiveActivities.value]
  if (currentTab.archive === 'top') {
    return list.sort((a, b) => (b.averageRating ?? 0) - (a.averageRating ?? 0))
  }
  return list.sort((a, b) => new Date(b.endTime) - new Date(a.endTime))
})

const topTabs = computed(() => moduleTabs[currentModule.value] || [])

function setMessage(type, message) {
  if (type === 'error') {
    errorMessage.value = message
    successMessage.value = ''
    return
  }
  successMessage.value = message
  errorMessage.value = ''
}

function resetMessages() {
  errorMessage.value = ''
  successMessage.value = ''
}

function formatStatus(status) {
  return {
    draft: '草稿',
    published: '报名中',
    cancelled: '已取消',
    finished: '已结束',
    approved: '已报名',
    attended: '已签到',
    normal: '正常签到',
    late: '迟到签到',
    manual: '人工补签'
  }[status] || status || '-'
}

function formatTime(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 16)
}

function toDateTimeLocal(value) {
  return value ? String(value).slice(0, 16) : ''
}

function fillEditForm(detail) {
  if (!detail) return
  editForm.categoryId = detail.categoryId
  editForm.title = detail.title
  editForm.description = detail.description || ''
  editForm.location = detail.location
  editForm.startTime = toDateTimeLocal(detail.startTime)
  editForm.endTime = toDateTimeLocal(detail.endTime)
  editForm.registrationDeadline = toDateTimeLocal(detail.registrationDeadline)
  editForm.capacity = detail.capacity
  editForm.status = detail.status
}

function toActivityPayload(form) {
  return {
    categoryId: Number(form.categoryId),
    title: form.title,
    description: form.description,
    location: form.location,
    startTime: form.startTime,
    endTime: form.endTime,
    registrationDeadline: form.registrationDeadline,
    capacity: Number(form.capacity),
    status: form.status
  }
}

async function withLoading(task) {
  loading.value = true
  resetMessages()
  try {
    await task()
  } catch (error) {
    setMessage('error', error.message)
  } finally {
    loading.value = false
  }
}

async function handleLogin() {
  await withLoading(async () => {
    const response = await login(loginForm)
    setAuthToken(response.token)
    currentUser.value = response.user
    setMessage('success', `欢迎回来，${response.user.realName}`)
    await loadWorkspace()
  })
}

async function restoreSession() {
  if (!getStoredToken()) {
    authChecking.value = false
    return
  }
  try {
    currentUser.value = await getCurrentUser()
    await loadWorkspace()
  } catch {
    clearAuthToken()
    currentUser.value = null
  } finally {
    authChecking.value = false
  }
}

function logout() {
  clearAuthToken()
  currentUser.value = null
  currentModule.value = 'dashboard'
  setMessage('success', '已退出登录')
}

function openActivity(activityId, postId = null) {
  currentModule.value = 'activities'
  selectedActivityId.value = activityId
  if (postId) selectedDiscussionPostId.value = postId
}

function openGuide(postId) {
  currentModule.value = 'guides'
  selectedDiscussionPostId.value = postId
}

function openArchive(activityId) {
  currentModule.value = 'archive'
  selectedArchiveActivityId.value = activityId
}

async function loadActivitiesBoard() {
  const [activityData, summaryData, statisticData] = await Promise.all([
    getActivities(),
    getDashboardSummary(),
    getStatistics()
  ])
  activities.value = activityData
  dashboardSummary.value = summaryData
  statistics.value = statisticData

  if (!selectedActivityId.value && activityData[0]) {
    selectedActivityId.value = activityData[0].activityId
  }
  if (selectedActivityId.value && !activityData.some((item) => item.activityId === selectedActivityId.value)) {
    selectedActivityId.value = activityData[0]?.activityId || null
  }
}

async function loadActivityWorkspace(activityId) {
  if (!activityId) return
  const [detailData, feedbackData, registrationData, checkinData] = await Promise.all([
    getActivityDetail(activityId),
    getFeedbacks(activityId),
    isManager.value ? getRegistrations(activityId) : Promise.resolve([]),
    isManager.value ? getCheckins(activityId) : Promise.resolve([])
  ])
  activityDetail.value = detailData
  activityFeedbacks.value = feedbackData
  registrations.value = registrationData
  checkins.value = checkinData
  fillEditForm(detailData)
}

async function loadDiscussionBoard() {
  const [tagData, postData, hotData] = await Promise.all([
    getForumTags(),
    getForumPosts({ sort: 'latest' }),
    getHotPosts()
  ])
  forumTags.value = tagData
  forumPosts.value = postData
  hotPosts.value = hotData

  const currentActivityPosts = postData.filter((item) => item.activityId === selectedActivityId.value)
  if (selectedDiscussionPostId.value && postData.some((item) => item.postId === selectedDiscussionPostId.value)) {
    return
  }
  selectedDiscussionPostId.value = currentActivityPosts[0]?.postId || postData[0]?.postId || null
}

async function loadDiscussionComments(postId) {
  if (!postId) {
    discussionComments.value = []
    return
  }
  discussionComments.value = await getForumComments(postId)
}

async function loadArchiveBoard() {
  archiveActivities.value = await getArchivedActivities()
  if (!selectedArchiveActivityId.value && archiveActivities.value[0]) {
    selectedArchiveActivityId.value = archiveActivities.value[0].activityId
  }
}

async function loadArchiveFeedbacks(activityId) {
  if (!activityId) return
  archiveFeedbacks.value = await getFeedbacks(activityId)
}

async function loadUserBoard() {
  if (!currentUser.value) return
  myHistory.value = await getUserHistory(currentUser.value.userId)
}

async function loadOptions() {
  const [categoryData, userData] = await Promise.all([getCategories(), getUsers()])
  categories.value = categoryData
  users.value = userData

  if (!createForm.categoryId && categoryData[0]) createForm.categoryId = categoryData[0].categoryId
  if (!checkinForm.userId && userData.find((item) => item.roleCode === 'student')) {
    checkinForm.userId = userData.find((item) => item.roleCode === 'student').userId
  }
}

async function loadWorkspace() {
  await withLoading(async () => {
    await loadActivitiesBoard()
    await loadOptions()
    await Promise.all([loadDiscussionBoard(), loadArchiveBoard(), loadUserBoard()])
    if (selectedActivityId.value) await loadActivityWorkspace(selectedActivityId.value)
    if (selectedDiscussionPostId.value) await loadDiscussionComments(selectedDiscussionPostId.value)
    if (selectedArchiveActivityId.value) await loadArchiveFeedbacks(selectedArchiveActivityId.value)
    authChecking.value = false
  })
}

async function submitRegister() {
  await withLoading(async () => {
    await registerActivity(selectedActivityId.value, { remark: registerForm.remark })
    registerForm.remark = ''
    setMessage('success', '活动报名成功')
    await loadActivitiesBoard()
    await loadActivityWorkspace(selectedActivityId.value)
    await loadUserBoard()
  })
}

async function submitFeedback() {
  await withLoading(async () => {
    await createFeedback(selectedActivityId.value, {
      rating: Number(feedbackForm.rating),
      content: feedbackForm.content
    })
    feedbackForm.content = ''
    setMessage('success', '反馈提交成功')
    await loadActivityWorkspace(selectedActivityId.value)
    await loadArchiveBoard()
    await loadArchiveFeedbacks(selectedActivityId.value)
  })
}

async function submitCreateActivity() {
  await withLoading(async () => {
    const created = await createActivity(toActivityPayload(createForm))
    selectedActivityId.value = created.activityId
    setMessage('success', `活动已创建：${created.title}`)
    await loadActivitiesBoard()
    await loadActivityWorkspace(created.activityId)
  })
}

async function submitUpdateActivity() {
  await withLoading(async () => {
    const updated = await updateActivity(selectedActivityId.value, toActivityPayload(editForm))
    setMessage('success', `活动已更新：${updated.title}`)
    await loadActivitiesBoard()
    await loadActivityWorkspace(updated.activityId)
    await loadArchiveBoard()
  })
}

async function submitCheckin() {
  await withLoading(async () => {
    await checkinActivity(selectedActivityId.value, {
      userId: Number(checkinForm.userId),
      checkinResult: checkinForm.checkinResult,
      note: checkinForm.note
    })
    setMessage('success', '人工补签完成')
    await loadActivityWorkspace(selectedActivityId.value)
    await loadActivitiesBoard()
  })
}

async function submitDiscussion() {
  await withLoading(async () => {
    await createForumPost({
      activityId: selectedActivityId.value,
      title: discussionForm.title,
      content: discussionForm.content,
      tagIds: discussionForm.tagIds
    })
    discussionForm.title = ''
    discussionForm.content = ''
    discussionForm.tagIds = []
    setMessage('success', '讨论已发布')
    await loadDiscussionBoard()
    if (activityPosts.value[0]) {
      selectedDiscussionPostId.value = activityPosts.value[0].postId
      await loadDiscussionComments(selectedDiscussionPostId.value)
    }
  })
}

async function submitGuide() {
  await submitDiscussion()
  currentTab.guides = 'latest'
}

async function submitComment() {
  await withLoading(async () => {
    await createForumComment(selectedDiscussionPostId.value, { content: commentForm.content })
    commentForm.content = ''
    setMessage('success', '评论已发布')
    await loadDiscussionComments(selectedDiscussionPostId.value)
    await loadDiscussionBoard()
  })
}

async function handleLike(postId) {
  await withLoading(async () => {
    await toggleForumLike(postId)
    await loadDiscussionBoard()
  })
}

watch(selectedActivityId, async (value) => {
  if (!currentUser.value || !value) return
  await withLoading(async () => {
    await loadActivityWorkspace(value)
    await loadDiscussionBoard()
    await loadDiscussionComments(selectedDiscussionPostId.value)
  })
})

watch(selectedDiscussionPostId, async (value) => {
  if (!currentUser.value) return
  await withLoading(async () => {
    await loadDiscussionComments(value)
  })
})

watch(selectedArchiveActivityId, async (value) => {
  if (!currentUser.value || !value) return
  await withLoading(async () => {
    await loadArchiveFeedbacks(value)
  })
})

onMounted(async () => {
  await restoreSession()
})
</script>

<template>
  <div v-if="authChecking" class="loading-shell">正在检查登录状态…</div>

  <div v-else-if="!currentUser" class="login-shell">
    <div class="login-panel">
      <p class="eyebrow">Campus Activity Hub</p>
      <h1>校园活动管理与交流平台</h1>
      <p class="login-copy">前台负责活动浏览、报名与交流；后台负责活动发布、状态维护与签到管理。</p>
      <div class="demo-accounts">
        <div>学生：`stu001` / `123456`</div>
        <div>社长：`org002` / `123456`</div>
        <div>管理员：`admin` / `123456`</div>
      </div>
      <div v-if="errorMessage" class="banner error">{{ errorMessage }}</div>
      <label>
        用户名
        <input v-model="loginForm.username" placeholder="输入账号" />
      </label>
      <label>
        密码
        <input v-model="loginForm.password" type="password" placeholder="输入密码" />
      </label>
      <button class="primary-btn" @click="handleLogin">登录系统</button>
    </div>
  </div>

  <div v-else class="layout-shell">
    <aside class="sidebar">
      <div class="brand-block">
        <p class="eyebrow">Student Frontstage / Admin Backoffice</p>
        <h1>活动平台</h1>
      </div>
      <nav class="side-nav">
        <button
          v-for="item in visibleNavItems"
          :key="item.key"
          class="side-link"
          :class="{ active: currentModule === item.key }"
          @click="currentModule = item.key"
        >
          {{ item.label }}
        </button>
      </nav>
      <div class="user-card">
        <strong>{{ currentUser.realName }}</strong>
        <span>{{ currentUser.roleName }}</span>
        <span v-if="currentUser.studentNo">{{ currentUser.studentNo }}</span>
        <button class="ghost-btn" @click="logout">退出登录</button>
      </div>
    </aside>

    <div class="main-shell">
      <header class="top-header">
        <div>
          <p class="page-kicker">{{ currentModule === 'admin' ? 'Backoffice' : 'Frontstage' }}</p>
          <h2>{{ visibleNavItems.find((item) => item.key === currentModule)?.label }}</h2>
        </div>
        <div class="status-chip">{{ loading ? '同步中' : '已连接真实数据库' }}</div>
      </header>

      <nav v-if="topTabs.length" class="top-tabs">
        <button
          v-for="tab in topTabs"
          :key="tab.key"
          class="top-tab"
          :class="{ active: currentTab[currentModule] === tab.key }"
          @click="currentTab[currentModule] = tab.key"
        >
          {{ tab.label }}
        </button>
      </nav>

      <section v-if="errorMessage" class="banner error">{{ errorMessage }}</section>
      <section v-if="successMessage" class="banner success">{{ successMessage }}</section>

      <main class="content-shell">
        <section v-if="currentModule === 'dashboard'" class="dashboard-grid">
          <div class="hero-panel dashboard-hero">
            <div>
              <p class="eyebrow">Now Happening</p>
              <h3>推荐活动</h3>
              <p class="panel-copy">优先展示正在开放报名的活动，点击后直接进入活动详情与报名页。</p>
            </div>
          </div>

          <section class="dashboard-feature-row">
            <button
              v-for="item in recommendedActivities"
              :key="item.activityId"
              class="feature-activity-card"
              @click="openActivity(item.activityId)"
            >
              <div class="feature-topline">
                <span class="featured-badge">进行中推荐</span>
                <span class="feature-status">{{ formatStatus(item.status) }}</span>
              </div>
              <div class="feature-banner-body">
                <div class="feature-copy">
                  <h4>{{ item.title }}</h4>
                  <p class="feature-summary">围绕 {{ item.categoryName }} 展开的校园活动，当前可直接进入详情页报名并参与讨论。</p>
                  <div class="feature-meta-grid">
                    <span>举办方 {{ item.organizerName }}</span>
                    <span>类型 {{ item.categoryName }}</span>
                    <span>地点 {{ item.location }}</span>
                    <span>{{ formatTime(item.startTime) }}</span>
                  </div>
                </div>
                <div class="feature-sidebox">
                  <div class="feature-quota">
                    <label>可报名</label>
                    <strong>{{ Math.max((item.capacity || 0) - (item.approvedCount || 0), 0) }}</strong>
                  </div>
                  <div class="feature-statline">
                    <span>已报名 {{ item.approvedCount }}/{{ item.capacity }}</span>
                    <span>点击进入报名页</span>
                  </div>
                </div>
              </div>
            </button>
          </section>

          <section class="panel dashboard-section dashboard-nav-section">
            <div class="panel-head"><h3>热门讨论</h3><span>来自活动指南评论区</span></div>
            <div class="dashboard-link-strip">
              <button
                v-for="post in hotPosts.slice(0, 6)"
                :key="post.postId"
                class="dashboard-link-card"
                @click="openGuide(post.postId)"
              >
                <strong>{{ post.title }}</strong>
                <span>{{ post.activityTitle }} · {{ post.commentCount }} 条评论</span>
              </button>
            </div>
          </section>

          <section class="panel dashboard-section dashboard-nav-section">
            <div class="panel-head"><h3>所有可报名活动</h3><span>{{ registerableActivities.length }} 条</span></div>
            <div class="dashboard-link-strip">
              <button
                v-for="item in registerableActivities"
                :key="item.activityId"
                class="dashboard-link-card"
                @click="openActivity(item.activityId)"
              >
                <strong>{{ item.title }}</strong>
                <span>{{ item.categoryName }} · {{ item.location }}</span>
              </button>
            </div>
          </section>

          <section class="panel dashboard-section dashboard-nav-section">
            <div class="panel-head"><h3>往期活动</h3><span>{{ archiveActivities.length }} 场</span></div>
            <div class="dashboard-link-strip archive-link-strip">
              <button
                v-for="item in sortedArchiveActivities.slice(0, 4)"
                :key="item.activityId"
                class="archive-entry-card"
                @click="openArchive(item.activityId)"
              >
                <div class="archive-entry-topline">
                  <span class="archive-score-tag">均分 {{ item.averageRating ?? 0 }}</span>
                  <span class="archive-feedback-tag">{{ item.feedbackCount }} 条评价</span>
                </div>
                <strong>{{ item.title }}</strong>
                <span>{{ item.categoryName }} · {{ item.organizerName }}</span>
                <p>{{ formatTime(item.endTime) }}</p>
              </button>
            </div>
          </section>
        </section>

        <section v-else-if="currentModule === 'activities'" class="activity-layout activity-center-layout">
          <section class="activity-stats-strip panel">
            <div v-for="card in summaryCards" :key="card.label" class="activity-stat-card">
              <span>{{ card.label }}</span>
              <strong>{{ card.value }}</strong>
            </div>
          </section>
          <div class="activity-sidebar panel">
            <div class="section-head compact-head">
              <div>
                <p class="eyebrow local">Current Activities</p>
                <h4>当前活动</h4>
              </div>
              <span class="muted-inline">这里只保留当前可报名和进行中的活动</span>
            </div>
            <div class="featured-list">
              <button
                v-for="item in featuredActivities"
                :key="`featured-${item.activityId}`"
                class="featured-card"
                @click="selectedActivityId = item.activityId"
              >
                <span class="featured-badge">{{ formatStatus(item.status) }}</span>
                <strong>{{ item.title }}</strong>
                <p>{{ item.categoryName }} · {{ item.organizerName }}</p>
                <div class="featured-meta">
                  <span>{{ formatTime(item.startTime) }}</span>
                  <span>{{ item.location }}</span>
                </div>
              </button>
            </div>

            <div class="panel-head side-head"><h3>当前可报名活动</h3><span>{{ registerableActivities.length }} 条</span></div>
            <div class="stack-list">
              <button
                v-for="item in registerableActivities"
                :key="item.activityId"
                class="list-card activity-list-card"
                :class="{ selected: selectedActivityId === item.activityId }"
                @click="selectedActivityId = item.activityId"
              >
                <div>
                  <strong>{{ item.title }}</strong>
                  <span>{{ item.categoryName }} · {{ formatStatus(item.status) }}</span>
                </div>
                <div class="meta-column">
                <span>{{ item.approvedCount }}/{{ item.capacity }}</span>
                  <span>{{ formatTime(item.startTime) }}</span>
                </div>
              </button>
            </div>
          </div>

          <article v-if="activityDetail" class="activity-post panel">
            <header class="post-header forum-topic-header">
              <div class="forum-topic-meta">
                <p class="post-kicker">活动详情 / {{ activityDetail.categoryName }} / {{ formatStatus(activityDetail.status) }}</p>
                <span class="topic-badge">#{{ selectedActivityId }}</span>
              </div>
              <h3>{{ activityDetail.title }}</h3>
              <div class="headline-meta">
                <span>发起者 {{ activityDetail.organizerName }}</span>
                <span>{{ formatTime(activityDetail.startTime) }}</span>
                <span>{{ activityDetail.location }}</span>
              </div>
              <div class="topic-strip">
                <div>
                  <label>浏览入口</label>
                  <strong>{{ formatStatus(activityDetail.status) }} 活动页</strong>
                </div>
                <div>
                  <label>活动类型</label>
                  <strong>{{ activityDetail.categoryName }}</strong>
                </div>
                <div>
                  <label>参与人数</label>
                  <strong>{{ activityDetail.approvedCount }}/{{ activityDetail.capacity }}</strong>
                </div>
              </div>
            </header>

            <section class="post-intro">
              <div class="intro-band">
                <div class="intro-highlight">
                  <span class="intro-label">活动摘要</span>
                  <strong>{{ activityDetail.description || '当前活动暂无详细简介。' }}</strong>
                </div>
              </div>
              <div class="intro-grid">
                <div><label>发起者</label><strong>{{ activityDetail.organizerName }}</strong></div>
                <div><label>地点</label><strong>{{ activityDetail.location }}</strong></div>
                <div><label>开始时间</label><strong>{{ formatTime(activityDetail.startTime) }}</strong></div>
                <div><label>结束时间</label><strong>{{ formatTime(activityDetail.endTime) }}</strong></div>
                <div><label>报名截止</label><strong>{{ formatTime(activityDetail.registrationDeadline) }}</strong></div>
                <div><label>当前状态</label><strong>{{ formatStatus(activityDetail.status) }}</strong></div>
              </div>
            </section>

            <section class="post-body">
              <h4>活动简介</h4>
              <p>{{ activityDetail.description || '当前活动暂无详细简介。' }}</p>
            </section>

            <section class="guide-callout">
              <div class="section-head">
                <div>
                  <p class="eyebrow local">Activity Guide</p>
                  <h4>相关活动指南</h4>
                </div>
                <span class="muted-inline">发布者会把注意事项、准备建议和经验帖沉淀在活动指南里</span>
              </div>
              <div v-if="organizerGuides[0]" class="guide-entry-card primary-guide-card">
                <div>
                  <strong>{{ organizerGuides[0].title }}</strong>
                  <span>{{ organizerGuides[0].authorName }} · 活动须知 · {{ organizerGuides[0].commentCount }} 条评论</span>
                </div>
                <button class="primary-btn" @click="openGuide(organizerGuides[0].postId)">查看指南</button>
              </div>
              <div v-else-if="activityHotPosts[0]" class="guide-entry-card">
                <div>
                  <strong>{{ activityHotPosts[0].title }}</strong>
                  <span>{{ activityHotPosts[0].authorName }} · {{ activityHotPosts[0].commentCount }} 条评论</span>
                </div>
                <button class="ghost-btn" @click="openGuide(activityHotPosts[0].postId)">查看详情</button>
              </div>
              <div v-else class="empty-box small">当前活动暂时还没有发布活动指南。</div>
            </section>

            <section v-if="currentUser.roleCode === 'student'" class="post-actions">
              <div class="action-block">
                <h4>报名活动</h4>
                <input v-model="registerForm.remark" placeholder="可填写参与备注" />
                <button class="primary-btn" @click="submitRegister">提交报名</button>
              </div>
              <div v-if="activityDetail.status === 'finished'" class="action-block">
                <h4>活动反馈</h4>
                <select v-model="feedbackForm.rating">
                  <option :value="5">5 分</option>
                  <option :value="4">4 分</option>
                  <option :value="3">3 分</option>
                  <option :value="2">2 分</option>
                  <option :value="1">1 分</option>
                </select>
                <textarea v-model="feedbackForm.content" rows="3" placeholder="写下你的参与体验"></textarea>
                <button class="primary-btn" @click="submitFeedback">提交反馈</button>
              </div>
            </section>
          </article>
        </section>

        <section v-else-if="currentModule === 'guides'" class="guide-shell">
          <div v-if="currentTab.guides !== 'publish'" class="guide-selector-panel panel">
            <div class="section-head compact-head">
              <div>
                <p class="eyebrow local">Activity Guides</p>
                <h4>选择活动指南</h4>
              </div>
              <span class="muted-inline">点选后直接进入完整指南内容页</span>
            </div>
            <div class="guide-selector-strip">
              <button
                v-for="post in guideList"
                :key="post.postId"
                class="guide-selector-chip"
                :class="{ selected: selectedDiscussionPostId === post.postId }"
                @click="selectedDiscussionPostId = post.postId"
              >
                <strong>{{ post.title }}</strong>
                <span>{{ post.activityTitle }} · {{ post.authorName }}</span>
              </button>
            </div>
          </div>

          <article v-if="currentTab.guides !== 'publish' && selectedGuide" class="activity-post panel guide-detail-post">
            <header class="post-header forum-topic-header">
              <div class="forum-topic-meta">
                <p class="post-kicker">活动指南 / {{ selectedGuide.activityTitle }}</p>
                <span class="topic-badge">#{{ selectedGuide.postId }}</span>
              </div>
              <h3>{{ selectedGuide.title }}</h3>
              <div class="tag-cloud tag-strip">
                <span v-for="tag in selectedGuide.tags" :key="tag.tagId" class="tag-chip static" :style="{ borderColor: tag.colorHex, color: tag.colorHex }">
                  {{ tag.tagName }}
                </span>
              </div>
              <div class="headline-meta">
                <span>作者 {{ selectedGuide.authorName }}</span>
                <span>{{ selectedGuide.activityTitle }}</span>
                <span>{{ formatTime(selectedGuide.createdAt) }}</span>
              </div>
            </header>

            <section class="post-body">
              <h4>指南内容</h4>
              <p>{{ selectedGuide.content }}</p>
            </section>

            <section class="discussion-zone">
              <div class="section-head">
                <div>
                  <p class="eyebrow local">Guide Discussion</p>
                  <h4>评论区</h4>
                </div>
                <div class="muted-inline">{{ discussionComments.length }} 条评论 · {{ selectedGuide.likeCount }} 点赞</div>
              </div>

              <div class="thread-actions guide-actions">
                <button class="ghost-btn" @click="handleLike(selectedGuide.postId)">
                  {{ selectedGuide.likedByCurrentUser ? '取消点赞' : '点赞' }} · {{ selectedGuide.likeCount }}
                </button>
                <button class="ghost-btn" @click="openActivity(selectedGuide.activityId, selectedGuide.postId)">查看对应活动</button>
              </div>

              <div v-if="discussionComments.length === 0" class="empty-box">这篇活动指南还没有评论。</div>
              <div v-else class="comment-feed guide-comment-feed">
                <div v-for="comment in discussionComments" :key="comment.commentId" class="comment-card reply-card">
                  <strong>{{ comment.authorName }}</strong>
                  <span>{{ formatTime(comment.createdAt) }}</span>
                  <p>{{ comment.content }}</p>
                </div>
              </div>

              <div class="panel inner-panel composer-panel guide-comment-box">
                <div class="panel-head"><h4>发表评论</h4><span>可以补充自己的经验、提醒或现场观察</span></div>
                <div class="form-stack">
                  <textarea v-model="commentForm.content" rows="3" placeholder="补充集合提醒、准备经验、现场感受或其他有帮助的信息"></textarea>
                  <button class="primary-btn" @click="submitComment">发布评论</button>
                </div>
              </div>
            </section>
          </article>

          <div v-else class="panel guide-publish-panel guide-publish-only">
            <div class="panel-head"><h3>发布活动指南</h3><span>个人和组织者都可以发布</span></div>
            <div class="info-block guide-editor-note">
              <p>组织者更适合发布活动须知，例如集合地点、签到方式、着装要求、注意事项；学生更适合发布亲身经历、参与体验和准备建议。</p>
            </div>
            <div class="form-stack">
              <label>关联活动
                <select v-model="selectedActivityId">
                  <option v-for="item in registerableActivities" :key="item.activityId" :value="item.activityId">{{ item.title }}</option>
                </select>
              </label>
              <input v-model="discussionForm.title" placeholder="指南标题，例如：活动集合地点说明 / 签到注意事项 / 参加体验分享" />
              <div class="tag-cloud">
                <button
                  v-for="tag in forumTags"
                  :key="tag.tagId"
                  class="tag-chip"
                  :class="{ active: discussionForm.tagIds.includes(tag.tagId) }"
                  :style="{ borderColor: tag.colorHex, color: tag.colorHex }"
                  @click="discussionForm.tagIds = discussionForm.tagIds.includes(tag.tagId) ? discussionForm.tagIds.filter((id) => id !== tag.tagId) : [...discussionForm.tagIds, tag.tagId]"
                >
                  {{ tag.tagName }}
                </button>
              </div>
              <textarea v-model="discussionForm.content" rows="8" placeholder="示例：集合地点在图书馆东门，18:40 开始签到，请穿便于活动的服装；或写下自己参加后的真实体验、流程感受和给后来者的建议。"></textarea>
              <button class="primary-btn" @click="submitGuide">发布指南</button>
            </div>
          </div>
        </section>

        <section v-else-if="currentModule === 'archive'" class="two-column">
          <div class="panel archive-sidebar">
            <div class="section-head compact-head">
              <div>
                <p class="eyebrow local">Archive Shelf</p>
                <h4>归档精选</h4>
              </div>
              <span class="muted-inline">从评分和时间维度回看活动沉淀</span>
            </div>
            <div class="featured-list">
              <button
                v-for="item in sortedArchiveActivities.slice(0, 2)"
                :key="`archive-${item.activityId}`"
                class="featured-card archive-featured"
                @click="selectedArchiveActivityId = item.activityId"
              >
                <span class="featured-badge">均分 {{ item.averageRating ?? 0 }}</span>
                <strong>{{ item.title }}</strong>
                <p>{{ item.categoryName }} · {{ item.organizerName }}</p>
                <div class="featured-meta">
                  <span>{{ formatTime(item.endTime) }}</span>
                  <span>{{ item.feedbackCount }} 条学生评价</span>
                </div>
              </button>
            </div>

            <div class="panel-head side-head"><h3>往期活动目录</h3><span>{{ sortedArchiveActivities.length }} 场</span></div>
            <div class="stack-list">
              <button
                v-for="item in sortedArchiveActivities"
                :key="item.activityId"
                class="list-card archive-list-card"
                :class="{ selected: selectedArchiveActivityId === item.activityId }"
                @click="selectedArchiveActivityId = item.activityId"
              >
                <div>
                  <strong>{{ item.title }}</strong>
                  <span>{{ item.categoryName }} · {{ item.organizerName }}</span>
                </div>
                <div class="meta-column">
                  <span>{{ item.averageRating ?? 0 }} 分</span>
                  <span>{{ item.feedbackCount }} 条评价</span>
                </div>
              </button>
            </div>
          </div>
          <article class="archive-post panel" v-if="selectedArchiveActivity">
            <header class="archive-header">
              <p class="post-kicker">{{ selectedArchiveActivity.categoryName }} / 活动归档</p>
              <h3>{{ selectedArchiveActivity.title }}</h3>
              <div class="headline-meta">
                <span>发起者 {{ selectedArchiveActivity.organizerName }}</span>
                <span>{{ formatTime(selectedArchiveActivity.endTime) }}</span>
                <span>{{ selectedArchiveActivity.feedbackCount }} 条评价</span>
              </div>
            </header>

            <section class="archive-summary">
              <div class="archive-score-card">
                <span class="intro-label">综合口碑</span>
                <strong>{{ selectedArchiveActivity.averageRating ?? 0 }}</strong>
                <p>基于参加学生的活动评分与文字评价沉淀得出。</p>
              </div>
              <div class="archive-mini-grid">
                <div><label>活动分类</label><strong>{{ selectedArchiveActivity.categoryName }}</strong></div>
                <div><label>活动时间</label><strong>{{ formatTime(selectedArchiveActivity.endTime) }}</strong></div>
                <div><label>发起者</label><strong>{{ selectedArchiveActivity.organizerName }}</strong></div>
                <div><label>评价数量</label><strong>{{ selectedArchiveActivity.feedbackCount }}</strong></div>
              </div>
            </section>

            <section class="archive-body">
              <div class="section-head compact-head">
                <div>
                  <p class="eyebrow local">Student Feedback</p>
                  <h4>往期评价</h4>
                </div>
                <span class="muted-inline">活动结束后的体验回顾与口碑沉淀</span>
              </div>
              <div class="archive-review-list">
                <div v-for="item in archiveFeedbacks" :key="item.feedbackId" class="review-card">
                  <div class="review-top">
                    <div>
                      <strong>{{ item.realName }}</strong>
                      <span>{{ formatTime(item.createdAt) }}</span>
                    </div>
                    <div class="review-score">{{ item.rating }}/5</div>
                  </div>
                  <p>{{ item.content || '未填写文字评价' }}</p>
                </div>
              </div>
            </section>
          </article>
        </section>

        <section v-else-if="currentModule === 'my'" class="two-column">
          <div class="panel">
            <div class="panel-head"><h3>我的活动参与记录</h3><span>{{ myHistory.length }} 条</span></div>
            <div class="stack-list">
              <div v-for="item in myHistory" :key="`${item.userId}-${item.activityId}`" class="timeline-card">
                <strong>{{ item.title }}</strong>
                <span>{{ item.categoryName }} · {{ formatStatus(item.registrationStatus) }}</span>
                <p>{{ item.checkinResult ? formatStatus(item.checkinResult) : '未签到' }}</p>
                <p>我的评分：{{ item.rating ? `${item.rating}/5` : '未评分' }}</p>
              </div>
            </div>
          </div>
          <div class="panel">
            <div class="panel-head"><h3>参与说明</h3><span>前台用户视角</span></div>
            <div class="info-block">
              <p>这里聚焦普通学生的活动参与链路：浏览活动、完成报名、查看签到结果，并沉淀自己对历史活动的评分与反馈。</p>
            </div>
          </div>
        </section>

        <section v-else-if="currentModule === 'admin' && isManager" class="admin-grid">
          <div v-if="currentTab.admin === 'create'" class="panel">
            <div class="panel-head"><h3>发布活动</h3><span>后台能力</span></div>
            <div class="form-grid">
              <label>活动分类<select v-model="createForm.categoryId"><option v-for="category in categories" :key="category.categoryId" :value="category.categoryId">{{ category.categoryName }}</option></select></label>
              <label>活动标题<input v-model="createForm.title" /></label>
              <label>活动地点<input v-model="createForm.location" /></label>
              <label>开始时间<input v-model="createForm.startTime" type="datetime-local" /></label>
              <label>结束时间<input v-model="createForm.endTime" type="datetime-local" /></label>
              <label>报名截止<input v-model="createForm.registrationDeadline" type="datetime-local" /></label>
              <label>容量<input v-model="createForm.capacity" type="number" min="1" /></label>
              <label>状态<select v-model="createForm.status"><option value="draft">draft</option><option value="published">published</option></select></label>
              <label class="wide">活动描述<textarea v-model="createForm.description" rows="4"></textarea></label>
            </div>
            <button class="primary-btn" @click="submitCreateActivity">发布活动</button>
          </div>

          <div v-else-if="currentTab.admin === 'manage'" class="panel">
            <div class="panel-head"><h3>活动管理</h3><span>{{ selectedActivity?.title || '先在活动中心选择活动' }}</span></div>
            <div class="form-grid">
              <label>活动分类<select v-model="editForm.categoryId"><option v-for="category in categories" :key="category.categoryId" :value="category.categoryId">{{ category.categoryName }}</option></select></label>
              <label>活动标题<input v-model="editForm.title" /></label>
              <label>活动地点<input v-model="editForm.location" /></label>
              <label>开始时间<input v-model="editForm.startTime" type="datetime-local" /></label>
              <label>结束时间<input v-model="editForm.endTime" type="datetime-local" /></label>
              <label>报名截止<input v-model="editForm.registrationDeadline" type="datetime-local" /></label>
              <label>容量<input v-model="editForm.capacity" type="number" min="1" /></label>
              <label>活动状态<select v-model="editForm.status"><option value="draft">draft</option><option value="published">published</option><option value="cancelled">cancelled</option><option value="finished">finished</option></select></label>
              <label class="wide">活动描述<textarea v-model="editForm.description" rows="4"></textarea></label>
            </div>
            <button class="primary-btn" @click="submitUpdateActivity">保存活动信息</button>
            <div class="stats-table">
              <div class="stats-row header"><span>活动</span><span>报名</span><span>签到</span><span>签到率</span></div>
              <div v-for="item in statistics" :key="item.activityId" class="stats-row">
                <span>{{ item.title }}</span>
                <span>{{ item.registeredCount }}</span>
                <span>{{ item.checkinCount }}</span>
                <span>{{ item.checkinRatePercent ?? 0 }}%</span>
              </div>
            </div>
          </div>

          <div v-else class="panel">
            <div class="panel-head"><h3>签到管理</h3><span>只有活动发起者或管理员可操作</span></div>
            <div class="form-grid">
              <label>签到学生<select v-model="checkinForm.userId"><option v-for="user in users.filter((item) => item.roleCode === 'student')" :key="user.userId" :value="user.userId">{{ user.realName }}</option></select></label>
              <label>签到结果<select v-model="checkinForm.checkinResult"><option value="manual">manual</option><option value="late">late</option><option value="normal">normal</option></select></label>
              <label class="wide">备注<input v-model="checkinForm.note" placeholder="例如：社长现场补签" /></label>
            </div>
            <button class="primary-btn" @click="submitCheckin">提交补签</button>
            <div class="records-grid">
              <div>
                <h4>报名名单</h4>
                <div class="stack-list">
                  <div v-for="item in registrations" :key="item.registrationId" class="comment-card">
                    <strong>{{ item.realName }}</strong>
                    <span>{{ formatStatus(item.status) }}</span>
                  </div>
                </div>
              </div>
              <div>
                <h4>签到名单</h4>
                <div class="stack-list">
                  <div v-for="item in checkins" :key="item.checkinId" class="comment-card">
                    <strong>{{ item.realName }}</strong>
                    <span>{{ formatStatus(item.checkinResult) }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>
      </main>
    </div>
  </div>
</template>
