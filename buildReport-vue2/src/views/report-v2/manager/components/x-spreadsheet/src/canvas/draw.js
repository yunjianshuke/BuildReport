/* xreport 1.类型_扩展_上下文_拖拽 */
import typeList from '../../assets/list.svg'
import typeGroup from '../../assets/group.svg'
import typeOrder from '../../assets/order.svg'
import typeSummary from '../../assets/summary.svg'
import expandDown from '../../assets/expand-down.svg'
import expandRight from '../../assets/expand-right.svg'
import contextDown from '../../assets/context-down.svg'
import contextRight from '../../assets/context-right.svg'
import expression from '../../assets/expression.svg'

function dpr() {
    return window.devicePixelRatio || 1
}

function thinLineWidth() {
    return dpr() - 0.5
}

function npx(px) {
    return parseInt(px * dpr(), 10)
}

function npxLine(px) {
    const n = npx(px)
    return n > 0 ? n - 0.5 : 0.5
}

// const debounce = (() => {
//     let timer = null
//     return (callback, wait = 200) => {
//         timer && clearTimeout(timer)
//         timer = setTimeout(callback, wait)
//     }
// })()

class DrawBox {
    constructor(x, y, w, h, padding = 0) {
        this.x = x
        this.y = y
        this.width = w
        this.height = h
        this.padding = padding
        this.bgcolor = '#ffffff'
        // border: [width, style, color]
        this.borderTop = null
        this.borderRight = null
        this.borderBottom = null
        this.borderLeft = null
    }

    setBorders({
        top, bottom, left, right
    }) {
        if (top) this.borderTop = top
        if (right) this.borderRight = right
        if (bottom) this.borderBottom = bottom
        if (left) this.borderLeft = left
    }

    innerWidth() {
        return this.width - (this.padding * 2) - 2
    }

    innerHeight() {
        return this.height - (this.padding * 2) - 2
    }

    textx(align) {
        const { width, padding } = this
        let { x } = this
        if (align === 'left') {
            x += padding
        } else if (align === 'center') {
            x += width / 2
        } else if (align === 'right') {
            x += width - padding
        }
        return x
    }

    texty(align, h) {
        const { height, padding } = this
        let { y } = this
        if (align === 'top') {
            y += padding
        } else if (align === 'middle') {
            y += height / 2 - h / 2
        } else if (align === 'bottom') {
            y += height - padding - h
        }
        return y
    }

    topxys() {
        const { x, y, width } = this
        return [[x, y], [x + width, y]]
    }

    rightxys() {
        const {
            x, y, width, height
        } = this
        return [[x + width, y], [x + width, y + height]]
    }

    bottomxys() {
        const {
            x, y, width, height
        } = this
        return [[x, y + height], [x + width, y + height]]
    }

    leftxys() {
        const {
            x, y, height
        } = this
        return [[x, y], [x, y + height]]
    }
}

function drawFontLine(type, tx, ty, align, valign, blheight, blwidth) {
    const floffset = { x: 0, y: 0 }
    if (type === 'underline') {
        if (valign === 'bottom') {
            floffset.y = 0
        } else if (valign === 'top') {
            floffset.y = -(blheight + 2)
        } else {
            floffset.y = -blheight / 2
        }
    } else if (type === 'strike') {
        if (valign === 'bottom') {
            floffset.y = blheight / 2
        } else if (valign === 'top') {
            floffset.y = -((blheight / 2) + 2)
        }
    }

    if (align === 'center') {
        floffset.x = blwidth / 2
    } else if (align === 'right') {
        floffset.x = blwidth
    }
    this.line(
        [tx - floffset.x, ty - floffset.y],
        [tx - floffset.x + blwidth, ty - floffset.y]
    )
}

class Draw {
    constructor(el, width, height) {
        this.el = el
        this.ctx = el.getContext('2d')
        this.resize(width, height)
        this.ctx.scale(dpr(), dpr())
        /* xreport 1.类型_扩展_上下文_拖拽 */
        this.typeWidth = 12
        this.typeHeight = 12
        this.typeGroup = new Image()
        this.typeGroup.src = typeGroup
        this.typeOrder = new Image()
        this.typeOrder.src = typeOrder
        this.typeList = new Image()
        this.typeList.src = typeList
        this.typeSummary = new Image()
        this.typeSummary.src = typeSummary
        this.expandWidth = 6
        this.expandHeight = 14
        this.expandDown = new Image()
        this.expandDown.src = expandDown
        this.expandRight = new Image()
        this.expandRight.src = expandRight
        this.contextWidth = 8
        this.contextHeight = 25
        this.contextDown = new Image()
        this.contextDown.src = contextDown
        this.contextRight = new Image()
        this.contextRight.src = contextRight
        this.expression = new Image()
        this.expression.src = expression
    }

