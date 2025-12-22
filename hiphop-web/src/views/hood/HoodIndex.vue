<template>
  <div class="hood-container">
    <div class="hood-header">
      <h1>社区 (THE HOOD)</h1>
      <el-button type="primary" class="post-btn">发布动态</el-button>
    </div>
    
    <div class="feed-list">
      <el-card v-for="i in 5" :key="i" class="feed-item" shadow="never">
        <div class="feed-header">
          <el-avatar :size="40" :src="`https://picsum.photos/50/50?random=${i+50}`" class="avatar"></el-avatar>
          <div class="user-info">
            <span class="username">用户_{{ i }}</span>
            <span class="time">10分钟前</span>
          </div>
          <el-button link class="more-btn"><el-icon><More /></el-icon></el-button>
        </div>
        <div class="feed-content">
          <p>今天去看了Livehouse，现场气氛太炸了！分享几张照片。 #HipHop #Life</p>
          <div class="feed-images">
             <img v-for="j in 3" :key="j" :src="`https://picsum.photos/150/150?random=${i*10+j}`" />
          </div>
        </div>
        <div class="feed-actions">
          <el-button text class="action-btn"><el-icon><thumb-up /></el-icon> 赞</el-button>
          <el-button text class="action-btn"><el-icon><chat-dot-square /></el-icon> 评论</el-button>
          <el-button text class="action-btn"><el-icon><share /></el-icon> 转发</el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.hood-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.hood-header h1 {
    font-family: 'Microsoft YaHei', 'Impact', sans-serif;
    font-weight: 900;
    letter-spacing: 1px;
    text-transform: uppercase;
    border-left: 5px solid var(--hh-accent);
    padding-left: 15px;
}
.post-btn {
    background-color: var(--hh-accent);
    border-color: var(--hh-accent);
    color: #000;
    font-weight: bold;
    font-family: 'Microsoft YaHei', sans-serif;
}
.post-btn:hover {
    background-color: #fff;
    border-color: #fff;
}

.feed-item {
  margin-bottom: 20px;
  background-color: #1e1e1e;
  border: 1px solid #333;
}
.feed-header {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  position: relative;
}
.avatar {
    border: 2px solid var(--hh-accent);
}
.user-info {
  margin-left: 10px;
  display: flex;
  flex-direction: column;
}
.username {
  font-weight: bold;
  color: var(--hh-text-primary);
  font-family: 'Microsoft YaHei', sans-serif;
}
.time {
  font-size: 12px;
  color: #666;
}
.more-btn {
    margin-left: auto;
    color: #666;
}

.feed-content p {
    color: #ddd;
    line-height: 1.6;
    margin-bottom: 15px;
    font-family: 'Microsoft YaHei', sans-serif;
}
.feed-images {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}
.feed-images img {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 4px;
  cursor: pointer;
  transition: opacity 0.2s;
}
.feed-images img:hover {
    opacity: 0.8;
}

.feed-actions {
  margin-top: 15px;
  border-top: 1px solid #333;
  padding-top: 10px;
  display: flex;
  justify-content: space-around;
}
.action-btn {
    color: #888;
    font-family: 'Microsoft YaHei', sans-serif;
}
.action-btn:hover {
    color: var(--hh-accent);
}
</style>
