/* xreport 9.模拟Excel工具栏 */
import { cssPrefix } from '../../config'
import { h } from '../element'

export default class Container {
    constructor(name, align) {
        this.nameText = name
        this.align = align || 'row'
        this.el = this.element()
    }

    element() {
        const container = h('div', `${cssPrefix}-toolbar-container`)
        this.body = h('div', `${cssPrefix}-toolbar-container-body`)
        this.body.addClass(`${cssPrefix}-toolbar-container-body-${this.align}`)
        this.name = h('div', `${cssPrefix}-toolbar-container-name`)
        this.name.html(this.nameText)
        container.child(this.body)
        container.child(this.name)
        return container
    }
}
