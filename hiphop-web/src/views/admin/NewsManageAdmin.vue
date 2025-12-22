<template>
  <div class="app-container">
    <el-card shadow="never" class="admin-card">
      <template #header>
        <div class="card-header">
          <div class="card-title">
            <span class="title-text">最新资讯管理</span>
            <span class="title-sub">单独管理 B 站 / 公众号等来源的最新资讯条目</span>
          </div>
          <div class="card-actions">
            <el-button type="primary" plain icon="Plus" @click="openCreate">新增资讯</el-button>
            <el-button icon="Refresh" @click="reload">刷新</el-button>
          </div>
        </div>
      </template>

      <!-- 搜索条件 -->
      <el-form :model="query" ref="queryForm" label-width="80px" class="query-form" :inline="true">
        <el-form-item label="标题" prop="title">
          <el-input v-model="query.title" placeholder="请输入标题关键字" style="width: 220px" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 160px">
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
        <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
        <el-table-column prop="source" label="来源" width="100" align="center" />
        <el-table-column prop="category" label="分类" width="120" align="center" />
        <el-table-column label="封面" width="120" align="center">
          <template #default="scope">
            <el-image
              v-if="scope.row.imgUrl"
              style="width: 60px; height: 60px; border-radius: 4px;"
              :src="normalizeImageUrl(scope.row.imgUrl)"
              :preview-src-list="[normalizeImageUrl(scope.row.imgUrl)]"
              fit="cover"
              preview-teleported
            />
          </template>
        </el-table-column>
        <el-table-column prop="linkUrl" label="原文链接" min-width="260" show-overflow-tooltip />
        <el-table-column prop="sortNo" label="排序" width="80" align="center" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="openEdit(scope.row)">编辑</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 弹窗：新增/编辑 -->
      <el-dialog :title="form.id ? '编辑资讯' : '新增资讯'" v-model="showDialog" width="640px" append-to-body>
        <el-form ref="newsFormRef" :model="form" :rules="rules" label-width="100px">
          <el-form-item label="自动获取" prop="url">
            <el-input
              v-model="form.url"
              placeholder="请输入微信公众号文章链接或B站BV号"
            >
              <template #append>
                <el-button @click="doFetchContent" :loading="fetching">自动获取</el-button>
              </template>
            </el-input>
            <div class="tip-text">支持：微信公众号文章链接、B站 BV 号，仅用于自动获取标题和封面，不会入库。</div>
          </el-form-item>
          <el-form-item label="标题" prop="title">
            <el-input v-model="form.title" placeholder="请输入标题" />
          </el-form-item>
          <el-form-item label="封面图片" prop="imgUrl">
            <el-input v-model="form.imgUrl" placeholder="请输入图片地址">
              <template #append>
                <el-button @click="generateRandomImage">随机</el-button>
              </template>
            </el-input>
            <div v-if="form.imgUrl" class="preview-img">
              <el-image
                :src="normalizeImageUrl(form.imgUrl)"
                style="width: 220px; height: 130px; margin-top: 10px; border-radius: 4px;"
                fit="cover"
                :preview-src-list="[normalizeImageUrl(form.imgUrl)]"
                @error="(e) => handleImageError(e, 'form')"
              />
            </div>
          </el-form-item>
          <el-form-item label="原文链接" prop="linkUrl">
            <el-input v-model="form.linkUrl" placeholder="请输入原文/原视频链接" />
          </el-form-item>
          <el-form-item label="来源" prop="source">
            <el-select v-model="form.source" placeholder="请选择来源" style="width: 100%" clearable>
              <el-option label="B站" value="B站" />
              <el-option label="公众号" value="公众号" />
              <el-option label="其他" value="其他" />
            </el-select>
          </el-form-item>
          <el-form-item label="分类标签" prop="category">
            <el-input v-model="form.category" placeholder="例如：专访、评论、现场、MV 等" />
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
import { getNewsList, createNews, updateNews, deleteNews, updateNewsStatus, fetchContent } from '../../api/adminHome'

const loading = ref(false)
const list = ref<any[]>([])
const showDialog = ref(false)
const fetching = ref(false)

const query = reactive<{ title?: string; status?: number | undefined }>({
  title: '',
  status: 1
})

