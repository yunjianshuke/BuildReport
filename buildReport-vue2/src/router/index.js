import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter)

const routes = [
    {
        path: '/',
        name: 'home',
        // 将根路径重定向到数据源管理页面
        redirect: '/source'
    },
    {
        path: '/source',
        name: '数据源管理',
        component: () =>
            import(/* webpackChunkName: "source" */ '../views/report-v2/source/index.vue')
    },
    {
        path: '/relation',
        name: '业务关系管理',
        component: () =>
            import(/* webpackChunkName: "relation" */ '../views/report-v2/relation/index.vue')
    },
    {
        path: '/cache',
        name: '缓存管理',
        component: () =>
            import(/* webpackChunkName: "cache" */ '../views/report-v2/cache/index.vue')
    },
    {
        path: '/manager',
        name: '报表设计',
        component: () =>
            import(/* webpackChunkName: "manager" */ '../views/report-v2/manager/index.vue')
    },
    {
        path: '/myreport',
        name: '报表台账',
        component: () =>
            import(/* webpackChunkName: "myreport" */ '../views/report-v2/myreport/index.vue')
    }
]

const router = new VueRouter({
    mode: 'hash',
    base: process.env.BASE_URL,
    routes
})

export default router
