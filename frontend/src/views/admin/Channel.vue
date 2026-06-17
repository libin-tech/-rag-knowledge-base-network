<template>
  <div>
    <a-page-header title="消息渠道" sub-title="配置飞书、钉钉等消息渠道" />
    <div v-for="ch in channelData" :key="ch.type" class="channel-card">
      <div class="channel-header">
        <div class="channel-title">
          <img v-if="ch.useImg" :src="ch.icon" class="channel-icon" />
          <component v-else :is="ch.icon" class="channel-icon" />
          <h4>{{ ch.name }}</h4>
          <a-tag :color="ch.enabled ? 'green' : 'red'">{{ ch.enabled ? '已启用' : '已停用' }}</a-tag>
        </div>
        <div>
          <a-button :type="ch.enabled ? 'default' : 'primary'" size="small" danger v-if="ch.enabled" @click="toggleChannel(ch.type, false)">停用</a-button>
          <a-button type="primary" size="small" v-else @click="toggleChannel(ch.type, true)">启用</a-button>
          <a-button type="link" size="small" @click="ch.showConfig = !ch.showConfig">配置</a-button>
        </div>
      </div>
      <div v-show="ch.showConfig" class="config-section">
        <a-form :model="ch.config" layout="vertical">
          <a-row :gutter="16">
            <a-col :span="12" v-for="field in ch.fields" :key="field.name">
              <a-form-item :label="field.label">
                <a-input-password v-if="field.type === 'password'" v-model:value="ch.config[field.name]" :placeholder="field.placeholder" />
                <a-input v-else v-model:value="ch.config[field.name]" :placeholder="field.placeholder" />
              </a-form-item>
            </a-col>
          </a-row>
          <a-button type="primary" size="small" @click="saveConfig(ch)">保存</a-button>
        </a-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import {onMounted, reactive} from 'vue'
import {message} from 'ant-design-vue'
import {getChannelList, updateChannel} from '../../api'

const channelData = reactive([])

const channelTemplates = {
  FEISHU: {
    type: 'FEISHU',
    name: '飞书',
    icon: '/images/feishu_logo.png',
    useImg: true,
    showConfig: false,
    enabled: false,
    fields: [
      { name: 'appId', label: 'App ID', placeholder: '请输入飞书 App ID' },
      { name: 'appSecret', label: 'App Secret', placeholder: '请输入飞书 App Secret', type: 'password' }
    ],
    config: { appId: '', appSecret: '' }
  },
  DINGTALK: {
    type: 'DINGTALK',
    name: '钉钉',
    icon: '/images/dingtalk_logo.png',
    useImg: true,
    showConfig: false,
    enabled: false,
    fields: [
      { name: 'clientId', label: 'Client ID', placeholder: '请输入钉钉 Client ID' },
      { name: 'clientSecret', label: 'Client Secret', placeholder: '请输入钉钉 Client Secret', type: 'password' }
    ],
    config: { clientId: '', clientSecret: '' }
  },
  WECHAT_WORK: {
    type: 'WECHAT_WORK',
    name: '企业微信',
    icon: '/images/wechat_work_logo.png',
    useImg: true,
    showConfig: false,
    enabled: false,
    fields: [
      { name: 'corpId', label: '企业ID', placeholder: '请输入企业微信企业ID' },
      { name: 'agentId', label: '应用ID', placeholder: '请输入企业微信应用ID' },
      { name: 'corpSecret', label: '应用密钥', placeholder: '请输入企业微信应用密钥', type: 'password' }
    ],
    config: { corpId: '', agentId: '', corpSecret: '' }
  }
}

onMounted(() => {
  loadChannels()
})

async function loadChannels() {
  try {
    const kbId = localStorage.getItem('currentKnowledgeBaseId') || ''
    const res = await getChannelList(kbId)
    if (res.success) {
      channelData.length = 0
      for (const [type, tmpl] of Object.entries(channelTemplates)) {
        const existing = (res.data || []).find(c => c.channelType === type)
        const ch = reactive(JSON.parse(JSON.stringify(tmpl)))
        if (existing) {
          ch.enabled = existing.enabled || false
          const cfgJson = existing.configJson ? JSON.parse(existing.configJson) : {}
          Object.assign(ch.config, cfgJson)
        }
        channelData.push(ch)
      }
    }
  } catch (e) {
    console.error('加载渠道配置失败', e)
  }
}

async function toggleChannel(channelType, enabled) {
  try {
    const kbId = localStorage.getItem('currentKnowledgeBaseId') || ''
    const res = await updateChannel(channelType, { channelType, enabled, knowledgeBaseId: kbId, modifier: 'admin' })
    if (res.success) {
      message.success(enabled ? '已启用' : '已停用')
      loadChannels()
    } else {
      message.error(res.message || '操作失败')
    }
  } catch (e) {
    message.error('操作失败')
  }
}

async function saveConfig(ch) {
  try {
    const kbId = localStorage.getItem('currentKnowledgeBaseId') || ''
    const res = await updateChannel(ch.type, {
      configJson: JSON.stringify(ch.config),
      knowledgeBaseId: kbId,
      modifier: 'admin'
    })
    if (res.success) {
      message.success('保存成功')
    } else {
      message.error(res.message || '保存失败')
    }
  } catch (e) {
    message.error('保存失败')
  }
}
</script>

<style scoped>
.channel-card {
  background: white;
  padding: 24px;
  border-radius: 12px;
  margin-bottom: 16px;
  border-left: 4px solid #1677ff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}
.channel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.channel-title {
  display: flex;
  align-items: center;
  gap: 10px;
}
.channel-title h4 { margin: 0; color: #2d3748; }
.channel-icon { width: 40px; height: 40px; border-radius: 8px; object-fit: contain; }
.config-section {
  margin-top: 16px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 8px;
}
:deep(.ant-page-header) { background: white; border-radius: 12px; padding: 16px 24px; margin-bottom: 24px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
</style>
