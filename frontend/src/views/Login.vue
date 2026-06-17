<template>
  <div class="login-container">
    <div class="login-content">
      <div class="login-top">
        <div class="login-header">
          <span class="logo-text">RAG 知识库</span>
          <span class="logo-sub">管理后台</span>
        </div>
        <div class="login-desc">企业级智能知识库问答系统</div>
      </div>

      <div class="login-card">
        <a-tabs centered class="login-tabs">
          <a-tab-pane key="account" tab="账号密码登录" />
        </a-tabs>

        <a-form
          :model="form"
          @finish="handleLogin"
          autocomplete="off"
          class="login-form"
        >
          <a-form-item
            name="username"
            :rules="[{ required: true, message: '请输入用户名' }]"
          >
            <a-input
              v-model:value="form.username"
              size="large"
              placeholder="用户名: admin"
            >
              <template #prefix>
                <user-outlined class="prefix-icon" />
              </template>
            </a-input>
          </a-form-item>

          <a-form-item
            name="password"
            :rules="[{ required: true, message: '请输入密码' }]"
          >
            <a-input-password
              v-model:value="form.password"
              size="large"
              placeholder="密码: admin@2026"
            >
              <template #prefix>
                <lock-outlined class="prefix-icon" />
              </template>
            </a-input-password>
          </a-form-item>

          <a-form-item>
            <a-button
              type="primary"
              html-type="submit"
              :loading="loading"
              block
              size="large"
              class="login-btn"
            >
              登 录
            </a-button>
          </a-form-item>
        </a-form>

        <a-alert
          v-if="errorMsg"
          :message="errorMsg"
          type="error"
          show-icon
          closable
          class="error-alert"
        />
      </div>

      <div class="intro-section">
        <div class="intro-item">
          <div class="intro-icon"><database-outlined /></div>
          <span>文档智能解析<br/>向量化存储</span>
        </div>
        <div class="intro-item">
          <div class="intro-icon"><search-outlined /></div>
          <span>语义检索增强<br/>精准问答</span>
        </div>
        <div class="intro-item">
          <div class="intro-icon"><robot-outlined /></div>
          <span>多平台接入<br/>飞书 / 钉钉</span>
        </div>
      </div>
    </div>

    <div class="login-footer">
      <span>&copy; 2026 bin.li.github@gmail.com. All Rights Reserved.</span>
    </div>
  </div>
</template>

<script setup>
import {reactive, ref} from 'vue'
import {useRouter} from 'vue-router'
import {DatabaseOutlined, LockOutlined, RobotOutlined, SearchOutlined, UserOutlined} from '@ant-design/icons-vue'
import {message} from 'ant-design-vue'
import {login} from '../api'

const router = useRouter()
const loading = ref(false)
const errorMsg = ref('')
const form = reactive({
  username: '',
  password: ''
})

async function handleLogin() {
  loading.value = true
  errorMsg.value = ''
  try {
    const res = await login(form.username, form.password)
    if (res.success) {
      localStorage.setItem('satoken', res.data.token)
      localStorage.setItem('username', res.data.username)
      message.success('登录成功')
      router.push('/admin/upload')
    } else {
      errorMsg.value = res.message || '登录失败'
    }
  } catch (e) {
    errorMsg.value = e.response?.data?.message || '登录失败，请检查网络连接'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;
  background-image: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='1'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
}

.login-content {
  width: 368px;
  margin-bottom: 40px;
}

.intro-section {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}

.intro-item {
  flex: 1;
  background: #fff;
  border-radius: 6px;
  padding: 16px 12px;
  text-align: center;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.intro-icon {
  font-size: 22px;
  color: #1677ff;
  margin-bottom: 8px;
}

.intro-item span {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.45);
  line-height: 1.6;
}

.login-top {
  text-align: center;
  margin-bottom: 40px;
}

.login-header {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 12px;
  margin-bottom: 12px;
}

.logo-text {
  font-size: 28px;
  font-weight: 600;
  color: rgba(0, 0, 0, 0.85);
}

.logo-sub {
  font-size: 14px;
  color: rgba(0, 0, 0, 0.45);
}

.login-desc {
  font-size: 14px;
  color: rgba(0, 0, 0, 0.45);
}

.login-card {
  background: #fff;
  padding: 32px 24px 24px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.login-tabs {
  margin-bottom: 24px;
}

.login-form {
  max-width: 100%;
}

.login-form :deep(.ant-input-affix-wrapper) {
  border-radius: 6px;
}

.login-form :deep(.ant-input) {
  font-size: 14px;
}

.prefix-icon {
  color: rgba(0, 0, 0, 0.25);
}

.login-btn {
  height: 40px;
  border-radius: 6px;
  font-size: 16px;
}

.error-alert {
  margin-top: 16px;
}

.login-footer {
  position: absolute;
  bottom: 40px;
  width: 100%;
  text-align: center;
  color: rgba(0, 0, 0, 0.45);
  font-size: 13px;
}
</style>
