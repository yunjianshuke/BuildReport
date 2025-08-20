import Item from './item'
import Icon from '../icon'

export default class IconItem extends Item {
    /* xreport 9.模拟Excel工具栏 */
    element() {
        return super.element()
            .child(new Icon(this.tag, this.size))
            .on('click', () => this.change(this.tag))
    }

    setState(disabled) {
        this.el.disabled(disabled)
    }
}
