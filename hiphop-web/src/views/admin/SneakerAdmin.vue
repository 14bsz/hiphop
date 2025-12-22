<template>
  <div class="app-container">
    <el-card shadow="never" class="admin-card">
      <template #header>
        <div class="card-header">
          <div class="card-title">
            <span class="title-text">球鞋资讯管理</span>
            <span class="title-sub">管理来自得物等平台的球鞋资讯列表</span>
          </div>
          <div class="card-actions">
            <el-button type="primary" plain icon="Plus" @click="openCreate">新增球鞋</el-button>
            <el-button type="success" plain icon="Link" @click="openQuickAdd">快速添加</el-button>
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
        <el-table-column prop="brand" label="品牌" width="120" align="center" />
        <el-table-column prop="category" label="分类" width="120" align="center" />
        <el-table-column prop="price" label="价格" width="120" align="center" />
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
        <el-table-column prop="linkUrl" label="商品链接" min-width="260" show-overflow-tooltip />
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
        <el-table-column label="操作" width="180" align="center">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="openEdit(scope.row)">编辑</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 弹窗：新增/编辑 -->
      <el-dialog :title="form.id ? '编辑球鞋' : '新增球鞋'" v-model="showDialog" width="640px" append-to-body>
        <el-form ref="sneakerFormRef" :model="form" :rules="rules" label-width="100px">
          <el-form-item label="自动获取" prop="url">
            <el-input
              v-model="form.url"
              placeholder="请输入得物商品详情链接"
            >
              <template #append>
                <el-button @click="doFetchContent" :loading="fetching">自动获取</el-button>
              </template>
            </el-input>
            <div class="tip-text">支持：得物商品详情页链接，用于自动获取标题和封面。</div>
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
          <el-form-item label="商品链接" prop="linkUrl">
            <el-input v-model="form.linkUrl" placeholder="请输入商品详情链接" />
          </el-form-item>
          <el-form-item label="品牌" prop="brand">
            <el-input v-model="form.brand" placeholder="例如：Nike / Jordan 等" />
          </el-form-item>
          <el-form-item label="分类标签" prop="category">
            <el-input v-model="form.category" placeholder="例如：发售、爆料、上脚 等" />
          </el-form-item>
          <el-form-item label="价格" prop="price">
            <el-input v-model="form.price" placeholder="例如：1299 元" />
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

      <!-- 快速添加弹窗 -->
      <el-dialog title="快速添加球鞋" v-model="showQuickAddDialog" width="540px" append-to-body>
        <el-form :model="quickForm" label-width="90px">
          <el-form-item label="得物链接">
            <el-input
              v-model="quickForm.url"
              placeholder="请输入得物商品详情链接"
            >
              <template #append>
                <el-button @click="doQuickFetch" :loading="fetching">自动获取</el-button>
              </template>
            </el-input>
            <div class="tip-text">
              粘贴得物商品详情页链接，一键抓取标题和封面。
            </div>
          </el-form-item>
          <el-form-item label="标题">
            <el-input v-model="quickForm.title" placeholder="自动获取，可手动调整" />
          </el-form-item>
          <el-form-item label="封面图片">
            <el-input v-model="quickForm.imgUrl" placeholder="自动获取，可手动调整" />
          </el-form-item>
          <el-form-item label="商品链接">
            <el-input v-model="quickForm.linkUrl" placeholder="自动获取，可手动调整" />
          </el-form-item>
          <el-form-item label="品牌">
            <el-input v-model="quickForm.brand" placeholder="例如：Nike / Jordan 等" />
          </el-form-item>
          <el-form-item label="分类">
            <el-input v-model="quickForm.category" placeholder="例如：发售、爆料、上脚 等" />
          </el-form-item>
          <el-form-item label="价格">
            <el-input v-model="quickForm.price" placeholder="例如：1299 元" />
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button type="primary" @click="submitQuickAdd">保存</el-button>
            <el-button @click="closeQuickAdd">取 消</el-button>
          </div>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getSneakerList,
  createSneaker,
  updateSneaker,
  deleteSneaker,
  updateSneakerStatus,
  fetchContent
} from '../../api/adminHome'

