<template>
  <div class="chat-page">
    <a-page-header title="问答测试" sub-title="测试 RAG 系统的问答效果" />
    <div class="chat-container">
      <div class="chat-messages" ref="chatRef">
        <div v-if="messages.length === 0" class="empty-state">
          <message-outlined />
          <h5>开始对话</h5>
          <p>在下方输入框中输入您的问题，测试问答功能</p>
        </div>
        <div v-for="(msg, i) in messages" :key="i" :class="['message', msg.role]">
          <div class="msg-avatar" :class="msg.role">
            <user-outlined v-if="msg.role === 'user'" />
            <robot-outlined v-else />
          </div>
          <div class="msg-content" :class="{ error: msg.isError }">
            <div v-if="msg.role === 'assistant' && msg.thinking" class="thinking-section">
              <a-collapse ghost :expandIconPosition="'end'">
                <a-collapse-panel key="thinking">
                  <template #header><bulb-outlined /> 思考过程</template>
                  <div class="thinking-content" v-html="msg.thinkingHtml" />
                </a-collapse-panel>
              </a-collapse>
            </div>
            <div v-if="msg.role === 'assistant' && !msg.content && streaming" class="thinking-text">
              <a-spin size="small" />
              <span>思考中...</span>
            </div>
            <div v-if="msg.content" class="msg-text" v-html="msg.html" />
            <div v-if="msg.role === 'assistant' && msg.content && streaming" class="stream-cursor" />
            <div v-if="msg.role === 'assistant' && msg.tokenUsage" class="token-info">
              <span>提示: <strong>{{ msg.tokenUsage.promptTokens }}</strong></span>
              <span>回答: <strong>{{ msg.tokenUsage.completionTokens }}</strong></span>
              <span>总计: <strong>{{ msg.tokenUsage.totalTokens }}</strong></span>
            </div>
          </div>
        </div>
      </div>
      <div class="chat-input-area">
        <a-textarea
          v-model:value="question"
          placeholder="输入您的问题..."
          :rows="2"
          @pressEnter="sendMessage"
          :disabled="streaming"
        />
        <a-button type="primary" :loading="streaming" @click="sendMessage" class="send-btn">
          <template #icon><send-outlined /></template>
          发送
        </a-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import {
  RobotOutlined, UserOutlined, MessageOutlined, SendOutlined, BulbOutlined
} from '@ant-design/icons-vue'
import { marked } from 'marked'

const question = ref('')
const messages = ref([])
const streaming = ref(false)
const chatRef = ref(null)

marked.setOptions({
  breaks: true,
  gfm: true
})

async function sendMessage() {
  const text = question.value.trim()
  if (!text || streaming.value) return
  question.value = ''

  addMessage(text, 'user')

  streaming.value = true

  try {
    const kbId = localStorage.getItem('currentKnowledgeBaseId') || 'default'
    const token = localStorage.getItem('satoken') || ''
    const response = await fetch('/admin/api/query/stream', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Authorization': token },
      body: JSON.stringify({ question: text, knowledgeBaseId: kbId })
    })

    if (!response.ok) throw new Error('请求失败')

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    messages.value.push({
      role: 'assistant',
      content: '',
      html: '',
      thinking: '',
      thinkingHtml: '',
      tokenUsage: null,
      isError: false
    })

    let currentEvent = ''
    let currentData = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })

      let newlineIdx
      while ((newlineIdx = buffer.indexOf('\n')) !== -1) {
        const line = buffer.substring(0, newlineIdx).trim()
        buffer = buffer.substring(newlineIdx + 1)

        if (line.startsWith('event:')) {
          currentEvent = line.substring(6).trim()
        } else if (line.startsWith('data:')) {
          currentData = line.substring(5).trim()
          if (currentEvent && currentData) {
            try {
              handleEvent(currentEvent, JSON.parse(currentData))
            } catch (e) { /* ignore parse errors */ }
            currentEvent = ''
            currentData = ''
          }
        } else if (!line && currentEvent && currentData) {
          try {
            handleEvent(currentEvent, JSON.parse(currentData))
          } catch (e) { /* ignore parse errors */ }
          currentEvent = ''
          currentData = ''
        }
      }
    }
  } catch (e) {
    if (!streaming.value) return;
    const lastMsg = messages.value[messages.value.length - 1]
    if (lastMsg && lastMsg.role === 'assistant' && lastMsg.content === '') {
      lastMsg.isError = true
      lastMsg.content = '请求失败：' + e.message
      lastMsg.html = lastMsg.content
    } else {
      addMessage('请求失败：' + e.message, 'assistant', true)
    }
  } finally {
    clearTimeout(timeoutId)
    streaming.value = false
    scrollToBottom()
  }
}

