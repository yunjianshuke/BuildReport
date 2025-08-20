import Dropdown from './dropdown'
import Icon from './icon'
import { h } from './element'
import { baseFormulas } from '../core/formula'
import { cssPrefix } from '../config'

export default class DropdownFormula extends Dropdown {
    constructor() {
        const nformulas = baseFormulas.map(it => h('div', `${cssPrefix}-item`)
            .on('click', () => {
                this.hide()
                this.change(it)
            })
        /* xreport 公式 */
            .child(it.title))
        super(new Icon('formula'), '180px', true, 'bottom-left', ...nformulas)
    }
}
