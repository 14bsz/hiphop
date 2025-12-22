<template>
  <div class="songs-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">歌曲</h1>
      <p class="page-description">聆听来自最佳嘻哈艺人的最新嘻哈和说唱歌曲。歌曲每日更新！</p>
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

    <!-- 歌曲列表 -->
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else class="songs-grid">
      <div 
        v-for="song in displayedSongs" 
        :key="song.id" 
        class="song-card"
        @click="openLink(song.linkUrl)"
      >
        <div class="song-cover">
          <img :src="song.cover" :alt="song.title" />
          <div class="song-overlay">
            <div class="rating-badge" v-if="song.rating">{{ song.rating }}</div>
          </div>
        </div>
        <div class="song-info">
          <h3 class="song-title">{{ song.title }}</h3>
          <p class="song-artist">{{ song.artist }}</p>
          <div class="song-meta" v-if="song.rating">
            <span class="meta-rating">
              VERY HOTTTTT
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getTopRatedList } from '../../api/adminHome'

const activeTab = ref('latest')
const songs = ref([])
const loading = ref(true)
const error = ref('')

// 获取单曲列表
const fetchSongs = async () => {
  try {
    loading.value = true
    error.value = ''
    // type=1 表示单曲
    const data = await getTopRatedList({ type: 1, status: 1 })
    songs.value = data || []
  } catch (err) {
    error.value = '加载失败，请稍后重试'
    console.error('获取歌曲列表失败:', err)
  } finally {
    loading.value = false
  }
}

// 根据tab显示不同的排序结果
const displayedSongs = computed(() => {
  if (activeTab.value === 'latest') {
    // 最新：按创建时间排序
    return [...songs.value].sort((a, b) => 
      new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    )
  } else {
    // 热门：按收藏数排序（API已经按favoriteCount降序排列）
    return songs.value
  }
})

// 格式化数字（如：1057 -> 1.1K）
const formatNumber = (num) => {
  if (!num) return '0'
  if (num >= 1000000) {
    return (num / 1000000).toFixed(1) + 'M'
  }
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'K'
  }
  return num.toString()
}


// 打开外链
const openLink = (url) => {
  if (url) {
    window.open(url, '_blank')
  }
}

onMounted(() => {
  fetchSongs()
})
</script>

<style scoped>
.songs-container {
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

/* 加载和错误状态 */
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

/* 歌曲网格 */
.songs-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 30px;
  margin-bottom: 40px;
}

/* 歌曲卡片 */
.song-card {
  background: #fff;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.02);
}

.song-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.08);
}

.song-cover {
  position: relative;
  width: 100%;
  padding-bottom: 100%; /* 1:1 比例 */
  overflow: hidden;
  background: #f5f5f5;
}

.song-cover img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.song-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(to bottom, rgba(0,0,0,0) 0%, rgba(0,0,0,0.3) 100%);
  opacity: 0;
  transition: opacity 0.3s;
}

.song-card:hover .song-overlay {
  opacity: 1;
}

.rating-badge {
  position: absolute;
  bottom: 10px;
  right: 10px;
  background: #e50914;
  color: #fff;
  font-size: 12px;
  font-weight: 900;
  padding: 4px 8px;
  border-radius: 3px;
}

/* 歌曲信息 */
.song-info {
  padding: 16px 12px 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.song-title {
  font-size: 16px;
  font-weight: 900;
  color: #000;
  margin: 0 0 8px 0;
  line-height: 1.3;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.song-artist {
  font-size: 13px;
  color: #666;
  margin: 0 0 10px 0;
  font-weight: 600;
}

.song-meta {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  color: #999;
}

.meta-rating {
  color: #e50914;
  font-weight: 900;
  text-transform: uppercase;
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
  .songs-container {
    padding: 20px 15px;
  }

  .page-title {
    font-size: 36px;
  }

  .songs-grid {
    grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
    gap: 20px;
  }
}
</style>