function handleEvent(eventType, data) {
  const lastMsg = messages.value[messages.value.length - 1]

  if (eventType === 'thinking') {
    lastMsg.thinking = data.thinking || ''
    lastMsg.thinkingHtml = marked.parse(lastMsg.thinking)
    scrollToBottom()
  } else if (eventType === 'message') {
    lastMsg.content = data.fullAnswer || ''
    lastMsg.html = marked.parse(lastMsg.content)
    scrollToBottom()
  } else if (eventType === 'done') {
    lastMsg.content = data.fullAnswer || ''
    lastMsg.html = marked.parse(lastMsg.content)
    lastMsg.tokenUsage = data.tokenUsage
    streaming.value = false
    scrollToBottom()
  } else if (eventType === 'error') {
    lastMsg.isError = true
    lastMsg.content = '抱歉，出现错误：' + (data.message || '未知错误')
    lastMsg.html = lastMsg.content
    streaming.value = false
    scrollToBottom()
  }
}

function addMessage(content, role, isError = false) {
  const html = role === 'assistant' ? marked.parse(content) : content
  messages.value.push({ role, content, html, thinking: '', thinkingHtml: '', tokenUsage: null, isError })
  scrollToBottom()
}

function scrollToBottom() {
  nextTick(() => {
    if (chatRef.value) {
      chatRef.value.scrollTop = chatRef.value.scrollHeight
    }
  })
}
</script>

<style scoped>
.chat-page {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 100px);
}
.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  overflow: hidden;
}
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f7fafc;
}
.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: #a0aec0;
}
.empty-state h5 { margin-top: 12px; }
.message {
  margin-bottom: 20px;
  display: flex;
  animation: fadeIn 0.3s;
}
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
.message.user { justify-content: flex-end; }
.message.assistant { justify-content: flex-start; }
.msg-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 10px;
  flex-shrink: 0;
}
.msg-avatar.user { background: #e2e8f0; color: #4a5568; order: 1; }
.msg-avatar.assistant { background: linear-gradient(135deg, #667eea, #764ba2); color: white; }
.msg-content {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 12px;
  word-wrap: break-word;
}
.message.user .msg-content { background: #4f46e5; color: white; border-bottom-right-radius: 4px; }
.message.assistant .msg-content { background: white; color: #2d3748; border-bottom-left-radius: 4px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
.msg-content.error { background: #fed7d7 !important; color: #c53030 !important; }
.thinking-section { margin-bottom: 8px; border-bottom: 1px dashed #e2e8f0; padding-bottom: 8px; }
.thinking-content { font-size: 0.9rem; color: #718096; background: #f8fafc; padding: 8px 12px; border-radius: 8px; border-left: 3px solid #667eea; }
.thinking-text { display: flex; align-items: center; gap: 8px; color: #718096; font-size: 0.9rem; padding: 4px 0; }
.stream-cursor { display: inline-block; width: 2px; height: 1em; background: #4f46e5; margin-left: 2px; animation: blink 0.8s step-end infinite; vertical-align: text-bottom; }
@keyframes blink { 50% { opacity: 0; } }
.token-info {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #e2e8f0;
  font-size: 0.8rem;
  color: #718096;
  display: flex;
  gap: 12px;
}
.chat-input-area {
  padding: 12px 16px;
  background: white;
  border-top: 1px solid #e2e8f0;
  display: flex;
  gap: 12px;
  align-items: flex-start;
}
.chat-input-area textarea { flex: 1; }
.send-btn { height: 70px; }
:deep(.ant-page-header) {
  background: white;
  border-radius: 12px;
  padding: 16px 24px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  flex-shrink: 0;
}
:deep(.msg-text pre) { background: #f6f8fa; border-radius: 8px; padding: 16px; overflow: auto; position: relative; }
:deep(.msg-text code) { background: #f6f8fa; padding: 2px 6px; border-radius: 4px; font-size: 0.9em; color: #e83e8c; }
:deep(.msg-text pre code) { background: transparent; padding: 0; color: inherit; }
:deep(.msg-text table) { width: 100%; border-collapse: collapse; }
:deep(.msg-text th, .msg-text td) { border: 1px solid #e2e8f0; padding: 8px; }
:deep(.msg-text blockquote) { border-left: 4px solid #4f46e5; padding-left: 16px; margin: 12px 0; color: #6c757d; }
</style>
