import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import axios from 'axios'
import path from 'path'

// 自定义中间件：图片代理，补充 Referer/User-Agent，规避热链限制
const imgProxyPlugin = {
  name: 'img-proxy',
  configureServer(server) {
    server.middlewares.use('/img-proxy', async (req, res) => {
      try {
        const url = new URL(req.url, 'http://localhost')
        const target = url.searchParams.get('url')
        if (!target) {
          res.statusCode = 400
          res.end('Missing url param')
          return
        }
        let referer = ''
        try {
          const th = new URL(target).hostname
          if (/^i[0-9]\.hdslb\.com$/i.test(th)) referer = 'https://www.bilibili.com/'
          else if (/mmbiz\.qpic\.cn$/i.test(th) || /qpic\.cn$/i.test(th)) referer = 'https://weixin.qq.com/'
        } catch {}
        const { data, headers, status } = await axios.get(target, {
          responseType: 'arraybuffer',
          headers: {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
            ...(referer ? { Referer: referer } : {})
          },
          timeout: 8000,
          validateStatus: () => true
        })
        res.statusCode = status
        const ct = headers['content-type'] || 'image/jpeg'
        res.setHeader('Content-Type', ct)
        res.setHeader('Cache-Control', 'public, max-age=3600')
        res.end(data)
      } catch (e) {
        res.statusCode = 502
        res.end('Proxy failed')
      }
    })
  }
}

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue(), imgProxyPlugin],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  },
  server: {
    host: '0.0.0.0',
    middlewareMode: false
  }
})
