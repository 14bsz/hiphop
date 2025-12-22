<template>
  <div class="media-container">
    <div class="page-header">
       <h1>资讯聚合 (MEDIA)</h1>
    </div>

    <el-tabs v-model="activeTab" class="custom-tabs">
      <el-tab-pane label="最新视频" name="video">
        <el-row :gutter="20">
          <el-col :span="6" v-for="i in 8" :key="i" style="margin-bottom: 20px;">
            <el-card :body-style="{ padding: '0px' }" shadow="hover" class="video-card">
              <div class="video-cover">
                 <img :src="`https://picsum.photos/300/200?random=${i+10}`" class="image"/>
                 <div class="play-overlay">
                    <el-icon class="play-icon"><VideoPlay /></el-icon>
                 </div>
              </div>
              <div style="padding: 14px">
                <span class="video-title">【MV】某知名Rapper - 全新单曲 (官方MV)</span>
                <div class="bottom">
                  <time class="time">2小时前</time>
                  <el-button text class="watch-btn">立即观看</el-button>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>
      <el-tab-pane label="精选文章" name="article">
        <el-timeline>
          <el-timeline-item timestamp="2024-05-20" placement="top" v-for="i in 5" :key="i" color="#ff6b00">
            <el-card class="article-card">
              <h4>深度解析：中文说唱的十年变迁</h4>
              <p>来源：嘻哈融合体</p>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref } from 'vue'
const activeTab = ref('video')
</script>

<style scoped>
.page-header h1 {
    font-family: 'Microsoft YaHei', 'Impact', sans-serif;
    font-weight: 900;
    letter-spacing: 1px;
    text-transform: uppercase;
    border-left: 5px solid var(--hh-accent);
    padding-left: 15px;
    margin-bottom: 20px;
}
.video-card {
    border: none;
    background-color: #222;
}
.video-cover {
  position: relative;
  height: 150px;
  overflow: hidden;
  cursor: pointer;
}
.image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.3s;
  filter: brightness(0.8);
}
.video-cover:hover .image {
    transform: scale(1.1);
    filter: brightness(1);
}
.play-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(0,0,0,0.3);
  opacity: 0;
  transition: opacity 0.3s;
}
.video-cover:hover .play-overlay {
  opacity: 1;
}
.play-icon {
  font-size: 50px;
  color: var(--hh-accent);
}

.video-title {
  font-size: 14px;
  font-weight: bold;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  color: var(--hh-text-primary);
  font-family: 'Microsoft YaHei', sans-serif;
}
.bottom {
  margin-top: 13px;
  line-height: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.time {
  font-size: 12px;
  color: #666;
}
.watch-btn {
    color: var(--hh-accent);
    font-weight: bold;
    font-family: 'Microsoft YaHei', sans-serif;
}
.article-card h4 {
    color: var(--hh-text-primary);
    font-family: 'Microsoft YaHei', sans-serif;
    font-weight: bold;
}
.article-card p {
    color: var(--hh-text-secondary);
}

/* Tabs override */
:deep(.el-tabs__item) {
    color: var(--hh-text-secondary);
    font-size: 16px;
    font-family: 'Microsoft YaHei', sans-serif;
    font-weight: bold;
}
:deep(.el-tabs__item.is-active) {
    color: var(--hh-accent);
}
:deep(.el-tabs__active-bar) {
    background-color: var(--hh-accent);
}
</style>
