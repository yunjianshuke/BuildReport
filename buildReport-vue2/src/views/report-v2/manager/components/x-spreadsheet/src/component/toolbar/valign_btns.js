/* xreport 9.模拟Excel工具栏 */
import Group from '../group'
import { h } from '../element'
import Icon from '../icon'
import { cssPrefix } from '../../config'

function buildItemWithIcon(iconName) {
    return h('div', `${cssPrefix}-toolbar-btn`).child(new Icon(iconName, ''))
}

export default class ValignBtns extends Group {
    constructor(align) {
        const naligns = ['top', 'middle', 'bottom'].reduce((all, it) => {
            all[`${it}`] = buildItemWithIcon(`valign-${it}`)
                .on('click', () => {
                    this.change('valign', it)
                    this.setState(it)
                })
            return all
        }, {})
        super('valign', naligns, align)
        this.setState(align)
    }
}
