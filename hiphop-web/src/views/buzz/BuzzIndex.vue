<template>
  <div class="buzz-container" @mousemove="handleMouseMove">
    <!-- Parallax Background Text -->
    <div class="bg-text-layer" :style="{ transform: `translateY(${scrollY * 0.2}px)` }">
       <!-- Removed HIPHOP background text -->
    </div>

    <!-- Featured Grid (HNHH Style) -->
    <div class="featured-wrapper">
      <div class="featured-grid">
        <!-- Left Column: Stacked News -->
        <div class="featured-col left">
             <div class="featured-card small" v-for="(item, index) in leftNews" :key="index" @click="openLink(item.url)">
               <div class="f-img-container">
                 <img :src="item.img" @error="onImgError" />
               </div>
               <div class="f-text-container">
                  <span class="f-tag">{{ item.tag }}</span>
                  <h3 class="f-title">{{ item.title }}</h3>
               </div>
             </div>
          </div>

        <!-- Center Column: Top Story -->
        <div class="featured-col center">
            <div class="featured-card big" @click="openLink(topStory.url)">
              <img :src="topStory.img" class="f-big-img" @error="onImgError" />
              <div class="f-big-title-box">
                 <span class="f-tag top-story">{{ topStory.tag }}</span>
                 <h1 class="f-big-title">{{ topStory.title }}</h1>
              </div>
            </div>
          </div>

        <!-- Right Column: Trending -->
        <div class="featured-col right">
           <div class="f-trending-header red-stripe">
              <h3>热门趋势</h3>
           </div>
           <div class="f-trending-list">
               <div class="f-trend-item" v-for="(item, index) in trendingList" :key="index" @click="openLink(item.url)">
                 <span class="f-trend-rank">{{ index + 1 }}</span>
                 <div class="f-trend-content">
                    <span class="f-trend-tag">{{ item.tag }}</span>
                    <h4 class="f-trend-title">{{ item.title }}</h4>
                    <span class="f-trend-views"><el-icon><View /></el-icon> {{ item.views }}</span>
                 </div>
                 <img :src="item.img" class="f-trend-thumb" @error="onImgError" />
              </div>
           </div>
         </div>
      </div>
    </div>

    <!-- The Ticker (Flow) -->
    <div class="ticker-wrap" v-if="tickerDisplay.length">
       <div class="ticker">
          <div
            class="ticker-item"
            v-for="(item, index) in tickerDisplay"
            :key="index"
            @click="openLink(item.url)"
          >
             <span class="dot">●</span>
             <span class="ticker-text">{{ item.text }}</span>
             <span class="divider">//</span>
          </div>
       </div>
    </div>

    <!-- Content Grid -->
    <div class="content-wrapper">
      
      <!-- Top Rated Music Section -->
      <div class="top-rated-section">
         <div class="section-head">
            <div class="head-info">
               <h2>高分音乐榜</h2>
               <p>发现你的下一首循环单曲</p>
            </div>
            <div class="toggle-pills">
               <span
                 class="pill"
                 :class="{ active: activeType === 1 }"
                 @click="activeType = 1"
               >单曲</span>
               <span
                 class="pill"
                 :class="{ active: activeType === 2 }"
                 @click="activeType = 2"
               >专辑</span>
            </div>
         </div>
         <div class="top-rated-grid">
             <div
               class="rated-card"
               v-for="(item, index) in (activeType === 1 ? topRatedSingles : topRatedAlbums)"
               :key="index"
               :class="[getTopRatedCardClass(index), { highlight: index === 0 }]"
               @click="openLink(item.url)"
             >
               <div class="rated-cover">
                 <img :src="item.cover" @error="onImgError" />
                 <div class="rated-overlay" v-if="index === 0">
                     <span class="fire-badge">热门</span>
                 </div>
               </div>
                 <div class="rated-info">
                  <h4>{{ item.title }}</h4>
                  <p>{{ item.artist }}</p>
                  <div class="rating-score">
                     <el-icon>
                       <ChatDotRound v-if="activeType === 1" />
                       <Share v-else />
                     </el-icon>
                     <span>
                       {{ item.favoriteCount ?? 0 }}
                       {{ activeType === 1 ? ' 评论' : ' 分享' }}
                     </span>
                  </div>
               </div>
            </div>
         </div>
      </div>

      <!-- Main Columns -->
      <div class="main-columns">
         <!-- Left: Daily Songs -->
         <div class="col-daily">
            <div class="col-header black">
               <h3>每日推荐</h3>
               <span class="archives-link">历史归档 →</span>
            </div>
            <!-- Diagonal stripe decoration -->
            <div class="daily-stripe-decoration"></div>
            <div class="daily-songs-list">
                <div class="daily-item" v-for="(item, index) in dailySongs" :key="index" @click="openLink(item.url)">
                   <div class="daily-img-wrapper">
                     <img :src="item.cover" @error="onImgError" />
                     <!-- TODAY badge for first item -->
                     <div class="daily-today-badge" v-if="index === 0">TODAY</div>
                   </div>
                   <div class="daily-info">
                      <h4 class="daily-title">{{ item.title }}</h4>
                      <p class="daily-artist">{{ item.artist }}</p>
                      <p class="daily-badge">{{ item.badge }}</p>
                   </div>
               </div>
            </div>
         </div>

         <!-- Center: Latest News -->
         <div class="col-news">
            <div class="col-header red-stripe">
               <h3>最新资讯</h3>
            </div>
            <div class="news-list">
               <div class="news-item" v-for="(item, index) in latestNews" :key="index" @click="openLink(item.url)">
                  <div class="news-img">
                     <img :src="item.cover" @error="onImgError" />
                  </div>
                  <div class="news-content">
                     <span class="news-cat">{{ item.category }}</span>
                     <h4>{{ item.title }}</h4>
                     <div class="news-meta">
                        <span class="news-views"><el-icon><View /></el-icon> {{ item.views }}</span>
                        <span class="news-date">{{ item.date }}</span>
                     </div>
                  </div>
               </div>
            </div>
         </div>

         <!-- Right: Ad -->
         <div class="col-ad">
             <div class="ad-placeholder">
                <span>广告位</span>
             </div>
         </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { VideoPlay, Headset, Document, CaretRight, VideoCamera, Mute, Microphone, Sunny, View, StarFilled, Share, ChatDotRound } from '@element-plus/icons-vue'
