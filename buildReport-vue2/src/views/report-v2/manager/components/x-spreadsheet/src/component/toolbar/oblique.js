import DropdownItem from './dropdown_item'
import DropdownOblique from '../dropdown_oblique'

export default class Oblique extends DropdownItem {
    constructor() {
        super('oblique')
    }

    getValue(it) {
        return it
    }

    dropdown() {
        return new DropdownOblique()
    }
}
