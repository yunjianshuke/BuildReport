import { CellRange } from './cell_range'

export default class Selector {
    constructor() {
        this.range = new CellRange(0, 0, 0, 0)
        this.ri = 0
        this.ci = 0
        /* xreport 7.类型_扩展_上下文_拖拽 */
        this.moveRi = null
        this.moveCi = null
    }

    multiple() {
        return this.range.multiple()
    }

    setIndexes(ri, ci) {
        this.ri = ri
        this.ci = ci
    }

    size() {
        return this.range.size()
    }

    /* xreport 7.类型_扩展_上下文_拖拽 */
    setMove(ri = null, ci = null) {
        this.moveRi = ri
        this.moveCi = ci
    }
}
