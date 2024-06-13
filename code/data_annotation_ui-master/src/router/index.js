import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'Manager',
    component: () => import('../views/Manager.vue'),
    redirect: '/login',  // 重定向到login
    children: [
      { path: '403', name: 'Auth', meta: { name: '无权限' }, component: () => import('../views/manager/Auth') },
      { path: 'annotation2', name: 'Annotation2', meta: { name: '视频标注' }, component: () => import('../views/manager/Annotation2') },
    ]
  },
  { path: '/login', name: 'Login', meta: { name: '登录' }, component: () => import('../views/Login.vue') },
  { path: '/register', name: 'Register', meta: { name: '注册' }, component: () => import('../views/Register.vue') },
  { path: '*', name: '404', meta: { name: '无法访问' }, component: () => import('../views/404.vue') },
  { path: '/lunwen', name: 'lunwen', meta: { name: 'lunwen' }, component: () => import('../views/lunwen.vue') },
  { path: '/lunwen2', name: 'lunwen2', meta: { name: 'lunwen2' }, component: () => import('../views/lunwen2.vue') },
]
const router = new VueRouter({
  mode: 'history',
  base: '/',
  routes
})

// router.beforeEach((to, from, next) => {
//   // to 是到达的路由信息
//   // from 是开源的路由信息
//   // next 是帮助我们跳转路由的函数
//   let adminPaths = ['/user']
//   let user = JSON.parse(localStorage.getItem('user') || '{}')
//   if (user.role !== '管理员' && adminPaths.includes(to.path)) {
//     // 如果当前登录的用户不是管理员，然后当前的到达的路径是管理员才有权限访问的路径，那这个时候我就让用户去到一个没有权限的页面，不让他访问实际的页面
//     next('/403')
//   } else {
//     next()
//   }
// })

export default router
