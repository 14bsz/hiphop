<template>
  <div class="app-container">
    <el-card shadow="never" class="admin-card">
      <template #header>
        <div class="card-header">
          <div class="card-title">
            <span class="title-text">高分音乐榜管理</span>
            <span class="title-sub">
              控制首页「高分音乐榜」模块的
              <span v-if="currentType === 1">单曲卡片</span>
              <span v-else>专辑卡片</span>
            </span>
          </div>
          <div class="card-type-toggle">
            <span
              class="pill"
              :class="{ active: currentType === 1 }"
              @click="switchType(1)"
            >
              单曲
            </span>
            <span
              class="pill"
              :class="{ active: currentType === 2 }"
              @click="switchType(2)"
            >
              专辑
            </span>
          </div>
          <div class="card-actions">
            <el-button
              type="primary"
              plain
              icon="Plus"
              @click="openCreate"
              :disabled="list.length >= 6"
            >
              新增{{ currentType === 1 ? '歌曲' : '专辑' }}
            </el-button>
            <el-button
              type="success"
              plain
              icon="Link"
              @click="openQuickAdd"
              :disabled="list.length >= 6"
            >
              快速添加
            </el-button>
            <el-button
              type="warning"
              plain
              icon="Search"
              @click="openNeteaseImport"
              :disabled="list.length >= 6"
            >
              网易云搜索导入
            </el-button>
            <el-button
              type="danger"
              plain
              icon="Delete"
              :disabled="!selectedRows.length"
              :loading="batchDeleting"
              @click="handleBatchDelete"
            >
              批量删除
            </el-button>
            <el-button
              type="warning"
              plain
              icon="Sort"
              :disabled="!list.length"
              :loading="autoSorting"
              @click="handleAutoSort"
            >
              一键按{{ currentType === 1 ? '评论数' : '分享数' }}排序
            </el-button>
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
          <el-input
            v-model="query.title"
            :placeholder="currentType === 1 ? '请输入歌曲标题' : '请输入专辑标题'"
            clearable
            style="width: 220px"
          />
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
      <el-table
        v-loading="loading"
        :data="list"
        style="width: 100%"
        border
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column type="index" label="ID" width="60" align="center" />
<!--        <el-table-column prop="id" label="ID" width="90" align="center" />-->
        <el-table-column
          prop="title"
          :label="currentType === 1 ? '歌曲名称' : '专辑名称'"
          min-width="180"
          show-overflow-tooltip
        />
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
        <el-table-column
          prop="favoriteCount"
          :label="currentType === 1 ? '评论数' : '分享数'"
          width="120"
          align="center"
        />
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
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="220">
          <template #default="scope">
            <div class="op-cell">
              <div class="op-left">
                <el-button link type="primary" icon="Edit" @click="openEdit(scope.row)">编辑</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
              </div>
              <div class="op-right">
                <el-button
                  class="sort-btn"
                  size="small"
                  type="primary"
                  plain
                  @click="moveUp(scope.row, scope.$index)"
                  :disabled="scope.$index === 0"
                >
                  ↑
                </el-button>
                <el-button
                  class="sort-btn"
                  size="small"
                  type="primary"
                  plain
                  @click="moveDown(scope.row, scope.$index)"
                  :disabled="scope.$index === list.length - 1"
                >
                  ↓
                </el-button>
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="list.length >= 6" class="limit-tip">
        已达到 6 首歌曲上限，如需新增请先删除部分现有歌曲。
      </div>

      <!-- 快速添加弹窗（网易云音乐） -->
      <el-dialog title="快速添加（网易云音乐）" v-model="showQuickAdd" width="600px" append-to-body>
        <el-form :model="quickForm" label-width="100px">
          <el-form-item label="网易云链接" required>
            <el-input
              v-model="quickForm.url"
              placeholder="请输入网易云音乐歌曲/专辑链接，例如：https://music.163.com/song?id=xxxxx"
            >
              <template #append>
                <el-button :loading="quickLoading" @click="handleQuickFetch">自动获取</el-button>
              </template>
            </el-input>
            <div class="tip-text">
              支持网易云音乐歌曲/专辑页面链接，点击“自动获取”后会自动解析标题、艺人和封面。
            </div>
          </el-form-item>
          <el-form-item :label="currentType === 1 ? '歌曲名称' : '专辑名称'">
            <el-input v-model="quickForm.title" placeholder="自动获取，可手动修改" />
          </el-form-item>
          <el-form-item label="艺人">
            <el-input v-model="quickForm.artist" placeholder="自动获取，可手动修改" />
          </el-form-item>
          <el-form-item :label="currentType === 1 ? '评论数' : '分享数'">
            <el-input-number
              v-model="quickForm.favoriteCount"
              :min="0"
              :step="1"
              :controls="false"
              placeholder="自动获取，可手动修改"
            />
          </el-form-item>
          <el-form-item label="封面图片">
            <el-input v-model="quickForm.cover" placeholder="自动获取，可手动修改" />
            <div v-if="quickForm.cover" class="preview-img">
              <el-image
                :src="normalizeImageUrl(quickForm.cover)"
                style="width: 200px; height: 120px; margin-top: 10px; border-radius: 4px;"
                fit="cover"
                :preview-src-list="[normalizeImageUrl(quickForm.cover)]"
              />
            </div>
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button type="primary" :loading="quickSubmitting" @click="submitQuickAdd">确 定</el-button>
            <el-button @click="closeQuickAdd">取 消</el-button>
          </div>
        </template>
      </el-dialog>

      <!-- 网易云搜索导入 -->
      <el-dialog
        :title="currentType === 1 ? '网易云搜索导入（单曲）' : '网易云搜索导入（专辑）'"
        v-model="showNeteaseDialog"
        width="1000px"
        append-to-body
      >
        <div class="netease-search-bar">
          <el-input
            v-model="neteaseKeyword"
            :placeholder="
              currentType === 1 ? '输入歌曲或艺人关键词，回车或点击搜索' : '输入专辑或艺人关键词，回车或点击搜索'
            "
            @keyup.enter="handleNeteaseSearch"
            clearable
          >
            <template #append>
              <el-button :loading="neteaseLoading" icon="Search" @click="handleNeteaseSearch">搜索</el-button>
            </template>
          </el-input>
          <div class="remain-tip">最多可再添加 {{ remainSlots }} 首</div>
        </div>
        <div class="netease-body">
          <el-table
            ref="neteaseTableRef"
            v-loading="neteaseLoading"
            :data="neteaseResults"
            style="width: 100%"
            height="520"
            @selection-change="handleNeteaseSelectionChange"
            :row-key="row => row.id"
          >
            <el-table-column type="selection" width="55" />
            <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
            <el-table-column prop="artist" label="艺人" min-width="150" show-overflow-tooltip />
            <el-table-column label="封面" width="90" align="center">
              <template #default="scope">
                <el-image
                  style="width: 44px; height: 44px; border-radius: 4px;"
                  :src="normalizeImageUrl(scope.row.cover)"
                  fit="cover"
                />
              </template>
            </el-table-column>
            <el-table-column prop="linkUrl" label="链接" min-width="220" show-overflow-tooltip />
          </el-table>
          <div class="selected-panel">
            <div class="panel-header">已勾选 ({{ selectedNetease.length }})</div>
            <el-scrollbar height="420px">
              <div v-if="selectedNetease.length === 0" class="empty-selected">
                暂无已勾选{{ currentType === 1 ? '歌曲' : '专辑' }}
              </div>
              <div v-else class="selected-list">
                <div v-for="item in selectedNetease" :key="item.id" class="selected-item">
                  <div class="selected-info">
                    <div class="selected-title" :title="item.title">{{ item.title }}</div>
                    <div class="selected-artist" :title="item.artist">{{ item.artist }}</div>
                  </div>
                  <el-button link type="danger" size="small" @click="removeSelected(item)">取消</el-button>
                </div>
              </div>
            </el-scrollbar>
          </div>
        </div>
        <template #footer>
          <div class="dialog-footer netease-footer">
            <span class="selected-tip">已选 {{ selectedNetease.length }} / 可添加 {{ remainSlots }}</span>
            <div>
              <el-button @click="closeNeteaseDialog">取 消</el-button>
              <el-button
                type="primary"
                :disabled="!selectedNetease.length"
                :loading="neteaseImporting"
                @click="importSelectedNetease"
              >
                一键导入
              </el-button>
            </div>
          </div>
        </template>
      </el-dialog>

      <!-- 弹窗 -->
      <el-dialog :title="form.id ? '编辑歌曲' : '新增歌曲'" v-model="showDialog" width="600px" append-to-body>
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
          <el-row>
            <el-col :span="24">
              <el-form-item label="排序" prop="sortNo">
                <el-input-number v-model="form.sortNo" :min="0" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="状态" prop="status">
            <el-radio-group v-model="form.status">
              <el-radio :value="1">启用</el-radio>
              <el-radio :value="0">禁用</el-radio>
            </el-radio-group>
          </el-form-item>
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
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowUp, ArrowDown } from '@element-plus/icons-vue'
import {
  getTopRatedList,
  createTopRated,
  updateTopRated,
  deleteTopRated,
  updateTopRatedStatus,
  fetchContent,
  searchNeteaseSongs,
  searchNeteaseAlbums,
  importTopRatedBatch
} from '../../api/adminHome'

