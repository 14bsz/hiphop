const mysql = require('mysql2/promise');
const path = require('path');
const fs = require('fs/promises');
const axios = require('axios');
const cheerio = require('cheerio');

// 数据库配置
const DB_CONFIG = {
  host: 'localhost',
  user: 'root',
  password: '123',
  database: 'hiphop'
};

// 关键词
const KEYWORDS = ['说唱', '中文说唱', 'Rap', 'HipHop'];

// 尝试读取 UP 主 RSS 配置
async function loadUpIds() {
  const upPath = path.join(__dirname, 'up_ids.json');
  try {
    const buf = await fs.readFile(upPath, 'utf8');
    const list = JSON.parse(buf);
    if (Array.isArray(list)) return list;
  } catch (e) {}
  return [];
}

// 从链接中提取 BV 号
function extractBvid(link) {
  const m = link && link.match(/BV[0-9A-Za-z]+/);
  return m ? m[0] : null;
}

// 解析 RSSHub 的 bilibili 用户视频 RSS
async function fetchFromRSS(uid) {
  try {
    const url = `https://rsshub.app/bilibili/user/video/${uid}`;
    const { data } = await axios.get(url, {
      headers: {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36'
      },
      timeout: 8000
    });
    const $ = cheerio.load(data, { xmlMode: true });
    const items = [];
    $('item').each((i, el) => {
      const title = $(el).find('title').first().text().trim();
      const link = $(el).find('link').first().text().trim();
      const pubDate = $(el).find('pubDate').first().text().trim();
      let cover = $(el).find('enclosure').attr('url');
      if (!cover) {
        const desc = $(el).find('description').first().text();
        const $d = cheerio.load(desc || '');
        const img = $d('img').attr('src');
        cover = img || '';
      }
      if (title && link) {
        items.push({
          title,
          summary: '',
          cover_image: (cover || `https://picsum.photos/300/200?random=${i}`).replace(/^http:/, 'https:'),
          author: 'Bilibili',
          publish_time: pubDate ? new Date(pubDate) : new Date(),
          tag: 'Bilibili',
          source: 'Bilibili',
          source_url: link,
          views: Math.floor(Math.random() * 50000)
        });
      }
    });
    return items;
  } catch (e) {
    return [];
  }
}

// B 站搜索接口抓取
async function fetchFromSearch() {
  const all = [];
  const seenBvid = new Set();
  for (const kw of KEYWORDS) {
    try {
      const url = `https://api.bilibili.com/x/web-interface/search/type?search_type=video&keyword=${encodeURIComponent(kw)}&order=pubdate`;
      const { data } = await axios.get(url, {
        headers: {
          'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
          'Referer': 'https://www.bilibili.com',
          'Origin': 'https://www.bilibili.com',
          'Accept': 'application/json,text/plain,*/*'
        },
        timeout: 8000
      });
      if (data.code === 0 && data.data && Array.isArray(data.data.result)) {
        data.data.result.slice(0, 10).forEach((item, i) => {
          const bvid = item.bvid || extractBvid(item.arcurl || '');
          if (bvid && seenBvid.has(bvid)) return;
          if (bvid) seenBvid.add(bvid);
          let cover = item.pic;
          if (cover && cover.startsWith('//')) cover = 'https:' + cover;
          if (cover && cover.startsWith('http://')) cover = cover.replace(/^http:/, 'https:');
          const titleSan = (item.title || '')
            .replace(/<em class="keyword">/g, '')
            .replace(/<\/em>/g, '')
            .trim();
          all.push({
            title: titleSan,
            summary: item.description || '',
            cover_image: cover || `https://picsum.photos/300/200?random=${i}`,
            author: item.author || 'Bilibili',
            publish_time: item.pubdate ? new Date(item.pubdate * 1000) : new Date(),
            tag: 'Bilibili',
            source: 'Bilibili',
            source_url: item.arcurl || `https://www.bilibili.com/video/${bvid}`,
            views: item.play || 0
          });
        });
      }
    } catch (e) {
      // 忽略单次失败，继续其他关键词
    }
  }
  // 过滤：标题或简介含说唱相关词
  const re = /(说唱|hiphop|hip-hop|rap)/i;
  return all.filter(x => re.test(x.title) || re.test(x.summary));
}

