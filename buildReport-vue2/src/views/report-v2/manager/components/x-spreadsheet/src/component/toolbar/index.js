/* xreport 9.模拟Excel工具栏 */
// import Align from './align'
import AlignBtns from './align_btns'
// import Valign from './valign'
import ValignBtns from './valign_btns'
// import Autofilter from './autofilter'
import Bold from './bold'
import Italic from './italic'
// import Strike from './strike'
import Underline from './underline'
import Border from './border'
import Clearformat from './clearformat'
import Paintformat from './paintformat'
import TextColor from './text_color'
import FillColor from './fill_color'
import FontSize from './font_size'
import Font from './font'
import Format from './format'
import Formula from './formula'
import Oblique from './oblique'
import Link from './link'
// import Share from './share'
import Image from './image'
import Expression from './expression'
import Freeze from './freeze'
import Merge from './merge'
import Redo from './redo'
import Undo from './undo'
import Print from './print'
import Pdf from './pdf'
import Word from './word'
import Excel from './excel'
import Textwrap from './textwrap'
// import More from './more'
import Save from './save'
import Close from './close'
import Preview from './preview'
// import Item from './item'
import Container from './container'
import Filter from './filter'
import Parameter from './parameter'
import Order from './order'

import { h } from '../element'
import { cssPrefix } from '../../config'
// import { bind } from '../event'

function buildDivider() {
    return h('div', `${cssPrefix}-toolbar-divider`)
}

// function initBtns2() {
//   this.btns2 = []
//   this.containers.forEach((it) => {
//     const rect = it.box()
//     const { marginLeft, marginRight } = it.computedStyle()
//     this.btns2.push([it, rect.width + parseInt(marginLeft, 10) + parseInt(marginRight, 10)])
//   })
// }

// function moreResize() {
//   const {
//     el, btns, moreEl, btns2
//   } = this
//   const { moreBtns, contentEl } = moreEl.dd
//   el.css('width', `${this.widthFn()}px`)
//   const elBox = el.box()

//   let sumWidth = 160
//   let sumWidth2 = 12
//   const list1 = []
//   const list2 = []
//   btns2.forEach(([it, w], index) => {
//     sumWidth += w
//     if (index === btns2.length - 1 || sumWidth < elBox.width) {
//       list1.push(it)
//     } else {
//       sumWidth2 += w
//       list2.push(it)
//     }
//   })
//   btns.html('').children(...list1)
//   moreBtns.html('').children(...list2)
//   contentEl.css('width', `${sumWidth2}px`)
//   if (list2.length > 0) {
//     moreEl.show()
//   } else {
//     moreEl.hide()
//   }
// }
/* xreport 9.模拟Excel工具栏 */
// function genBtn(it) {
//   const btn = new Item()
//   btn.el.on('click', () => {
//     if (it.onClick) it.onClick(this.data.getData(), this.data)
//   })
//   btn.tip = it.tip || ''

//   let { el } = it

//   if (it.icon) {
//     el = h('img').attr('src', it.icon)
//   }

//   if (el) {
//     const icon = h('div', `${cssPrefix}-icon`)
//     icon.child(el)
//     btn.el.child(icon)
//   }

//   return btn
// }

