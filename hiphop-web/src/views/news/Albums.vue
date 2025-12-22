<template>
  <div class="albums-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">混音专辑</h1>
      <p class="page-description">
        聆听最新的嘻哈与说唱混音专辑，来自最顶级的嘻哈艺人。专辑持续更新。
      </p>
    </div>

    <!-- Tab 切换 -->
    <div class="tabs">
      <div
        class="tab-item"
        :class="{ active: activeTab === 'latest' }"
        @click="activeTab = 'latest'"
      >
        最新
      </div>
      <div
        class="tab-item"
        :class="{ active: activeTab === 'popular' }"
        @click="activeTab = 'popular'"
      >
        热门
      </div>
    </div>

    <!-- 专辑列表 -->
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else class="albums-grid">
      <div
        v-for="album in displayedAlbums"
        :key="album.id"
        class="album-card"
        @click="openLink(album.linkUrl)"
      >
        <div class="album-cover">
          <img :src="album.cover" :alt="album.title" />
        </div>
        <div class="album-info">
          <h3 class="album-title">{{ album.title }}</h3>
          <p class="album-artist">{{ album.artist }}</p>
          <div class="album-meta" v-if="album.rating">
            <span class="meta-rating">超高热度</span>
          </div>
          <div class="album-extra">
            <span class="meta-views" v-if="album.favoriteCount">
              <el-icon><View /></el-icon>
              {{ formatNumber(album.favoriteCount) }} 次播放
            </span>
            <span class="meta-date" v-if="album.createdAt">
              {{ formatDate(album.createdAt) }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { View } from '@element-plus/icons-vue'
import { getTopRatedList } from '../../api/adminHome'

const activeTab = ref('latest')
const albums = ref([])
const loading = ref(true)
const error = ref('')

// 获取专辑列表（type=2 表示专辑）
const fetchAlbums = async () => {
  try {
    loading.value = true
    error.value = ''
    const data = await getTopRatedList({ type: 2, status: 1 })
    albums.value = data || []
  } catch (err) {
    console.error('获取专辑列表失败:', err)
    error.value = '加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

// 根据 Tab 显示不同的排序结果
const displayedAlbums = computed(() => {
  if (activeTab.value === 'latest') {
    return [...albums.value].sort(
      (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    )
  }
  // 热门：按收藏数排序（接口默认已按 favoriteCount 排序，这里做一次兜底）
  return [...albums.value].sort((a, b) => (b.favoriteCount || 0) - (a.favoriteCount || 0))
})

// 格式化播放量
const formatNumber = (num) => {
  if (!num) return '0'
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + ' 万'
  }
  return String(num)
}

// 格式化日期为中文
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  if (Number.isNaN(date.getTime())) return ''
  const year = date.getFullYear()
  const month = date.getMonth() + 1
  const day = date.getDate()
  return `${year}年${month}月${day}日`
}

// 打开外链
const openLink = (url) => {
  if (url) {
    window.open(url, '_blank')
  }
}

onMounted(() => {
  fetchAlbums()
})
</script>

<style scoped>
.albums-container {
  min-height: 100vh;
  background-color: #fff;
  padding: 40px 20px;
  max-width: 1200px;
  margin: 0 auto;
}

/* 页面头部 */
.page-header {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 2px solid #000;
}

.page-title {
  font-family: 'Impact', sans-serif;
  font-size: 48px;
  font-weight: 900;
  color: #000;
  margin: 0 0 10px 0;
  text-transform: uppercase;
  letter-spacing: 2px;
}

.page-description {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin: 0;
}

/* Tab 切换 */
.tabs {
  display: flex;
  gap: 0;
  margin-bottom: 30px;
  border-bottom: 2px solid #e0e0e0;
}

.tab-item {
  padding: 12px 30px;
  font-size: 14px;
  font-weight: 900;
  text-transform: uppercase;
  cursor: pointer;
  color: #999;
  border-bottom: 3px solid transparent;
  transition: all 0.3s;
  position: relative;
  bottom: -2px;
}

.tab-item:hover {
  color: #000;
}

.tab-item.active {
  color: #e50914;
  border-bottom-color: #e50914;
}

/* 状态 */
.loading,
.error {
  text-align: center;
  padding: 60px 20px;
  font-size: 16px;
  color: #666;
}

.error {
  color: #e50914;
}

/* 专辑网格 */
.albums-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 40px;
  margin-bottom: 40px;
}

/* 专辑卡片 */
.album-card {
  cursor: pointer;
}

.album-cover {
  position: relative;
  width: 100%;
  padding-bottom: 100%;
  overflow: hidden;
  background: #f5f5f5;
}

.album-cover img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.album-info {
  padding: 18px 4px 22px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.album-title {
  font-size: 16px;
  font-weight: 900;
  color: #000;
  margin: 0 0 6px 0;
  line-height: 1.3;
}

.album-artist {
  font-size: 13px;
  color: #666;
  margin: 0 0 10px 0;
  font-weight: 600;
}

.album-meta {
  font-size: 11px;
  color: #999;
}

.meta-rating {
  color: #e50914;
  font-weight: 900;
}

.album-extra {
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 24px;
  font-size: 12px;
  color: #999;
}

.meta-views {
  display: flex;
  align-items: center;
  gap: 4px;
}

.meta-date {
  color: #999;
}

/* 响应式 */
@media (max-width: 768px) {
  .albums-container {
    padding: 20px 15px;
  }

  .page-title {
    font-size: 36px;
  }

  .albums-grid {
    grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
    gap: 24px;
  }
}
</style>
