import Dropdown from './dropdown'
import Icon from './icon'
import BorderPalette from './border_palette'

export default class DropdownBorder extends Dropdown {
    constructor() {
        const icon = new Icon('border-all')
        const borderPalette = new BorderPalette()
        borderPalette.change = v => {
            this.change(v)
            this.hide()
        }
        /* xreport 9.模拟Excel工具栏 */
        super(icon, 'auto', true, 'bottom-left', borderPalette.el)
    }
}
