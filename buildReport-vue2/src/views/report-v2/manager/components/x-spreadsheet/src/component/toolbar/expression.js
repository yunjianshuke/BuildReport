/* xreport 9.模拟Excel工具栏 */
import IconItem from './icon_item'

export default class Expression extends IconItem {
    constructor() {
        super('expression', '', '', '')
    }

    setState(active) {
        this.el.active(active)
    }

}
