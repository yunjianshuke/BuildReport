import { expr2xy } from './alphabet'
import { numberCalc } from './helper'

// Converting infix expression to a suffix expression
// src: AVERAGE(SUM(A1,A2), B1) + 50 + B20
// return: [A1, A2], SUM[, B1],AVERAGE,50,+,B20,+
/* xreport 公式 */
const infixExprToSuffixExpr = src => {
    src = src.toUpperCase().replace('=', '')
    let result = []
    function expandRange(range) {
        const [start, end] = range.split(':')
        const startRow = start.match(/\d+/) ? parseInt(start.match(/\d+/)[0]) : 0
        const endRow = end.match(/\d+/) ? parseInt(end.match(/\d+/)[0]) : 0
        const expanded = []
        let startCol = start.match(/[A-Z]+/) ? start.match(/[A-Z]+/)[0] : ''
        let endCol = end.match(/[A-Z]+/) ? end.match(/[A-Z]+/)[0] : ''
        if (startRow === 0 || endRow === 0) {
            return expanded
        }
        if (startCol > endCol) {
            [startCol, endCol] = [endCol, startCol]
        }
        for (let col = startCol.charCodeAt(0); col <= endCol.charCodeAt(0); col++) {
            for (let row = Math.min(startRow, endRow); row <= Math.max(startRow, endRow); row++) {
                expanded.push(String.fromCharCode(col) + row)
            }
        }
        return expanded
    }
    // 正则表达式匹配函数名和参数
    const regex = /([A-Z]+)\(([^)]+)\)/
    const match = regex.exec(src)
    if (match) {
        const funcName = match[1]
        const params = match[2].split(',')
        // 处理参数
        params.forEach(param => {
            if (param.includes(':')) {
                result = result.concat(expandRange(param.trim()))
            } else {
                result.push(param.trim())
            }
        })
        // 将函数名和参数数量添加到结果
        result.push([funcName, result.length])
    }
    return result
}

const evalSubExpr = (subExpr, cellRender) => {
    const [fl] = subExpr
    let expr = subExpr
    if (fl === '"') {
        return subExpr.substring(1)
    }
    let ret = 1
    if (fl === '-') {
        expr = subExpr.substring(1)
        ret = -1
    }
    if (expr[0] >= '0' && expr[0] <= '9') {
        return ret * Number(expr)
    }
    const [x, y] = expr2xy(expr)
    return ret * cellRender(x, y)
}

// evaluate the suffix expression
// srcStack: <= infixExprToSufixExpr
// formulaMap: {'SUM': {}, ...}
// cellRender: (x, y) => {}
const evalSuffixExpr = (srcStack, formulaMap, cellRender, cellList) => {
    const stack = []
    // console.log(':::::formulaMap:', formulaMap);
    for (let i = 0; i < srcStack.length; i += 1) {
    // console.log(':::>>>', srcStack[i]);
        const expr = srcStack[i]
        const fc = expr[0]
        if (expr === '+') {
            const top = stack.pop()
            stack.push(numberCalc('+', stack.pop(), top))
        } else if (expr === '-') {
            if (stack.length === 1) {
                const top = stack.pop()
                stack.push(numberCalc('*', top, -1))
            } else {
                const top = stack.pop()
                stack.push(numberCalc('-', stack.pop(), top))
            }
        } else if (expr === '*') {
            stack.push(numberCalc('*', stack.pop(), stack.pop()))
        } else if (expr === '/') {
            const top = stack.pop()
            stack.push(numberCalc('/', stack.pop(), top))
        } else if (fc === '=' || fc === '>' || fc === '<') {
            let top = stack.pop()
            if (!Number.isNaN(top)) top = Number(top)
            let left = stack.pop()
            if (!Number.isNaN(left)) left = Number(left)
            let ret = false
            if (fc === '=') {
                ret = (left === top)
            } else if (expr === '>') {
                ret = (left > top)
            } else if (expr === '>=') {
                ret = (left >= top)
            } else if (expr === '<') {
                ret = (left < top)
            } else if (expr === '<=') {
                ret = (left <= top)
            }
            stack.push(ret)
        } else if (Array.isArray(expr)) {
            const [formula, len] = expr
            const params = []
            for (let j = 0; j < len; j += 1) {
                params.push(stack.pop())
            }
            /* xreport 公式 */
            if (formulaMap[formula]) {
                stack.push(formulaMap[formula].render(params.reverse()))
            }
        } else {
            if (cellList.includes(expr)) {
                return 0
            }
            if ((fc >= 'a' && fc <= 'z') || (fc >= 'A' && fc <= 'Z')) {
                cellList.push(expr)
            }
            stack.push(evalSubExpr(expr, cellRender))
            cellList.pop()
        }
    // console.log('stack:', stack);
    }
    return stack[0]
}
/* xreport 公式 */
const cellRender = (src, formulaMap, getCellText, cellList = [], cell, mode) => {
    if (src[0] === '=') {
        const stack = infixExprToSuffixExpr(src.substring(1))
        if (stack.length > 0 && mode === 'preview') {
            return evalSuffixExpr(
                stack,
                formulaMap,
                (x, y) => cellRender(getCellText(x, y), formulaMap, getCellText, cellList, cell, mode),
                cellList
            )
        } else if (stack.length > 0) {
            // 挂载cell属性
            cell.type = 0
            cell.editable = true
            cell.formula = stack.filter(v => typeof v === 'string' && v.match(/[A-Z]\d/)).map(v => {
                const [x, y] = expr2xy(v)
                return { x, y }
            })
            cell.formula.unshift(stack[stack.length - 1][0])
        }
    }
    return src
}

export default {
    render: cellRender
}
export {
    infixExprToSuffixExpr
}