    resize(width, height) {
    // console.log('dpr:', dpr);
        this.el.style.width = `${width}px`
        this.el.style.height = `${height}px`
        this.el.width = npx(width)
        this.el.height = npx(height)
    }

    clear() {
        const { width, height } = this.el
        this.ctx.clearRect(0, 0, width, height)
        return this
    }

    attr(options) {
        Object.assign(this.ctx, options)
        return this
    }

    save() {
        this.ctx.save()
        this.ctx.beginPath()
        return this
    }

    restore() {
        this.ctx.restore()
        return this
    }

    beginPath() {
        this.ctx.beginPath()
        return this
    }

    translate(x, y) {
        this.ctx.translate(npx(x), npx(y))
        return this
    }

    scale(x, y) {
        this.ctx.scale(x, y)
        return this
    }

    clearRect(x, y, w, h) {
        this.ctx.clearRect(x, y, w, h)
        return this
    }

    fillRect(x, y, w, h) {
        this.ctx.fillRect(npx(x) - 0.5, npx(y) - 0.5, npx(w), npx(h))
        return this
    }

    /* xreport 1.类型_扩展_上下文_拖拽 */
    contextAxisY(x, y, w, h, scrollX = 0, bgColor = 'rgb(68, 132, 205)') {
        this.ctx.fillStyle = bgColor
        this.ctx.fillRect(x - scrollX === 0 ? npx(x) : npx(x - w), npx(y), npx(w), npx(h))
        this.ctx.drawImage(this.contextDown, x - scrollX === 0 ? npx(x + (w - this.contextWidth) / 2) : npx(x - w + (w - this.contextWidth) / 2), npx(y + (h - this.contextHeight) / 2), npx(this.contextWidth), npx(this.contextHeight))
        return this
    }

    contextAxisX(x, y, w, h, scrollY = 0, bgColor = 'rgb(68, 132, 205)') {
        this.ctx.fillStyle = bgColor
        this.ctx.fillRect(npx(x), y - scrollY === 0 ? npx(y) : npx(y - h), npx(w), npx(h))
        this.ctx.drawImage(this.contextRight, npx(x + (w - this.contextHeight) / 2), y - scrollY === 0 ? npx(y + (h - this.contextWidth) / 2) : npx(y - h + (h - this.contextWidth) / 2), npx(this.contextHeight), npx(this.contextWidth))
        return this
    }

