const axios = require('axios');
const fs = require('fs/promises');
const path = require('path');

const articleUrl = process.argv[2];

async function main() {
  if (!articleUrl) {
    console.error('缺少链接参数，例如：node scripts/fetch_wechat.cjs "https://mp.weixin.qq.com/..."');
    process.exit(1);
  }
  try {
    const { data } = await axios.get(articleUrl, {
      headers: {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
        'Referer': 'https://mp.weixin.qq.com/'
      },
      timeout: 10000
    });
    const html = data || '';
    function pick(re) {
      const m = html.match(re);
      return m ? m[1] : null;
    }
    let title = pick(/var\s+msg_title\s*=\s*"(.*?)"/);
    let cover = pick(/var\s+msg_cdn_url\s*=\s*"(.*?)"/);
    let link = pick(/var\s+msg_link\s*=\s*"(.*?)"/);
    if (!title) title = pick(/<meta[^>]*property=["']og:title["'][^>]*content=["'](.*?)["']/i);
    if (!cover) cover = pick(/<meta[^>]*property=["']og:image["'][^>]*content=["'](.*?)["']/i);
    if (!link) link = articleUrl;
    if (cover && cover.startsWith('//')) cover = 'https:' + cover;
    if (cover && cover.startsWith('http://')) cover = cover.replace(/^http:/, 'https:');
    const item = {
      title: title || '无标题',
      cover_image: cover || `https://picsum.photos/800/600?random=${Date.now()}`,
      source_url: link
    };
    const mockPath = path.join(__dirname, '../public/mock/buzz.json');
    let list = [];
    try {
      const buf = await fs.readFile(mockPath, 'utf8');
      const json = JSON.parse(buf);
      if (Array.isArray(json)) list = json;
    } catch (e) {}
    list.unshift(item);
    await fs.mkdir(path.dirname(mockPath), { recursive: true });
    await fs.writeFile(mockPath, JSON.stringify(list, null, 2), 'utf8');
    console.log('✅ 已追加公众号文章到', mockPath);
  } catch (e) {
    console.error('❌ 解析失败：', e.message);
    process.exit(1);
  }
}

main();