// 辅助函数：随机打乱数组
function shuffle(array) {
  let currentIndex = array.length,  randomIndex;
  while (currentIndex != 0) {
    randomIndex = Math.floor(Math.random() * currentIndex);
    currentIndex--;
    [array[currentIndex], array[randomIndex]] = [
      array[randomIndex], array[currentIndex]];
  }
  return array;
}

// 辅助函数：生成随机时间（最近3天内）
function getRandomDate() {
    const end = new Date();
    const start = new Date();
    start.setDate(start.getDate() - 3);
    return new Date(start.getTime() + Math.random() * (end.getTime() - start.getTime()));
}

async function run() {
  let connection;
  try {
    // 1. 建立数据库连接
    connection = await mysql.createConnection(DB_CONFIG);
    console.log('🔌 数据库连接成功');

    // 2. 获取数据：优先 RSS，其次搜索，最后兜底随机
    console.log('📡 正在通过 RSS 获取 UP 主最新视频...');
    const upIds = await loadUpIds();
    let rssItems = [];
    for (const uid of upIds) {
      const items = await fetchFromRSS(uid);
      rssItems = rssItems.concat(items);
      if (rssItems.length > 20) break;
    }

    console.log('🔎 正在通过 B 站搜索接口抓取“说唱”相关视频...');
    const searchItems = await fetchFromSearch();
    let combined = [...rssItems, ...searchItems];
    if (combined.length === 0) {
      console.warn('⚠️ RSS 与搜索均未获取到数据，采用兜底策略');
      combined = [
        {
          title: '中文说唱热门合辑',
          summary: '兜底数据：请稍后重试或填充 up_ids.json',
          cover_image: `https://picsum.photos/300/200?random=${Math.random()}`,
          author: 'Bilibili',
          publish_time: new Date(),
          tag: 'Bilibili',
          source: 'Bilibili',
          source_url: 'https://search.bilibili.com/all?keyword=%E8%AF%B4%E5%94%B1',
          views: Math.floor(Math.random() * 50000)
        }
      ];
    }

    // 为数据添加随机的“播放量波动”，并统一时间
    const finalData = combined.slice(0, 20).map(item => ({
        ...item,
        views: item.views + Math.floor(Math.random() * 5000) // 模拟实时播放量变化
    }));

    // 3. 存入数据库
    console.log('💾 正在保存到数据库...');
    await connection.execute('TRUNCATE TABLE buzz'); // 清空旧数据，保证展示的都是高质量数据

    const insertSQL = `
      INSERT INTO buzz (title, summary, cover_image, author, publish_time, tag, views, source, source_url)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
    `;

    for (const article of finalData) {
      const params = [
        article.title,
        article.summary,
        article.cover_image,
        article.author,
        article.publish_time || getRandomDate(),
        article.tag,
        article.views,
        article.source,
        article.source_url
      ];

      await connection.execute(insertSQL, params);
    }
    console.log(`✅ 成功保存 ${finalData.length} 条精选 HipHop 数据到数据库`);

    // 4. 生成 JSON 文件供前端 Mock 使用
    const mockDir = path.join(__dirname, '../public/mock');
    try {
        await fs.mkdir(mockDir, { recursive: true });
        await fs.writeFile(
            path.join(mockDir, 'buzz.json'), 
            JSON.stringify(finalData, null, 2),
            'utf8'
        );
        console.log('✅ Mock JSON 文件已更新: public/mock/buzz.json');
    } catch (e) {
        console.error('❌ 生成 JSON 失败:', e.message);
    }

  } catch (error) {
    console.error('❌ 运行出错:', error);
  } finally {
    if (connection) {
      await connection.end();
    }
  }
}

run();
