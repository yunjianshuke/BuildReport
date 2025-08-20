const { defineConfig } = require('@vue/cli-service')
const path = require('path')
const { resolve } = require('path'); // 引入 path 模块的 resolve 函数

module.exports = defineConfig({
    transpileDependencies: true,
    publicPath: process.env.NODE_ENV === 'production' ? '/buildreport' : '/',
    // 开发服务器配置
    devServer: {
        host: '0.0.0.0',
        compress: false,
        headers: {
            'Access-Control-Allow-Origin': '*',
            'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE, PATCH, OPTIONS',
            'Access-Control-Allow-Headers': 'X-Requested-With, content-type, Authorization'
        },
        client: {
            overlay: false,
            progress: true
        },
        open: true,
        historyApiFallback: true,
        port: 8090,
        hot: 'only', // 修改为 'only' 模式
        liveReload: true, // 添加实时重载
        watchFiles: ['src/**/*'], // 添加文件监听
        static: {
            directory: './public',
            publicPath: process.env.NODE_ENV === 'development'
                ? '/'
                : '',
            watch: true
        },
        proxy: {
            '/': {
                target: 'http://192.168.2.6:8016',
                ws: false,
                changeOrigin: true,
                // secure: false,
                pathRewrite: {
                    '^/': '/'
                }
            }

        }
    },

    chainWebpack: config => {
        config.module
            .rule('svg')
            .exclude.add(path.join(__dirname, 'src/assets/icons'))
            .end()
        
        // SVG 处理配置
        config.module
            .rule('svg-sprite')
            .test(/\.svg$/)
            .include.add(path.join(__dirname, 'src/assets/icons')) // 只处理 assets/icons 下的 SVG
            .end()
            .use('svg-sprite-loader')
            .loader('svg-sprite-loader')
            .options({
                symbolId: 'icon-[name]' // 定义 sprite 的 id 格式
            })
    }
})
