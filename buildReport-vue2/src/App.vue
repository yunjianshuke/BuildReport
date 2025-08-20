<template>
    <div id="app">
        <el-container class="app-container">
            <el-aside width="200px">
                <el-menu 
                    :default-active="activeIndex"
                    class="app-menu"
                >
                    <li class="logo">
                        <img :src="logo" alt="build-report">
                        <span class="logo-text">Build-Report</span>
                    </li>
                    <el-menu-item 
                        v-for="item in menuItems" 
                        :key="item.path"
                        :index="item.index"
                        @click="go(item.path)"
                    >
                        {{ item.label }}
                    </el-menu-item>
                </el-menu>
            </el-aside>

            <el-main class="app-main">
                <router-view />
            </el-main>
        </el-container>
    </div>
</template>

<script>
export default {
    name: 'App',
    data() {
        return {
            activeIndex: '1',
            logo: require('@/assets/logo/logo.png'),
            // 菜单配置
            menuItems: [
                { index: '2', path: '/source', label: '数据源管理' },
                { index: '3', path: '/relation', label: '业务关系管理' },
                { index: '4', path: '/cache', label: '缓存管理' },
                { index: '5', path: '/manager', label: '报表设计' },
                { index: '7', path: '/myreport', label: '报表台账' }

            ]
        }
    },
    computed: {
        // 路径到索引的映射
        pathToIndexMap() {
            return this.menuItems.reduce((map, item) => {
                map[item.path] = item.index
                return map
            }, {})
        }
    },
    watch: {
        // 监听路由变化，同步更新菜单激活项
        $route(to) {
            this.updateActiveIndex(to.path)
        }
    },
    mounted() {
        // 初始化时根据当前路由设置激活菜单项
        this.updateActiveIndex(this.$route.path)
    },
    methods: {
        /**
         * 更新激活的菜单索引
         * @param {string} path - 路由路径
         */
        updateActiveIndex(path) {
            this.activeIndex = this.pathToIndexMap[path] || '2'
        },
        
        /**
         * 跳转到指定路径
         * @param {string} path - 目标路径
         */
        go(path) {
            // 更新激活菜单项
            this.updateActiveIndex(path)
            
            // 处理路由跳转，避免冗余导航错误
            this.$router.push(path).catch(err => {
                // 忽略NavigationDuplicated错误
                if (err.name !== 'NavigationDuplicated') {
                    console.error('Navigation error:', err)
                }
            })
        }
    }
}
</script>

<style lang="scss">
#app {
    font-family: Avenir, Helvetica, Arial, sans-serif;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    text-align: left;
    color: #2c3e50;
}

.app-container {
    height: 100vh;
    border: 1px solid #eee;
}

.app-menu {
    height: 100%;
    
    li {
        text-align: left;
    }
    
    .logo {
        width: 100%; 
        height: 120px;
        display: flex;
        justify-content: center;
        align-items: center;
        flex-direction: column;
        
        img {
            width: 50px;
            height: 50px;
        }
        
        .logo-text {
            font-weight: 600;
        }
    }
    
    li.el-menu-item {
        padding-left: 50px !important;
    }
    
    li.is-active {
        background-color: #ecf5ff;
    }
}

.app-main {
    width: calc(100vw - 200px);
}

// 导航链接样式
nav {
    padding: 30px;

    a {
        font-weight: bold;
        color: #2c3e50;

        &.router-link-exact-active {
            color: #42b983;
        }
    }
}
</style>
