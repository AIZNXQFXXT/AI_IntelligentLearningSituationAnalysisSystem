<template>
  <el-card>
    <template #header>期末评语</template>
    <el-table :data="comments" v-loading="loading" border stripe>
      <el-table-column prop="semester" label="学期" width="120" />
      <el-table-column prop="content" label="评语内容" show-overflow-tooltip />
      <el-table-column prop="teacherId" label="教师ID" width="100" />
      <el-table-column prop="isTeacherEdited" label="是否修改" width="100">
        <template #default="{ row }">
          <el-tag :type="row.isTeacherEdited ? 'warning' : ''">{{ row.isTeacherEdited ? '教师修改' : 'AI生成' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="生成时间" width="180" />
    </el-table>
    <el-empty v-if="!loading && comments.length === 0" description="暂无评语" />
  </el-card>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getCommentPage } from '@/api/comment'
import { useUserStore } from '@/stores/user'
import type { AiComment } from '@/types'

const userStore = useUserStore()
const loading = ref(false)
const comments = ref<AiComment[]>([])

onMounted(async () => {
  loading.value = true
  try {
    const res = await getCommentPage({ page: 1, size: 50, studentId: userStore.userId })
    comments.value = res.records
  } finally { loading.value = false }
})
</script>