const form = reactive<any>({
  id: undefined,
  url: '',
  title: '',
  imgUrl: '',
  linkUrl: '',
  source: '',
  category: '',
  sortNo: 0,
  status: 1
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  imgUrl: [{ required: true, message: '请输入封面图片地址', trigger: 'blur' }],
  linkUrl: [{ required: true, message: '请输入原文链接', trigger: 'blur' }]
}

function normalizeImageUrl(url: string): string {
  if (!url) return url
  const https = url.replace(/^http:/, 'https:')
  try {
    const urlObj = new URL(https)
    if (/mmbiz\.qpic\.cn$/i.test(urlObj.hostname) || /qpic\.cn$/i.test(urlObj.hostname)) {
      return `/img-proxy?url=${encodeURIComponent(https)}`
    }
    if (/^i[0-9]\.hdslb\.com$/i.test(urlObj.hostname)) {
      if (https.includes('@') && https.endsWith('.webp')) {
        return https
      }
      return `/img-proxy?url=${encodeURIComponent(https)}`
    }
  } catch {}
  return https
}

function handleImageError(e: Event, type: 'form') {
  console.warn('图片加载失败', e)
  if (type === 'form') {
    form.imgUrl = ''
  }
}

function generateRandomImage() {
  const rand = Math.floor(Math.random() * 1000)
  form.imgUrl = `https://picsum.photos/600/360?random=${rand}`
}

async function reload() {
  loading.value = true
  try {
    const data = await getNewsList({
      title: query.title || undefined,
      status: query.status
    })
    list.value = Array.isArray(data) ? data : []
  } catch (e) {
    console.error(e)
    ElMessage.error('加载资讯失败')
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.title = ''
  query.status = 1
  reload()
}

function openCreate() {
  Object.assign(form, {
    id: undefined,
    url: '',
    title: '',
    imgUrl: '',
    linkUrl: '',
    source: '',
    category: '',
    sortNo: 0,
    status: 1
  })
  showDialog.value = true
}

function openEdit(row: any) {
  Object.assign(form, {
    id: row.id,
    url: '',
    title: row.title,
    imgUrl: row.imgUrl,
    linkUrl: row.linkUrl,
    source: row.source,
    category: row.category,
    sortNo: row.sortNo,
    status: row.status
  })
  showDialog.value = true
}

function closeDialog() {
  showDialog.value = false
}

async function handleStatusChange(row: any) {
  try {
    await updateNewsStatus(row.id, row.status)
    ElMessage.success('状态已更新')
  } catch (e) {
    console.error(e)
    ElMessage.error('状态更新失败')
  }
}

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm('确认删除该资讯吗？', '提示', {
      type: 'warning'
    })
    await deleteNews(row.id)
    ElMessage.success('删除成功')
    reload()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
      ElMessage.error('删除失败')
    }
  }
}

async function doFetchContent() {
  if (!form.url) {
    ElMessage.warning('请先输入链接或 BV 号')
    return
  }
  fetching.value = true
  try {
    const data = await fetchContent(form.url)
    if (data) {
      if (data.title) form.title = data.title
      if (data.coverImage || data.cover_image) form.imgUrl = data.coverImage || data.cover_image
      if (data.sourceUrl || data.source_url) form.linkUrl = data.sourceUrl || data.source_url
      ElMessage.success('自动获取成功')
    } else {
      ElMessage.warning('未获取到有效数据')
    }
  } catch (e: any) {
    console.error(e)
    ElMessage.error(e?.message || '自动获取失败')
  } finally {
    fetching.value = false
  }
}

async function submit() {
  const payload: any = {
    title: form.title,
    imgUrl: form.imgUrl,
    linkUrl: form.linkUrl,
    source: form.source,
    category: form.category,
    sortNo: form.sortNo,
    status: form.status
  }

  try {
    if (form.id) {
      await updateNews(form.id, payload)
      ElMessage.success('更新成功')
    } else {
      await createNews(payload)
      ElMessage.success('创建成功')
    }
    showDialog.value = false
    reload()
  } catch (e) {
    console.error(e)
    ElMessage.error('保存失败')
  }
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
  border-radius: 8px;
}
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
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
}
.card-actions {
  display: flex;
  gap: 10px;
}
.query-form {
  margin-bottom: 16px;
}
.preview-img {
  margin-top: 8px;
}
.tip-text {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}
</style>