    /* xreport 斜表头 */
    obliqueMeterHead(text = '', x, y, w, h, underline, fontSize) {
        const textList = text.split('|')
        const lineNum = textList.length < 3 || textList.length > 5 ? 1 : textList.length - 1
        const textSpacing = 2
        const lineCoordinate = {
            1: [
                { x: x + w, y: y + h }
            ],
            2: [
                { x: x + w / 2, y: y + h },
                { x: x + w, y: y + h / 2 }
            ],
            3: [
                { x: x + w / 2, y: y + h },
                { x: x + w, y: y + h },
                { x: x + w, y: y + h / 2 }
            ],
            4: [
                { x: x + w / 3, y: y + h },
                { x: x + w * 2 / 3, y: y + h },
                { x: x + w, y: y + h / 3 },
                { x: x + w, y: y + h * 2 / 3 }
            ]
        }
        const textCoordinate = {
            1: [
                { textAlign: 'left', textBaseline: 'bottom', x: x + textSpacing, y: y + h - textSpacing },
                { textAlign: 'right', textBaseline: 'top', x: x + w - textSpacing, y: y + textSpacing }
            ],
            2: [
                { textAlign: 'left', textBaseline: 'bottom', x: x + textSpacing, y: y + h - textSpacing },
                { textAlign: 'right', textBaseline: 'bottom', x: x + w - textSpacing, y: y + h - textSpacing },
                { textAlign: 'right', textBaseline: 'top', x: x + w - textSpacing, y: y + textSpacing }
            ],
            3: [
                { textAlign: 'left', textBaseline: 'bottom', x: x + textSpacing, y: y + h - textSpacing },
                { textAlign: 'left', textBaseline: 'bottom', x: x + w / 2, y: y + h - textSpacing },
                { textAlign: 'right', textBaseline: 'top', x: x + w - textSpacing, y: y + h / 2 },
                { textAlign: 'right', textBaseline: 'top', x: x + w - textSpacing, y: y + textSpacing }
            ],
            4: [
                { textAlign: 'left', textBaseline: 'bottom', x: x + textSpacing, y: y + h - textSpacing },
                { textAlign: 'left', textBaseline: 'bottom', x: x + w / 3, y: y + h - textSpacing },
                { textAlign: 'right', textBaseline: 'bottom', x: x + w - textSpacing, y: y + h - textSpacing },
                { textAlign: 'right', textBaseline: 'top', x: x + w - textSpacing, y: y + h / 3 },
                { textAlign: 'right', textBaseline: 'top', x: x + w - textSpacing, y: y + textSpacing }
            ]
        }
        for (let i = 0; i < lineCoordinate[lineNum].length; i++) {
            this.ctx.save()
            this.ctx.moveTo(x, y)
            this.ctx.lineTo(lineCoordinate[lineNum][i].x, lineCoordinate[lineNum][i].y)
            this.ctx.stroke()
            this.ctx.restore()
        }
        for (let i = 0; i < textCoordinate[lineNum].length; i++) {
            if (!textList[i]) break
            this.ctx.save()
            const text = textList[i].trim() || ''
            const textWidth = this.ctx.measureText(text).width
            const textAlign = textCoordinate[lineNum][i].textAlign
            const textBaseline = textCoordinate[lineNum][i].textBaseline
            let textX = textCoordinate[lineNum][i].x
            let textY = textCoordinate[lineNum][i].y
            this.ctx.textAlign = textAlign
            this.ctx.textBaseline = textBaseline
            this.ctx.fillText(text, textX, textY)
            if (underline) {
                if (textAlign === 'right') {
                    textX -= textWidth
                }
                if (textBaseline === 'top') {
                    textY += fontSize
                }
                this.ctx.moveTo(textX, textY)
                this.ctx.lineTo(textX + textWidth, textY)
                this.ctx.stroke()
            }
            this.ctx.restore()
        }
    }

    /* xreport 公式 */
    formulaArea(x, y, w, h, borderColor = 'rgb(75, 137, 255)') {
        this.ctx.setLineDash([6, 5])
        this.ctx.lineWidth = 3
        this.ctx.strokeStyle = borderColor
        this.ctx.strokeRect(npx(x), npx(y), npx(w), npx(h))
        return this
    }

    fillText(text, x, y) {
        this.ctx.fillText(text, npx(x), npx(y))
        return this
    }

