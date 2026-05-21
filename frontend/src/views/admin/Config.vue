<template>
  <div class="config-page">
    <a-page-header title="模型配置" sub-title="配置 LLM 和 Embedding 模型参数" />
    <a-card title="LLM 配置" class="config-card">
      <template #extra>
        <a-tag :color="tagColor(llmMode)">{{ llmModeLabel }}</a-tag>
      </template>
      <a-form :model="llmForm" layout="vertical">
        <a-form-item label="模式选择">
          <a-select v-model:value="llmForm.mode" @change="onLlmModeChange">
            <a-select-option value="dashscope">DashScope (阿里云通义千问)</a-select-option>
            <a-select-option value="ollama">Ollama (本地部署)</a-select-option>
            <a-select-option value="openai">OpenAI (OpenAI协议模型)</a-select-option>
          </a-select>
        </a-form-item>
        <a-alert message="选择不同模式后，请填写对应配置的参数，然后点击保存按钮" type="info" show-icon style="margin-bottom:16px" />
        <div v-show="llmForm.mode === 'dashscope'">
          <a-row :gutter="16">
            <a-col :span="12">
              <a-form-item label="API Key">
                <a-input-password v-model:value="llmForm.dashscope_apiKey" placeholder="请输入阿里云API Key" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="模型名称">
                <a-input v-model:value="llmForm.dashscope_modelName" />
              </a-form-item>
            </a-col>
          </a-row>
        </div>
        <div v-show="llmForm.mode === 'ollama'">
          <a-row :gutter="16">
            <a-col :span="8">
              <a-form-item label="服务地址">
                <a-input v-model:value="llmForm.ollama_baseUrl" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="模型名称">
                <a-input v-model:value="llmForm.ollama_modelName" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="超时时间">
                <a-input v-model:value="llmForm.ollama_timeout" />
              </a-form-item>
            </a-col>
          </a-row>
        </div>
        <div v-show="llmForm.mode === 'openai'">
          <a-row :gutter="16">
            <a-col :span="12">
              <a-form-item label="API Key">
                <a-input-password v-model:value="llmForm.openai_apiKey" placeholder="请输入OpenAI API Key" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="服务地址">
                <a-input v-model:value="llmForm.openai_baseUrl" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="模型名称">
                <a-input v-model:value="llmForm.openai_modelName" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="超时时间">
                <a-input v-model:value="llmForm.openai_timeout" />
              </a-form-item>
            </a-col>
          </a-row>
        </div>
        <a-button type="primary" :loading="llmSaving" @click="saveLlmConfig">保存配置</a-button>
      </a-form>
    </a-card>

    <a-card title="Embedding 配置" class="config-card">
      <template #extra>
        <a-tag :color="tagColor(embedMode)">{{ embedModeLabel }}</a-tag>
      </template>
      <a-form :model="embedForm" layout="vertical">
        <a-form-item label="模式选择">
          <a-select v-model:value="embedForm.mode" @change="onEmbedModeChange">
            <a-select-option value="dashscope">DashScope (阿里云通义千问)</a-select-option>
            <a-select-option value="ollama">Ollama (本地部署)</a-select-option>
            <a-select-option value="openai">OpenAI (OpenAI协议模型)</a-select-option>
          </a-select>
        </a-form-item>
        <a-alert message="选择不同模式后，请填写对应配置的参数，然后点击保存按钮" type="info" show-icon style="margin-bottom:16px" />
        <div v-show="embedForm.mode === 'dashscope'">
          <a-row :gutter="16">
            <a-col :span="12">
              <a-form-item label="API Key">
                <a-input-password v-model:value="embedForm.dashscope_apiKey" placeholder="请输入阿里云API Key" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="模型名称">
                <a-input v-model:value="embedForm.dashscope_modelName" />
              </a-form-item>
            </a-col>
          </a-row>
        </div>
        <div v-show="embedForm.mode === 'ollama'">
          <a-row :gutter="16">
            <a-col :span="8">
              <a-form-item label="服务地址">
                <a-input v-model:value="embedForm.ollama_baseUrl" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="模型名称">
                <a-input v-model:value="embedForm.ollama_modelName" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="超时时间">
                <a-input v-model:value="embedForm.ollama_timeout" />
              </a-form-item>
            </a-col>
          </a-row>
        </div>
        <div v-show="embedForm.mode === 'openai'">
          <a-row :gutter="16">
            <a-col :span="12">
              <a-form-item label="API Key">
                <a-input-password v-model:value="embedForm.openai_apiKey" placeholder="请输入OpenAI API Key" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="服务地址">
                <a-input v-model:value="embedForm.openai_baseUrl" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="模型名称">
                <a-input v-model:value="embedForm.openai_modelName" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="超时时间">
                <a-input v-model:value="embedForm.openai_timeout" />
              </a-form-item>
            </a-col>
          </a-row>
        </div>
        <a-button type="primary" :loading="embedSaving" @click="saveEmbedConfig">保存配置</a-button>
      </a-form>
    </a-card>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { getLlmConfig, getEmbeddingConfig, updateLlmConfig, updateEmbeddingConfig } from '../../api'

const llmSaving = ref(false)
const embedSaving = ref(false)
const llmMode = ref('dashscope')
const embedMode = ref('dashscope')

const llmForm = reactive({
  mode: 'dashscope',
  dashscope_apiKey: '',
  dashscope_modelName: 'qwen-plus',
  ollama_baseUrl: 'http://127.0.0.1:11434',
  ollama_modelName: 'qwen3',
  ollama_timeout: '60s',
  openai_apiKey: '',
  openai_baseUrl: 'https://api.openai.com',
  openai_modelName: 'gpt-4o-mini',
  openai_timeout: '120s'
})

