import Vue from 'vue'
import App from './App.vue'
import router from './router'
// import store from './store'
import Http from './http'
import { dialogDragInstall } from './utils/directive/drag'
import { dragSize } from './utils/dragSize/dragSize'
import ElementUI from 'element-ui'
import locale from 'element-ui/lib/locale/lang/zh-CN'
import 'element-ui/lib/theme-chalk/index.css'
import VueDragDrop from 'vue-drag-drop'
import '@/assets/styles/main.scss'

Vue.config.productionTip = false

Vue.use(Http)

Vue.use(dialogDragInstall)
Vue.directive('drag-size', dragSize)

Vue.prototype.$ELEMENT = ElementUI
Vue.use(ElementUI, { locale })

Vue.use(VueDragDrop)
Vue.prototype.$eventBus = new Vue({})

// 导入所有 icons 目录下的 SVG
const req = require.context('@/assets/icons', false, /\.svg$/)
const requireAll = requireContext => requireContext.keys().forEach(requireContext)
requireAll(req) // 执行导入

// 全局注册 SvgIcon 组件
import SvgIcon from '@/components/SvgIcon/index.vue'
Vue.component('SvgIcon', SvgIcon)

new Vue({
    router,
    // store,
    render: h => h(App)
}).$mount('#app')
