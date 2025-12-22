<template>
  <div class="artists-container">
    <div class="page-header">
       <h1>新血 (NEW BLOOD)</h1>
    </div>
    
    <el-row :gutter="20">
      <el-col :span="6" v-for="i in 8" :key="i">
        <el-card :body-style="{ padding: '0px' }" class="artist-card" shadow="hover">
          <div class="artist-cover">
             <img :src="`https://picsum.photos/200/200?random=${i+100}`" class="artist-avatar"/>
             <div class="artist-overlay">
                 <el-button type="primary" size="small" round>关注 +</el-button>
             </div>
          </div>
          <div style="padding: 14px; text-align: center;">
            <h3>MC_{{ i }}</h3>
            <p class="genre">爵士说唱 / Boombap</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

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
.artist-card {
    margin-bottom: 20px;
    background-color: #1e1e1e;
    border: 1px solid #333;
    transition: transform 0.3s;
}
.artist-card:hover {
    transform: translateY(-5px);
    border-color: var(--hh-accent);
}

.artist-cover {
    position: relative;
    overflow: hidden;
}
.artist-avatar {
  width: 100%;
  height: 200px;
  object-fit: cover;
  display: block;
}
.artist-overlay {
    position: absolute;
    bottom: 0;
    left: 0;
    width: 100%;
    padding: 10px 0;
    background: linear-gradient(to top, rgba(0,0,0,0.8), transparent);
    display: flex;
    justify-content: center;
    opacity: 0;
    transition: opacity 0.3s;
}
.artist-card:hover .artist-overlay {
    opacity: 1;
}

.artist-card h3 {
    margin: 5px 0;
    color: var(--hh-text-primary);
    font-family: 'Microsoft YaHei', sans-serif;
}
.genre {
    color: #888;
    font-size: 12px;
    text-transform: uppercase;
    letter-spacing: 1px;
    font-family: 'Microsoft YaHei', sans-serif;
}
</style>