import axios from 'axios'
import { getLatestDaily } from '../../api/adminHome'

const scrollY = ref(0)
const defaultTickerItems = [
  { text: '突发新闻：KANYE WEST 宣布全新 YEEZY 系列发售', url: '' },
  { text: 'METRO BOOMIN 想要更多', url: '' }
]
const tickerItems = ref(defaultTickerItems)
const isUsingDefaultTicker = () => {
  return (
    tickerItems.value.length === defaultTickerItems.length &&
    tickerItems.value.every((item, idx) => item.text === defaultTickerItems[idx].text)
  )
}
const tickerDisplay = computed(() => {
  const base = tickerItems.value && tickerItems.value.length > 0 ? tickerItems.value : defaultTickerItems
  const loop = Math.max(10, base.length * 3)
  return Array.from({ length: loop }, (_, i) => base[i % base.length])
})
const onImgError = (e) => {
  const w = e.target.clientWidth || 300
  const h = e.target.clientHeight || 200
  const src = e.target.src || ''
  // 处理 Bilibili 图片
  if (/i[0-9]\.hdslb\.com/.test(src) && !src.startsWith('/img-proxy')) {
    e.target.src = `/img-proxy?url=${encodeURIComponent(src)}`
    return
  }
  // 处理微信公众号图片
  if ((/mmbiz\.qpic\.cn/i.test(src) || /qpic\.cn/i.test(src)) && !src.startsWith('/img-proxy')) {
    e.target.src = `/img-proxy?url=${encodeURIComponent(src)}`
    return
  }
  e.target.src = `https://picsum.photos/${w}/${h}?random=${Date.now()}`
}
const normalize = (u) => {
  if (!u) return u
  const https = u.replace(/^http:/, 'https:')
  try {
    const url = new URL(https)
    // 处理 Bilibili 图片
    if (/^i[0-9]\.hdslb\.com$/i.test(url.hostname)) {
      if (https.includes('@') && https.endsWith('.webp')) {
        return https
      }
      return `/img-proxy?url=${encodeURIComponent(https)}`
    }
    // 处理微信公众号图片 - 自动通过代理
    if (/mmbiz\.qpic\.cn$/i.test(url.hostname) || /qpic\.cn$/i.test(url.hostname)) {
      return `/img-proxy?url=${encodeURIComponent(https)}`
    }
  } catch {}
  return https
}

// 高分音乐榜：从后端表读取（单曲 / 专辑）
const activeType = ref(1)
const topRatedSingles = ref([])
const topRatedAlbums = ref([])

const dailySongs = ref([
    { title: 'Tron Cena', artist: 'BabyTron', badge: '热度爆表', date: '2025年12月13日', cover: 'https://picsum.photos/100/100?random=11' },
    { title: 'Chances', artist: 'Polo G', badge: '热度爆表', date: '2025年12月13日', cover: 'https://picsum.photos/100/100?random=12' },
    { title: 'Top Cobain', artist: 'NBA Youngboy', badge: '热度爆表', date: '2025年12月12日', cover: 'https://picsum.photos/100/100?random=13' },
    { title: 'Over', artist: 'Bone Thugs-N-Harmony', badge: '热度爆表', date: '2025年12月12日', cover: 'https://picsum.photos/100/100?random=14' },
])

const latestNews = ref([
    { title: 'The A Ma Maniere x Air Jordan 4 "Dark Mocha" Drops This Week', category: '球鞋', views: '1.9万', date: '2025年12月13日', cover: 'https://picsum.photos/200/150?random=21' },
    { title: '21 Savage "WHAT HAPPENED TO THE STREETS?" Review', category: '乐评', views: '2.9万', date: '2025年12月13日', cover: 'https://picsum.photos/200/150?random=22' },
    { title: 'Jaidyn Alexis Involved In Car Crash With Blueface\'s Kids On Board', category: '八卦', views: '50.9万', date: '2025年12月13日', cover: 'https://picsum.photos/200/150?random=23' },
    { title: 'DJ Akademiks Posts Video Of Himself Partying With Maino As Foud...', category: '音乐', views: '50.5万', date: '2025年12月13日', cover: 'https://picsum.photos/200/150?random=24' },
    { title: 'Mother Of NLE Choppa\'s Child Jokes He\'s More Focused On NBA...', category: '情感', views: '88.4万', date: '2025年12月13日', cover: 'https://picsum.photos/200/150?random=25' },
])

const leftNews = ref([
    { title: 'Charlamagne Tha God 与 "The Breakfast Club" 将在 Netflix 上线', tag: '新闻', img: 'https://picsum.photos/300/200?random=101' },
    { title: 'NBA Youngboy 说唱涉嫌殴打狱友', tag: '音乐', img: 'https://picsum.photos/300/200?random=102' }
])
const trendingList = ref([
    { title: 'Air Jordan 12 "Bloodline" 发售日期及详情', tag: '球鞋', views: '3.8k', img: 'https://picsum.photos/100/100?random=103' },
    { title: '50 Cent 希望 Eminem 制作新音乐，称他为"依然是第一"', tag: '电影', views: '270k', img: 'https://picsum.photos/100/100?random=104' },
    { title: '50 Cent 称 Jim Jones 为"告密者"，回应他的评论', tag: '音乐', views: '42k', img: 'https://picsum.photos/100/100?random=105' },
    { title: 'Air Jordan 12 "Ducks" 传闻发售日期', tag: '球鞋', views: '64', img: 'https://picsum.photos/100/100?random=106' },
    { title: 'Drake 告诉 Euro "慢点死" 在泄露的 DM 中', tag: '牛肉', views: '1.8k', img: 'https://picsum.photos/100/100?random=107' }
])
const topStory = ref({
  title: 'Kay Flock 被判处三十年监禁',
  img: 'https://picsum.photos/800/600?random=108',
  tag: '头条故事'
})
const feedList = ref([])

// 根据索引返回不同的高分音乐卡片样式类名
const getTopRatedCardClass = (index) => {
  const base = 'rated-style-'
  // 只需要 0-5 六种样式，防御性处理一下
  const i = index % 6
  return base + i
}

const handleScroll = () => {
  scrollY.value = window.scrollY
}

const toggleMute = () => {
    if (heroVideo.value) {
        heroVideo.value.muted = !heroVideo.value.muted
        isMuted.value = heroVideo.value.muted
    }
}

