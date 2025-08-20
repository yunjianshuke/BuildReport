import Dropdown from './dropdown'
import Icon from './icon'
import { h } from './element'
import { cssPrefix } from '../config'
export default class DropdownOblique extends Dropdown {
    constructor() {
        const inputEl = document.createElement('input')
        inputEl.className = `${cssPrefix}-oblique-input`
        const obliqueEl = [
            h('div', `${cssPrefix}-oblique-title`).child('斜表头格式：xxx | xxx'),
            h('div', `${cssPrefix}-oblique-wrap`).children(
                h(inputEl),
                h('button', `${cssPrefix}-oblique-button`).on('click', () => {
                    this.change(inputEl.value)
                    this.hide()
                }).child('确定')
            )
        ]
        super(new Icon('oblique'), '180px', true, 'bottom-left', ...obliqueEl)
    }
}