export default class Toolbar {
    constructor(data, widthFn, isHide = false) {
        this.data = data
        this.change = () => {}
        this.widthFn = widthFn
        this.isHide = isHide
        const style = data.defaultStyle()
        this.items = [
            {
                type: 'container',
                align: 'column',
                name: '撤销',
                child: [
                    this.undoEl = new Undo(),
                    this.redoEl = new Redo()
                ]
            },
            buildDivider(),
            {
                type: 'container',
                name: '格式刷',
                align: 'column',
                child: [
                    this.paintformatEl = new Paintformat(),
                    this.clearformatEl = new Clearformat()
                ]
            },
            buildDivider(),
            {
                type: 'container',
                name: '字体',
                align: 'column',
                child: [
                    [
                        this.fontEl = new Font(),
                        this.fontSizeEl = new FontSize()
                    ],
                    [
                        this.boldEl = new Bold(),
                        this.italicEl = new Italic(),
                        this.underlineEl = new Underline(),
                        // this.strikeEl = new Strike(),
                        h('div', `${cssPrefix}-toolbar-divider`),
                        this.borderEl = new Border(),
                        h('div', `${cssPrefix}-toolbar-divider`),
                        this.fillColorEl = new FillColor(style.bgcolor),
                        this.textColorEl = new TextColor(style.color)
                    ]
                ]
            },
            buildDivider(),
            {
                type: 'container',
                name: '对齐方式',
                align: 'column',
                child: [
                    [
                        this.alignBtnsEl = new AlignBtns(style.align),
                        h('div', `${cssPrefix}-toolbar-divider`),
                        this.textwrapEl = new Textwrap()
                    ],
                    [
                        this.valignBtnsEl = new ValignBtns(style.valign),
                        h('div', `${cssPrefix}-toolbar-divider`),
                        this.mergeEl = new Merge()
                    ]
                ]
            },
            buildDivider(),
            {
                type: 'container',
                name: '数字',
                child: [
                    this.formatEl = new Format()
                ]
            }
        ]
        if (this.data.settings.mode === 'preview') {
            this.items.push(
                buildDivider(),
                {
                    type: 'container',
                    name: '编辑',
                    align: 'column',
                    child: [
                        [
                            // this.autofilterEl = new Autofilter(),
                            this.formulaEl = new Formula()
                        ],
                        [
                            /* xreport 斜表头 */
                            this.obliqueEl = new Oblique()
                        ]
                    ]
                },
                buildDivider(),
                {
                    type: 'container',
                    name: '窗口',
                    child: [
                        this.freezeEl = new Freeze()
                    ]
                },
                buildDivider(),
                {
                    type: 'container',
                    name: '筛选',
                    child: [
                        this.filterEl = new Filter()
                    ]
                },
                buildDivider(),
                {
                    type: 'container',
                    name: '导出',
                    align: 'column',
                    child: [
                        [
                            this.pdfEl = new Pdf(),
                            this.wordEl = new Word()
                        ],
                        [
                            this.excelEl = new Excel()
                        ]
                    ]
                },
                buildDivider(),
                {
                    type: 'container',
                    name: '打印',
                    child: [
                        this.printEl = new Print()
                    ]
                }
            )
        } else {
            this.items.push(
                buildDivider(),
                {
                    type: 'container',
                    name: '编辑',
                    align: 'column',
                    child: [
                        [
                            // this.autofilterEl = new Autofilter(),
                            this.formulaEl = new Formula(),
                            this.expressionEl = new Expression(),
                            this.linkEl = new Link()
                        ],
                        [
                            /* xreport 斜表头 */
                            this.obliqueEl = new Oblique(),
                            this.imageEl = new Image(),
                            this.orderEl = new Order()
                        ]
                    ]
                },
                buildDivider(),
                {
                    type: 'container',
                    name: '窗口',
                    child: [
                        this.freezeEl = new Freeze()
                    ]
                },
                buildDivider(),
                {
                    type: 'container',
                    name: '属性',
                    child: [
                        this.parameterEl = new Parameter()
                    ]
                },
                buildDivider(),
                {
                    type: 'container',
                    name: '预览',
                    child: [
                        new Preview()
                    ]
                },
                // buildDivider(),
                // {
                //     type: 'container',
                //     name: '分享',
                //     child: [
                //         this.shareEl = new Share()
                //     ]
                // },
                buildDivider(),
                {
                    type: 'container',
                    name: '保存',
                    child: [
                        this.saveEl = new Save()
                    ]
                },
                buildDivider(),
                {
                    type: 'container',
                    name: '关闭',
                    child: [
                        new Close()
                    ]
                })
        }

        // const { extendToolbar = {}} = data.settings

        // if (extendToolbar.left && extendToolbar.left.length > 0) {
        //   this.items.unshift(buildDivider())
        //   const btns = extendToolbar.left.map(genBtn.bind(this))

        //   this.items.unshift(btns)
        // }
        // if (extendToolbar.right && extendToolbar.right.length > 0) {
        //   this.items.push(buildDivider())
        //   const btns = extendToolbar.right.map(genBtn.bind(this))
        //   this.items.push(btns)
        // }

        // this.items.push({ type: 'container', name: '更多', child: [this.moreEl = new More()] })

        this.el = h('div', `${cssPrefix}-toolbar`)
        this.btns = h('div', `${cssPrefix}-toolbar-btns`)
        this.containers = []

        this.items.forEach(it => {
            if (it.type === 'container') {
                const container = new Container(it.name, it.align)
                this.containers.push(container.el)
                it.child.forEach(i => {
                    if (Array.isArray(i)) {
                        const rowDiv = h('div', `${cssPrefix}-toolbar-btns`)
                        i.forEach(sub => {
                            rowDiv.child(sub.el)
                            sub.change = (...args) => {
                                this.change(...args)
                            }
                        })
                        container.body.child(rowDiv.el)
                    } else {
                        container.body.child(i.el)
                        i.change = (...args) => {
                            this.change(...args)
                        }
                    }
                })
                this.btns.child(container.el)
            } else {
                this.containers.push(it)
                this.btns.child(it.el)
            }
        })

        this.el.child(this.btns)
        if (isHide) {
            this.el.hide()
        } else {
            this.reset()
        }
    }

    paintformatActive() {
        return this.paintformatEl.active()
    }

    paintformatToggle() {
        this.paintformatEl.toggle()
    }

    trigger(type) {
        this[`${type}El`].click()
    }

    resetData(data) {
        this.data = data
        this.reset()
    }

    reset() {
        if (this.isHide) return
        const { data } = this
        const style = data.getSelectedCellStyle()
        const cell = data.getSelectedCell()
        // console.log('canUndo:', data.canUndo());
        this.undoEl.setState(!data.canUndo())
        this.redoEl.setState(!data.canRedo())
        this.mergeEl.setState(data.canUnmerge(), !data.selector.multiple())
        // this.autofilterEl.setState(!data.canAutofilter())
        // this.mergeEl.disabled();
        // console.log('selectedCell:', style, cell);
        const { font, format } = style
        this.formatEl.setState(format)
        this.fontEl.setState(font.name)
        this.fontSizeEl.setState(font.size)
        this.boldEl.setState(font.bold)
        this.italicEl.setState(font.italic)
        this.underlineEl.setState(style.underline)
        // this.strikeEl.setState(style.strike)
        this.textColorEl.setState(style.color)
        this.fillColorEl.setState(style.bgcolor)
        // this.alignEl.setState(style.align)
        this.alignBtnsEl.setState(style.align)
        // this.valignEl.setState(style.valign)
        this.valignBtnsEl.setState(style.valign)
        this.textwrapEl.setState(style.textwrap)
        // console.log('freeze is Active:', data.freezeIsActive());
        this.freezeEl.setState(data.freezeIsActive())

        if (this.expressionEl) {
            this.expressionEl.setState(cell ? !!cell.expression : false)
        }
        if (this.orderEl) {
            this.orderEl.setState(cell ? cell.type === 7 : false)
        }
    }
}
