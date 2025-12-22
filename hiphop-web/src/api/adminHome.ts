export const apiBase = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
export async function getBuzzList(params: Record<string, any> = {}) {
  // 过滤掉 undefined 和 null 值
  const filteredParams: Record<string, any> = {}
  for (const key in params) {
    if (params[key] !== undefined && params[key] !== null && params[key] !== 'undefined') {
      filteredParams[key] = params[key]
    }
  }
  const qs = new URLSearchParams(filteredParams as any).toString()
  const res = await fetch(`${apiBase}/api/home/buzz${qs ? `?${qs}` : ''}`)
  return res.json()
}
export async function createBuzz(data: any) {
  const res = await fetch(`${apiBase}/api/home/buzz`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  })
  return res.json()
}
export async function updateBuzz(id: number, data: any) {
  const res = await fetch(`${apiBase}/api/home/buzz/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  })
  return res.json()
}
export async function updateBuzzStatus(id: number, status: number) {
  const res = await fetch(`${apiBase}/api/home/buzz/${id}/status?status=${status}`, {
    method: 'PATCH'
  })
  return res.json()
}
export async function deleteBuzz(id: number) {
  const res = await fetch(`${apiBase}/api/home/buzz/${id}`, {
    method: 'DELETE'
  })
  return res.json()
}
export async function getSections(params: Record<string, any> = {}) {
  // 过滤掉 undefined 和 null 值
  const filteredParams: Record<string, any> = {}
  for (const key in params) {
    if (params[key] !== undefined && params[key] !== null && params[key] !== 'undefined') {
      filteredParams[key] = params[key]
    }
  }
  const qs = new URLSearchParams(filteredParams as any).toString()
  const res = await fetch(`${apiBase}/api/home/sections${qs ? `?${qs}` : ''}`)
  return res.json()
}
// 最新资讯
export async function getNewsList(params: Record<string, any> = {}) {
  const filteredParams: Record<string, any> = {}
  for (const key in params) {
    if (params[key] !== undefined && params[key] !== null && params[key] !== 'undefined') {
      filteredParams[key] = params[key]
    }
  }
  const qs = new URLSearchParams(filteredParams as any).toString()
  const res = await fetch(`${apiBase}/api/home/news${qs ? `?${qs}` : ''}`)
  return res.json()
}
export async function createNews(data: any) {
  const res = await fetch(`${apiBase}/api/home/news`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  })
  return res.json()
}
export async function updateNews(id: number | string, data: any) {
  const idStr = String(id)
  const res = await fetch(`${apiBase}/api/home/news/${idStr}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  })
  return res.json()
}
export async function deleteNews(id: number | string) {
  const idStr = String(id)
  const res = await fetch(`${apiBase}/api/home/news/${idStr}`, {
    method: 'DELETE'
  })
  return res.json()
}
export async function updateNewsStatus(id: number | string, status: number) {
  const idStr = String(id)
  const res = await fetch(`${apiBase}/api/home/news/${idStr}/status?status=${status}`, {
    method: 'PATCH'
  })
  return res.json()
}

// 球鞋资讯
export async function getSneakerList(params: Record<string, any> = {}) {
  const filteredParams: Record<string, any> = {}
  for (const key in params) {
    if (params[key] !== undefined && params[key] !== null && params[key] !== 'undefined') {
      filteredParams[key] = params[key]
    }
  }
  const qs = new URLSearchParams(filteredParams as any).toString()
  const res = await fetch(`${apiBase}/api/home/sneakers${qs ? `?${qs}` : ''}`)
  return res.json()
}

export async function createSneaker(data: any) {
  const res = await fetch(`${apiBase}/api/home/sneakers`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  })
  return res.json()
}

