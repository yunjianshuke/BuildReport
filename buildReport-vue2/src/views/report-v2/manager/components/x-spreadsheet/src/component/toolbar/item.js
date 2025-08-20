import { cssPrefix } from '../../config'
import tooltip from '../tooltip'
import { h } from '../element'
import { t } from '../../locale/locale'

export default class Item {
    // tooltip
    // tag: the subclass type
    // shortcut: shortcut key
    /* xreport 9.模拟Excel工具栏 */
    constructor(tag, shortcut, value, size) {
        this.tip = ''
        if (tag) this.tip = t(`toolbar.${tag.replace(/-[a-z]/g, c => c[1].toUpperCase())}`)
        if (shortcut) this.tip += ` (${shortcut})`
        this.tag = tag
        this.shortcut = shortcut
        this.value = value
        /* xreport 9.模拟Excel工具栏 */
        this.size = size || ''
        this.el = this.element()
        this.change = () => {}
    }

    element() {
        const { tip } = this
        /* xreport 9.模拟Excel工具栏 */
        return h('div', `${cssPrefix}-toolbar-btn ${this.size}`)
            .on('mouseenter', evt => {
                if (this.tip) tooltip(this.tip, evt.target)
            })
            .attr('data-tooltip', tip)
    }

    setState() {}
}