    /*
    txt: render text
    box: DrawBox
    attr: {
      align: left | center | right
      valign: top | middle | bottom
      color: '#333333',
      strike: false,
      font: {
        name: 'Arial',
        size: 14,
        bold: false,
        italic: false,
      }
    }
    textWrap: text wrapping
  */
    /* xreport 1.类型_扩展_上下文_拖拽 */
    async text(mtxt, box, attr = {}, options = {}) {
        const {
            type = 0,
            expand = 0,
            summary = 'Sum',
            mode = 'edit',
            textwrap = true,
            expression = false
        } = options
        const { ctx } = this
        const {
            align, valign, font, color, strike, underline, oblique
        } = attr
        const tx = box.textx(align)
        const { x, y, width, height } = box
        ctx.save()
        ctx.beginPath()
        this.attr({
            textAlign: align,
            textBaseline: valign,
            font: `${font.italic ? 'italic' : ''} ${font.bold ? 'bold' : ''} ${npx(font.size)}px ${font.name}`,
            fillStyle: color,
            strokeStyle: color
        })
        const txts = `${mtxt}`.split('\n')
        const biw = box.innerWidth()
        const ntxts = []
        txts.forEach(it => {
            const txtWidth = ctx.measureText(it).width
            if (textwrap && txtWidth > npx(biw)) {
                let textLine = { w: 0, len: 0, start: 0 }
                for (let i = 0; i < it.length; i += 1) {
                    if (textLine.w >= npx(biw)) {
                        ntxts.push(it.substr(textLine.start, textLine.len))
                        textLine = { w: 0, len: 0, start: i }
                    }
                    textLine.len += 1
                    textLine.w += ctx.measureText(it[i]).width + 1
                }
                if (textLine.len > 0) {
                    ntxts.push(it.substr(textLine.start, textLine.len))
                }
            } else {
                ntxts.push(it)
            }
        })
        const txtHeight = (ntxts.length - 1) * (font.size + 2)
        let ty = box.texty(valign, txtHeight)
        for (const txt of ntxts) {
            if (type >= 1 && type <= 3 && mode !== 'preview') {
                /* xreport 汇总 */
                if (type === 3) {
                    this.fillText(`${summary}(${txt})`, align === 'left' ? tx + this.typeWidth + 4 : tx, ty)
                } else {
                    this.fillText(txt, align === 'left' ? tx + this.typeWidth + 4 : tx, ty)
                }
                this.ctx.save()
                this.ctx.fillStyle = 'white'
                this.ctx.fillRect(npx(x), npx(y), npx(20), npx(20))
                this.ctx.restore()
                /* xreport 表达式 */
                if (expression) {
                    ctx.drawImage(this.expression, npx(x + 7), npx(y + 6), npx(this.typeWidth), npx(this.typeHeight))
                } else {
                    /* xreport 分组 */
                    if (type === 1) {
                        ctx.drawImage(this.typeGroup, npx(x + 7), npx(y + 6), npx(this.typeWidth), npx(this.typeHeight))
                    } else if (type === 2) {
                        ctx.drawImage(this.typeList, npx(x + 7), npx(y + 6), npx(this.typeWidth), npx(this.typeHeight))
                    } else if (type === 3) {
                        ctx.drawImage(this.typeSummary, npx(x + 3), npx(y + 3), npx(this.typeWidth + 3), npx(this.typeHeight + 3))
                    }
                }
                if (type !== 3) {
                    if (expand === 1) {
                        ctx.drawImage(this.expandDown, npx(x + 1), npx(y), npx(this.expandWidth), npx(this.expandHeight))
                    } else if (expand === 2) {
                        ctx.drawImage(this.expandRight, npx(x), npx(y + 1), npx(this.expandHeight), npx(this.expandWidth))
                    }
                }
            } else if (type === 7 && mode !== 'preview') {
                this.fillText(txt, align === 'left' ? tx + this.typeWidth + 4 : tx, ty)
                this.ctx.save()
                this.ctx.fillStyle = 'white'
                this.ctx.fillRect(npx(x), npx(y), npx(20), npx(20))
                this.ctx.restore()
                ctx.drawImage(this.typeOrder, npx(x + 7), npx(y + 6), npx(this.typeWidth), npx(this.typeHeight))
                if (expand === 1) {
                    ctx.drawImage(this.expandDown, npx(x + 1), npx(y), npx(this.expandWidth), npx(this.expandHeight))
                } else if (expand === 2) {
                    ctx.drawImage(this.expandRight, npx(x), npx(y + 1), npx(this.expandHeight), npx(this.expandWidth))
                }
            } else {
                /* xreport 斜表头 */
                if (oblique) {
                    this.obliqueMeterHead(txt, npx(x), npx(y), npx(width), npx(height), underline, font.size)
                /* xreport 表达式 */
                } else if (expression && mode !== 'preview') {
                    this.fillText(txt, align === 'left' ? tx + this.typeWidth + 4 : tx, ty)
                    this.ctx.save()
                    this.ctx.fillStyle = 'white'
                    this.ctx.fillRect(npx(x), npx(y), npx(20), npx(20))
                    this.ctx.restore()
                    ctx.drawImage(this.expression, npx(x + 7), npx(y + 6), npx(this.typeWidth), npx(this.typeHeight))
                } else {
                    this.fillText(txt, tx, ty)
                }
            }
            const txtWidth = ctx.measureText(txt).width
            if (strike) {
                drawFontLine.call(this, 'strike', tx, ty, align, valign, font.size, txtWidth)
            }
            if (underline && !oblique) {
                if (type >= 1 && type <= 3 && mode !== 'preview') {
                    drawFontLine.call(this, 'underline', align === 'left' ? tx + this.typeWidth + 4 : tx, ty, align, valign, font.size, txtWidth)
                } else {
                    drawFontLine.call(this, 'underline', tx, ty, align, valign, font.size, txtWidth)
                }
            }
            ty += font.size + 2
        }
        ctx.restore()
        return this
    }

