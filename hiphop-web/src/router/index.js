import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../views/layout/Layout.vue'
import AdminLayout from '../views/layout/AdminLayout.vue'

const routes = [
  {
    path: '/',
    component: Layout,
    redirect: '/buzz',
    children: [
      {
        path: 'buzz',
        name: 'Buzz',
        component: () => import('../views/buzz/BuzzIndex.vue'),
        meta: { title: '热榜', icon: 'TrendCharts' }
      },
      {
        path: 'news',
        name: 'News',
        redirect: '/news/albums',
        meta: { title: '资讯', icon: 'Document' },
        children: [
          {
            path: 'songs',
            name: 'NewsSongs',
            component: () => import('../views/news/Songs.vue'),
            meta: { title: '歌曲' }
          },
          {
            path: 'albums',
            name: 'NewsAlbums',
            component: () => import('../views/news/Albums.vue'),
            meta: { title: '专辑' }
          },
          {
            path: 'sneakers',
            name: 'NewsSneakers',
            component: () => import('../views/news/Sneakers.vue'),
            meta: { title: '球鞋' }
          }
        ]
      },
      {
        path: 'media',
        name: 'Media',
        component: () => import('../views/media/MediaIndex.vue'),
        meta: { title: '资讯聚合', icon: 'VideoPlay' }
      },
      {
        path: 'hood',
        name: 'Hood',
        component: () => import('../views/hood/HoodIndex.vue'),
        meta: { title: '社区', icon: 'ChatLineSquare' }
      },
      {
        path: 'artists',
        name: 'Artists',
        component: () => import('../views/artists/ArtistsIndex.vue'),
        meta: { title: '音乐人', icon: 'Mic' }
      },
      {
        path: 'live',
        name: 'Live',
        component: () => import('../views/live/LiveIndex.vue'),
        meta: { title: '演出日历', icon: 'Calendar' }
      },
      {
        path: 'me',
        name: 'Me',
        component: () => import('../views/user/UserCenter.vue'),
        meta: { title: '个人中心', requiresAuth: true }
      }
    ]
  },
  {
    path: '/admin',
    component: AdminLayout,
    redirect: '/admin/home/featured',
    children: [
      // 兼容老路径 /admin/buzz，自动跳到新的首页管理-展位图
      {
        path: 'buzz',
        name: 'AdminBuzzLegacy',
        redirect: '/admin/home/featured',
        meta: { hidden: true }
      },
      {
        path: 'home',
        component: () => import('../views/admin/HomeAdminLayout.vue'),
        meta: { title: '首页管理', icon: 'House', requiresAuth: true },
        children: [
          {
            path: 'featured',
            name: 'AdminFeatured',
            component: () => import('../views/admin/BuzzManageAdmin.vue'),
            meta: { title: '展位图管理', icon: 'Picture', requiresAuth: true }
          },
          {
            path: 'news',
            name: 'AdminNews',
            component: () => import('../views/admin/NewsManageAdmin.vue'),
            meta: { title: '最新资讯管理', icon: 'Collection', requiresAuth: true }
          },
          {
            path: 'top-rated',
            name: 'AdminTopRated',
            component: () => import('../views/admin/TopRatedAdmin.vue'),
            meta: { title: '高分音乐榜管理', icon: 'StarFilled', requiresAuth: true }
          },
          {
            path: 'daily',
            name: 'AdminDaily',
            component: () => import('../views/admin/DailyRecommendAdmin.vue'),
            meta: { title: '每日推荐管理', icon: 'Calendar', requiresAuth: true }
          },
          {
            path: 'sneakers',
            name: 'AdminSneakers',
            component: () => import('../views/admin/SneakerAdmin.vue'),
            meta: { title: '球鞋资讯管理', icon: 'Goods', requiresAuth: true }
          }
        ]
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/user/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/error/NotFound.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
