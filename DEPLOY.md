# 部署到 Netlify 指南

## 前置准备

1. **注册 Netlify 账号**
   - 访问 https://www.netlify.com/
   - 使用 GitHub 账号登录（推荐）

2. **准备 Git 仓库**
   - 确保你的代码已经推送到 GitHub/GitLab/Bitbucket

## 部署步骤

### 方法一：通过 Netlify 网站部署（推荐）

1. **登录 Netlify**
   - 访问 https://app.netlify.com/

2. **创建新站点**
   - 点击 "Add new site" > "Import an existing project"
   - 选择你的 Git 提供商（GitHub/GitLab/Bitbucket）
   - 授权 Netlify 访问你的仓库
   - 选择 `hiphop` 仓库

3. **配置构建设置**
   - Netlify 会自动检测到 `netlify.toml` 配置文件
   - 确认以下设置：
     - **Base directory**: `hiphop-web`
     - **Build command**: `npm ci && npm run build`
     - **Publish directory**: `hiphop-web/dist`
     - **Functions directory**: `hiphop-web/netlify/functions`

4. **设置环境变量**
   - 在 Netlify 项目设置中，找到 "Environment variables"
   - 添加环境变量：
     ```
     VITE_API_BASE_URL = https://your-backend-api-url.com
     ```
   - **重要**：将 `https://your-backend-api-url.com` 替换为你的实际后端 API 地址

5. **部署**
   - 点击 "Deploy site"
   - 等待构建完成（通常需要 2-5 分钟）

6. **访问你的网站**
   - 部署成功后，Netlify 会提供一个临时域名（如：`random-name-123.netlify.app`）
   - 你可以在 "Domain settings" 中自定义域名

### 方法二：通过 Netlify CLI 部署

1. **安装 Netlify CLI**
   ```bash
   npm install -g netlify-cli
   ```

2. **登录 Netlify**
   ```bash
   netlify login
   ```

3. **初始化项目**（在项目根目录下）
   ```bash
   netlify init
   ```
   - 选择 "Create & configure a new site"
   - 选择团队
   - 输入站点名称（可选）

4. **部署**
   ```bash
   netlify deploy --prod
   ```

## 后端 API 部署

你的前端需要连接到后端 API。你需要先部署后端服务：

### 选项 1: Railway（推荐 - 免费）
1. 访问 https://railway.app/
2. 使用 GitHub 登录
3. 创建新项目，选择你的仓库
4. Railway 会自动检测 Spring Boot 项目
5. 部署后会得到一个 URL（如：`https://xxx.railway.app`）
6. 将这个 URL 设置到 Netlify 的环境变量 `VITE_API_BASE_URL`

### 选项 2: Render（免费）
1. 访问 https://render.com/
2. 创建新的 Web Service
3. 连接你的 Git 仓库
4. 选择 `hiphop-home-admin` 目录
5. 配置构建命令和启动命令
6. 部署后获得 URL

### 选项 3: 自己的服务器
- 如果你有自己的服务器，确保：
  - 后端 API 可以通过公网访问
  - 配置了正确的 CORS 设置
  - 使用 HTTPS（Netlify 是 HTTPS，需要后端也是 HTTPS）

## 环境变量配置

在 Netlify 项目设置中配置以下环境变量：

```
VITE_API_BASE_URL=https://your-backend-api.com
```

**重要提示**：
- 替换为你的实际后端 API 地址
- 确保 URL 不要以 `/` 结尾
- 使用 HTTPS 而不是 HTTP（生产环境安全要求）

## 自定义域名（可选）

1. 在 Netlify 项目中，进入 "Domain settings"
2. 点击 "Add custom domain"
3. 输入你的域名（如：`hiphop.example.com`）
4. 根据提示在你的域名服务商处添加 DNS 记录
5. Netlify 会自动配置 HTTPS 证书

## 持续部署

- 每次推送代码到 Git 仓库，Netlify 会自动重新构建和部署
- 你可以在 Netlify 控制台查看部署日志和状态

## 常见问题

### 1. 构建失败
- 检查 Node.js 版本是否正确（在 `netlify.toml` 中配置）
- 查看构建日志，确认错误信息
- 确保 `package.json` 中的依赖都正确安装

### 2. API 请求失败
- 确认 `VITE_API_BASE_URL` 环境变量设置正确
- 检查后端 API 是否正常运行
- 确认后端配置了正确的 CORS 设置

### 3. 图片无法加载
- Bilibili 和微信图片会通过 Netlify Functions 代理加载
- 如果仍然失败，检查 Functions 日志

### 4. 路由 404 错误
- 已在 `netlify.toml` 中配置 SPA 重定向
- 如果仍有问题，检查 `public/_redirects` 文件

## 测试部署

部署完成后，测试以下功能：
- [ ] 首页加载正常
- [ ] 图片显示正常（包括 Bilibili 和微信图片）
- [ ] API 数据加载正常
- [ ] 路由跳转正常
- [ ] 各个页面功能正常

## 下一步

部署成功后，你可以：
1. 配置自定义域名
2. 设置 Netlify Analytics（网站分析）
3. 配置表单处理（如果需要）
4. 设置部署通知（邮件/Slack）

## 需要帮助？

- Netlify 文档：https://docs.netlify.com/
- Netlify 社区：https://answers.netlify.com/
- 或者直接联系你的开发团队
