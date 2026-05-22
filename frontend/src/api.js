const TOKEN_KEY = 'campus_activity_token'

let authToken = localStorage.getItem(TOKEN_KEY) || ''

function buildHeaders(extraHeaders = {}) {
  const headers = {
    'Content-Type': 'application/json',
    ...extraHeaders
  }
  if (authToken) {
    headers.Authorization = `Bearer ${authToken}`
  }
  return headers
}

async function request(path, options = {}) {
  const response = await fetch(path, {
    headers: buildHeaders(options.headers),
    ...options
  })

  const data = await response.json()
  if (response.status === 401) {
    clearAuthToken()
  }
  if (!response.ok || data.success === false) {
    throw new Error(data.message || '请求失败')
  }
  return data.data
}

export function setAuthToken(token) {
  authToken = token
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearAuthToken() {
  authToken = ''
  localStorage.removeItem(TOKEN_KEY)
}

export function getStoredToken() {
  return authToken
}

export function login(payload) {
  return request('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}

export function getCurrentUser() {
  return request('/api/auth/me')
}

export function getActivities() {
  return request('/api/activities')
}

export function getActivityDetail(activityId) {
  return request(`/api/activities/${activityId}`)
}

export function getRegistrations(activityId) {
  return request(`/api/activities/${activityId}/registrations`)
}

export function getCheckins(activityId) {
  return request(`/api/activities/${activityId}/checkins`)
}

export function getFeedbacks(activityId) {
  return request(`/api/activities/${activityId}/feedbacks`)
}

export function getStatistics() {
  return request('/api/activities/statistics')
}

export function getDashboardSummary() {
  return request('/api/activities/dashboard-summary')
}

export function getCategories() {
  return request('/api/categories')
}

export function getUsers() {
  return request('/api/users')
}

export function getUserHistory(userId) {
  return request(`/api/users/${userId}/activities`)
}

export function createActivity(payload) {
  return request('/api/activities', {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}

export function updateActivity(activityId, payload) {
  return request(`/api/activities/${activityId}`, {
    method: 'PUT',
    body: JSON.stringify(payload)
  })
}

export function registerActivity(activityId, payload) {
  return request(`/api/activities/${activityId}/registrations`, {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}

export function checkinActivity(activityId, payload) {
  return request(`/api/activities/${activityId}/checkins`, {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}

export function createFeedback(activityId, payload) {
  return request(`/api/activities/${activityId}/feedbacks`, {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}

export function getForumTags() {
  return request('/api/forum/tags')
}

export function getForumPosts(params = {}) {
  const search = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== null && value !== undefined && value !== '') {
      search.set(key, value)
    }
  })
  return request(`/api/forum/posts${search.toString() ? `?${search}` : ''}`)
}

export function getHotPosts() {
  return request('/api/forum/hot')
}

export function createForumPost(payload) {
  return request('/api/forum/posts', {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}

export function getForumComments(postId) {
  return request(`/api/forum/posts/${postId}/comments`)
}

export function createForumComment(postId, payload) {
  return request(`/api/forum/posts/${postId}/comments`, {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}

export function toggleForumLike(postId) {
  return request(`/api/forum/posts/${postId}/likes`, {
    method: 'POST'
  })
}

export function getArchivedActivities() {
  return request('/api/archive/activities')
}
