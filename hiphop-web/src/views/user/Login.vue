<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2>欢迎来到 HipHopHub</h2>
      <el-form label-width="80px">
        <el-form-item label="用户名">
          <el-input placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="密码">
          <el-input type="password" placeholder="请输入密码"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" style="width: 100%" @click="$router.push('/')">登录</el-button>
        </el-form-item>
      </el-form>
      <div class="links">
        <el-link type="primary">注册账号</el-link>
        <el-link type="info">忘记密码</el-link>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #2d3a4b;
}
.login-card {
  width: 400px;
}
.links {
  display: flex;
  justify-content: space-between;
}
</style>
