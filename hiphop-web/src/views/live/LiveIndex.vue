<template>
  <div class="live-container">
    <div class="page-header">
        <h1>现场 (LIVE MAP)</h1>
    </div>
    
    <el-alert title="地图模式正在开发中，目前仅展示列表" type="warning" show-icon style="margin-bottom: 20px; background-color: #332b00; border-color: #665500; color: #ffcc00;" :closable="false" />
    
    <el-table :data="tableData" style="width: 100%" class="live-table" :header-cell-style="{background:'#1e1e1e', color:'#a0a0a0', borderColor:'#333', fontFamily: 'Microsoft YaHei'}" :cell-style="{background:'#121212', color:'#e0e0e0', borderColor:'#333', fontFamily: 'Microsoft YaHei'}">
      <el-table-column prop="date" label="日期" width="180" />
      <el-table-column prop="city" label="城市" width="100">
         <template #default="scope">
            <el-tag effect="dark" type="info">{{ scope.row.city }}</el-tag>
         </template>
      </el-table-column>
      <el-table-column prop="name" label="演出名称" />
      <el-table-column prop="venue" label="场地" width="180" />
      <el-table-column label="操作" width="100">
        <template #default>
          <el-button link class="ticket-btn" size="small">购票 ></el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
const tableData = [
  {
    date: '2024-06-01',
    city: '上海',
    name: '2024上海草莓音乐节',
    venue: '东方绿舟'
  },
  {
    date: '2024-06-15',
    city: '成都',
    name: 'CDC 集团巡演',
    venue: '成都量子光'
  },
  {
    date: '2024-06-20',
    city: '北京',
    name: '地下8英里',
    venue: '疆进酒'
  }
]
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
.live-table {
    --el-table-border-color: #333;
    --el-table-row-hover-bg-color: #222;
}
.ticket-btn {
    color: var(--hh-accent);
    font-weight: bold;
    font-family: 'Microsoft YaHei', sans-serif;
}
</style>
