import Item from './item'
import Icon from '../icon'

export default class ToggleItem extends Item {
    element() {
        const { tag, size } = this
        return super.element()
            .child(new Icon(tag, size))
            .on('click', () => this.click())
    }

    click() {
        this.change(this.tag, this.toggle())
    }

    setState(active) {
        this.el.active(active)
    }

    toggle() {
        return this.el.toggle()
    }

    active() {
        return this.el.hasClass('active')
    }
}