const embedForm = reactive({
  mode: 'dashscope',
  dashscope_apiKey: '',
  dashscope_modelName: 'text-embedding-v3',
  ollama_baseUrl: 'http://127.0.0.1:11434',
  ollama_modelName: 'nomic-embed-text',
  ollama_timeout: '60s',
  openai_apiKey: '',
  openai_baseUrl: 'https://api.openai.com',
  openai_modelName: 'text-embedding-3-small',
  openai_timeout: '120s'
})

const modeLabels = { dashscope: 'DashScope (阿里云)', ollama: 'Ollama (本地)', openai: 'OpenAI (协议模型)' }
const modeColors = { dashscope: 'gold', ollama: 'green', openai: 'blue' }

const llmModeLabel = computed(() => modeLabels[llmMode.value] || 'DashScope')
const embedModeLabel = computed(() => modeLabels[embedMode.value] || 'DashScope')

function tagColor(mode) { return modeColors[mode] || 'default' }

function onLlmModeChange(val) { llmMode.value = val }
function onEmbedModeChange(val) { embedMode.value = val }

onMounted(() => {
  loadLlmConfig()
  loadEmbedConfig()
})

async function loadLlmConfig() {
  try {
    const res = await getLlmConfig()
    if (res.success) {
      const cfg = res.data
      llmMode.value = cfg.mode || 'dashscope'
      Object.assign(llmForm, {
        mode: cfg.mode || 'dashscope',
        dashscope_apiKey: cfg.dashscope_apiKey || '',
        dashscope_modelName: cfg.dashscope_modelName || 'qwen-plus',
        ollama_baseUrl: cfg.ollama_baseUrl || 'http://127.0.0.1:11434',
        ollama_modelName: cfg.ollama_modelName || 'qwen3',
        ollama_timeout: cfg.ollama_timeout || '60s',
        openai_apiKey: cfg.openai_apiKey || '',
        openai_baseUrl: cfg.openai_baseUrl || 'https://api.openai.com',
        openai_modelName: cfg.openai_modelName || 'gpt-4o-mini',
        openai_timeout: cfg.openai_timeout || '120s'
      })
    }
  } catch (e) { console.error('加载LLM配置失败', e) }
}

async function loadEmbedConfig() {
  try {
    const res = await getEmbeddingConfig()
    if (res.success) {
      const cfg = res.data
      embedMode.value = cfg.mode || 'dashscope'
      Object.assign(embedForm, {
        mode: cfg.mode || 'dashscope',
        dashscope_apiKey: cfg.dashscope_apiKey || '',
        dashscope_modelName: cfg.dashscope_modelName || 'text-embedding-v3',
        ollama_baseUrl: cfg.ollama_baseUrl || 'http://127.0.0.1:11434',
        ollama_modelName: cfg.ollama_modelName || 'nomic-embed-text',
        ollama_timeout: cfg.ollama_timeout || '60s',
        openai_apiKey: cfg.openai_apiKey || '',
        openai_baseUrl: cfg.openai_baseUrl || 'https://api.openai.com',
        openai_modelName: cfg.openai_modelName || 'text-embedding-3-small',
        openai_timeout: cfg.openai_timeout || '120s'
      })
    }
  } catch (e) { console.error('加载Embedding配置失败', e) }
}

async function saveLlmConfig() {
  llmSaving.value = true
  try {
    const configs = [
      { key: 'mode', value: llmForm.mode },
      { key: 'dashscope_apiKey', value: llmForm.dashscope_apiKey },
      { key: 'dashscope_modelName', value: llmForm.dashscope_modelName },
      { key: 'ollama_baseUrl', value: llmForm.ollama_baseUrl },
      { key: 'ollama_modelName', value: llmForm.ollama_modelName },
      { key: 'ollama_timeout', value: llmForm.ollama_timeout },
      { key: 'openai_apiKey', value: llmForm.openai_apiKey },
      { key: 'openai_baseUrl', value: llmForm.openai_baseUrl },
      { key: 'openai_modelName', value: llmForm.openai_modelName },
      { key: 'openai_timeout', value: llmForm.openai_timeout }
    ]
    await Promise.all(configs.map(c => updateLlmConfig(c.key, c.value)))
    message.success('LLM配置保存成功，模型已重新加载')
    loadLlmConfig()
  } catch (e) {
    message.error('保存LLM配置失败')
  } finally {
    llmSaving.value = false
  }
}

async function saveEmbedConfig() {
  embedSaving.value = true
  try {
    const configs = [
      { key: 'mode', value: embedForm.mode },
      { key: 'dashscope_apiKey', value: embedForm.dashscope_apiKey },
      { key: 'dashscope_modelName', value: embedForm.dashscope_modelName },
      { key: 'ollama_baseUrl', value: embedForm.ollama_baseUrl },
      { key: 'ollama_modelName', value: embedForm.ollama_modelName },
      { key: 'ollama_timeout', value: embedForm.ollama_timeout },
      { key: 'openai_apiKey', value: embedForm.openai_apiKey },
      { key: 'openai_baseUrl', value: embedForm.openai_baseUrl },
      { key: 'openai_modelName', value: embedForm.openai_modelName },
      { key: 'openai_timeout', value: embedForm.openai_timeout }
    ]
    await Promise.all(configs.map(c => updateEmbeddingConfig(c.key, c.value)))
    message.success('Embedding配置保存成功，模型已重新加载')
    loadEmbedConfig()
  } catch (e) {
    message.error('保存Embedding配置失败')
  } finally {
    embedSaving.value = false
  }
}
</script>

<style scoped>
.config-card { margin-bottom: 24px; }
:deep(.ant-page-header) { background: white; border-radius: 12px; padding: 16px 24px; margin-bottom: 24px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
</style>
