import { Element, h } from './element'
import { cssPrefix } from '../config'

export default class Icon extends Element {
    /* xreport 9.模拟Excel工具栏 */
    constructor(name, size) {
        super('div', `${cssPrefix}-icon ${size}`)
        this.iconNameEl = h('div', `${cssPrefix}-icon-img ${name}`)
        this.child(this.iconNameEl)
    }

    setName(name) {
        this.iconNameEl.className(`${cssPrefix}-icon-img ${name}`)
    }
}
