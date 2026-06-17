<template>
  <a-layout style="min-height: 100vh">
    <a-layout-sider v-model:collapsed="collapsed" :trigger="null" collapsible class="sidebar">
      <div class="sidebar-brand">
        <h4><file-text-outlined /> RAG知识库</h4>
        <small>管理后台</small>
      </div>
      <div class="kb-selector">
        <label>当前知识库</label>
        <a-select v-model:value="currentKbId" style="width: 100%" @change="onKbChange">
          <a-select-option v-for="kb in kbList" :key="kb.id" :value="kb.id">{{ kb.name }}</a-select-option>
        </a-select>
      </div>
      <a-menu theme="dark" mode="inline" :selectedKeys="[selectedKey]" @click="onMenuClick">
        <a-menu-item key="upload">
          <cloud-upload-outlined /><span>文档上传</span>
        </a-menu-item>
        <a-menu-item key="documents">
          <file-text-outlined /><span>文档管理</span>
        </a-menu-item>
        <a-menu-item key="chat">
          <message-outlined /><span>问答测试</span>
        </a-menu-item>
        <a-menu-item key="channel">
          <apartment-outlined /><span>消息渠道</span>
        </a-menu-item>
      </a-menu>
    </a-layout-sider>
    <a-layout>
      <a-layout-header class="header">
        <div class="header-actions">
          <a-tooltip title="模型配置">
            <a-button type="text" class="header-btn" @click="router.push('/admin/config')">
              <setting-outlined />
            </a-button>
          </a-tooltip>
          <a-dropdown :trigger="['click']">
            <a-button type="text" class="header-btn user-btn">
              <a-avatar size="small" style="background: #1677ff;">
                <template #icon><user-outlined /></template>
              </a-avatar>
              <span class="user-name">{{ username }}</span>
            </a-button>
            <template #overlay>
              <a-menu>
                <a-menu-item @click="router.push('/admin/knowledge-base')">
                  <folder-outlined /> 知识库管理
                </a-menu-item>
                <a-menu-item @click="router.push('/admin/config')">
                  <setting-outlined /> 模型配置
                </a-menu-item>
                <a-menu-divider />
                <a-menu-item @click="handleLogout" danger>
                  <logout-outlined /> 退出登录
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </a-layout-header>
      <a-layout-content class="main-content">
        <router-view />
      </a-layout-content>
      <a-layout-footer class="layout-footer">
        &copy; 2026 bin.li.github@gmail.com. All Rights Reserved.
      </a-layout-footer>
    </a-layout>
  </a-layout>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {message} from 'ant-design-vue'
import {
  ApartmentOutlined,
  CloudUploadOutlined,
  FileTextOutlined,
  FolderOutlined,
  LogoutOutlined,
  MessageOutlined,
  SettingOutlined,
  UserOutlined
} from '@ant-design/icons-vue'
import {getKnowledgeBaseList, logout} from '../../api'

const router = useRouter()
const route = useRoute()
const collapsed = ref(false)
const currentKbId = ref('default')
const kbList = ref([])
const username = ref(localStorage.getItem('username') || '管理员')

const selectedKey = computed(() => {
  const path = route.path
  const parts = path.split('/')
  return parts[parts.length - 1] || 'upload'
})

onMounted(async () => {
  try {
    const data = await getKnowledgeBaseList()
    kbList.value = data || []
    if (kbList.value.length > 0) {
      currentKbId.value = kbList.value[0].id
    }
  } catch (e) {
    console.error('加载知识库失败', e)
  }
})

function onMenuClick({ key }) {
  router.push(`/admin/${key}`)
}

function onKbChange(value) {
  localStorage.setItem('currentKnowledgeBaseId', value)
}

async function handleLogout() {
  try {
    await logout()
  } catch (e) {
    console.error('登出请求失败', e)
  }
  localStorage.removeItem('satoken')
  localStorage.removeItem('username')
  message.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
.sidebar {
  background: #001529;
}

.sidebar-brand {
  padding: 20px 16px 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  margin-bottom: 12px;
}

.sidebar-brand h4 {
  color: white;
  margin: 0;
  font-size: 1.1rem;
}

.sidebar-brand small {
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.75rem;
}

.kb-selector {
  padding: 0 16px 12px;
}

.kb-selector label {
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.75rem;
  display: block;
  margin-bottom: 4px;
}

:deep(.kb-selector .ant-select) .ant-select-selector {
  background: rgba(255, 255, 255, 0.15) !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  color: white !important;
}

:deep(.kb-selector .ant-select-arrow) {
  color: rgba(255, 255, 255, 0.8);
}

.header {
  background: white;
  padding: 0 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  display: flex;
  align-items: center;
  justify-content: flex-end;
  height: 56px;
  line-height: 56px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: #1677ff;
}

.user-btn {
  width: auto;
  padding: 0 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  border-radius: 8px;
}

.user-name {
  color: #2d3748;
  font-size: 14px;
}

.main-content {
  margin: 0;
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 56px - 48px);
}

.layout-footer {
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  color: #94a3b8;
  font-size: 0.8rem;
  border-top: 1px solid #e2e8f0;
}

:deep(.ant-layout-sider-trigger) {
  background: rgba(0, 0, 0, 0.3);
}

:deep(.ant-menu-dark) {
  background: transparent;
}

:deep(.ant-menu-dark .ant-menu-item) {
  color: rgba(255, 255, 255, 0.8);
}

:deep(.ant-menu-dark .ant-menu-item-selected) {
  background: rgba(255, 255, 255, 0.2);
  color: white;
}

:deep(.ant-menu-dark .ant-menu-item:hover) {
  color: white;
}
</style>
