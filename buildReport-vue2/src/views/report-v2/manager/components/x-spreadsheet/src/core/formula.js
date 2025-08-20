/**
  formula:
    key
    title
    render
*/
/**
 * @typedef {object} Formula
 * @property {string} key
 * @property {function} title
 * @property {function} render
 */
// import { tf } from '../locale/locale'
import { numberCalc } from './helper'
/* xreport 公式 */
/** @type {Formula[]} */
const baseFormulas = [
    {
        key: 'SUM',
        title: 'SUM() 求和',
        render: ary => {
            const result = ary.filter(v => Number(v))
            if (result.length) {
                return result.reduce((a, b) => numberCalc('+', a, b), 0)
            } else {
                return 0
            }
        }
    },
    {
        key: 'AVG',
        title: 'AVG() 平均值',
        render: ary => {
            const result = ary.filter(v => Number(v))
            if (result.length) {
                return result.reduce((a, b) => Number(a) + Number(b), 0) / result.length
            } else {
                return '#DIV/0!'
            }
        }
    },
    {
        key: 'MAX',
        title: 'MAX() 最大值',
        render: ary => {
            const result = ary.filter(v => Number(v))
            if (result.length) {
                return Math.max(...result)
            } else {
                return 0
            }
        }
    },
    {
        key: 'MIN',
        title: 'MIN() 最小值',
        render: ary => {
            const result = ary.filter(v => Number(v))
            if (result.length) {
                return Math.min(...result)
            } else {
                return 0
            }
        }
    },
    {
        key: 'COUNT',
        title: 'COUNT() 计数',
        render: ary => {
            return ary.length
        }
    }
    /* {
    key: 'IF',
    title: tf('formula._if'),
    render: ([b, t, f]) => (b ? t : f)
  },
  {
    key: 'AND',
    title: tf('formula.and'),
    render: ary => ary.every(it => it)
  },
  {
    key: 'OR',
    title: tf('formula.or'),
    render: ary => ary.some(it => it)
  },
  {
    key: 'CONCAT',
    title: tf('formula.concat'),
    render: ary => ary.join('')
  },
  // support:  1 + A1 + B2 * 3
  {
    key: 'DIVIDE',
    title: tf('formula.divide'),
    render: ary => ary.reduce((a, b) => Number(a) / Number(b)),
  },
  {
    key: 'PRODUCT',
    title: tf('formula.product'),
    render: ary => ary.reduce((a, b) => Number(a) * Number(b),1),
  },
  {
    key: 'SUBTRACT',
    title: tf('formula.subtract'),
    render: ary => ary.reduce((a, b) => Number(a) - Number(b)),
  },
  */
]

const formulas = baseFormulas

// const formulas = (formulaAry = []) => {
//   const formulaMap = {};
//   baseFormulas.concat(formulaAry).forEach((f) => {
//     formulaMap[f.key] = f;
//   });
//   return formulaMap;
// };
const formulam = {}
baseFormulas.forEach(f => {
    formulam[f.key] = f
})
/* xreport 公式 */
function formulaVerification(data, successFn) {
    if (data.getSelectedCell() && data.getSelectedCell().text) {
        const regex = /^=([A-Z]+)\s*\(((?:\s*[A-Z]{1,2}\d+\s*(?::\s*[A-Z]{1,2}\d+\s*)?\s*(?:,\s*[A-Z]{1,2}\d+\s*(?::\s*[A-Z]{1,2}\d+\s*)?\s*)*)\s*)\)$/i
        const match = ('' + data.getSelectedCell().text).match(regex)
        if (match && formulam[match[1].toUpperCase()]) {
            const coordinateSet = match[2].split(',')
            successFn(coordinateSet)
        }
    }
}

export default {
}

export {
    formulam,
    formulas,
    baseFormulas,
    formulaVerification
}
