<template>
  <div>
    <a-page-header title="文档管理" sub-title="查看和管理已上传的知识库文档">
      <template #extra>
        <a-button @click="loadDocuments"><template #icon><reload-outlined /></template>刷新</a-button>
      </template>
    </a-page-header>
    <a-row :gutter="16" class="stats-row">
      <a-col :span="8">
        <a-card class="stats-card primary">
          <h3>{{ totalElements }}</h3>
          <p>已上传文档</p>
        </a-card>
      </a-col>
      <a-col :span="8">
        <a-card class="stats-card success">
          <h3>{{ totalElements }}</h3>
          <p>总文件数</p>
        </a-card>
      </a-col>
      <a-col :span="8">
        <a-card class="stats-card danger">
          <h3>PDF</h3>
          <p>文档类型</p>
        </a-card>
      </a-col>
    </a-row>
    <a-card>
      <a-table
        :columns="columns"
        :data-source="dataSource"
        :pagination="pagination"
        :loading="loading"
        row-key="id"
        @change="onTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-badge status="success" text="已上传" />
          </template>
          <template v-if="column.key === 'action'">
            <a-button type="link" size="small" @click="previewDoc(record.id)">预览</a-button>
            <a-button type="link" danger size="small" @click="showDeleteConfirm(record)">删除</a-button>
          </template>
        </template>
      </a-table>
    </a-card>
    <a-modal v-model:open="previewVisible" title="PDF 预览" width="80%" :footer="null" :destroyOnClose="true">
      <iframe :src="previewUrl" style="width:100%; height:80vh; border:none;" />
    </a-modal>
    <a-modal v-model:open="deleteVisible" title="删除确认" @ok="confirmDelete" :confirmLoading="deleteLoading">
      <p>将删除：<strong>{{ deleteTarget?.filename }}</strong></p>
      <a-input-password v-model:value="deletePassword" placeholder="请输入管理员密码确认删除" />
      <div style="margin-top: 8px"><a-checkbox v-model:checked="deleteConfirm">我确认将同时删除 pgsql、minio 与向量库数据</a-checkbox></div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { ReloadOutlined } from '@ant-design/icons-vue'
import { getDocuments, deleteDocument, getDocumentPreviewUrl } from '../../api'

const loading = ref(false)
const dataSource = ref([])
const totalElements = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const totalPages = ref(1)
const previewVisible = ref(false)
const previewUrl = ref('')
const deleteVisible = ref(false)
const deleteTarget = ref(null)
const deletePassword = ref('')
const deleteConfirm = ref(false)
const deleteLoading = ref(false)

const columns = [
  { title: '文件名', dataIndex: 'filename', key: 'filename' },
  { title: '大小', dataIndex: 'size', key: 'size' },
  { title: '上传时间', dataIndex: 'uploadTime', key: 'uploadTime' },
  { title: '分段数', dataIndex: 'segmentCount', key: 'segmentCount' },
  { title: '状态', key: 'status' },
  { title: '操作', key: 'action' }
]

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: total => `共 ${total} 条`
})

onMounted(() => {
  loadDocuments()
})

async function loadDocuments() {
  loading.value = true
  try {
    const kbId = localStorage.getItem('currentKnowledgeBaseId') || ''
    const params = { page: currentPage.value, size: pageSize.value }
    if (kbId) params.knowledgeBaseId = kbId
    const res = await getDocuments(params)
    if (res.success) {
      dataSource.value = res.data
      totalElements.value = res.pagination.totalElements
      totalPages.value = res.pagination.totalPages
      pagination.total = res.pagination.totalElements
      pagination.current = res.pagination.page
    }
  } catch (e) {
    message.error('加载文档列表失败')
  } finally {
    loading.value = false
  }
}

function onTableChange(pag) {
  currentPage.value = pag.current
  pageSize.value = pag.pageSize
  loadDocuments()
}

function previewDoc(id) {
  previewUrl.value = getDocumentPreviewUrl(id)
  previewVisible.value = true
}

function showDeleteConfirm(record) {
  deleteTarget.value = record
  deletePassword.value = ''
  deleteConfirm.value = false
  deleteVisible.value = true
}

async function confirmDelete() {
  if (!deletePassword.value) {
    message.warning('请输入删除确认密码')
    return
  }
  if (!deleteConfirm.value) {
    message.warning('请先勾选删除确认')
    return
  }
  deleteLoading.value = true
  try {
    const res = await deleteDocument(deleteTarget.value.id, deletePassword.value)
    if (res.success) {
      message.success(res.message)
      deleteVisible.value = false
      loadDocuments()
    } else {
      message.error(res.message)
    }
  } catch (e) {
    message.error('删除失败')
  } finally {
    deleteLoading.value = false
  }
}
</script>

<style scoped>
.stats-row { margin-bottom: 24px; }
.stats-card { border-radius: 12px; }
.stats-card.primary { background: linear-gradient(135deg, #667eea, #764ba2); color: white; }
.stats-card.success { background: #d1fae5; color: #065f46; }
.stats-card.danger { background: #fee2e2; color: #991b1b; }
.stats-card h3 { margin: 0; font-size: 2rem; font-weight: 700; }
.stats-card p { margin: 4px 0 0; opacity: 0.9; }
:deep(.ant-page-header) { background: white; border-radius: 12px; padding: 16px 24px; margin-bottom: 24px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
</style>
