const axios = require('axios');
const fs = require('fs/promises');
const path = require('path');

const bvid = process.argv[2] || process.env.BVID;

async function main() {
  if (!bvid) {
    console.error('缺少 BV 号参数，例如：node scripts/fetch_bvid.cjs BV1DxqGBCE2g');
    process.exit(1);
  }

  try {
    const url = `https://api.bilibili.com/x/web-interface/view?bvid=${encodeURIComponent(bvid)}`;
    const { data } = await axios.get(url, {
      headers: {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
        'Referer': 'https://www.bilibili.com',
        'Origin': 'https://www.bilibili.com',
        'Accept': 'application/json,text/plain,*/*'
      },
      timeout: 8000
    });

    if (data.code !== 0 || !data.data) {
      console.error('获取视频信息失败：', data.message || '未知错误');
      process.exit(1);
    }

    const v = data.data;
    let cover = v.pic || '';
    if (cover && cover.startsWith('//')) cover = 'https:' + cover;
    if (cover && cover.startsWith('http://')) cover = cover.replace(/^http:/, 'https:');
    const coverImage = cover ? `${cover}@672w_378h_1c.webp` : `https://picsum.photos/800/600?random=${Date.now()}`;

    const item = {
      title: v.title || '无标题',
      cover_image: coverImage,
      source_url: `https://www.bilibili.com/video/${bvid}`
    };

    const mockPath = path.join(__dirname, '../public/mock/buzz.json');
    await fs.mkdir(path.dirname(mockPath), { recursive: true });
    await fs.writeFile(mockPath, JSON.stringify([item], null, 2), 'utf8');
    console.log('✅ 已生成仅包含 标题/封面/链接 的文件：', mockPath);
  } catch (e) {
    console.error('请求或写入失败：', e.message);
    process.exit(1);
  }
}

main();
