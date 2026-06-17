<template>
  <div>
    <a-page-header title="文档上传" sub-title="上传PDF文档到知识库" />
    <a-row :gutter="24">
      <a-col :span="16">
        <a-card>
          <div
            class="upload-zone"
            @dragover.prevent="dragover = true"
            @dragleave="dragover = false"
            @drop.prevent="handleDrop"
            @click="openFilePicker"
            :class="{ dragover }"
          >
            <cloud-upload-outlined class="upload-icon" />
            <h4>拖拽 PDF 文件到这里或点击选择</h4>
            <p>支持单个或多个文件上传，单个文件最大 50MB</p>
            <a-button type="primary" @click.stop="openFilePicker">
              <folder-open-outlined /> 选择文件
            </a-button>
            <input
              ref="fileInputRef"
              type="file"
              accept=".pdf"
              multiple
              style="display: none"
              @change="onFileSelect"
            />
          </div>
          <a-list v-if="fileList.length > 0" :data-source="fileList" class="file-list">
            <template #renderItem="{ item, index }">
              <a-list-item>
                <a-list-item-meta>
                  <template #avatar>
                    <file-pdf-outlined style="font-size: 24px; color: #e53e3e" />
                  </template>
                  <template #title>{{ item.name }}</template>
                  <template #description>{{ formatSize(item.size) }}</template>
                </a-list-item-meta>
                <template #actions>
                  <a-button type="text" danger @click="removeFile(index)">
                    <template #icon><delete-outlined /></template>
                  </a-button>
                </template>
              </a-list-item>
            </template>
          </a-list>
          <div v-if="fileList.length > 0" class="upload-btn-wrapper">
            <a-button type="primary" size="large" :loading="uploading" @click="startUpload">
              <template #icon><cloud-upload-outlined /></template>
              开始上传
            </a-button>
          </div>
        </a-card>
      </a-col>
      <a-col :span="8">
        <a-card title="使用说明">
          <h5>上传步骤：</h5>
          <ol>
            <li>点击"选择文件"或拖拽 PDF 到上传区域</li>
            <li>确认文件列表</li>
            <li>点击"开始上传"按钮</li>
          </ol>
          <a-divider />
          <h5>注意事项：</h5>
          <ul>
            <li>仅支持 PDF 格式文件</li>
            <li>文件会被解析并存储到向量数据库</li>
            <li>上传完成后可在"文档管理"中查看</li>
          </ul>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
import {ref} from 'vue'
import {message} from 'ant-design-vue'
import {CloudUploadOutlined, DeleteOutlined, FilePdfOutlined, FolderOpenOutlined} from '@ant-design/icons-vue'
import {uploadDocument} from '../../api'

const fileInputRef = ref(null)
const fileList = ref([])
const uploading = ref(false)
const dragover = ref(false)

function openFilePicker() {
  fileInputRef.value?.click()
}

function onFileSelect(e) {
  handleFiles(e.target.files)
}

function handleDrop(e) {
  dragover.value = false
  handleFiles(e.dataTransfer.files)
}

function handleFiles(files) {
  const pdfFiles = Array.from(files).filter(f => f.name.toLowerCase().endsWith('.pdf'))
  fileList.value = [...fileList.value, ...pdfFiles]
}

function removeFile(index) {
  fileList.value.splice(index, 1)
}

function formatSize(bytes) {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB'
  if (bytes < 1024 * 1024 * 1024) return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
  return (bytes / (1024 * 1024 * 1024)).toFixed(2) + ' GB'
}

async function startUpload() {
  if (fileList.value.length === 0) return
  uploading.value = true
  try {
    const formData = new FormData()
    fileList.value.forEach(f => formData.append('files', f))
    formData.append('knowledgeBaseId', localStorage.getItem('currentKnowledgeBaseId') || 'default')
    const res = await uploadDocument(formData)
    if (res.success) {
      message.success(res.message)
      fileList.value = []
    } else {
      message.error(res.message)
    }
  } catch (e) {
    message.error('上传失败: ' + (e.response?.data?.message || e.message))
  } finally {
    uploading.value = false
  }
}
</script>

<style scoped>
.upload-zone {
  border: 3px dashed #d9d9d9;
  border-radius: 12px;
  padding: 60px 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  background: #fafafa;
}

.upload-zone:hover,
.upload-zone.dragover {
  border-color: #1677ff;
  background: #e6f7ff;
}

.upload-icon {
  font-size: 64px;
  color: #a0aec0;
  margin-bottom: 16px;
}

.upload-zone h4 {
  color: #4a5568;
  margin-bottom: 8px;
}

.upload-zone p {
  color: #718096;
  margin-bottom: 16px;
}

.file-list {
  margin-top: 16px;
}

.upload-btn-wrapper {
  text-align: right;
  margin-top: 16px;
}

:deep(.ant-page-header) {
  background: white;
  border-radius: 12px;
  padding: 16px 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}
</style>
