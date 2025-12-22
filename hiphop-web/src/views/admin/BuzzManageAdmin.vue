<template>
  <div class="app-container">
    <el-card shadow="never" class="admin-card">
      <template #header>
        <div class="card-header">
          <div class="card-title">
            <span class="title-text">首页展位管理</span>
            <span class="title-sub">控制首页中间大图、左侧两个展位以及右侧热门趋势1-5</span>
          </div>
          <div class="card-actions">
            <el-button type="primary" plain icon="Plus" @click="openCreate">新增展位</el-button>
            <el-button type="success" plain icon="Link" @click="openQuickAdd">快速添加</el-button>
            <el-button icon="Refresh" @click="reload">刷新</el-button>
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
        <el-form-item label="展位位置" prop="position">
          <el-select v-model="query.position" placeholder="全部" clearable style="width: 200px">
            <el-option
              v-for="pos in positionOptions"
              :key="pos.value"
              :label="pos.label"
              :value="pos.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 200px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="reload">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 展位图列表 -->
      <div class="section-title">展位图管理</div>
      <el-table v-loading="loading" :data="featuredList" style="width: 100%" border>
        <el-table-column type="index" label="ID" width="60" align="center" />
        <el-table-column prop="position" label="位置" width="120" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.position === 'center'" type="success">中间大图</el-tag>
            <el-tag v-else-if="scope.row.position === 'left_top'" type="warning">左侧上方</el-tag>
            <el-tag v-else-if="scope.row.position === 'left_bottom'" type="info">左侧下方</el-tag>
            <el-tag v-else-if="scope.row.position === 'ticker'" type="primary">资讯跑马灯</el-tag>
            <el-tag v-else>{{ scope.row.position }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="150" show-overflow-tooltip />
        <el-table-column prop="tag" label="分类" width="100" align="center">
          <template #default="scope">
            <span>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="views" label="浏览量" width="100" align="center">
          <template #default="scope">
            <span>-</span>
          </template>
        </el-table-column>
        <el-table-column label="封面" width="100" align="center">
          <template #default="scope">
            <el-image 
              style="width: 50px; height: 50px; border-radius: 4px;"
              :src="normalizeImageUrl(scope.row.imgUrl)" 
              :preview-src-list="[normalizeImageUrl(scope.row.imgUrl)]"
              fit="cover"
              preview-teleported
            />
          </template>
        </el-table-column>
        <el-table-column prop="linkUrl" label="链接" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" align="center" width="80">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="openEdit(scope.row)">编辑</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分割线 -->
      <el-divider />

      <!-- 跑马灯列表 -->
      <div class="section-title">资讯跑马灯</div>
      <el-table v-loading="loading" :data="tickerList" style="width: 100%" border>
        <el-table-column type="index" label="ID" width="60" align="center" />
        <el-table-column prop="title" label="内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="linkUrl" label="链接" min-width="220" show-overflow-tooltip />
        <el-table-column prop="sortNo" label="排序" width="80" align="center" />
        <el-table-column label="状态" align="center" width="80">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="openEdit(scope.row)">编辑</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-divider />

      <!-- 热门趋势列表 -->
      <div class="section-title">
        <span>热门趋势管理</span>
        <el-button 
          type="primary" 
          size="small" 
          icon="Sort" 
          @click="sortTrendingByViews"
          style="margin-left: 12px;"
        >
          按浏览量排序
        </el-button>
      </div>
      <el-table v-loading="loading" :data="trendingList" style="width: 100%" border>
        <el-table-column type="index" label="ID" width="60" align="center" />
        <el-table-column prop="position" label="位置" width="120" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.position === 'trending_1'" type="danger">热门趋势1</el-tag>
            <el-tag v-else-if="scope.row.position === 'trending_2'" type="danger">热门趋势2</el-tag>
            <el-tag v-else-if="scope.row.position === 'trending_3'" type="danger">热门趋势3</el-tag>
            <el-tag v-else-if="scope.row.position === 'trending_4'" type="danger">热门趋势4</el-tag>
            <el-tag v-else-if="scope.row.position === 'trending_5'" type="danger">热门趋势5</el-tag>
            <el-tag v-else>{{ scope.row.position }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="150" show-overflow-tooltip />
        <el-table-column prop="tag" label="分类" width="100" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.tag" type="danger" size="small">{{ scope.row.tag }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="views" label="浏览量" width="100" align="center">
          <template #default="scope">
            {{ scope.row.views || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="封面" width="100" align="center">
          <template #default="scope">
            <el-image 
              style="width: 50px; height: 50px; border-radius: 4px;"
              :src="normalizeImageUrl(scope.row.imgUrl)" 
              :preview-src-list="[normalizeImageUrl(scope.row.imgUrl)]"
              fit="cover"
              preview-teleported
            />
          </template>
        </el-table-column>
        <el-table-column prop="linkUrl" label="链接" min-width="200" show-overflow-tooltip />
        <el-table-column prop="sortNo" label="排序" width="80" align="center" />
        <el-table-column label="状态" align="center" width="80">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="openEdit(scope.row)">编辑</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Quick Add Dialog -->
      <el-dialog title="快速添加" v-model="showQuickAddDialog" width="600px" append-to-body>
        <el-form ref="quickAddFormRef" :model="quickAddForm" label-width="100px">
          <el-form-item label="链接/BV号" required>
            <el-input 
              v-model="quickAddForm.url" 
              placeholder="请输入微信公众号文章链接或B站BV号（如：BV1DxqGBCE2g）"
              @blur="handleUrlBlur"
            >
              <template #append>
                <el-button @click="doFetchContent" :loading="fetching">自动获取</el-button>
              </template>
            </el-input>
            <div class="tip-text">支持：微信公众号文章链接、B站BV号</div>
          </el-form-item>
          <el-form-item label="展位位置">
            <el-select
              v-model="quickAddForm.position"
              style="width: 100%;"
              placeholder="请选择展位位置"
              clearable
            >
              <el-option
                v-for="pos in positionOptions"
                :key="pos.value"
                :label="pos.label"
                :value="pos.value"
                :disabled="isPositionDisabled(pos.value)"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="标题">
            <el-input v-model="quickAddForm.title" placeholder="自动获取" :disabled="true" />
          </el-form-item>
          <el-form-item label="封面图片">
            <el-input v-model="quickAddForm.imgUrl" placeholder="自动获取" :disabled="true" />
            <div v-if="quickAddForm.imgUrl" class="preview-img">
              <el-image 
                :src="normalizeImageUrl(quickAddForm.imgUrl)" 
                style="width: 200px; height: 120px; margin-top: 10px; border-radius: 4px;"
                fit="cover"
                :preview-src-list="[normalizeImageUrl(quickAddForm.imgUrl)]"
                @error="(e) => handleImageError(e, 'quick')"
              />
            </div>
          </el-form-item>
          <!-- 显示获取到的浏览量 -->
          <el-form-item v-if="quickAddForm.position === 'trending'" label="浏览量">
            <el-input 
              v-model="quickAddForm.views" 
              placeholder="输入数字，如: 10.9（自动添加k）" 
              @blur="handleViewsBlur('quick')"
              @focus="handleViewsFocus('quick')"
            />
            <div class="tip-text" v-if="quickAddForm.views">
              已自动获取浏览量: <strong style="color: #409EFF;">{{ quickAddForm.views }}</strong>
              <span style="color: #909399; margin-left: 8px;">（可手动修改，输入数字自动添加k）</span>
              <div style="color: #67C23A; margin-top: 4px;">💡 保存时会根据浏览量自动分配位置（浏览量高的排在前面）</div>
            </div>
            <div class="tip-text" v-else>
              点击"自动获取"按钮后，将自动爬取浏览量，或可手动输入数字（如：10.9）
              <div style="color: #67C23A; margin-top: 4px;">💡 保存时会根据浏览量自动分配位置（浏览量高的排在前面）</div>
            </div>
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button type="primary" @click="submitQuickAdd" :loading="fetching">确 定</el-button>
            <el-button @click="closeQuickAddDialog">取 消</el-button>
          </div>
        </template>
      </el-dialog>

      <!-- Dialog -->
      <el-dialog :title="form.id ? '编辑展位' : '新增展位'" v-model="showDialog" width="600px" append-to-body>
        <el-form ref="buzzFormRef" :model="form" :rules="rules" label-width="100px">
          <el-form-item label="展位位置" prop="position">
            <el-select v-model="form.position" style="width: 100%;">
              <el-option
                v-for="pos in positionOptions"
                :key="pos.value"
                :label="pos.label"
                :value="pos.value"
                :disabled="isPositionDisabled(pos.value, form.id)"
              />
            </el-select>
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
                style="width: 200px; height: 120px; margin-top: 10px; border-radius: 4px;"
                fit="cover"
                :preview-src-list="[normalizeImageUrl(form.imgUrl)]"
                @error="(e) => handleImageError(e, 'form')"
              />
            </div>
          </el-form-item>
          <el-form-item label="原文链接" prop="linkUrl">
            <el-input v-model="form.linkUrl" placeholder="请输入原文/原视频链接" />
          </el-form-item>
          <!-- 热门趋势专用字段 -->
          <template v-if="form.position === 'trending'">
            <el-form-item label="分类标签" prop="tag">
              <el-select v-model="form.tag" style="width: 100%;" placeholder="请选择分类">
                <el-option label="NEWS" value="NEWS" />
                <el-option label="FEATURES" value="FEATURES" />
                <el-option label="BEEF" value="BEEF" />
                <el-option label="MUSIC" value="MUSIC" />
              </el-select>
            </el-form-item>
            <el-form-item label="浏览量" prop="views">
              <el-input 
                v-model="form.views" 
                placeholder="输入数字，如: 10.9（自动添加k）" 
                @blur="handleViewsBlur('form')"
                @focus="handleViewsFocus('form')"
              />
              <div class="tip-text">输入数字即可，系统会自动添加k单位（如：输入10.9 → 10.9k）。保存时会根据浏览量自动分配位置（浏览量高的排在前面）</div>
            </el-form-item>
          </template>
          <el-row>
             <el-col :span="12">
                 <el-form-item label="状态" prop="status">
                    <el-radio-group v-model="form.status">
                    <el-radio :value="1">启用</el-radio>
                    <el-radio :value="0">禁用</el-radio>
                    </el-radio-group>
                </el-form-item>
             </el-col>
             <el-col :span="12" v-if="form.position === 'trending' || (form.position && form.position.startsWith('trending_')) || form.position === 'ticker'">
                <el-form-item label="排序" prop="sortNo">
                  <el-input-number v-model="form.sortNo" :min="0" />
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
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchContent, getFeaturedList, createFeatured, updateFeatured, deleteFeatured, updateFeaturedStatus } from '../../api/adminHome'

// 图片URL代理处理函数
function normalizeImageUrl(url: string): string {
  if (!url) return url
  const https = url.replace(/^http:/, 'https:')
  try {
    const urlObj = new URL(https)
    // 处理微信公众号图片
    if (/mmbiz\.qpic\.cn$/i.test(urlObj.hostname) || /qpic\.cn$/i.test(urlObj.hostname)) {
      return `/img-proxy?url=${encodeURIComponent(https)}`
    }
    // 处理B站图片
    if (/^i[0-9]\.hdslb\.com$/i.test(urlObj.hostname)) {
      if (https.includes('@') && https.endsWith('.webp')) {
        return https
      }
      return `/img-proxy?url=${encodeURIComponent(https)}`
    }
  } catch {}
  return https
}

const loading = ref(false)
const list = ref<any[]>([])
const query = reactive<{ position?: string | undefined; status?: number | undefined }>({ position: undefined, status: 1 })
const showDialog = ref(false)
const showQuickAddDialog = ref(false)
const fetching = ref(false)

// 展位位置选项（全局共享）
const positionOptions = [
  { label: '中间大图', value: 'center' },
  { label: '左侧上方', value: 'left_top' },
  { label: '左侧下方', value: 'left_bottom' },
  { label: '热门趋势', value: 'trending' },
  { label: '资讯跑马灯', value: 'ticker' }
]

const form = reactive<any>({ 
  id: undefined, 
  position: 'center', 
  title: '', 
  imgUrl: '', 
  linkUrl: '', 
  tag: '',
  views: '',
  sortNo: 0, 
  status: 1
})
const quickAddForm = reactive<any>({
  url: '',
  title: '',
  imgUrl: '',
  linkUrl: '',
  position: undefined,
  views: ''
})

// 计算当前已被启用展位占用的位置（只看 status = 1）
const usedPositions = computed(() => {
  return list.value
    .filter(item => item && item.status === 1 && item.position)
    .map(item => item.position)
})

// 展位图列表（center, left_top, left_bottom）
const featuredList = computed(() => {
  const filtered = list.value.filter(item => {
    if (!item || !item.position) return false
    return item.position === 'center' || item.position === 'left_top' || item.position === 'left_bottom'
  })
  
  return filtered.sort((a, b) => {
    // 按位置排序：center -> left_top -> left_bottom（每个位置只有一个固定的）
    const order: Record<string, number> = { 'center': 0, 'left_top': 1, 'left_bottom': 2 }
    const posA = (a.position || '').toLowerCase()
    const posB = (b.position || '').toLowerCase()
    const orderA = order[posA] ?? 999
    const orderB = order[posB] ?? 999
    
    // 如果位置不同，按位置排序
    if (orderA !== orderB) {
      return orderA - orderB
    }
    
    // 如果位置相同（理论上不应该发生，但为了稳定性），按ID排序
    const idA = a.id ? Number(a.id) : 999999
    const idB = b.id ? Number(b.id) : 999999
    return idA - idB
  })
})

// 热门趋势列表（trending_1 到 trending_5）
const trendingList = computed(() => {
  return list.value.filter(item => {
    if (!item || !item.position) return false
    return item.position.startsWith('trending_')
  }).sort((a, b) => {
    // 按sortNo排序：sortNo为1的显示在最上面
    const sortNoA = a.sortNo !== undefined && a.sortNo !== null ? a.sortNo : 999
    const sortNoB = b.sortNo !== undefined && b.sortNo !== null ? b.sortNo : 999
    return sortNoA - sortNoB
  })
})

// 跑马灯列表（ticker），按 sortNo 升序
const tickerList = computed(() => {
  return list.value
    .filter(item => item && item.position === 'ticker')
    .sort((a, b) => {
      const sortNoA = a.sortNo !== undefined && a.sortNo !== null ? a.sortNo : 999
      const sortNoB = b.sortNo !== undefined && b.sortNo !== null ? b.sortNo : 999
      return sortNoA - sortNoB
    })
})

// 判断某个位置是否已被其它展位占用
function isPositionDisabled(pos: string, currentId?: string | number) {
  // 跑马灯允许多条共存
  if (pos === 'ticker') return false
  return list.value.some(item => {
    if (!item) return false
    // 只考虑启用的展位
    if (item.status !== 1) return false
    if (item.position !== pos) return false
    // 编辑自身时，允许保留原来的位置
    if (currentId !== undefined && String(item.id) === String(currentId)) {
      return false
    }
    return true
  })
}

const rules = {
  title: [{ required: true, message: '标题不能为空', trigger: 'blur' }],
  imgUrl: [{ required: true, message: '图片地址不能为空', trigger: 'blur' }]
}

function reload() {
  loading.value = true
  const params: any = {}
  if (query.status !== undefined) {
    params.status = query.status
  }
  // 只在明确筛选时才传position，否则获取所有数据
  if (query.position) {
    params.position = query.position
  }
  console.log('刷新列表，查询参数:', params)
  return getFeaturedList(params).then(res => {
    console.log('获取到的数据:', res)
    list.value = Array.isArray(res) ? res : []
    return res
  }).catch((e: any) => {
    console.error('获取列表失败:', e)
    throw e
  }).finally(() => {
    loading.value = false
  })
}

function resetQuery() {
    query.position = undefined
    query.status = undefined
    reload()
}

function openCreate() {
  Object.assign(form, { 
    id: undefined, 
    position: 'center', 
    title: '', 
    imgUrl: `https://picsum.photos/300/200?random=${Date.now()}`, 
    linkUrl: '', 
    tag: '',
    views: '',
    sortNo: 0, 
    status: 1
  })
  showDialog.value = true
}

function openQuickAdd() {
  Object.assign(quickAddForm, {
    url: '',
    title: '',
    imgUrl: '',
    linkUrl: '',
    position: undefined,
    views: ''
  })
  showQuickAddDialog.value = true
}

function closeQuickAddDialog() {
  showQuickAddDialog.value = false
}

async function handleUrlBlur() {
  if (quickAddForm.url && quickAddForm.url.trim()) {
    await doFetchContent()
  }
}

// 处理浏览量输入，自动添加"k"单位
function handleViewsInput(value: string, formType: 'quick' | 'form') {
  // 如果值为空或只有空格，返回空字符串
  if (!value || value.trim() === '') {
    return ''
  }
  
  // 去除所有空格
  const trimmed = value.replace(/\s/g, '')
  
  // 如果值为"0"或"0k"，返回空字符串，让用户可以重新输入
  if (trimmed === '0' || trimmed === '0k' || trimmed === '0K') {
    return ''
  }
  
  // 如果已经包含k或K（不区分大小写），直接返回（统一转为小写k）
  if (/[kK]$/.test(trimmed)) {
    return trimmed.replace(/[kK]$/, 'k')
  }
  
  // 如果是纯数字（可能包含小数点），自动添加k
  // 匹配：123, 123., 123.45, 0.5 等（但0已经被上面处理了）
  if (/^\d+\.?\d*$/.test(trimmed)) {
    return trimmed + 'k'
  }
  
  // 其他情况（如用户输入了其他字符），直接返回原值（去除空格）
  return trimmed
}

// 处理浏览量输入框获得焦点事件（清除默认值"0"）
function handleViewsFocus(formType: 'quick' | 'form') {
  if (formType === 'quick') {
    // 如果当前值是"0"或"0k"，清空以便用户输入
    if (quickAddForm.views === '0' || quickAddForm.views === '0k' || quickAddForm.views === '0K') {
      quickAddForm.views = ''
    }
  } else {
    // 如果当前值是"0"或"0k"，清空以便用户输入
    if (form.views === '0' || form.views === '0k' || form.views === '0K') {
      form.views = ''
    }
  }
}

// 处理浏览量输入框失焦事件
function handleViewsBlur(formType: 'quick' | 'form') {
  if (formType === 'quick') {
    quickAddForm.views = handleViewsInput(quickAddForm.views, 'quick')
  } else {
    form.views = handleViewsInput(form.views, 'form')
  }
}

async function doFetchContent() {
  if (!quickAddForm.url || !quickAddForm.url.trim()) {
    ElMessage.warning('请输入链接或BV号')
    return
  }
  
  fetching.value = true
  try {
    const data = await fetchContent(quickAddForm.url.trim())
    quickAddForm.title = data.title || ''
    quickAddForm.imgUrl = data.coverImage || ''
    quickAddForm.linkUrl = data.sourceUrl || quickAddForm.url
    // 如果获取到了浏览量，保存到临时变量中（用于后续提交）
    if (data.views) {
      quickAddForm.views = data.views
      // 如果已选择热门趋势位置，显示提示
      if (quickAddForm.position === 'trending') {
        ElMessage.success(`获取成功！浏览量: ${data.views}`)
      } else {
        ElMessage.success('获取成功！' + (data.views ? ` 浏览量: ${data.views}（选择热门趋势位置后将自动使用）` : ''))
      }
    } else {
      ElMessage.success('获取成功！')
    }
  } catch (e: any) {
    ElMessage.error(e.message || '获取失败，请检查链接是否正确')
  } finally {
    fetching.value = false
  }
}

async function submitQuickAdd() {
  if (!quickAddForm.title || !quickAddForm.imgUrl) {
    ElMessage.warning('请先获取标题和封面')
    return
  }

  if (!quickAddForm.position) {
    ElMessage.warning('请选择展位位置')
    return
  }
  
  // 直接保存到展位表
  let finalPosition = quickAddForm.position
  let finalSortNo = quickAddForm.position === 'center' ? 0 : (quickAddForm.position === 'left_top' ? 1 : 2)
  
  // 如果是热门趋势位置，根据浏览量自动分配位置
  if (quickAddForm.position === 'trending') {
    finalPosition = assignTrendingPosition(quickAddForm.views || '0')
    finalSortNo = parseInt(finalPosition.replace('trending_', '')) || 0
  }

  // 跑马灯默认按现有数量追加到队尾
  if (quickAddForm.position === 'ticker') {
    const tickerCount = list.value.filter(item => item && item.position === 'ticker').length
    finalSortNo = tickerCount + 1
  }
  
  const featuredPayload: any = {
    position: finalPosition,
    title: quickAddForm.title,
    imgUrl: quickAddForm.imgUrl,
    linkUrl: quickAddForm.linkUrl,
    status: 1,
    sortNo: finalSortNo
  }
  // 如果是热门趋势位置，添加 tag 和 views
  if (quickAddForm.position === 'trending') {
    featuredPayload.tag = 'NEWS'
    // 使用爬取到的浏览量，如果为空则设置为空字符串（允许用户后续手动输入）
    featuredPayload.views = (quickAddForm.views && quickAddForm.views.trim() !== '') ? quickAddForm.views.trim() : ''
  }
  
  try {
    await createFeatured(featuredPayload)
    // 如果是热门趋势，保存后重新排序所有热门趋势项目
    if (quickAddForm.position === 'trending') {
      // 先重新加载数据，确保包含刚添加的项目
      await reload()
      // 然后重新排序所有热门趋势项目
      await reorderAllTrendingItems()
      // 排序后再次重新加载，显示最新排序结果
      await reload()
    } else {
      reload()
    }
    ElMessage.success('展位添加成功' + (quickAddForm.position === 'trending' ? '，已自动按浏览量排序' : ''))
    showQuickAddDialog.value = false
  } catch (e: any) {
    ElMessage.error('添加失败: ' + (e.message || '未知错误'))
  }
}

function openEdit(it: any) {
  console.log('编辑数据:', it)
  // 如果是trending_X位置，转换为trending以便编辑
  let editPosition = it.position || 'center'
  if (editPosition && editPosition.startsWith('trending_')) {
    editPosition = 'trending'
  }
  
  Object.assign(form, {
    id: it.id,
    position: editPosition,
    title: it.title || '',
    imgUrl: it.imgUrl || '',
    linkUrl: it.linkUrl || '',
    tag: it.tag || '',
    views: it.views || '',
    status: it.status !== undefined ? it.status : 1,
    sortNo: it.sortNo || 0
  })
  console.log('表单数据:', form)
  showDialog.value = true
}

function closeDialog() {
  showDialog.value = false
}

function generateRandomImage() {
    form.imgUrl = `https://picsum.photos/300/200?random=${Date.now()}`
}

function handleImageError(e: any, source: 'quick' | 'form') {
  // 对于 el-image，不直接改 DOM img.src，而是改绑定的数据，避免虚拟 DOM 覆盖导致不停闪动
  const model = source === 'quick' ? quickAddForm : form
  let current = model.imgUrl as string
  if (!current) return

  // 统一成 https
  const https = current.replace(/^http:/, 'https:')

  // 已经是占位图了，再失败就直接放弃，避免无限循环
  if (https.startsWith('https://picsum.photos')) {
    console.warn('占位图加载也失败，停止重试:', https)
    return
  }

  // 已经通过代理请求过了，说明代理也失败，直接换占位图，避免闪动
  if (https.startsWith('/img-proxy')) {
    console.warn('代理图片加载失败，使用占位图:', https)
    model.imgUrl = `https://picsum.photos/300/200?random=${Date.now()}`
    return
  }

  try {
    const urlObj = new URL(https)
    const host = urlObj.hostname

    // B 站图片：第一次失败时，改用本地 /img-proxy 代理再试一次
    if (/^i[0-9]\.hdslb\.com$/i.test(host)) {
      console.warn('B站图片直链失败，尝试代理:', https)
      model.imgUrl = `/img-proxy?url=${encodeURIComponent(https)}`
      return
    }

    // 微信图片：第一次失败时，改用本地 /img-proxy 代理再试一次
    if (/mmbiz\.qpic\.cn$/i.test(host) || /qpic\.cn$/i.test(host)) {
      console.warn('微信图片直链失败，尝试代理:', https)
      model.imgUrl = `/img-proxy?url=${encodeURIComponent(https)}`
      return
    }
  } catch {
    // URL 解析失败，走占位图兜底
  }

  // 其它情况，直接退回到随机占位图，避免界面出现 FAILED
  console.warn('图片最终加载失败，使用占位图:', https)
  model.imgUrl = `https://picsum.photos/300/200?random=${Date.now()}`
}


function submit() {
  // 只提交需要的字段
  let finalPosition = form.position
  let finalSortNo = form.sortNo
  
  // 如果是热门趋势位置，根据浏览量自动分配位置
  if (form.position === 'trending' || (form.position && form.position.startsWith('trending_'))) {
    // 如果是编辑已有的trending项目，需要传入当前ID
    const currentId = form.id
    finalPosition = assignTrendingPosition(form.views || '0', currentId)
    finalSortNo = parseInt(finalPosition.replace('trending_', '')) || 0
  } else {
    // 非热门趋势位置，只在创建新项目时设置默认sortNo，编辑时保留用户设置的sortNo
    if (!form.id && form.position !== 'ticker') {
      // 创建新项目时，使用默认sortNo
      finalSortNo = form.position === 'center' ? 0 : (form.position === 'left_top' ? 1 : 2)
    }
    if (!form.id && form.position === 'ticker') {
      const tickerCount = list.value.filter(item => item && item.position === 'ticker').length
      finalSortNo = tickerCount + 1
    }
    // 编辑时，保留用户设置的sortNo（已在form.sortNo中）
  }
  
  const payload: any = {
    position: finalPosition,
    title: form.title,
    imgUrl: form.imgUrl,
    linkUrl: form.linkUrl,
    status: form.status,
    sortNo: finalSortNo
  }
  // 如果是热门趋势位置，添加 tag 和 views
  if (form.position === 'trending' || (form.position && form.position.startsWith('trending_'))) {
    payload.tag = form.tag || 'NEWS'
    // 始终发送 views 字段，如果为空则设置为空字符串（允许用户后续手动输入）
    payload.views = (form.views !== undefined && form.views !== null && form.views.trim() !== '') ? form.views.trim() : ''
  }
  
  console.log('提交数据:', payload)
  console.log('提交ID:', form.id, '类型:', typeof form.id)
  
  // 确保 ID 作为字符串传递，避免 JavaScript Number 精度问题
  const action = form.id ? updateFeatured(String(form.id), payload) : createFeatured(payload)
  
  action.then(async (result) => {
    console.log('更新结果:', result)
    // 如果是热门趋势，保存后重新排序所有热门趋势项目
    if (form.position === 'trending' || (form.position && form.position.startsWith('trending_'))) {
      // 先重新加载数据，确保包含刚更新的项目
      await reload()
      // 然后重新排序所有热门趋势项目
      await reorderAllTrendingItems()
      // 排序后再次重新加载，显示最新排序结果
      await reload()
    } else {
      // 清除位置筛选，确保能看到所有数据
      query.position = undefined
      await reload()
    }
    ElMessage.success(form.id ? '修改成功' + (form.position === 'trending' || (form.position && form.position.startsWith('trending_')) ? '，已自动按浏览量排序' : '') : '新增成功')
    showDialog.value = false
    // 清除位置筛选，确保能看到所有数据
    query.position = undefined
  }).catch((e: any) => {
    console.error('操作失败:', e)
    ElMessage.error('操作失败: ' + (e.message || '未知错误'))
  })
}

function handleStatusChange(row: any) {
  const status = typeof row.status === 'number' ? row.status : (row.status === 1 ? 1 : 0)
  updateFeaturedStatus(row.id, status).then(() => {
      ElMessage.success('状态更新成功')
  }).catch(() => {
      row.status = row.status === 1 ? 0 : 1 // revert on failure
      ElMessage.error('状态更新失败')
  })
}

function handleDelete(row: any) {
    ElMessageBox.confirm('是否确认删除该展位?', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(() => {
        deleteFeatured(row.id).then(() => {
            ElMessage.success('删除成功')
            reload()
        }).catch((e: any) => {
            ElMessage.error('删除失败: ' + (e.message || '未知错误'))
        })
    })
}

// 根据浏览量自动分配热门趋势位置（1-5）
function assignTrendingPosition(views: string, currentId?: string | number): string {
  // 获取所有启用的热门趋势项目（排除当前编辑的项目）
  const allTrending = list.value.filter(item => {
    if (!item || item.status !== 1) return false
    if (!item.position || !item.position.startsWith('trending_')) return false
    // 编辑时排除自己
    if (currentId !== undefined && String(item.id) === String(currentId)) return false
    return true
  })
  
  // 将当前项目加入列表进行排序
  const currentViews = parseViewsToNumber(views || '0')
  const itemsToSort = [
    ...allTrending,
    { views: views || '0', id: currentId }
  ]
  
  // 按浏览量降序排序
  itemsToSort.sort((a, b) => {
    const viewsA = parseViewsToNumber(a.views || '0')
    const viewsB = parseViewsToNumber(b.views || '0')
    
    // 如果两个都是0或无效值，保持原顺序
    if (viewsA === 0 && viewsB === 0) {
      const sortNoA = a.sortNo !== undefined && a.sortNo !== null ? a.sortNo : 999
      const sortNoB = b.sortNo !== undefined && b.sortNo !== null ? b.sortNo : 999
      return sortNoA - sortNoB
    }
    
    // 如果一个是0，另一个不是0，0的排在后面
    if (viewsA === 0) return 1
    if (viewsB === 0) return -1
    
    // 两个都有浏览量，按降序排序
    return viewsB - viewsA
  })
  
  // 找到当前项目在排序后的位置（1-5）
  let currentIndex = -1
  if (currentId !== undefined) {
    // 编辑模式：通过ID查找
    currentIndex = itemsToSort.findIndex(item => String(item.id) === String(currentId))
  } else {
    // 新建模式：通过浏览量查找（可能不准确，但新建时通常不会有完全相同的浏览量）
    currentIndex = itemsToSort.findIndex((item, index) => {
      // 如果是最后一个且没有ID，说明是新建的项目
      return index === itemsToSort.length - 1 && !item.id
    })
  }
  
  // 如果找不到，默认放在最后
  if (currentIndex === -1) {
    currentIndex = itemsToSort.length - 1
  }
  
  // 返回对应的trending_X位置（1-5）
  const position = Math.min(currentIndex + 1, 5)
  return `trending_${position}`
}

// 解析浏览量字符串为数字（用于排序）
function parseViewsToNumber(views: string): number {
  if (!views || views.trim() === '' || views === '-') {
    return 0
  }
  
  const trimmed = views.trim().toLowerCase()
  
  // 处理"万"单位（中文）
  if (trimmed.includes('万') || trimmed.includes('w')) {
    const numStr = trimmed.replace(/[万千w]/g, '')
    const num = parseFloat(numStr) || 0
    return num * 10000
  }
  
  // 处理"k"单位（千）
  if (trimmed.includes('k')) {
    const numStr = trimmed.replace(/k/g, '')
    const num = parseFloat(numStr) || 0
    return num * 1000
  }
  
  // 处理"M"单位（百万）
  if (trimmed.includes('m')) {
    const numStr = trimmed.replace(/m/g, '')
    const num = parseFloat(numStr) || 0
    return num * 1000000
  }
  
  // 纯数字
  return parseFloat(trimmed) || 0
}

// 重新排序所有热门趋势项目（保存时自动调用）
async function reorderAllTrendingItems() {
  // 获取所有启用的热门趋势项目
  const allTrending = list.value.filter(item => {
    if (!item || item.status !== 1) return false
    if (!item.position || !item.position.startsWith('trending_')) return false
    return true
  })
  
  if (allTrending.length === 0) {
    return
  }
  
  // 按浏览量降序排序
  const sorted = [...allTrending].sort((a, b) => {
    const viewsA = parseViewsToNumber(a.views || '0')
    const viewsB = parseViewsToNumber(b.views || '0')
    
    // 如果两个都是0或无效值，保持原顺序
    if (viewsA === 0 && viewsB === 0) {
      const sortNoA = a.sortNo !== undefined && a.sortNo !== null ? a.sortNo : 999
      const sortNoB = b.sortNo !== undefined && b.sortNo !== null ? b.sortNo : 999
      return sortNoA - sortNoB
    }
    
    // 如果一个是0，另一个不是0，0的排在后面
    if (viewsA === 0) return 1
    if (viewsB === 0) return -1
    
    // 两个都有浏览量，按降序排序
    return viewsB - viewsA
  })
  
  // 更新所有项目的sortNo和position
  const updatePromises = sorted.map((item, index) => {
    const newSortNo = index + 1
    const newPosition = `trending_${newSortNo}`
    
    // 如果位置和sortNo都已经正确，跳过更新
    if (item.position === newPosition && item.sortNo === newSortNo) {
      return Promise.resolve()
    }
    
    return updateFeatured(item.id, {
      position: newPosition,
      title: item.title,
      imgUrl: item.imgUrl,
      linkUrl: item.linkUrl,
      tag: item.tag || '',
      views: item.views || '',
      status: item.status,
      sortNo: newSortNo
    })
  })
  
  try {
    await Promise.all(updatePromises)
  } catch (e: any) {
    console.error('重新排序失败:', e)
    // 不抛出错误，避免影响主流程
  }
}

// 按浏览量排序热门趋势（手动点击按钮）
async function sortTrendingByViews() {
  const trendingItems = trendingList.value.filter(item => item.status === 1)
  
  if (trendingItems.length === 0) {
    ElMessage.warning('没有启用的热门趋势项目')
    return
  }
  
  // 按浏览量降序排序（没有浏览量或浏览量为0的项目排在最后）
  const sorted = [...trendingItems].sort((a, b) => {
    const viewsA = parseViewsToNumber(a.views || '0')
    const viewsB = parseViewsToNumber(b.views || '0')
    
    // 如果两个都是0或无效值，按原来的sortNo排序（保持稳定）
    if (viewsA === 0 && viewsB === 0) {
      const sortNoA = a.sortNo !== undefined && a.sortNo !== null ? a.sortNo : 999
      const sortNoB = b.sortNo !== undefined && b.sortNo !== null ? b.sortNo : 999
      return sortNoA - sortNoB
    }
    
    // 如果一个是0，另一个不是0，0的排在后面
    if (viewsA === 0) return 1
    if (viewsB === 0) return -1
    
    // 两个都有浏览量，按降序排序
    return viewsB - viewsA
  })
  
  // 更新sortNo（1-5）
  const updatePromises = sorted.map((item, index) => {
    const newSortNo = index + 1
    // 如果sortNo已经正确，跳过更新
    if (item.sortNo === newSortNo) {
      return Promise.resolve()
    }
    
    return updateFeatured(item.id, {
      position: item.position,
      title: item.title,
      imgUrl: item.imgUrl,
      linkUrl: item.linkUrl,
      tag: item.tag || '',
      views: item.views || '',
      status: item.status,
      sortNo: newSortNo
    })
  })
  
  try {
    await Promise.all(updatePromises)
    const hasViewsCount = sorted.filter(item => parseViewsToNumber(item.views || '0') > 0).length
    const noViewsCount = sorted.length - hasViewsCount
    let message = '排序成功！'
    if (hasViewsCount > 0) {
      message += `已按浏览量从高到低排序（前${hasViewsCount}个有浏览量）`
    }
    if (noViewsCount > 0) {
      message += `，${noViewsCount}个无浏览量的项目已排到最后`
    }
    ElMessage.success(message)
    reload()
  } catch (e: any) {
    ElMessage.error('排序失败: ' + (e.message || '未知错误'))
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
.card-actions > .el-button + .el-button {
  margin-left: 8px;
}
.query-form {
  margin-bottom: 16px;
}
.mb8 {
  margin-bottom: 8px;
}
.tip-text {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
.preview-img {
  margin-top: 10px;
}
.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 20px 0 16px 0;
  padding-bottom: 8px;
  border-bottom: 2px solid #409EFF;
  display: flex;
  align-items: center;
}
.section-title:first-of-type {
  margin-top: 0;
}
</style>
