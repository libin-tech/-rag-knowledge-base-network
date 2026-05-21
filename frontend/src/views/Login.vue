<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <div class="logo">
          <file-text-outlined />
        </div>
        <h2>RAG知识库</h2>
        <p>管理后台登录</p>
      </div>
      <a-form :model="form" @finish="handleLogin" layout="vertical" autocomplete="off">
        <a-form-item name="username" :rules="[{ required: true, message: '请输入用户名' }]">
          <a-input v-model:value="form.username" size="large" placeholder="用户名">
            <template #prefix><user-outlined /></template>
          </a-input>
        </a-form-item>
        <a-form-item name="password" :rules="[{ required: true, message: '请输入密码' }]">
          <a-input-password v-model:value="form.password" size="large" placeholder="密码">
            <template #prefix><lock-outlined /></template>
          </a-input-password>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit" :loading="loading" block size="large">
            登录
          </a-button>
        </a-form-item>
      </a-form>
      <a-alert v-if="errorMsg" :message="errorMsg" type="error" show-icon closable />
    </div>
    <div class="copyright">©2026@bin.li.github@gmail.com. All Rights Reserved.</div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { UserOutlined, LockOutlined, FileTextOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { login } from '../api'

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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
  background: white;
  border-radius: 16px;
  padding: 40px 32px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  animation: slideUp 0.5s ease-out;
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo {
  font-size: 64px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1;
  margin-bottom: 16px;
}

.login-header h2 {
  margin: 0 0 4px;
  color: #2d3748;
  font-size: 1.8rem;
}

.login-header p {
  margin: 0;
  color: #718096;
  font-size: 0.9rem;
}

.copyright {
  margin-top: 24px;
  color: rgba(255, 255, 255, 0.85);
  font-size: 0.8rem;
}
</style>
