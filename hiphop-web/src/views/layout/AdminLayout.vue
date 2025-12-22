<template>
  <el-container class="admin-layout">
    <el-aside width="220px" class="admin-aside">
      <div class="admin-logo">后台管理</div>
      <el-menu
        router
        :default-active="activeMenu"
        class="admin-menu"
        background-color="#001529"
        text-color="#fff"
        active-text-color="#ffd04b"
      >
        <el-sub-menu index="/admin/home">
          <template #title>
            <el-icon><House /></el-icon>
            <span>首页管理</span>
          </template>
          <el-menu-item index="/admin/home/featured">
            <el-icon><Picture /></el-icon>
            <span>展位图管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/home/news">
            <el-icon><Collection /></el-icon>
            <span>最新资讯管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/home/top-rated">
            <el-icon><StarFilled /></el-icon>
            <span>高分音乐榜管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/home/daily">
            <el-icon><Calendar /></el-icon>
            <span>每日推荐管理</span>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="admin-header">
        <div class="admin-header-title">嘻哈阵地 · 后台</div>
      </el-header>
      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { House, Picture, StarFilled, Calendar, Collection } from '@element-plus/icons-vue'

const route = useRoute()

const activeMenu = computed(() => route.path)
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
}
.admin-aside {
  background-color: #001529;
  color: #fff;
}
.admin-logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}
.admin-menu {
  border-right: none;
}
.admin-header {
  height: 60px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  border-bottom: 1px solid #ebeef5;
}
.admin-header-title {
  font-size: 16px;
  font-weight: 600;
}
.admin-main {
  padding: 0;
}
</style>








