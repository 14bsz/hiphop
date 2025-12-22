<template>
  <div class="not-found">
    <h1>404</h1>
    <p>页面走丢了 (Lost in the hood)</p>
    <el-button type="primary" @click="$router.push('/')">回到首页</el-button>
  </div>
</template>

<style scoped>
.not-found {
  text-align: center;
  margin-top: 100px;
}
h1 {
  font-size: 100px;
  margin: 0;
  color: #909399;
}
</style>
