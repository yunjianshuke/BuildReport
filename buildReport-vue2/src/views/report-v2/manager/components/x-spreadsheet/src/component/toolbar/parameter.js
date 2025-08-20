/* xreport 9.模拟Excel工具栏 */
import ToggleItem from './toggle_item'

export default class Parameter extends ToggleItem {
    constructor() {
        super('parameter', '', '', 'big')
        this.click()
    }
}
