/* xreport 9.模拟Excel工具栏 */
import Group from '../group'
import { h } from '../element'
import Icon from '../icon'
import { cssPrefix } from '../../config'

function buildItemWithIcon(iconName) {
    return h('div', `${cssPrefix}-toolbar-btn`).child(new Icon(iconName, ''))
}

export default class AlignBtns extends Group {
    constructor(align) {
        const naligns = ['left', 'center', 'right'].reduce((all, it) => {
            all[`${it}`] = buildItemWithIcon(`align-${it}`)
                .on('click', () => {
                    this.change('align', it)
                    this.setState(it)
                })
            return all
        }, {})
        super('align', naligns, align)
        this.setState(align)
    }
}
