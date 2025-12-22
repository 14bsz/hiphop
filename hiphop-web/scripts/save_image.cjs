const axios = require('axios');
const fs = require('fs/promises');
const path = require('path');

const imgUrl = process.argv[2];
if (!imgUrl) {
  console.error('缺少图片链接参数，例如：node scripts/save_image.cjs "https://i0.hdslb.com/..."');
  process.exit(1);
}

function safeNameFromUrl(u, extFromQuery) {
  try {
    const url = new URL(u);
    const segs = url.pathname.split('/').filter(Boolean);
    let base = segs[segs.length - 1] || 'image';
    if (base === '0' && segs.length >= 2) base = segs[segs.length - 2];
    const safe = base.replace(/@/g, '_').replace(/[^a-zA-Z0-9._-]/g, '_');
    const ext = extFromQuery || '';
    return 'wx_' + safe + ext;
  } catch {
    return 'wx_image_' + Date.now() + '.jpg';
  }
}

async function main() {
  try {
    const { data, headers } = await axios.get(imgUrl, {
      responseType: 'arraybuffer',
      headers: {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
        'Referer': 'https://www.bilibili.com/'
      },
      timeout: 10000
    });
    let ext = '';
    try {
      const u = new URL(imgUrl);
      const fmt = u.searchParams.get('wx_fmt');
      if (fmt) {
        if (fmt.toLowerCase() === 'jpeg' || fmt.toLowerCase() === 'jpg') ext = '.jpg';
        else if (fmt.toLowerCase() === 'png') ext = '.png';
        else if (fmt.toLowerCase() === 'gif') ext = '.gif';
        else if (fmt.toLowerCase() === 'webp') ext = '.webp';
      }
    } catch {}
    if (!ext) {
      const ct = headers['content-type'] || '';
      if (ct.includes('jpeg') || ct.includes('jpg')) ext = '.jpg';
      else if (ct.includes('png')) ext = '.png';
      else if (ct.includes('gif')) ext = '.gif';
      else if (ct.includes('webp')) ext = '.webp';
      else ext = '.jpg';
    }
    const outDir = path.join(__dirname, '../public/images');
    await fs.mkdir(outDir, { recursive: true });
    const fileName = safeNameFromUrl(imgUrl, ext);
    const outPath = path.join(outDir, fileName);
    await fs.writeFile(outPath, data);
    console.log('✅ 已保存静态图片：', outPath);
    console.log('静态访问路径：', '/images/' + fileName);
  } catch (e) {
    console.error('❌ 下载失败：', e.message);
    process.exit(1);
  }
}

main();
