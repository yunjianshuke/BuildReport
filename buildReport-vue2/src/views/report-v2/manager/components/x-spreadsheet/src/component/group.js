/* xreport 9.模拟Excel工具栏 */
import { Element } from './element'
import { cssPrefix } from '../config'
import tooltip from './tooltip'
import { t } from '../locale/locale'

export default class Group extends Element {
    constructor(placement, children, align) {
        super('div', `${cssPrefix}-group ${placement}`)
        this.change = () => {}
        this.oldAlign = align
        this.groups = children
        /* xreport 10.提示信息 */
        this.tips = {}
        Object.keys(children).forEach(it => {
            const iconName = `${placement}-${it}`
            const tip = t(`toolbar.${iconName.replace(/-[a-z]/g, c => c[1].toUpperCase())}`)
            if (tip) {
                this.tips[`${it}`] = tip
            }
            children[`${it}`]
                .on('mouseenter', evt => {
                    if (this.tips[`${it}`]) tooltip(this.tips[`${it}`], evt.target)
                })
                .attr('data-tooltip', tip)
        })
        this.children(...Object.values(children))
    }

    setState(align) {
        this.groups[this.oldAlign].active(false)
        this.groups[align].active(true)
        this.oldAlign = align
    }
}