    border(style, color) {
        const { ctx } = this
        ctx.lineWidth = thinLineWidth
        ctx.strokeStyle = color
        // console.log('style:', style);
        if (style === 'medium') {
            ctx.lineWidth = npx(2) - 0.5
        } else if (style === 'thick') {
            ctx.lineWidth = npx(3)
        } else if (style === 'dashed') {
            ctx.setLineDash([npx(3), npx(2)])
        } else if (style === 'dotted') {
            ctx.setLineDash([npx(1), npx(1)])
        } else if (style === 'double') {
            ctx.setLineDash([npx(2), 0])
        }
        return this
    }

    line(...xys) {
        const { ctx } = this
        if (xys.length > 1) {
            ctx.beginPath()
            const [x, y] = xys[0]
            ctx.moveTo(npxLine(x), npxLine(y))
            for (let i = 1; i < xys.length; i += 1) {
                const [x1, y1] = xys[i]
                ctx.lineTo(npxLine(x1), npxLine(y1))
            }
            ctx.stroke()
        }
        return this
    }

    strokeBorders(box) {
        const { ctx } = this
        ctx.save()
        // border
        const {
            borderTop, borderRight, borderBottom, borderLeft
        } = box
        if (borderTop) {
            this.border(...borderTop)
            // console.log('box.topxys:', box.topxys());
            this.line(...box.topxys())
        }
        if (borderRight) {
            this.border(...borderRight)
            this.line(...box.rightxys())
        }
        if (borderBottom) {
            this.border(...borderBottom)
            this.line(...box.bottomxys())
        }
        if (borderLeft) {
            this.border(...borderLeft)
            this.line(...box.leftxys())
        }
        ctx.restore()
    }

    dropdown(box) {
        const { ctx } = this
        const {
            x, y, width, height
        } = box
        const sx = x + width - 15
        const sy = y + height - 15
        ctx.save()
        ctx.beginPath()
        ctx.moveTo(npx(sx), npx(sy))
        ctx.lineTo(npx(sx + 8), npx(sy))
        ctx.lineTo(npx(sx + 4), npx(sy + 6))
        ctx.closePath()
        ctx.fillStyle = 'rgba(0, 0, 0, .45)'
        ctx.fill()
        ctx.restore()
    }

    error(box) {
        const { ctx } = this
        const { x, y, width } = box
        const sx = x + width - 1
        ctx.save()
        ctx.beginPath()
        ctx.moveTo(npx(sx - 8), npx(y - 1))
        ctx.lineTo(npx(sx), npx(y - 1))
        ctx.lineTo(npx(sx), npx(y + 8))
        ctx.closePath()
        ctx.fillStyle = 'rgba(255, 0, 0, .65)'
        ctx.fill()
        ctx.restore()
    }

    frozen(box) {
        const { ctx } = this
        const { x, y, width } = box
        const sx = x + width - 1
        ctx.save()
        ctx.beginPath()
        ctx.moveTo(npx(sx - 8), npx(y - 1))
        ctx.lineTo(npx(sx), npx(y - 1))
        ctx.lineTo(npx(sx), npx(y + 8))
        ctx.closePath()
        ctx.fillStyle = 'rgba(0, 255, 0, .85)'
        ctx.fill()
        ctx.restore()
    }

    rect(box, dtextcb) {
        const { ctx } = this
        const {
            x, y, width, height, bgcolor
        } = box
        ctx.save()
        ctx.beginPath()
        ctx.fillStyle = bgcolor || '#fff'
        ctx.rect(npxLine(x + 1), npxLine(y + 1), npx(width - 2), npx(height - 2))
        ctx.clip()
        ctx.fill()
        dtextcb()
        ctx.restore()
    }

    drawImage(data, cell, rindex, cindex, yoffset, fw, fh, tx, ty) {
        const { ctx } = this
        const {
            left, top
        } = data.cellRect(rindex, cindex)
        // console.log(left, top, yoffset, fw, fh, tx, ty)
        const img = new Image()
        img.onload = () => {
            // console.log('drawImage')
            ctx.drawImage(img, npx(left + fw + tx), npx(top + fh + ty + yoffset), npx(cell.image.width), npx(cell.image.height))
        }
        img.src = cell.text
    }
}

export default {}
export {
    Draw,
    DrawBox,
    thinLineWidth,
    npx
}
