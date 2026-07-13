<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>评语管理</span>
        <div style="display:flex;gap:12px">
          <el-select v-model="semester" placeholder="学期" style="width:150px" @change="loadData">
            <el-option label="2025-1" value="2025-1" />
            <el-option label="2024-2" value="2024-2" />
          </el-select>
          <el-button type="primary" :loading="generating" @click="handleBatchGenerate">AI批量生成评语</el-button>
        </div>
      </div>
    </template>
    <el-table :data="comments" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="studentId" label="学生ID" width="100" />
      <el-table-column prop="semester" label="学期" width="120" />
      <el-table-column prop="content" label="评语内容" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'APPROVED' ? 'success' : 'info'">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="isTeacherEdited" label="教师修改" width="100">
        <template #default="{ row }">
          <el-tag :type="row.isTeacherEdited ? 'warning' : ''">{{ row.isTeacherEdited ? '已修改' : '未修改' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" @click="showVersions(row.id)">版本</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="editVisible" title="编辑评语" width="600px">
      <el-input v-model="editContent" type="textarea" :rows="6" />
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpdate">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="versionVisible" title="评语版本历史" width="600px">
      <el-timeline>
        <el-timeline-item v-for="v in versions" :key="v.id" :timestamp="v.createdAt" placement="top">
          <el-card shadow="never">
            <div>版本 {{ v.versionNo }} (来源: {{ v.source }})</div>
            <p>{{ v.content }}</p>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-dialog>
  </el-card>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getCommentPage, updateComment, getCommentVersions, batchGenerateComments } from '@/api/comment'
import type { AiComment, AiCommentVersion } from '@/types'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const generating = ref(false)
const comments = ref<AiComment[]>([])
const semester = ref('2025-1')
const editVisible = ref(false)
const editId = ref(0)
const editContent = ref('')
const versionVisible = ref(false)
const versions = ref<AiCommentVersion[]>([])

async function loadData() {
  loading.value = true
  try {
    const res = await getCommentPage({ page: 1, size: 100, semester: semester.value })
    comments.value = res.records
  } finally { loading.value = false }
}

async function handleBatchGenerate() {
  generating.value = true
  try {
    await batchGenerateComments({ studentIds: [], semester: semester.value })
    ElMessage.success('评语生成任务已提交')
    loadData()
  } finally { generating.value = false }
}

function openEdit(row: AiComment) {
  editId.value = row.id
  editContent.value = row.content
  editVisible.value = true
}

async function handleUpdate() {
  await updateComment(editId.value, { content: editContent.value })
  ElMessage.success('保存成功')
  editVisible.value = false
  loadData()
}

async function showVersions(commentId: number) {
  versions.value = await getCommentVersions(commentId)
  versionVisible.value = true
}

onMounted(loadData)
</script>
