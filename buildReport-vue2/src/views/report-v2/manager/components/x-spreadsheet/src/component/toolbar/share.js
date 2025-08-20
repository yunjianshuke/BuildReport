/* xreport 9.模拟Excel工具栏 */
import IconItem from './icon_item'

export default class Share extends IconItem {
    constructor() {
        super('share', '', '', 'big')
    }

    click() {
        this.el.el.click()
    }
}
