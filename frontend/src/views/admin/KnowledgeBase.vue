<template>
  <div>
    <a-page-header title="知识库管理" sub-title="管理系统中的知识库">
      <template #extra>
        <a-button type="primary" @click="showCreateModal"><plus-outlined /> 新建知识库</a-button>
      </template>
    </a-page-header>
    <a-card>
      <a-table :columns="columns" :data-source="dataSource" :loading="loading" row-key="id" :pagination="false">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'name'">
            <strong>{{ record.name }}</strong>
            <a-tag v-if="record.id === 'default'" color="default" style="margin-left:8px">默认</a-tag>
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="record.enabled ? 'green' : 'red'">{{ record.enabled ? '已启用' : '已禁用' }}</a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-button type="link" size="small" @click="showEditModal(record)">编辑</a-button>
            <a-popconfirm
              v-if="record.id !== 'default'"
              title="确定要删除此知识库吗？此操作不可撤销。"
              @confirm="handleDelete(record)"
              ok-text="确认删除"
              cancel-text="取消"
            >
              <a-button type="link" danger size="small">删除</a-button>
            </a-popconfirm>
          </template>
        </template>
      </a-table>
    </a-card>
    <a-modal v-model:open="modalVisible" :title="isEdit ? '编辑知识库' : '新建知识库'" @ok="saveKnowledgeBase" :confirmLoading="saving">
      <a-form :model="form" layout="vertical">
        <a-form-item label="知识库名称" required>
          <a-input v-model:value="form.name" placeholder="请输入知识库名称" />
        </a-form-item>
        <a-form-item label="描述">
          <a-textarea v-model:value="form.description" placeholder="请输入知识库描述" :rows="3" />
        </a-form-item>
        <a-form-item>
          <a-checkbox v-model:checked="form.enabled">启用</a-checkbox>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue'
import {message} from 'ant-design-vue'
import {PlusOutlined} from '@ant-design/icons-vue'
import {
  checkKnowledgeBaseDelete,
  createKnowledgeBase,
  deleteKnowledgeBase,
  getAllKnowledgeBases,
  updateKnowledgeBase
} from '../../api'

const loading = ref(false)
const dataSource = ref([])
const modalVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const form = reactive({
  name: '',
  description: '',
  enabled: true
})
const editingId = ref(null)

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 220 },
  { title: '名称', dataIndex: 'name', key: 'name' },
  { title: '描述', dataIndex: 'description', key: 'description' },
  { title: '状态', key: 'status' },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime' },
  { title: '操作', key: 'action' }
]

onMounted(() => {
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    const res = await getAllKnowledgeBases()
    dataSource.value = (res || []).map(kb => ({
      ...kb,
      createTime: formatDateTime(kb.createTime)
    }))
  } catch (e) {
    message.error('加载知识库失败')
  } finally {
    loading.value = false
  }
}

function formatDateTime(dateStr) {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  return d.toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

function showCreateModal() {
  isEdit.value = false
  editingId.value = null
  form.name = ''
  form.description = ''
  form.enabled = true
  modalVisible.value = true
}

async function showEditModal(record) {
  isEdit.value = true
  editingId.value = record.id
  form.name = record.name
  form.description = record.description || ''
  form.enabled = record.enabled
  modalVisible.value = true
}

async function saveKnowledgeBase() {
  if (!form.name.trim()) {
    message.warning('请输入知识库名称')
    return
  }
  saving.value = true
  try {
    const data = {
      name: form.name,
      description: form.description,
      enabled: form.enabled,
      creator: 'admin',
      modifier: 'admin'
    }
    if (isEdit.value) {
      const res = await updateKnowledgeBase(editingId.value, data)
      if (res.success !== false) {
        message.success('更新成功')
      } else {
        message.error(res.message || '更新失败')
        return
      }
    } else {
      data.id = 'kb_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
      const res = await createKnowledgeBase(data)
      if (res.success !== false) {
        message.success('创建成功')
      } else {
        message.error(res.message || '创建失败')
        return
      }
    }
    modalVisible.value = false
    loadData()
  } catch (e) {
    message.error('保存失败')
  } finally {
    saving.value = false
  }
}

async function handleDelete(record) {
  try {
    const check = await checkKnowledgeBaseDelete(record.id)
    if (check.success === false) {
      message.error(check.message)
      return
    }
    const canDelete = check.canDelete
    if (!canDelete) {
      const msg = `知识库 "${record.name}" 下存在相关数据，确定要删除吗？此操作不可撤销。`
      if (!confirm(msg)) return
      const res = await deleteKnowledgeBase(record.id, true)
      if (res.success) {
        message.success('删除成功')
        loadData()
      } else {
        message.error(res.message || '删除失败')
      }
    } else {
      const res = await deleteKnowledgeBase(record.id)
      if (res.success) {
        message.success('删除成功')
        loadData()
      } else {
        message.error(res.message || '删除失败')
      }
    }
  } catch (e) {
    message.error('删除失败')
  }
}
</script>

<style scoped>
:deep(.ant-page-header) { background: white; border-radius: 12px; padding: 16px 24px; margin-bottom: 24px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
</style>
