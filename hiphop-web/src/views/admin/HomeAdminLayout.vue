<template>
  <div class="admin-home-layout">
    <div class="admin-home-header">
      <h2>首页管理</h2>
      <p>在这里可以管理首页的展位图、高分音乐榜以及每日推荐三个模块。</p>
    </div>
    <!-- 二级路由出口：展位图管理 / 高分音乐榜管理 / 每日推荐管理 -->
    <router-view />
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped>
.admin-home-layout {
  padding: 20px;
}
.admin-home-header {
  margin-bottom: 16px;
}
.admin-home-header h2 {
  margin: 0 0 4px;
  font-size: 18px;
}
.admin-home-header p {
  margin: 0;
  font-size: 13px;
  color: #909399;
}
</style>









