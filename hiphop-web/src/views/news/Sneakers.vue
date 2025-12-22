<template>
  <div class="sneakers-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">SNEAKERS</h1>
      <p class="page-description">
        紧跟最新的球鞋发售与新闻，关注 Air Jordan、Yeezy 以及各类签名鞋的上新和爆料。
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

    <!-- 内容区域 -->
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else class="content-layout">
      <!-- 左侧：列表 -->
      <div class="list-column">
        <div
          v-for="(item, index) in displayedSneakers"
          :key="item.id || index"
          class="sneaker-card"
          :class="{ 'sneaker-card--featured': index === 0 }"
          @click="openLink(item.linkUrl)"
        >
          <div class="sneaker-cover">
            <img :src="item.imgUrl" :alt="item.title" />
          </div>
          <div class="sneaker-info">
            <span class="sneaker-tag">SNEAKERS</span>
            <h3 class="sneaker-title">{{ item.title }}</h3>
            <p class="sneaker-sub" v-if="item.brand || item.category">
              <span v-if="item.brand">{{ item.brand }}</span>
              <span v-if="item.brand && item.category" class="dot">·</span>
              <span v-if="item.category">{{ item.category }}</span>
            </p>
            <div class="sneaker-meta">
              <span v-if="item.price" class="meta-price">{{ item.price }}</span>
              <span v-if="item.createdAt" class="meta-date">{{ formatDate(item.createdAt) }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：广告占位 -->
      <div class="ad-column">
        <div class="ad-placeholder">AD</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getSneakerList } from '../../api/adminHome'

const activeTab = ref('latest')
const sneakers = ref([])
const loading = ref(true)
const error = ref('')

const fetchSneakers = async () => {
  try {
    loading.value = true
    error.value = ''
    const data = await getSneakerList({ status: 1 })
    // 统一字段命名，方便模板使用
    sneakers.value = (data || []).map((item) => ({
      id: item.id,
      title: item.title,
      imgUrl: item.imgUrl,
      linkUrl: item.linkUrl,
      brand: item.brand,
      category: item.category,
      price: item.price,
      sortNo: item.sortNo ?? 0,
      createdAt: item.createdAt
    }))
  } catch (err) {
    console.error('获取球鞋列表失败:', err)
    error.value = '加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

const displayedSneakers = computed(() => {
  const list = [...sneakers.value]
  if (activeTab.value === 'latest') {
    return list.sort((a, b) => {
      const ta = a.createdAt ? new Date(a.createdAt).getTime() : 0
      const tb = b.createdAt ? new Date(b.createdAt).getTime() : 0
      return tb - ta
    })
  }
  // 热门：按排序号从小到大
  return list.sort((a, b) => (a.sortNo || 0) - (b.sortNo || 0))
})

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  if (Number.isNaN(date.getTime())) return ''
  const year = date.getFullYear()
  const month = date.getMonth() + 1
  const day = date.getDate()
  return `${year}年${month}月${day}日`
}

const openLink = (url) => {
  if (url) {
    window.open(url, '_blank')
  }
}

onMounted(() => {
  fetchSneakers()
})
</script>

<style scoped>
.sneakers-container {
  min-height: 100vh;
  background-color: #fff;
  padding: 40px 20px;
  max-width: 1200px;
  margin: 0 auto;
}

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

.content-layout {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 40px;
}

.list-column {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.sneaker-card {
  display: grid;
  grid-template-columns: 260px 1fr;
  gap: 20px;
  border-bottom: 1px solid #eee;
  padding-bottom: 24px;
  cursor: pointer;
}

.sneaker-card--featured {
  border-top: 3px solid #e50914;
  padding-top: 12px;
}

.sneaker-cover {
  position: relative;
  width: 100%;
  padding-bottom: 70%;
  overflow: hidden;
  background: #f5f5f5;
}

.sneaker-cover img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.sneaker-info {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.sneaker-tag {
  font-size: 11px;
  color: #e50914;
  font-weight: 900;
  text-transform: uppercase;
  margin-bottom: 6px;
}

.sneaker-title {
  font-size: 20px;
  font-weight: 900;
  color: #000;
  margin: 0 0 8px 0;
  line-height: 1.3;
}

.sneaker-sub {
  font-size: 13px;
  color: #666;
  margin: 0 0 8px 0;
}

.sneaker-sub .dot {
  margin: 0 4px;
}

.sneaker-meta {
  font-size: 12px;
  color: #999;
  display: flex;
  gap: 12px;
  align-items: center;
}

.meta-price {
  font-weight: 700;
  color: #000;
}

.ad-column {
  display: flex;
  justify-content: center;
}

.ad-placeholder {
  background: #f5f5f5;
  width: 100%;
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #e0e0e0;
  font-size: 32px;
  font-weight: 900;
}

@media (max-width: 992px) {
  .content-layout {
    grid-template-columns: 1fr;
  }

  .sneaker-card {
    grid-template-columns: 1fr;
  }
}
</style>