const openLink = (url) => {
  if (url && url !== '#') {
    window.open(url, '_blank')
  }
}

const fetchData = async () => {
  try {
    let featuredData = []
    // 1. 优先从展位表获取三个展位的数据
    try {
      const featuredRes = await axios.get('http://localhost:8080/api/home/featured?status=1')
      if (Array.isArray(featuredRes.data) && featuredRes.data.length > 0) {
        featuredData = featuredRes.data
        
        // 中间大图
        const centerItem = featuredData.find(d => d.position === 'center')
        if (centerItem) {
          topStory.value = {
            title: centerItem.title,
            tag: '头条故事',
            img: normalize(centerItem.imgUrl),
            url: centerItem.linkUrl
          }
        }
        
        // 左侧两个小图
        const leftTopItem = featuredData.find(d => d.position === 'left_top')
        const leftBottomItem = featuredData.find(d => d.position === 'left_bottom')
        const leftItems = []
        if (leftTopItem) {
          leftItems.push({
            title: leftTopItem.title,
            tag: '资讯',
            img: normalize(leftTopItem.imgUrl),
            url: leftTopItem.linkUrl
          })
        }
        if (leftBottomItem) {
          leftItems.push({
            title: leftBottomItem.title,
            tag: '资讯',
            img: normalize(leftBottomItem.imgUrl),
            url: leftBottomItem.linkUrl
          })
        }
        if (leftItems.length > 0) {
          leftNews.value = leftItems
        }
      }
    } catch (featuredError) {
      console.warn('展位数据获取失败:', featuredError)
    }
    
    // 2. 从展位表获取热门趋势数据（position为trending_1到trending_5）
    if (Array.isArray(featuredData) && featuredData.length > 0) {
      const trendingItems = featuredData.filter(item => 
        item.position && item.position.startsWith('trending_')
      )
      if (trendingItems.length > 0) {
        // 按sortNo排序（如果sortNo相同，则按position排序作为备用）
        trendingItems.sort((a, b) => {
          const sortNoA = a.sortNo !== undefined && a.sortNo !== null ? a.sortNo : 999
          const sortNoB = b.sortNo !== undefined && b.sortNo !== null ? b.sortNo : 999
          if (sortNoA !== sortNoB) {
            return sortNoA - sortNoB
          }
          // 如果sortNo相同，按position排序作为备用
          const posA = parseInt(a.position.replace('trending_', '')) || 0
          const posB = parseInt(b.position.replace('trending_', '')) || 0
          return posA - posB
        })
        // 只取前5条
        trendingList.value = trendingItems.slice(0, 5).map((item) => ({
          title: item.title,
          tag: item.tag || 'NEWS', // 从数据库获取标签
          views: item.views || '0', // 从数据库获取浏览量
          img: normalize(item.imgUrl),
          url: item.linkUrl
        }))
      }

      // 跑马灯内容（position = ticker）
      const tickerRecords = featuredData.filter(item => item.position === 'ticker' && item.status === 1)
      if (tickerRecords.length > 0) {
        tickerItems.value = tickerRecords.map(item => ({
          text: item.title || item.tag || '',
          url: item.linkUrl
        })).filter(item => item.text)
      }
    }
    
    // 3. 获取其他数据（如果热门趋势数据为空，则从buzz接口获取作为fallback）
    let data = []
    try {
      const apiRes = await axios.get('http://localhost:8080/api/home/buzz?status=1')
      if (Array.isArray(apiRes.data) && apiRes.data.length > 0) {
        data = apiRes.data.map(item => ({
          title: item.title,
          cover_image: item.imgUrl,
          source_url: item.linkUrl,
          tag: item.category || '资讯',
          views: item.views || 0,
          author: item.category,
          publish_time: item.createdAt,
          category: item.category,
          featured: item.featured || 0
        }))
      }
    } catch (apiError) {
      console.warn('后端API获取失败，尝试使用mock数据:', apiError)
    }
    
    // 如果后端没有数据，使用mock数据作为fallback
    if (data.length === 0) {
      try {
        const res = await axios.get('/mock/buzz.json')
        data = res.data || []
      } catch (mockError) {
        console.error('Mock数据获取失败:', mockError)
      }
    }
    
    // 3. 如果没有从展位表获取到数据，使用fallback逻辑
    if (!topStory.value.title && data && data.length > 0) {
      let centerItem = data.find(d => d.category === 'center' || d.featured === 1)
      if (!centerItem) {
        centerItem = data.find(d => d.source_url && d.source_url.includes('mp.weixin.qq.com'))
      }
      if (!centerItem && data.length > 2) {
        centerItem = data[2]
      }
      if (centerItem) {
        topStory.value = {
          title: centerItem.title,
          tag: '头条故事',
          img: normalize(centerItem.cover_image),
          url: centerItem.source_url
        }
      }
    }
    
    if (leftNews.value.length === 0 && data && data.length > 0) {
      const leftItems = data.filter(d => d.category === 'left' || d.tag === 'left').slice(0, 2)
      if (leftItems.length === 0) {
        leftItems.push(...data.slice(0, 2))
      }
      leftNews.value = leftItems.map((item, i) => ({
          title: item.title,
          tag: item.tag || '资讯',
          img: normalize(item.cover_image) || `https://picsum.photos/300/200?random=${i}`,
          url: item.source_url
      }))
    }
    
    // 4. Trending列表（如果从热门趋势接口没有获取到数据，则使用fallback逻辑）
    if (trendingList.value.length === 0 && data && data.length > 0) {
      const rightItems = data.filter(d => d.category === 'right' || d.tag === 'right').slice(0, 5)
      const remainingItems = data.filter(d => 
        d.category !== 'left' && d.category !== 'center' && 
        d.tag !== 'left' && d.tag !== 'center'
      )
      const trendingSource = rightItems.length > 0 ? rightItems : remainingItems.slice(0, 5)
      trendingList.value = trendingSource.map((item, i) => ({
          title: item.title,
          tag: item.tag || '热点',
          views: item.views ? (item.views > 1000 ? (item.views/1000).toFixed(1) + 'k' : item.views) : '10k',
          img: normalize(item.cover_image) || `https://picsum.photos/100/100?random=${i+10}`,
          url: item.source_url
      }))

      // 跑马灯兜底：如果还在用默认内容，则用资讯标题拼跑马灯
      if (isUsingDefaultTicker()) {
        tickerItems.value = data.slice(0, 6).map((item, i) => ({
          text: item.title || `热讯 ${i + 1}`,
          url: item.source_url
        }))
      }

      // 5. Daily Songs (Mix of Bilibili/Music tagged items or random)
      const musicItems = data.filter(d => d.tag === 'Bilibili' || d.tag === 'Music' || d.source === 'Bilibili');
      const songSource = musicItems.length > 0 ? musicItems : data.slice(0, 4);
      dailySongs.value = songSource.slice(0, 4).map((item, i) => ({
          title: item.title,
          artist: item.author || '未知艺术家',
          badge: '热度爆表',
          date: item.publish_time ? new Date(item.publish_time).toLocaleDateString() : '今天',
          cover: normalize(item.cover_image) || `https://picsum.photos/100/100?random=${i+30}`,
          url: item.source_url
      }));

      // 6. Feed List (All)
      feedList.value = data
    }

    // 7. Latest News：优先从最新资讯表获取
    try {
      const newsRes = await axios.get('http://localhost:8080/api/home/news', {
        params: { status: 1 }
      })
      if (Array.isArray(newsRes.data) && newsRes.data.length > 0) {
        latestNews.value = newsRes.data.map((item, i) => ({
            title: item.title,
            category: item.category || item.source || '综合',
            views: '',
            date: item.createdAt ? new Date(item.createdAt).toLocaleDateString() : '刚刚',
            cover: normalize(item.imgUrl) || `https://picsum.photos/200/150?random=${i+20}`,
            url: item.linkUrl
        }))
      }
    } catch (newsError) {
      console.warn('最新资讯数据获取失败:', newsError)
    }

    // 如果最新资讯仍然为空，使用原有的 buzz 数据兜底
    if (latestNews.value.length === 0 && data && data.length > 0) {
      const newsSource = data.length > 8 ? data.slice(8) : data.slice(0, 5)
      latestNews.value = newsSource.map((item, i) => ({
          title: item.title,
          category: item.tag || '综合',
          views: item.views ? (item.views > 1000 ? (item.views/1000).toFixed(1) + '万' : item.views) : '',
          date: item.publish_time ? new Date(item.publish_time).toLocaleDateString() : '刚刚',
          cover: normalize(item.cover_image) || `https://picsum.photos/200/150?random=${i+20}`,
          url: item.source_url
      }))
    }
    // 如果前面的步骤没有填充跑马灯，最后再用通用数据兜底
    if (isUsingDefaultTicker() && data.length > 0) {
      tickerItems.value = data.slice(0, 6).map((item, i) => ({
        text: item.title || `快讯 ${i + 1}`,
        url: item.source_url
      }))
    }
  } catch (e) {
    console.error('Failed to fetch buzz data:', e)
  }
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
  // 先加载首页 Buzz/资讯数据
  fetchData()
  // 单独加载高分音乐榜（只取状态启用的），区分单曲 / 专辑
  const loadTopRated = async (type, targetRef) => {
    try {
      const res = await axios.get('http://localhost:8080/api/home/top-rated', {
        params: { status: 1, type }
      })
      if (Array.isArray(res.data)) {
        // 按「排序」字段 sortNo 从小到大排列，完全跟后台管理一致
        const sorted = [...res.data].sort((a, b) => {
          const sa = Number(a.sortNo ?? 0)
          const sb = Number(b.sortNo ?? 0)
          if (sa !== sb) return sa - sb
          // sortNo 一样时，按评论数/分享数从高到低作为次级排序，保证顺序稳定
          const fa = Number(a.favoriteCount ?? 0)
          const fb = Number(b.favoriteCount ?? 0)
          return fb - fa
        })
        targetRef.value = sorted
          .slice(0, 6)
          .map((item, i) => ({
            title: item.title,
            artist: item.artist || '未知',
                favoriteCount: item.favoriteCount ?? 0,
            cover: normalize(item.cover || item.imgUrl) || `https://picsum.photos/200/200?random=${i + 40}`,
            url: item.linkUrl
          }))
      }
    } catch (err) {
      console.warn('加载高分音乐榜失败:', err)
    }
  }
  loadTopRated(1, topRatedSingles)
  loadTopRated(2, topRatedAlbums)
  
  // 加载每日推荐
  const loadDailyRecommend = async () => {
    try {
      const data = await getLatestDaily(10)
      if (Array.isArray(data) && data.length > 0) {
        dailySongs.value = data.map((item, i) => {
          // 格式化日期
          let dateStr = '今天'
          if (item.recommendDate) {
            const date = new Date(item.recommendDate)
            const year = date.getFullYear()
            const month = date.getMonth() + 1
            const day = date.getDate()
            dateStr = `${year}年${month}月${day}日`
          }
          
          return {
            title: item.title,
            artist: item.artist || '未知艺人',
            badge: item.badge || 'HOTTTTT',
            date: dateStr,
            cover: normalize(item.cover) || `https://picsum.photos/100/100?random=${i + 30}`,
            url: item.linkUrl
          }
        })
      }
    } catch (err) {
      console.warn('加载每日推荐失败:', err)
    }
  }
  loadDailyRecommend()
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.buzz-container {
  background-color: var(--hh-bg-dark);
  color: var(--hh-text-primary);
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
}

/* --- Parallax Background Text --- */
.bg-text-layer {
  position: fixed;
  top: 20%;
  left: -5%;
  z-index: 0;
  pointer-events: none;
  opacity: 0.03;
  font-family: 'Impact', sans-serif;
  font-size: 20vw;
  line-height: 0.8;
  color: #fff;
  white-space: nowrap;
  transform-origin: left top;
}

/* --- Featured Grid (HNHH Style) --- */
.featured-wrapper {
   max-width: 1400px;
   margin: 24px auto 16px;
   padding: 0 32px;
}
.featured-grid {
   display: flex;
   gap: 20px;
   height: 500px; /* Fixed height for the grid layout */
}
.featured-col {
   display: flex;
   flex-direction: column;
}
.featured-col.left { width: 20%; gap: 20px; }
.featured-col.center { width: 55%; }
.featured-col.right { width: 25%; }

/* Left Small Cards */
.featured-card.small {
   flex: 1;
   display: flex;
   flex-direction: column;
   cursor: pointer;
}
.featured-card.small .f-img-container {
   height: 180px;
   overflow: hidden;
   border-radius: 4px;
   position: relative;
   margin-bottom: 10px;
}
.featured-card.small img {
   width: 100%; height: 100%; object-fit: cover;
   transition: transform 0.3s;
}
.featured-card.small:hover img { transform: scale(1.05); }

.f-text-container {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
}
.f-tag {
   background: transparent;
   color: #d32f2f;
   font-size: 10px;
   font-weight: 800;
   text-transform: uppercase;
   margin-bottom: 5px;
   padding: 0;
}
.f-title {
   font-size: 16px;
   line-height: 1.3;
   margin: 0;
   font-weight: bold;
   color: var(--hh-text-primary);
}
.featured-card.small:hover .f-title { color: #d32f2f; }

/* Center Big Card */
.featured-card.big {
   width: 100%; height: 100%;
   position: relative;
   border-radius: 4px;
   overflow: hidden;
   cursor: pointer;
}
.f-big-img {
   width: 100%; height: 100%; object-fit: cover;
   transition: transform 0.5s;
}
.featured-card.big:hover .f-big-img { transform: scale(1.02); }

.f-big-title-box {
   position: absolute;
   bottom: 40px;
   left: 50%;
   transform: translateX(-50%);
   background: #fff;
   padding: 20px 40px;
   text-align: center;
   min-width: 80%;
   box-shadow: 0 5px 15px rgba(0,0,0,0.2);
}
.f-tag.top-story {
   position: absolute;
   top: -12px;
   left: 50%;
   transform: translateX(-50%);
   background: #d32f2f;
   color: #fff;
   font-size: 10px;
   padding: 4px 10px;
   margin: 0;
   font-weight: bold;
   text-transform: uppercase;
}
.f-big-title {
   color: #000;
   font-size: 28px;
   font-weight: 900;
   line-height: 1.2;
   margin: 0;
   text-transform: capitalize;
}

/* Right Trending List */
.f-trending-header.red-stripe {
    background: repeating-linear-gradient(
     45deg,
     #d32f2f,
     #d32f2f 10px,
     #e53935 10px,
     #e53935 20px
   );
   padding: 10px;
   margin-bottom: 20px;
   border: none;
}
.f-trending-header h3 {
   margin: 0;
   color: #fff;
   font-size: 16px;
   font-weight: 900;
   text-transform: uppercase;
   background: transparent;
   padding: 0;
}

.f-trending-list {
   display: flex;
   flex-direction: column;
   gap: 20px;
}
.f-trend-item {
   display: flex;
   align-items: flex-start;
   gap: 15px;
   cursor: pointer;
}
.f-trend-rank {
   font-size: 36px;
   font-weight: 900;
   color: #ccc;
   line-height: 0.8;
   font-family: 'Impact', sans-serif;
   min-width: 25px;
}
.f-trend-content {
   flex: 1;
}
.f-trend-tag {
   font-size: 10px;
   color: #d32f2f;
   font-weight: bold;
   text-transform: uppercase;
   display: block;
   margin-bottom: 4px;
}
.f-trend-title {
   font-size: 14px;
   font-weight: bold;
   margin: 0 0 5px 0;
   line-height: 1.3;
   color: #000;
}
.f-trend-item:hover .f-trend-title { color: #d32f2f; }
.f-trend-views {
   font-size: 11px;
   color: #999;
   font-weight: normal;
   display: flex; align-items: center; gap: 4px;
}
.f-trend-thumb {
   width: 100px; height: 70px;
   object-fit: cover;
   border-radius: 4px;
}

/* --- The Ticker (Flow) --- */
.ticker-wrap {
  width: 100%;
  overflow: hidden;
  background-color: var(--hh-accent);
  color: #000;
  padding: 12px 0;
  font-family: 'Microsoft YaHei', sans-serif;
  font-weight: 700;
  font-size: 14px;
  white-space: nowrap;
  position: relative;
  z-index: 10;
  box-shadow: 0 4px 20px rgba(0,0,0,0.2);
  margin-top: 40px;
  margin-bottom: 1px;
  letter-spacing: 1px;
}
.ticker {
  display: inline-block;
  animation: ticker 40s linear infinite;
}
.ticker-item {
  display: inline-block;
  margin-right: 60px;
  cursor: pointer;
}
.ticker-item:hover .ticker-text { color: #222; }
.ticker-text { margin-right: 8px; }
.ticker .dot {
  color: #000;
  margin-right: 10px;
}
.ticker .divider {
  margin-left: 20px;
  opacity: 0.3;
}
@keyframes ticker {
  0% { transform: translateX(0); }
  100% { transform: translateX(-50%); }
}

/* --- Content Wrapper --- */
.content-wrapper {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 40px 80px;
  position: relative;
  z-index: 2;
}

.section-title h2 {
  font-family: 'Microsoft YaHei', 'Arial Black', sans-serif;
  font-weight: 900;
  font-size: 36px;
  margin: 0 0 40px 0;
  position: relative;
  display: inline-block;
  text-transform: uppercase;
  color: var(--hh-text-primary);
}
.section-title .dot {
  color: var(--hh-accent);
  font-size: 60px;
  line-height: 0;
  position: absolute;
  right: -20px;
  bottom: 10px;
}

/* Trending Grid - The Drop (Visual Hierarchy) */
.trend-card {
  position: relative;
  overflow: hidden;
  cursor: pointer;
  background: var(--hh-bg-card);
  border-radius: var(--hh-radius);
  box-shadow: 0 10px 30px -10px rgba(0, 0, 0, 0.3);
  border: 1px solid var(--hh-border);
  transition: all 0.4s ease;
}
.trend-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 20px 40px -10px rgba(0, 0, 0, 0.5);
  border-color: rgba(255,255,255,0.2);
}
.img-container {
  overflow: hidden;
  position: relative;
  width: 100%;
  height: 100%;
}
.trend-card img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  filter: saturate(0.8) brightness(0.9);
}
.trend-card:hover img {
  transform: scale(1.05);
  filter: saturate(1.1) brightness(1);
}
.trend-info {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  padding: 30px;
  background: linear-gradient(to top, rgba(15,17,21,0.95), transparent);
  z-index: 2;
  transition: transform 0.3s;
}

/* Highlight Drop for Rank 1 */
.highlight-drop {
    border-left: 3px solid var(--hh-accent); /* Highlighting vertical bar */
    padding-left: 30px;
}
.highlight-drop h3 {
    font-size: 32px; /* Bigger font */
    line-height: 1.35;
    font-weight: 700;
    color: #fff;
    margin: 0;
}
.highlight-drop .trend-meta {
    color: var(--hh-accent);
    font-weight: bold;
}

.trend-rank {
  position: absolute;
  top: 20px;
  left: 20px;
  font-family: 'Impact', sans-serif;
  font-size: 64px;
  color: rgba(255,255,255,0.06);
  z-index: 5;
  line-height: 1;
  pointer-events: none;
}
.trend-card.big .trend-rank {
    font-size: 120px;
    right: 30px;
    left: auto;
    color: rgba(255,255,255,0.05);
}

.trend-card.big { height: 500px; }
.trend-row { display: flex; gap: 20px; }
.trend-card.medium { flex: 1; height: 300px; }

.trend-meta {
    color: var(--hh-accent);
    font-weight: bold;
    font-size: 12px;
    margin-bottom: 8px;
    display: inline-flex;
    align-items: center;
    gap: 5px;
    background: rgba(0,0,0,0.3);
    padding: 4px 8px;
    border-radius: 4px;
    backdrop-filter: blur(4px);
}
.trend-info h3 {
    margin: 0;
    font-size: 20px; /* Smaller for rank 2/3 */
    font-weight: 500;
    line-height: 1.3;
}

/* Latest Feed - Daily Drops Style */
.latest-feed {
    border-top: 1px solid var(--hh-border);
    padding-top: 40px;
    margin-top: 60px;
}
.section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 30px;
}
.section-header h3 {
    font-size: 24px;
    font-weight: 900;
    color: var(--hh-text-primary);
    margin: 0;
    font-family: 'Impact', sans-serif;
    letter-spacing: 1px;
}
.sub-text {
    font-size: 14px;
    color: var(--hh-text-secondary);
    font-weight: normal;
    font-family: 'Microsoft YaHei', sans-serif;
    margin-left: 10px;
    letter-spacing: 0;
}
.view-all {
    color: var(--hh-accent);
    font-weight: bold;
}

/* Live Indicator */
.live-indicator {
   display: inline-flex;
   align-items: center;
   gap: 6px;
   font-size: 10px;
   color: #ff3b30;
   background: rgba(255, 59, 48, 0.1);
   padding: 2px 6px;
   border-radius: 4px;
   margin-left: 10px;
   vertical-align: middle;
}
.live-dot {
   width: 6px;
   height: 6px;
   background-color: #ff3b30;
   border-radius: 50%;
   animation: blink 1.5s infinite;
}
@keyframes blink {
   0% { opacity: 1; }
   50% { opacity: 0.3; }
   100% { opacity: 1; }
}

/* Countdown Timer */
.countdown-timer {
   font-size: 14px;
   color: var(--hh-text-secondary);
   font-weight: bold;
   font-family: 'Monaco', 'Courier New', monospace; /* Monospaced for timer look */
   background: var(--hh-bg-card);
   padding: 4px 10px;
   border-radius: 4px;
   border: 1px solid var(--hh-border);
   margin-left: 15px;
   letter-spacing: 0;
   vertical-align: middle;
}

.daily-group {
    margin-bottom: 50px;
}
.date-header {
    background-color: var(--hh-bg-card);
    padding: 10px 20px;
    font-family: 'Impact', sans-serif;
    font-size: 20px;
    color: var(--hh-text-primary);
    border-left: 4px solid var(--hh-accent);
    margin-bottom: 20px;
    display: flex;
    align-items: center;
    gap: 15px;
}
.date-sub {
    font-size: 14px;
    color: var(--hh-text-secondary);
    font-family: 'Microsoft YaHei', sans-serif;
    font-weight: bold;
}

.drop-list-item {
    display: flex;
    align-items: center;
    padding: 15px;
    border-bottom: 1px solid var(--hh-border);
    transition: all 0.2s ease;
    cursor: pointer;
}
.drop-list-item:hover {
    background-color: var(--hh-bg-card);
    padding-left: 25px; /* Slight slide right */
}
.item-cover {
    width: 60px;
    height: 60px;
    position: relative;
    border-radius: 4px;
    overflow: hidden;
    flex-shrink: 0;
    margin-right: 20px;
}
.item-cover img {
    width: 100%; height: 100%; object-fit: cover;
}
.play-overlay {
    position: absolute; top:0; left:0; width:100%; height:100%;
    background: rgba(0,0,0,0.5);
    display: flex; align-items: center; justify-content: center;
    opacity: 0; transition: opacity 0.2s;
    color: #fff;
}
.drop-list-item:hover .play-overlay { opacity: 1; }

.item-info {
    flex: 1;
    margin-right: 20px;
}
.item-title {
    margin: 0 0 5px 0;
    font-size: 16px;
    font-weight: bold;
    color: var(--hh-text-primary);
    font-family: 'Microsoft YaHei', sans-serif;
}
.drop-list-item:hover .item-title { color: var(--hh-accent); }
.item-artist {
    margin: 0;
    font-size: 13px;
    color: var(--hh-text-secondary);
}

.item-meta {
    text-align: right;
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: 5px;
}
.rating {
    display: flex;
    gap: 2px;
}
.fire-icon {
    font-size: 14px;
    color: #333; /* Inactive color */
}
.fire-icon.active {
    color: #ff5e00; /* Fire Orange */
}
.rating-text {
    font-size: 10px;
    font-weight: 900;
    margin-left: 5px;
    color: #ff5e00;
    font-style: italic;
}
.item-views {
    font-size: 12px;
    color: var(--hh-text-primary); /* Darker for visibility */
    font-weight: bold;
}

/* Sidebar Chart List */
.chart-list {
    margin-top: 20px;
}
.chart-item {
    display: flex;
    align-items: center;
    margin-bottom: 15px;
    padding-bottom: 15px;
    border-bottom: 1px solid rgba(255,255,255,0.05);
}
.chart-rank {
    font-size: 24px;
    font-weight: 900;
    color: var(--hh-text-tertiary);
    width: 30px;
    font-family: 'Impact', sans-serif;
    opacity: 0.5;
}
.chart-cover {
    width: 50px; height: 50px;
    border-radius: 4px;
    overflow: hidden;
    margin: 0 15px;
}
.chart-cover img { width: 100%; height: 100%; object-fit: cover; }
.chart-info h4 {
    margin: 0 0 4px 0;
    font-size: 14px;
    color: var(--hh-text-primary);
}
.chart-info p {
    margin: 0;
    font-size: 12px;
    color: var(--hh-text-secondary);
}
.full-width-btn {
    width: 100%;
    margin-top: 10px;
    border-color: var(--hh-border);
    color: var(--hh-text-primary);
}
.full-width-btn:hover {
    border-color: var(--hh-accent);
    color: var(--hh-accent);
}


.sidebar-widget h3 {
    border-bottom: 1px solid var(--hh-border);
    font-family: 'Microsoft YaHei', sans-serif;
    font-weight: 800;
    padding-bottom: 20px;
    margin-top: 0;
    font-size: 18px;
    color: var(--hh-text-primary);
    letter-spacing: 0.5px;
}
.tag-pill {
    background-color: rgba(255,255,255,0.03);
    border: 1px solid rgba(255,255,255,0.08);
    text-transform: uppercase;
    letter-spacing: 1px;
    font-family: 'Microsoft YaHei', sans-serif;
    color: var(--hh-text-secondary);
    border-radius: 6px;
    padding: 8px 16px;
    font-size: 13px;
    transition: all 0.3s;
    cursor: pointer;
    display: inline-block;
    margin-right: 10px;
    margin-bottom: 10px;
}
.tag-pill:hover {
    background-color: var(--hh-accent);
    border-color: var(--hh-accent);
    color: #000;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(209, 255, 82, 0.2);
}
/* Dark background for dark mode, light for light mode */
.sidebar-widget.dark {
    background-color: var(--hh-bg-card); /* Use theme variable */
    border: 1px solid var(--hh-border);
}
/* Specific overrides if needed for dark mode specifically, can use html.dark selector */
html.dark .sidebar-widget.dark {
    background: linear-gradient(145deg, #1a1d24, #14161b);
    border: 1px solid rgba(255,255,255,0.05);
}

.event-mini {
    display: flex;
    gap: 15px;
    margin-bottom: 20px;
    padding-bottom: 20px;
    border-bottom: 1px solid var(--hh-border); /* Use theme variable */
}
.event-mini:last-child {
    border-bottom: none;
    margin-bottom: 0;
    padding-bottom: 0;
}
.event-date {
    background-color: var(--hh-bg-dark); /* Use theme variable */
    border: 1px solid var(--hh-border);
    border-radius: 8px;
    padding: 10px;
    text-align: center;
    width: 50px;
    height: 50px;
    display: flex;
    flex-direction: column;
    justify-content: center;
}
.event-date .day {
    font-size: 18px;
    font-weight: bold;
    color: var(--hh-accent); /* Use accent color for visibility */
    line-height: 1;
}
.event-date .month {
    font-size: 10px;
    color: var(--hh-text-secondary);
    margin-top: 2px;
}
.event-details h4 {
    font-family: 'Microsoft YaHei', sans-serif;
    font-weight: bold;
    font-size: 15px;
    margin: 0 0 4px;
    color: var(--hh-text-primary);
}
.event-details p {
    font-size: 12px;
    color: var(--hh-text-secondary);
    margin: 0;
}
/* --- New Sections CSS --- */
.content-wrapper {
   background-color: #fff;
   color: #000;
   padding-top: 40px;
   padding-bottom: 60px;
}

/* Top Rated Music */
.top-rated-section {
   margin-bottom: 50px;
}
.section-head {
   display: flex;
   justify-content: space-between;
   align-items: flex-end;
   margin-bottom: 20px;
}
.head-info h2 {
   font-size: 24px;
   font-weight: 900;
   margin: 0 0 5px;
   color: #000;
}
.head-info p {
   font-size: 14px;
   color: #666;
   margin: 0;
}
.toggle-pills {
   display: flex;
   background: #eee;
   border-radius: 20px;
   padding: 4px;
}
.pill {
   padding: 4px 16px;
   font-size: 12px;
   font-weight: bold;
   cursor: pointer;
   border-radius: 16px;
   color: #666;
}
.pill.active {
   background: #333;
   color: #fff;
}

.top-rated-grid {
   display: grid;
   grid-template-columns: repeat(6, 1fr);
   gap: 18px;
}
.rated-card {
   border: 1px solid #eee;
   border-radius: 8px;
   overflow: hidden;
   padding: 10px;
   transition: transform 0.2s;
   background: #fff;
}
.rated-card:hover {
   transform: translateY(-5px);
   box-shadow: 0 10px 20px rgba(0,0,0,0.1);
   cursor: pointer;
}

/* 不同序号的卡片样式，参考示例图做出层次感 */
.rated-card.rated-style-0 {
   /* 保持与其他卡片同宽，只做颜色强调 */
   background: #fef0f0;
   border-color: #f28b82;
}
.rated-card.rated-style-1 {
   background: #ffe8ec;
   border-color: #f48fb1;
}
.rated-card.rated-style-2 {
   background: #fff4e5;
   border-color: #ffb74d;
}
.rated-card.rated-style-3 {
   background: #f3f8ff;
   border-color: #90caf9;
}
.rated-card.rated-style-4 {
   background: #f9f5ff;
   border-color: #b39ddb;
}
.rated-card.rated-style-5 {
   background: #fff;
   border-color: #e0e0e0;
}
.rated-card.highlight {
   background: #d32f2f;
   border-color: #d32f2f;
}
.rated-card.highlight h4, 
.rated-card.highlight p, 
.rated-card.highlight .rating-score {
   color: #fff;
}
.rated-cover {
   position: relative;
   border-radius: 4px;
   overflow: hidden;
   margin-bottom: 10px;
   aspect-ratio: 1;
}
.rated-cover img {
   width: 100%; height: 100%; object-fit: cover;
}
.rated-overlay {
   position: absolute;
   top: 5px; left: 5px;
}
.fire-badge {
   background: #ff5e00;
   color: #fff;
   font-size: 10px;
   font-weight: bold;
   padding: 2px 6px;
   border-radius: 4px;
}
.rated-info h4 {
   font-size: 14px;
   font-weight: bold;
   margin: 0 0 4px;
   white-space: nowrap;
   overflow: hidden;
   text-overflow: ellipsis;
   color: #000;
}
.rated-info p {
   font-size: 12px;
   color: #666;
   margin: 0 0 8px;
   white-space: nowrap;
   overflow: hidden;
   text-overflow: ellipsis;
}
.rating-score {
   font-size: 12px;
   font-weight: bold;
   color: #d32f2f;
   display: flex;
   align-items: center;
   gap: 4px;
}
.vote-count {
   font-weight: normal;
   color: #999;
}
.rated-card.highlight .vote-count {
   color: rgba(255,255,255,0.7);
}
.rated-card.highlight .rating-score {
    color: #fff;
}

/* Main Columns Layout */
.main-columns {
   display: flex;
   gap: 30px;
}
.col-daily {
   flex: 0 0 30%;
   background: #000;
   color: #fff;
   padding: 20px;
   border-radius: 4px;
}
.col-news {
   flex: 0 0 45%;
}
.col-ad {
   flex: 1;
}

/* Daily Songs Column - HNHH Style */
.col-header {
   display: flex;
   justify-content: space-between;
   align-items: center;
   margin-bottom: 0;
   border-bottom: none;
   padding: 6px 16px;
   height: 50px;
}
.col-header.black {
   background: transparent;
}
.col-header.black h3 {
   color: #fff;
   font-size: 24px;
   font-weight: 900;
   margin: 0;
   font-family: 'Microsoft YaHei', 'PingFang SC', 'Helvetica Neue', sans-serif;
   letter-spacing: 1px;
}
.archives-link {
   font-size: 11px;
   color: #fff;
   cursor: pointer;
   font-weight: 600;
   letter-spacing: 0.5px;
   transition: color 0.2s;
}
.archives-link:hover {
   color: #d32f2f;
}

/* Diagonal stripe decoration below header */
.daily-stripe-decoration {
   background: repeating-linear-gradient(
     45deg,
     #fff,
     #fff 5px,
     #ccc 5px,
     #ccc 10px
   );
   height: 8px;
   margin: 0 0 32px 0;
   opacity: 0.8;
}

.daily-songs-list {
   /* Remove any extra padding/margin */
}

.daily-item {
   display: flex;
   gap: 16px;
   margin-bottom: 16px;
   position: relative;
   padding: 0;
   border-bottom: 1px solid rgba(255,255,255,0.1);
   transition: background 0.2s;
   padding-bottom: 16px;
}
.daily-item:last-child {
   border-bottom: none;
}
.daily-item:hover {
   background: rgba(255,255,255,0.03);
   cursor: pointer;
}
.daily-item:hover .daily-title {
   color: #d32f2f;
}

.daily-img-wrapper {
   width: 130px;
   height: 130px;
   flex-shrink: 0;
   position: relative;
   overflow: hidden;
}
.daily-img-wrapper img {
   width: 100%;
   height: 100%;
   object-fit: cover;
   display: block;
}

/* TODAY badge - red background, top right of image */
.daily-today-badge {
   position: absolute;
   top: 0;
   right: 0;
   background: #d32f2f;
   color: #fff;
   font-size: 9px;
   font-weight: 900;
   padding: 4px 8px;
   letter-spacing: 0.5px;
   font-family: 'Arial', sans-serif;
}

.daily-info {
   flex: 1;
   display: flex;
   flex-direction: column;
   justify-content: center;  /* 垂直居中 */
   align-items: flex-start;
   gap: 6px;
   min-width: 0; /* Allow text truncation */
}
.daily-title {
   font-size: 16px;
   font-weight: 700;
   margin: 0;
   color: #fff;
   line-height: 1.3;
   overflow: hidden;
   text-overflow: ellipsis;
   display: -webkit-box;
   -webkit-line-clamp: 2;
   -webkit-box-orient: vertical;
}
.daily-artist {
   font-size: 14px;
   color: rgba(255,255,255,0.6);
   margin: 0;
   font-weight: 400;
   white-space: nowrap;
   overflow: hidden;
   text-overflow: ellipsis;
}
.daily-badge {
   font-size: 12px;
   color: #d32f2f;
   font-weight: 700;
   font-style: italic;
   margin: 0;
   text-transform: uppercase;
   letter-spacing: 0.3px;
}

/* Latest News Column */
.col-header.red-stripe {
   background: repeating-linear-gradient(
     45deg,
     #d32f2f,
     #d32f2f 10px,
     #e53935 10px,
     #e53935 20px
   );
   padding: 6px 16px;
   border: none;
   margin-bottom: 32px;
   height: 50px;
   display: flex;
   align-items: center;
}
.col-header.red-stripe h3 {
   color: #fff;
   margin: 0;
   font-size: 24px;
   font-weight: 900;
   font-family: 'Microsoft YaHei', 'PingFang SC', sans-serif;
   letter-spacing: 0.5px;
}
.news-item {
   display: flex;
   gap: 16px;
   margin-bottom: 24px;
   padding-bottom: 24px;
   border-bottom: 1px solid #eee;
   cursor: pointer;
   transition: background 0.2s;
}
.news-img {
   width: 140px;
   height: 105px;
   flex-shrink: 0;
   border-radius: 4px;
   overflow: hidden;
}
.news-img img {
   width: 100%;
   height: 100%;
   object-fit: cover;
   transition: transform 0.3s;
}
.news-item:hover .news-img img {
   transform: scale(1.05);
}
.news-cat {
   font-size: 11px;
   color: #d32f2f;
   font-weight: bold;
   margin-bottom: 6px;
   display: block;
   text-transform: uppercase;
   letter-spacing: 0.5px;
}
.news-content h4 {
   font-size: 17px;
   font-weight: bold;
   margin: 0 0 10px;
   line-height: 1.4;
   color: #000;
   font-family: 'Microsoft YaHei', 'PingFang SC', sans-serif;
}
.news-meta {
   font-size: 12px;
   color: #999;
   display: flex;
   gap: 12px;
   align-items: center;
}
.news-views {
   display: flex;
   align-items: center;
   gap: 4px;
}

.news-item:hover {
   background: #f9f9f9;
   cursor: pointer;
}
.news-item:hover .news-content h4 {
   color: #d32f2f;
}

/* Ad Column */
.ad-placeholder {
   background: #f0f0f0;
   height: 600px;
   display: flex;
   align-items: center;
   justify-content: center;
   color: #ccc;
   font-size: 24px;
   font-weight: bold;
}
</style>
