// Netlify Function: 图片代理，规避热链限制
const axios = require('axios');

exports.handler = async (event, context) => {
  // 只允许 GET 请求
  if (event.httpMethod !== 'GET') {
    return {
      statusCode: 405,
      body: 'Method Not Allowed'
    };
  }

  try {
    const url = event.queryStringParameters?.url;
    
    if (!url) {
      return {
        statusCode: 400,
        body: 'Missing url parameter'
      };
    }

    // 根据目标 URL 设置 Referer
    let referer = '';
    try {
      const hostname = new URL(url).hostname;
      if (/^i[0-9]\.hdslb\.com$/i.test(hostname)) {
        referer = 'https://www.bilibili.com/';
      } else if (/mmbiz\.qpic\.cn$/i.test(hostname) || /qpic\.cn$/i.test(hostname)) {
        referer = 'https://weixin.qq.com/';
      }
    } catch (err) {
      console.error('Invalid URL:', err);
    }

    // 请求图片
    const response = await axios.get(url, {
      responseType: 'arraybuffer',
      headers: {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
        ...(referer ? { Referer: referer } : {})
      },
      timeout: 8000,
      validateStatus: () => true
    });

    const contentType = response.headers['content-type'] || 'image/jpeg';
    
    return {
      statusCode: response.status,
      headers: {
        'Content-Type': contentType,
        'Cache-Control': 'public, max-age=3600',
        'Access-Control-Allow-Origin': '*'
      },
      body: response.data.toString('base64'),
      isBase64Encoded: true
    };
  } catch (error) {
    console.error('Proxy error:', error);
    return {
      statusCode: 502,
      body: 'Proxy failed'
    };
  }
};