const loading = ref(false)
const list = ref<any[]>([])
const showDialog = ref(false)
const showQuickAddDialog = ref(false)
const fetching = ref(false)

const query = reactive<{ title?: string; status?: number | undefined}>({
  title: '',
  status: 1
})

const form = reactive<any>({
  id: undefined,
  url: '',
  title: '',
  imgUrl: '',
  linkUrl: '',
  brand: '',
  category: '',
  price: '',
  sortNo: 0,
  status: 1
})

const quickForm = reactive<any>({
  url: '',
  title: '',
  imgUrl: '',
  linkUrl: '',
  brand: '',
  category: '',
  price: '',
  sortNo: 0,
  status: 1
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  imgUrl: [{ required: true, message: '请输入封面图片地址', trigger: 'blur' }],
  linkUrl: [{ required: true, message: '请输入商品链接', trigger: 'blur' }]
}

function normalizeImageUrl(url: string): string {
  if (!url) return url
  const https = url.replace(/^http:/, 'https:')
  return https
}

function handleImageError(e: Event, type: 'form') {
  console.warn('图片加载失败', e)
  if (type === 'form') {
    ;(form as any).imgUrl = ''
  }
}

function generateRandomImage() {
  const rand = Math.floor(Math.random() * 1000)
  form.imgUrl = `https://picsum.photos/600/360?random=${rand}`
}

async function reload() {
  loading.value = true
  try {
    const data = await getSneakerList({
      title: query.title || undefined,
      status: query.status
    })
    list.value = Array.isArray(data) ? data : []
  } catch (e) {
    console.error(e)
    ElMessage.error('加载球鞋资讯失败')
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
    brand: '',
    category: '',
    price: '',
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
    brand: row.brand,
    category: row.category,
    price: row.price,
    sortNo: row.sortNo,
    status: row.status
  })
  showDialog.value = true
}

function closeDialog() {
  showDialog.value = false
}

function openQuickAdd() {
  Object.assign(quickForm, {
    url: '',
    title: '',
    imgUrl: '',
    linkUrl: '',
    brand: '',
    category: '',
    price: '',
    sortNo: 0,
    status: 1
  })
  showQuickAddDialog.value = true
}

function closeQuickAdd() {
  showQuickAddDialog.value = false
}

async function handleStatusChange(row: any) {
  try {
    await updateSneakerStatus(row.id, row.status)
    ElMessage.success('状态已更新')
  } catch (e) {
    console.error(e)
    ElMessage.error('状态更新失败')
  }
}

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm('确认删除该球鞋资讯吗？', '提示', {
      type: 'warning'
    })
    await deleteSneaker(row.id)
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
    ElMessage.warning('请先输入得物商品链接')
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

async function doQuickFetch() {
  if (!quickForm.url) {
    ElMessage.warning('请先输入得物商品链接')
    return
  }
  fetching.value = true
  try {
    const data = await fetchContent(quickForm.url)
    if (data) {
      if (data.title) quickForm.title = data.title
      if (data.coverImage || data.cover_image) quickForm.imgUrl = data.coverImage || data.cover_image
      if (data.sourceUrl || data.source_url) quickForm.linkUrl = data.sourceUrl || data.source_url
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
    brand: form.brand,
    category: form.category,
    price: form.price,
    sortNo: form.sortNo,
    status: form.status
  }

  try {
    if (form.id) {
      await updateSneaker(form.id, payload)
      ElMessage.success('更新成功')
    } else {
      await createSneaker(payload)
      ElMessage.success('创建成功')
    }
    showDialog.value = false
    reload()
  } catch (e) {
    console.error(e)
    ElMessage.error('保存失败')
  }
}

async function submitQuickAdd() {
  const payload: any = {
    title: quickForm.title,
    imgUrl: quickForm.imgUrl,
    linkUrl: quickForm.linkUrl,
    brand: quickForm.brand,
    category: quickForm.category,
    price: quickForm.price,
    sortNo: quickForm.sortNo,
    status: quickForm.status
  }

  try {
    await createSneaker(payload)
    ElMessage.success('创建成功')
    showQuickAddDialog.value = false
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