export async function updateSneaker(id: number | string, data: any) {
  const idStr = String(id)
  const res = await fetch(`${apiBase}/api/home/sneakers/${idStr}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  })
  return res.json()
}

export async function deleteSneaker(id: number | string) {
  const idStr = String(id)
  const res = await fetch(`${apiBase}/api/home/sneakers/${idStr}`, {
    method: 'DELETE'
  })
  return res.json()
}

export async function updateSneakerStatus(id: number | string, status: number) {
  const idStr = String(id)
  const res = await fetch(`${apiBase}/api/home/sneakers/${idStr}/status?status=${status}`, {
    method: 'PATCH'
  })
  return res.json()
}

export async function fetchContent(url: string) {
  const res = await fetch(`${apiBase}/api/home/buzz/fetch?url=${encodeURIComponent(url)}`, {
    method: 'POST'
  })
  if (!res.ok) {
    const error = await res.text()
    throw new Error(error || '爬取失败')
  }
  return res.json()
}
export async function getFeaturedList(params: Record<string, any> = {}) {
  // 过滤掉 undefined 和 null 值
  const filteredParams: Record<string, any> = {}
  for (const key in params) {
    if (params[key] !== undefined && params[key] !== null && params[key] !== 'undefined') {
      filteredParams[key] = params[key]
    }
  }
  const qs = new URLSearchParams(filteredParams as any).toString()
  const res = await fetch(`${apiBase}/api/home/featured${qs ? `?${qs}` : ''}`)
  return res.json()
}
export async function createFeatured(data: any) {
  const res = await fetch(`${apiBase}/api/home/featured`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  })
  return res.json()
}
export async function updateFeatured(id: number | string, data: any) {
  // 将 ID 转换为字符串，避免 JavaScript Number 精度问题
  const idStr = String(id)
  const res = await fetch(`${apiBase}/api/home/featured/${idStr}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  })
  if (!res.ok) {
    const error = await res.text()
    throw new Error(error || '更新失败')
  }
  return res.json()
}
export async function deleteFeatured(id: number | string) {
  // 将 ID 转换为字符串，避免 JavaScript Number 精度问题
  const idStr = String(id)
  const res = await fetch(`${apiBase}/api/home/featured/${idStr}`, {
    method: 'DELETE'
  })
  return res.json()
}
export async function updateFeaturedStatus(id: number | string, status: number) {
  // 将 ID 转换为字符串，避免 JavaScript Number 精度问题
  const idStr = String(id)
  const res = await fetch(`${apiBase}/api/home/featured/${idStr}/status?status=${status}`, {
    method: 'PATCH'
  })
  return res.json()
}

// 高分音乐榜
export async function getTopRatedList(params: Record<string, any> = {}) {
  const filteredParams: Record<string, any> = {}
  for (const key in params) {
    if (params[key] !== undefined && params[key] !== null && params[key] !== 'undefined') {
      filteredParams[key] = params[key]
    }
  }
  const qs = new URLSearchParams(filteredParams as any).toString()
  const res = await fetch(`${apiBase}/api/home/top-rated${qs ? `?${qs}` : ''}`)
  return res.json()
}

export async function createTopRated(data: any) {
  const res = await fetch(`${apiBase}/api/home/top-rated`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  })
  return res.json()
}