function normalizeImageUrl(url: string): string {
  if (!url) return url
  const https = url.replace(/^http:/, 'https:')
  return https
}

const loading = ref(false)
const autoSorting = ref(false)
const list = ref<any[]>([])
const showDialog = ref(false)
const showQuickAdd = ref(false)
const quickLoading = ref(false)
const quickSubmitting = ref(false)
const showNeteaseDialog = ref(false)
const neteaseKeyword = ref('')
const neteaseLoading = ref(false)
const neteaseImporting = ref(false)
const neteaseResults = ref<any[]>([])
const selectedNetease = ref<any[]>([])
const neteaseTableRef = ref()

const quickForm = reactive<any>({
  url: '',
  title: '',
  cover: '',
  artist: '',
  favoriteCount: undefined
})

const selectedRows = ref<any[]>([])
const batchDeleting = ref(false)

const query = reactive<{ title?: string; status?: number | undefined }>({
  title: '',
  status: undefined
})

// 1-单曲，2-专辑
const currentType = ref<1 | 2>(1)

const form = reactive<any>({
  id: undefined,
  title: '',
  artist: '',
  cover: '',
  linkUrl: '',
  rating: 4.0,
  favoriteCount: 0,
  sortNo: 1,
  status: 1
})

const rules = {
  title: [{ required: true, message: '标题不能为空', trigger: 'blur' }],
  artist: [{ required: true, message: '艺人不能为空', trigger: 'blur' }],
  cover: [{ required: true, message: '封面图片不能为空', trigger: 'blur' }]
}

