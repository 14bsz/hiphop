<template>
  <div class="app-container">
    <el-card shadow="never" class="admin-card">
      <template #header>
        <div class="card-header">
          <div class="card-title">
            <span class="title-text">每日推荐管理</span>
            <span class="title-sub">控制首页「每日推荐」栏目内容</span>
          </div>
          <div class="card-actions">
            <el-button type="warning" plain icon="Download" @click="handleFetchDaily">抓取推荐</el-button>
            <el-button type="primary" plain icon="Plus" @click="openCreate">新增推荐</el-button>
            <el-button type="success" plain icon="Refresh" @click="reload">刷新</el-button>
          </div>
        </div>
      </template>

      <!-- 搜索条件 -->
      <el-form
        :model="query"
        ref="queryForm"
        label-width="80px"
        class="query-form"
        :inline="true"
      >
        <el-form-item label="标题" prop="title">
          <el-input v-model="query.title" placeholder="请输入标题" clearable style="width: 220px" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 180px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="reload">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 列表 -->
      <el-table v-loading="loading" :data="list" style="width: 100%" border>
        <el-table-column type="index" label="ID" width="60" align="center" />
        <!-- <el-table-column prop="id" label="ID" width="90" align="center" /> -->
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="artist" label="艺人" min-width="140" show-overflow-tooltip />
        <el-table-column label="封面" width="100" align="center">
          <template #default="scope">
            <el-image
              style="width: 50px; height: 50px; border-radius: 4px;"
              :src="normalizeImageUrl(scope.row.cover)"
              :preview-src-list="[normalizeImageUrl(scope.row.cover)]"
              fit="cover"
              preview-teleported
            />
          </template>
        </el-table-column>
        <el-table-column prop="badge" label="标签" width="120" show-overflow-tooltip />
        <el-table-column label="日期" width="140" show-overflow-tooltip>
          <template #default="scope">
            {{ formatDate(scope.row.recommendDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="sortNo" label="排序" width="80" align="center" />
        <el-table-column label="状态" align="center" width="90">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="160">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="openEdit(scope.row)">编辑</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 弹窗 -->
      <el-dialog :title="form.id ? '编辑推荐' : '新增推荐'" v-model="showDialog" width="600px" append-to-body>
        <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
          <el-form-item label="标题" prop="title">
            <el-input v-model="form.title" placeholder="请输入标题" />
          </el-form-item>
          <el-form-item label="艺人" prop="artist">
            <el-input v-model="form.artist" placeholder="请输入艺人" />
          </el-form-item>
          <el-form-item label="封面图片" prop="cover">
            <el-input v-model="form.cover" placeholder="请输入封面图片地址" />
            <div v-if="form.cover" class="preview-img">
              <el-image
                :src="normalizeImageUrl(form.cover)"
                style="width: 200px; height: 120px; margin-top: 10px; border-radius: 4px;"
                fit="cover"
                :preview-src-list="[normalizeImageUrl(form.cover)]"
              />
            </div>
          </el-form-item>
          <el-form-item label="跳转链接" prop="linkUrl">
            <el-input v-model="form.linkUrl" placeholder="请输入外链地址" />
          </el-form-item>
          <el-form-item label="标签文案" prop="badge">
            <el-input v-model="form.badge" placeholder="例如：热度爆表" />
          </el-form-item>
          <el-form-item label="日期" prop="date">
            <el-date-picker
              v-model="form.date"
              type="date"
              placeholder="请选择日期"
              value-format="YYYY-MM-DD"
              style="width: 100%;"
            />
          </el-form-item>
          <el-row>
            <el-col :span="12">
              <el-form-item label="排序" prop="sortNo">
                <el-input-number v-model="form.sortNo" :min="0" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="状态" prop="status">
                <el-radio-group v-model="form.status">
                  <el-radio :value="1">启用</el-radio>
                  <el-radio :value="0">禁用</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button type="primary" @click="submit">确 定</el-button>
            <el-button @click="closeDialog">取 消</el-button>
          </div>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDailyList, createDaily, updateDaily, deleteDaily, updateDailyStatus, fetchDailyFromPlaylist } from '../../api/adminHome'

function normalizeImageUrl(url: string): string {
  if (!url) return url
  const https = url.replace(/^http:/, 'https:')
  return https
}

function formatDate(dateStr: string | Date): string {
  if (!dateStr) return ''
  try {
    const date = new Date(dateStr)
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  } catch (e) {
    return String(dateStr)
  }
}

const loading = ref(false)
const list = ref<any[]>([])
const showDialog = ref(false)

const query = reactive<{ title?: string; status?: number | undefined }>({
  title: '',
  status: undefined
})

const form = reactive<any>({
  id: undefined,
  title: '',
  artist: '',
  cover: '',
  linkUrl: '',
  badge: '热度爆表',
  date: '',
  sortNo: 0,
  status: 1
})

const rules = {
  title: [{ required: true, message: '标题不能为空', trigger: 'blur' }],
  artist: [{ required: true, message: '艺人不能为空', trigger: 'blur' }],
  cover: [{ required: true, message: '封面图片不能为空', trigger: 'blur' }]
}

function reload() {
  loading.value = true
  const params: any = {}
  if (query.title) params.title = query.title
  if (query.status !== undefined) params.status = query.status
  getDailyList(params)
    .then((res: any) => {
      // 后端返回的数据结构: { list: [], total: 0, page: 1, size: 10 }
      if (res && res.list) {
        list.value = Array.isArray(res.list) ? res.list : []
      } else if (Array.isArray(res)) {
        // 兼容直接返回数组的情况
        list.value = res
      } else {
        list.value = []
      }
    })
    .catch((e: any) => {
      console.error('获取列表失败:', e)
      ElMessage.error('获取列表失败: ' + (e.message || '未知错误'))
      list.value = []
    })
    .finally(() => {
      loading.value = false
    })
}

function resetQuery() {
  query.title = ''
  query.status = undefined
  reload()
}

function openCreate() {
  Object.assign(form, {
    id: undefined,
    title: '',
    artist: '',
    cover: '',
    linkUrl: '',
    badge: '热度爆表',
    date: '',
    sortNo: 0,
    status: 1
  })
  showDialog.value = true
}

function openEdit(row: any) {
  Object.assign(form, {
    id: row.id,
    title: row.title,
    artist: row.artist,
    cover: row.cover,
    linkUrl: row.linkUrl,
    badge: row.badge,
    date: formatDate(row.recommendDate), // 后端返回 recommendDate
    sortNo: row.sortNo,
    status: row.status
  })
  showDialog.value = true
}

function closeDialog() {
  showDialog.value = false
}

function submit() {
  const payload: any = {
    title: form.title,
    artist: form.artist,
    cover: form.cover,
    linkUrl: form.linkUrl,
    badge: form.badge,
    recommendDate: form.date, // 前端使用 date，后端需要 recommendDate
    sortNo: form.sortNo,
    status: form.status
  }
  const action = form.id ? updateDaily(String(form.id), payload) : createDaily(payload)
  action
    .then(() => {
      ElMessage.success(form.id ? '修改成功' : '新增成功')
      showDialog.value = false
      reload()
    })
    .catch((e: any) => {
      ElMessage.error('操作失败: ' + (e.message || '未知错误'))
    })
}

function handleStatusChange(row: any) {
  const status = typeof row.status === 'number' ? row.status : row.status === 1 ? 1 : 0
  updateDailyStatus(row.id, status)
    .then(() => {
      ElMessage.success('状态更新成功')
    })
    .catch(() => {
      row.status = row.status === 1 ? 0 : 1
      ElMessage.error('状态更新失败')
    })
}

function handleDelete(row: any) {
  ElMessageBox.confirm('是否确认删除该记录?', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    deleteDaily(row.id)
      .then(() => {
        ElMessage.success('删除成功')
        reload()
      })
      .catch((e: any) => {
        ElMessage.error('删除失败: ' + (e.message || '未知错误'))
      })
  })
}

function handleFetchDaily() {
  ElMessageBox.confirm('将从网易云歌单随机抽取10首歌曲作为今日推荐，是否继续？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(() => {
    const loadingMsg = ElMessage({
      message: '正在抓取中...',
      type: 'info',
      duration: 0
    })
    // TODO: 更换为歌曲数更多的歌单ID，建议100+首
    // 例如：'2884035' (华语说唱精选2000+首)
    fetchDailyFromPlaylist('5205824122,2949295961', 10)
      .then((res: any) => {
        loadingMsg.close()
        if (res.success) {
          ElMessage.success(`抓取成功，共${res.count}首歌曲`)
          reload()
        } else {
          ElMessage.error('抓取失败: ' + (res.message || '未知错误'))
        }
      })
      .catch((e: any) => {
        loadingMsg.close()
        ElMessage.error('抓取失败: ' + (e.message || '未知错误'))
      })
  })
}

onMounted(() => {
  reload()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.admin-card {
  border-radius: 4px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-title {
  display: flex;
  flex-direction: column;
}
.title-text {
  font-size: 16px;
  font-weight: 600;
}
.title-sub {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
.card-actions > .el-button + .el-button {
  margin-left: 8px;
}
.query-form {
  margin-bottom: 16px;
}
.preview-img {
  margin-top: 10px;
}
</style>









