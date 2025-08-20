// import helper from '../helper';

export default class History {
    constructor() {
        this.undoItems = []
        this.redoItems = []
        this.textItems = {}
    }

    add(data) {
        this.undoItems.push(JSON.stringify(data))
        this.redoItems = []
    }

    canUndo() {
        return this.undoItems.length > 0
    }

    canRedo() {
        return this.redoItems.length > 0
    }

    undo(currentd, cb) {
        const { undoItems, redoItems } = this
        if (this.canUndo()) {
            redoItems.push(JSON.stringify(currentd))
            cb(JSON.parse(undoItems.pop()))
        }
    }

    redo(currentd, cb) {
        const { undoItems, redoItems } = this
        if (this.canRedo()) {
            undoItems.push(JSON.stringify(currentd))
            cb(JSON.parse(redoItems.pop()))
        }
    }
    /* xreport start 2.修复历史记录BUG */
    recordText(ri, ci, text) {
        if (text !== undefined) {
            this.textItems[`${ri}-${ci}`] = text
        } else {
            return this.textItems[`${ri}-${ci}`] || ''
        }
    }
    /* xreport end */
}