const remainSlots = computed(() => {
  const remain = 6 - list.value.length
  return remain > 0 ? remain : 0
})

function reload() {
  loading.value = true
  selectedRows.value = []
  const params: any = {}
  if (query.title) params.title = query.title
  if (query.status !== undefined) params.status = query.status
  params.type = currentType.value
  getTopRatedList(params)
    .then((res: any) => {
      list.value = Array.isArray(res) ? res : []
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

function switchType(type: 1 | 2) {
  if (currentType.value === type) return
  currentType.value = type
  // 切换类型时清空选中和搜索条件，只加载该类型数据
  query.title = ''
  query.status = undefined
  selectedRows.value = []
  reload()
}

function openCreate() {
  Object.assign(form, {
    id: undefined,
    title: '',
    artist: '',
    cover: '',
    linkUrl: '',
    type: currentType.value,
    rating: 4.0,
    favoriteCount: 0,
    sortNo: list.value.length + 1,
    status: 1
  })
  showDialog.value = true
}

function openQuickAdd() {
  quickForm.url = ''
  quickForm.title = ''
  quickForm.cover = ''
  quickForm.artist = ''
  quickForm.favoriteCount = undefined
  showQuickAdd.value = true
}

function openNeteaseImport() {
  neteaseKeyword.value = ''
  neteaseResults.value = []
  selectedNetease.value = []
  showNeteaseDialog.value = true
}

function openEdit(row: any) {
  Object.assign(form, {
    id: row.id,
    title: row.title,
    artist: row.artist,
    cover: row.cover,
    linkUrl: row.linkUrl,
    rating: row.rating,
    favoriteCount: row.favoriteCount,
    sortNo: row.sortNo,
    status: row.status
  })
  showDialog.value = true
}

function closeDialog() {
  showDialog.value = false
}

function closeQuickAdd() {
  showQuickAdd.value = false
}

function closeNeteaseDialog() {
  showNeteaseDialog.value = false
}

function submit() {
  const payload: any = {
    title: form.title,
    artist: form.artist,
    cover: form.cover,
    linkUrl: form.linkUrl,
    rating: form.rating,
    favoriteCount: form.favoriteCount,
    sortNo: form.sortNo,
    status: form.status
  }
  const action = form.id ? updateTopRated(String(form.id), payload) : createTopRated(payload)
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

async function handleQuickFetch() {
  if (!quickForm.url) {
    ElMessage.warning('请先输入网易云音乐链接')
    return
  }
  quickLoading.value = true
  try {
    const res: any = await fetchContent(quickForm.url)
    if (res) {
      quickForm.title = res.title || quickForm.title
      quickForm.cover = res.coverImage || quickForm.cover
      quickForm.artist = res.artist || quickForm.artist
      if (res.favoriteCount !== undefined && res.favoriteCount !== null) {
        const n = Number(res.favoriteCount)
        if (!Number.isNaN(n) && n >= 0) {
          quickForm.favoriteCount = n
        }
      }
      ElMessage.success('获取成功')
    } else {
      ElMessage.error('未获取到数据')
    }
  } catch (e: any) {
    ElMessage.error('获取失败: ' + (e.message || '未知错误'))
  } finally {
    quickLoading.value = false
  }
}

async function handleNeteaseSearch() {
  if (!neteaseKeyword.value.trim()) {
    ElMessage.warning('请输入关键词后再搜索')
    return
  }
  if (remainSlots.value <= 0) {
    ElMessage.warning('已达到 6 条记录上限，请先删除部分记录')
    return
  }
  neteaseLoading.value = true
  try {
    const keyword = neteaseKeyword.value.trim()
    const res: any =
      currentType.value === 1
        ? await searchNeteaseSongs(keyword, 20)
        : await searchNeteaseAlbums(keyword, 20)
    neteaseResults.value = Array.isArray(res) ? res : []
    await nextTick()
    restoreNeteaseSelection()
    if (!neteaseResults.value.length) {
      ElMessage.info('未搜索到相关歌曲')
    }
  } catch (e: any) {
    ElMessage.error(e.message || '搜索失败')
  } finally {
    neteaseLoading.value = false
  }
}

function handleNeteaseSelectionChange(rows: any[]) {
  const currentSelected = rows || []
  const keepExisting = selectedNetease.value.filter(
    item => !neteaseResults.value.find((row: any) => row.id === item.id)
  )
  const merged = [...keepExisting]
  const seen = new Set<number | string>(keepExisting.map((i: any) => i.id))
  for (const item of currentSelected) {
    if (!seen.has(item.id)) {
      merged.push(item)
      seen.add(item.id)
    }
  }
  if (merged.length > remainSlots.value) {
    ElMessage.warning(`最多可再添加 ${remainSlots.value} 首，已自动截取选择`)
  }
  selectedNetease.value = merged.slice(0, remainSlots.value)
}

function handleSelectionChange(rows: any[]) {
  selectedRows.value = rows || []
}

async function importSelectedNetease() {
  if (!selectedNetease.value.length) {
    ElMessage.warning(`请先选择要导入的${currentType.value === 1 ? '歌曲' : '专辑'}`)
    return
  }
  if (selectedNetease.value.length > remainSlots.value) {
    ElMessage.warning(`最多还能添加 ${remainSlots.value} 条记录`)
    return
  }
  neteaseImporting.value = true
  try {
    const payload = selectedNetease.value.map((item: any, idx: number) => ({
      title: item.title,
      artist: item.artist || '',
      cover: item.cover,
      linkUrl: item.linkUrl,
      rating: 4.0,
      sortNo: list.value.length + idx + 1,
      status: 1,
      type: currentType.value
    }))
    await importTopRatedBatch(payload)
    ElMessage.success('导入成功')
    showNeteaseDialog.value = false
    reload()
  } catch (e: any) {
    ElMessage.error(e.message || '导入失败')
  } finally {
    neteaseImporting.value = false
  }
}

function restoreNeteaseSelection() {
  if (!neteaseTableRef.value) return
  neteaseTableRef.value.clearSelection()
  const set = new Set(selectedNetease.value.map((item: any) => item.id))
  neteaseResults.value.forEach((row: any) => {
    if (set.has(row.id)) {
      neteaseTableRef.value.toggleRowSelection(row, true)
    }
  })
}

function removeSelected(item: any) {
  selectedNetease.value = selectedNetease.value.filter((i: any) => i.id !== item.id)
  restoreNeteaseSelection()
}

async function submitQuickAdd() {
  if (!quickForm.url) {
    ElMessage.warning('请先输入网易云音乐链接')
    return
  }
  if (!quickForm.title) {
    ElMessage.warning('请先点击“自动获取”或填写歌曲名称')
    return
  }
  if (!quickForm.cover) {
    ElMessage.warning('请先点击“自动获取”或填写封面地址')
    return
  }
  quickSubmitting.value = true
  try {
    const payload: any = {
      title: quickForm.title,
      artist: quickForm.artist || '',
      cover: quickForm.cover,
      linkUrl: quickForm.url,
      rating: 4.0,
      favoriteCount: quickForm.favoriteCount,
      sortNo: list.value.length + 1,
      status: 1,
      type: currentType.value
    }
    await createTopRated(payload)
    ElMessage.success('新增成功')
    showQuickAdd.value = false
    reload()
  } catch (e: any) {
    ElMessage.error('新增失败: ' + (e.message || '未知错误'))
  } finally {
    quickSubmitting.value = false
  }
}

async function moveUp(row: any, index: number) {
  if (index === 0) return
  const prev = list.value[index - 1]
  if (!prev) return
  const currSort = Number(row.sortNo ?? 0)
  const prevSort = Number(prev.sortNo ?? 0)
  try {
    await Promise.all([
      updateTopRated(String(row.id), { sortNo: prevSort }),
      updateTopRated(String(prev.id), { sortNo: currSort })
    ])
    ElMessage.success('上移成功')
    reload()
  } catch (e: any) {
    ElMessage.error('上移失败: ' + (e.message || '未知错误'))
  }
}

async function moveDown(row: any, index: number) {
  if (index === list.value.length - 1) return
  const next = list.value[index + 1]
  if (!next) return
  const currSort = Number(row.sortNo ?? 0)
  const nextSort = Number(next.sortNo ?? 0)
  try {
    await Promise.all([
      updateTopRated(String(row.id), { sortNo: nextSort }),
      updateTopRated(String(next.id), { sortNo: currSort })
    ])
    ElMessage.success('下移成功')
    reload()
  } catch (e: any) {
    ElMessage.error('下移失败: ' + (e.message || '未知错误'))
  }
}

// 一键根据「评论数 / 分享数」重排 sortNo，数值高的排前面
async function handleAutoSort() {
  if (!list.value.length) return
  autoSorting.value = true
  try {
    // 先按 favoriteCount 降序、本身 sortNo 升序生成一个新顺序
    const sorted = [...list.value].sort((a, b) => {
      const fa = Number(a.favoriteCount ?? 0)
      const fb = Number(b.favoriteCount ?? 0)
      if (fb !== fa) return fb - fa
      const sa = Number(a.sortNo ?? 0)
      const sb = Number(b.sortNo ?? 0)
      return sa - sb
    })

    // 按新顺序重新设置 sortNo，从 1 开始递增
    await Promise.all(
      sorted.map((item, index) =>
        updateTopRated(String(item.id), { sortNo: index + 1 })
      )
    )

    ElMessage.success('一键排序完成')
    reload()
  } catch (e: any) {
    ElMessage.error('一键排序失败: ' + (e?.message || '未知错误'))
  } finally {
    autoSorting.value = false
  }
}

function handleStatusChange(row: any) {
  const status = typeof row.status === 'number' ? row.status : row.status === 1 ? 1 : 0
  updateTopRatedStatus(row.id, status)
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
    deleteTopRated(row.id)
      .then(() => {
        ElMessage.success('删除成功')
        reload()
      })
      .catch((e: any) => {
        ElMessage.error('删除失败: ' + (e.message || '未知错误'))
      })
  })
}

async function handleBatchDelete() {
  if (!selectedRows.value.length) {
    ElMessage.warning('请先选择要删除的歌曲')
    return
  }
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${selectedRows.value.length} 条记录？`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }

  batchDeleting.value = true
  try {
    await Promise.all(selectedRows.value.map((row: any) => deleteTopRated(row.id)))
    ElMessage.success('批量删除成功')
    reload()
  } catch (e: any) {
    ElMessage.error('批量删除失败: ' + (e?.message || '未知错误'))
  } finally {
    batchDeleting.value = false
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
.card-type-toggle {
  display: flex;
  align-items: center;
  gap: 8px;
}
.card-type-toggle .pill {
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 12px;
  cursor: pointer;
  background: #f2f3f5;
  color: #606266;
}
.card-type-toggle .pill.active {
  background: #409eff;
  color: #fff;
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
.netease-search-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}
.remain-tip {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
}
.netease-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}
.netease-body {
  display: flex;
  gap: 16px;
}
.selected-panel {
  width: 280px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 8px;
  background: #fafafa;
  flex-shrink: 0;
}
.panel-header {
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 6px;
}
.selected-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.selected-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 6px;
  padding: 6px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}
.selected-info {
  flex: 1;
  min-width: 0;
}
.selected-title {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.selected-artist {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.empty-selected {
  font-size: 12px;
  color: #c0c4cc;
  padding: 12px 6px;
  text-align: center;
}
.selected-tip {
  font-size: 12px;
  color: #909399;
}
.tip-text {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}
.limit-tip {
  margin-top: 10px;
  font-size: 12px;
  color: #f56c6c;
}
.op-cell {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}
.op-left {
  display: flex;
  gap: 4px;
}
.op-right {
  display: flex;
  gap: 4px;
}
.sort-btn {
  width: 28px;
  height: 28px;
  padding: 0;
  text-align: center;
  font-weight: 700;
}
</style>



