/* xreport 9.模拟Excel工具栏 */
import IconItem from './icon_item'

export default class Save extends IconItem {
    constructor() {
        super('save', 'Ctrl+S', '', 'big')
    }

    click() {
        this.el.el.click()
    }
}
