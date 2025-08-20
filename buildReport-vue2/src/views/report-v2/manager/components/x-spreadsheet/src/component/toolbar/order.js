/* xreport 9.模拟Excel工具栏 */
import IconItem from './icon_item'

export default class Order extends IconItem {
    constructor() {
        super('order', '', '', '')
    }

    setState(active) {
        this.el.active(active)
    }

}