export async function updateTopRated(id: number | string, data: any) {
  const idStr = String(id)
  const res = await fetch(`${apiBase}/api/home/top-rated/${idStr}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  })
  return res.json()
}

export async function deleteTopRated(id: number | string) {
  const idStr = String(id)
  const res = await fetch(`${apiBase}/api/home/top-rated/${idStr}`, {
    method: 'DELETE'
  })
  return res.json()
}

export async function updateTopRatedStatus(id: number | string, status: number) {
  const idStr = String(id)
  const res = await fetch(`${apiBase}/api/home/top-rated/${idStr}/status?status=${status}`, {
    method: 'PATCH'
  })
  return res.json()
}

export async function searchNeteaseSongs(keywords: string, limit = 20) {
  const params = new URLSearchParams()
  params.append('keywords', keywords)
  params.append('limit', String(limit))
  const res = await fetch(`${apiBase}/api/home/top-rated/netease/search?${params.toString()}`)
  if (!res.ok) {
    const error = await res.text()
    throw new Error(error || '搜索失败')
  }
  return res.json()
}

export async function searchNeteaseAlbums(keywords: string, limit = 20) {
  const params = new URLSearchParams()
  params.append('keywords', keywords)
  params.append('limit', String(limit))
  const res = await fetch(`${apiBase}/api/home/top-rated/netease/search-album?${params.toString()}`)
  if (!res.ok) {
    const error = await res.text()
    throw new Error(error || '搜索失败')
  }
  return res.json()
}

export async function importTopRatedBatch(items: any[]) {
  const res = await fetch(`${apiBase}/api/home/top-rated/netease/import`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(items || [])
  })
  if (!res.ok) {
    const error = await res.text()
    throw new Error(error || '导入失败')
  }
  return res.json()
}

// 每日推荐
export async function getDailyList(params: Record<string, any> = {}) {
  const filteredParams: Record<string, any> = {}
  for (const key in params) {
    if (params[key] !== undefined && params[key] !== null && params[key] !== 'undefined') {
      filteredParams[key] = params[key]
    }
  }
  const qs = new URLSearchParams(filteredParams as any).toString()
  const res = await fetch(`${apiBase}/api/home/daily${qs ? `?${qs}` : ''}`)
  return res.json()
}

export async function createDaily(data: any) {
  const res = await fetch(`${apiBase}/api/home/daily`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  })
  return res.json()
}

export async function updateDaily(id: number | string, data: any) {
  const idStr = String(id)
  const res = await fetch(`${apiBase}/api/home/daily/${idStr}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  })
  return res.json()
}

export async function deleteDaily(id: number | string) {
  const idStr = String(id)
  const res = await fetch(`${apiBase}/api/home/daily/${idStr}`, {
    method: 'DELETE'
  })
  return res.json()
}

export async function updateDailyStatus(id: number | string, status: number) {
  const idStr = String(id)
  const res = await fetch(`${apiBase}/api/home/daily/${idStr}/status?status=${status}`, {
    method: 'PATCH'
  })
  return res.json()
}

export async function getLatestDaily(limit: number = 10) {
  const res = await fetch(`${apiBase}/api/home/daily/latest?limit=${limit}`)
  return res.json()
}

export async function fetchDailyFromPlaylist(playlistId: string = '5205824122,2949295961', count: number = 10) {
  const res = await fetch(`${apiBase}/api/home/daily/fetch?playlistId=${playlistId}&count=${count}`, {
    method: 'POST'
  })
  return res.json()
}

// 热门趋势
export async function getTrendingList(params: Record<string, any> = {}) {
  const filteredParams: Record<string, any> = {}
  for (const key in params) {
    if (params[key] !== undefined && params[key] !== null && params[key] !== 'undefined') {
      filteredParams[key] = params[key]
    }
  }
  const qs = new URLSearchParams(filteredParams as any).toString()
  const res = await fetch(`${apiBase}/api/home/trending${qs ? `?${qs}` : ''}`)
  return res.json()
}

export async function createTrending(data: any) {
  const res = await fetch(`${apiBase}/api/home/trending`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  })
  return res.json()
}

export async function updateTrending(id: number | string, data: any) {
  const idStr = String(id)
  const res = await fetch(`${apiBase}/api/home/trending/${idStr}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  })
  if (!res.ok) {
    const error = await res.text()
    throw new Error(error || '更新失败')
  }
  return res.json()
}

export async function deleteTrending(id: number | string) {
  const idStr = String(id)
  const res = await fetch(`${apiBase}/api/home/trending/${idStr}`, {
    method: 'DELETE'
  })
  return res.json()
}

export async function updateTrendingStatus(id: number | string, status: number) {
  const idStr = String(id)
  const res = await fetch(`${apiBase}/api/home/trending/${idStr}/status?status=${status}`, {
    method: 'PATCH'
  })
  return res.json()
}
