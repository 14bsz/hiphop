# Netlify 部署检查清单

## ✅ 已完成的准备工作

- [x] 配置 `netlify.toml` 文件
- [x] 创建 Netlify Functions 处理图片代理
- [x] 更新前端代码使用环境变量
- [x] 更新图片代理路径
- [x] 本地构建测试成功

## 📝 部署前必做事项

### 1. 推送代码到 Git
```bash
git add .
git commit -m "feat: 配置 Netlify 部署"
git push origin main
```

### 2. 部署后端 API（必须先完成）

**选择一个后端部署平台：**

#### Option A: Railway (推荐)
1. 访问 https://railway.app/
2. 登录并创建新项目
3. 选择你的 GitHub 仓库
4. 选择 `hiphop-home-admin` 目录
5. Railway 会自动检测 Spring Boot 并部署
6. 复制生成的 URL（例如：`https://hiphop-api.up.railway.app`）

#### Option B: Render
1. 访问 https://render.com/
2. 创建 New Web Service
3. 连接 Git 仓库，选择 `hiphop-home-admin`
4. 设置构建命令：`./mvnw clean package`
5. 设置启动命令：`java -jar target/hiphop-home-admin.jar`
6. 复制生成的 URL

#### Option C: 自己的服务器
- 确保后端 API 可公网访问
- 配置 HTTPS（Netlify 要求）
- 配置 CORS 允许 Netlify 域名

### 3. 部署前端到 Netlify

#### 3.1 登录 Netlify
- 访问 https://app.netlify.com/
- 使用 GitHub 登录

#### 3.2 导入项目
1. 点击 "Add new site" > "Import an existing project"
2. 选择 GitHub
3. 选择你的 `hiphop` 仓库
4. Netlify 会自动读取 `netlify.toml` 配置

#### 3.3 配置环境变量 ⚠️ **非常重要**
在 Netlify 项目设置中：
1. 进入 "Site settings" > "Environment variables"
2. 添加变量：
   - **Key**: `VITE_API_BASE_URL`
   - **Value**: 你的后端 API 地址（例如：`https://hiphop-api.up.railway.app`）
   - ⚠️ 注意：URL 不要以 `/` 结尾

#### 3.4 部署
1. 点击 "Deploy site"
2. 等待构建完成（约 2-5 分钟）
3. 构建成功后会得到一个临时域名

## 🧪 部署后测试

访问你的 Netlify 网站，测试以下功能：

- [ ] 网站能正常打开
- [ ] 首页内容加载正常
- [ ] 图片显示正常（包括 Bilibili、微信图片）
- [ ] API 数据加载正常
- [ ] 页面路由跳转正常
- [ ] 所有功能模块正常工作

## 🐛 常见问题排查

### 问题 1: 白屏或构建失败
**解决方案：**
1. 查看 Netlify 构建日志
2. 确认 Node.js 版本正确（18）
3. 检查 `package.json` 依赖

### 问题 2: API 请求失败（Network Error）
**解决方案：**
1. 确认后端 API 已部署并可访问
2. 检查 `VITE_API_BASE_URL` 环境变量设置正确
3. 确认后端 CORS 配置允许 Netlify 域名
4. 在浏览器开发者工具查看具体错误

**检查后端 CORS 配置：**
```java
// 在 WebCorsConfig.java 中确认
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(Arrays.asList(
        "http://localhost:5173",
        "https://your-netlify-site.netlify.app"  // 添加你的 Netlify 域名
    ));
    // ...
}
```

### 问题 3: 图片无法加载
**解决方案：**
1. 检查 Netlify Functions 是否正常运行
2. 在浏览器中访问 `https://your-site.netlify.app/.netlify/functions/img-proxy?url=测试图片URL`
3. 查看 Functions 日志

### 问题 4: 刷新页面 404
**解决方案：**
- 已在 `netlify.toml` 中配置 SPA 重定向
- 如果仍有问题，检查 `public/_redirects` 文件

## 📊 监控和优化

### 查看部署日志
1. Netlify Dashboard > 你的站点 > Deploys
2. 点击最新的部署查看日志

### 查看 Functions 日志
1. Netlify Dashboard > Functions
2. 点击 `img-proxy` 查看调用日志

### 性能优化建议
- 启用 Netlify Analytics（可选）
- 配置 CDN 缓存策略
- 优化图片大小和格式

## 🎯 下一步

部署成功后，你可以：

1. **配置自定义域名**
   - Site settings > Domain management > Add custom domain

2. **设置 HTTPS**
   - Netlify 会自动为你配置 Let's Encrypt 证书

3. **配置持续部署**
   - 每次 push 到 main 分支会自动部署
   - 可以在 Netlify 设置中配置分支部署

4. **监控网站**
   - 设置部署通知（邮件/Slack）
   - 启用 Netlify Analytics

## 📞 需要帮助？

- Netlify 文档: https://docs.netlify.com/
- Netlify 社区: https://answers.netlify.com/
- 详细部署指南: 见 `DEPLOY.md`

---

**最后提醒：**
- ⚠️ 务必先部署后端，再部署前端
- ⚠️ 务必在 Netlify 中设置 `VITE_API_BASE_URL` 环境变量
- ⚠️ 确保后端配置了正确的 CORS 设置
