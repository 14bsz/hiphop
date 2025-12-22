<template>
  <el-container class="layout-container">
    <el-header class="header">
      <div class="header-inner">
        <!-- Left: Logo -->
        <div class="logo-wrapper">
          <div class="logo">
            <h1>HNHH</h1>
            <span class="logo-star">★</span>
          </div>
        </div>
        
        <!-- Center: Navigation (HNHH Style) -->
        <nav class="nav-menu">
           <div class="nav-item has-dropdown">
              资讯 <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
              <div class="dropdown-menu">
                 <div class="dropdown-item" @click="navigateTo('/news/songs')">单曲</div>
                 <div class="dropdown-item">专辑</div>
                 <div class="dropdown-item">音乐</div>
                 <div class="dropdown-item">生活方式</div>
                 <div class="dropdown-item">运动</div>
                 <div class="dropdown-item">球鞋</div>
                 <div class="dropdown-item">政治</div>
                 <div class="dropdown-item">科技</div>
              </div>
           </div>
           <div class="nav-item">百大榜单</div>
           <div class="nav-item has-dropdown">
              深度专栏 <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
           </div>
           <div class="nav-item">视频</div>
           <div class="nav-item">音乐人</div>
        </nav>

        <!-- Right: Actions -->
        <div class="header-right">
          <el-button type="danger" class="comment-btn" round>
             <el-icon><ChatDotRound /></el-icon> 参与讨论
          </el-button>
          
          <div class="search-bar">
             <input type="text" placeholder="搜索..." />
             <el-icon class="search-icon"><Search /></el-icon>
          </div>

          <div class="nav-item has-dropdown subscribe-btn">
             订阅 <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
          </div>
          
          <!-- Theme Toggle -->
          <div class="theme-toggle" @click="toggleTheme">
             <component :is="isDark ? Sunny : Moon" />
          </div>
        </div>
      </div>
    </el-header>
    
    <el-main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </el-main>
    
    <el-footer class="footer">
      <div class="footer-content">
        <div class="footer-logo">嘻哈阵地</div>
        <div class="footer-links">
          <span>关于我们</span>
          <span>联系合作</span>
          <span>隐私条款</span>
        </div>
        <p class="copyright">© 2024 嘻哈阵地 (HipHopHub). All Rights Reserved.</p>
      </div>
    </el-footer>
  </el-container>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search, Moon, Sunny, ArrowDown, ChatDotRound } from '@element-plus/icons-vue'
import { useThemeStore } from '../../stores/theme'
import { storeToRefs } from 'pinia'

const route = useRoute()
const router = useRouter()
const themeStore = useThemeStore()
const { isDark } = storeToRefs(themeStore)
const { toggleTheme, initTheme } = themeStore

onMounted(() => {
  initTheme()
})

const activeMenu = computed(() => {
    const path = route.path
    if (path.startsWith('/buzz')) return '/buzz'
    if (path.startsWith('/media')) return '/media'
    if (path.startsWith('/hood')) return '/hood'
    if (path.startsWith('/artists')) return '/artists'
    if (path.startsWith('/live')) return '/live'
    return path
})

// 导航跳转
const navigateTo = (path) => {
  router.push(path)
}
</script>

<style scoped>
.layout-container {
  min-height: 100vh;
  background-color: var(--hh-bg-dark);
}

/* Header Styles */
.header {
  height: 60px; /* Thinner header like HNHH */
  background-color: #000; /* Pure black header */
  border-bottom: none;
  position: sticky;
  top: 0;
  z-index: 1000;
  padding: 0;
  color: #fff;
}
.header-inner {
  max-width: 1400px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  padding: 0 20px;
}

/* Logo */
.logo-wrapper {
  display: flex;
  align-items: center;
  margin-right: 40px; /* Space between logo and nav */
}
.logo {
  display: flex;
  align-items: center;
  gap: 5px;
}
.logo h1 {
  margin: 0;
  font-family: 'Impact', sans-serif;
  font-size: 32px;
  color: #fff;
  letter-spacing: 1px;
  line-height: 1;
  font-style: italic;
  transform: scaleY(1.2); /* Stretch vertically */
}
.logo-star {
  font-size: 12px;
  color: #fff;
  margin-top: -15px; /* Position star like HNHH */
}

/* Nav Menu */
.nav-menu {
  display: flex;
  align-items: center;
  gap: 25px;
  height: 100%;
}
.nav-item {
  color: #fff;
  font-size: 16px; /* Increased from 13px */
  font-weight: 900; /* Increased weight for impact */
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  height: 100%;
  position: relative;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
  letter-spacing: 0.5px;
}
.nav-item:hover {
  color: #ccc;
}
.dropdown-icon {
  font-size: 10px;
  margin-top: 1px;
}

/* Dropdown Menu */
.has-dropdown:hover .dropdown-menu {
  display: block;
}
.dropdown-menu {
  display: none;
  position: absolute;
  top: 60px; /* Below header */
  left: -10px;
  background-color: #000;
  min-width: 160px;
  padding: 10px 0;
  z-index: 1001;
}
.dropdown-item {
  padding: 10px 20px;
  color: #fff;
  font-size: 12px;
  font-weight: 800;
  transition: background 0.2s;
}
.dropdown-item:hover {
  background-color: #333;
}

/* Right Side Actions */
.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-left: auto; /* Push to the right */
}
.comment-btn {
  background-color: #ff3b30; /* Red button */
  border: none;
  color: #fff;
  font-weight: 800;
  font-size: 12px;
  height: 32px;
  padding: 0 15px;
  display: flex;
  align-items: center;
  gap: 5px;
}
.comment-btn:hover {
  background-color: #ff5e55;
}

.search-bar {
  position: relative;
  display: flex;
  align-items: center;
}
.search-bar input {
  background-color: #222;
  border: none;
  height: 32px;
  width: 200px;
  padding: 0 35px 0 15px;
  color: #fff;
  font-size: 12px;
  border-radius: 4px;
}
.search-bar input:focus {
  outline: none;
  background-color: #333;
}
.search-icon {
  position: absolute;
  right: 10px;
  color: #666;
  font-size: 16px;
}

/* Theme Toggle */
.theme-toggle {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 50%;
  transition: all 0.3s;
  background-color: #222; /* Dark grey background */
  color: #fff;
  margin-left: 10px;
}
.theme-toggle:hover {
  background-color: #333;
  color: #ffb703; /* Gold color on hover */
  transform: rotate(15deg);
}

.subscribe-btn {
  margin-left: 10px;
}

/* Main Content */
.main-content {
  padding: 0;
  overflow-x: hidden;
}

/* Footer */
.footer {
  background-color: #0b0c0f;
  padding: 80px 0;
  border-top: 1px solid #1a1a1a;
  height: auto;
}
.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
}
.footer-logo {
  font-family: 'Impact', sans-serif;
  font-size: 32px;
  color: #333;
  letter-spacing: 2px;
}
.footer-links {
  display: flex;
  gap: 40px;
  color: var(--hh-text-secondary);
  font-size: 14px;
}
.footer-links span {
  cursor: pointer;
  transition: color 0.3s;
}
.footer-links span:hover {
  color: var(--hh-text-primary);
}
.copyright {
  color: #444;
  font-size: 12px;
  margin-top: 20px;
}

/* Page Transitions */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
