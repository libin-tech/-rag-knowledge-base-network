import axios from 'axios'

const request = axios.create({
  baseURL: '',
  timeout: 60000
})

request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('satoken')
    if (token) {
      config.headers['Authorization'] = token
    }
    return config
  },
  error => Promise.reject(error)
)

request.interceptors.response.use(
  response => response.data,
  error => {
    if (error.response && error.response.status === 401) {
      const url = error.response.config?.url || ''
      if (!url.includes('/api/auth/login')) {
        localStorage.removeItem('satoken')
        localStorage.removeItem('username')
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

export function login(username, password) {
  return request.post('/api/auth/login', { username, password })
}

export function logout() {
  return request.get('/api/auth/logout')
}

export function getUserInfo() {
  return request.get('/api/auth/info')
}

export function getKnowledgeBaseList() {
  return request.get('/admin/api/knowledge-base/list')
}

export function getAllKnowledgeBases() {
  return request.get('/admin/api/knowledge-base/all')
}

export function getKnowledgeBase(id) {
  return request.get(`/admin/api/knowledge-base/${id}`)
}

export function createKnowledgeBase(data) {
  return request.post('/admin/api/knowledge-base', data)
}

export function updateKnowledgeBase(id, data) {
  return request.put(`/admin/api/knowledge-base/${id}`, data)
}

export function deleteKnowledgeBase(id, deleteRelated = false) {
  return request.delete(`/admin/api/knowledge-base/${id}`, { params: { deleteRelated } })
}

export function checkKnowledgeBaseDelete(id) {
  return request.get(`/admin/api/knowledge-base/${id}/check-delete`)
}

export function checkKnowledgeBaseName(name, excludeId) {
  const params = { name }
  if (excludeId) params.excludeId = excludeId
  return request.get('/admin/api/knowledge-base/check-name', { params })
}

export function uploadDocument(formData) {
  return request.post('/admin/api/upload/batch', formData)
}

export function getDocuments(params) {
  return request.get('/admin/api/documents', { params })
}

export function deleteDocument(id, password) {
  return request.delete(`/admin/api/document/${id}`, { data: { password } })
}

export function getDocumentPreviewUrl(id) {
  const token = localStorage.getItem('satoken') || ''
  return `/admin/api/document/${id}/preview?Authorization=${encodeURIComponent(token)}`
}

export function getLlmConfig() {
  return request.get('/admin/api/config/llm')
}

export function updateLlmConfig(configKey, configValue) {
  return request.put('/admin/api/config/llm', { configKey, configValue, modifier: 'admin' })
}

export function getEmbeddingConfig() {
  return request.get('/admin/api/config/embedding')
}

export function updateEmbeddingConfig(configKey, configValue) {
  return request.put('/admin/api/config/embedding', { configKey, configValue, modifier: 'admin' })
}

export function getChannelList(knowledgeBaseId) {
  return request.get('/admin/api/channel/list', { params: { knowledgeBaseId } })
}

export function getChannel(channelType) {
  return request.get(`/admin/api/channel/${channelType}`)
}

export function updateChannel(channelType, data) {
  return request.put(`/admin/api/channel/${channelType}`, data)
}

export function queryQuestion(data) {
  return request.post('/admin/api/query', data)
}

export function createStreamQuery(data) {
  const token = localStorage.getItem('satoken') || ''
  const params = {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': token
    },
    body: JSON.stringify(data)
  }
  return fetch('/admin/api/query/stream', params)
}

export default request
