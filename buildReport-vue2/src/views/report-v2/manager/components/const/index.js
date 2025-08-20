const date = [{ name: '范围', key: 'BETWEEN' }, { name: '大于', key: 'GT' }, { name: '大于等于', key: 'GE' }, { name: '小于', key: 'LT' }, { name: '小于等于', key: 'LE' }, { name: '等于年', key: 'YEAR' }, { name: '等于年月', key: 'MONTH' }, { name: '等于年月日', key: 'DAY' }, { name: '动态范围内', key: 'DYNAMIC_DATE' }]
export const expressionMap = {
    'VARCHAR': [{ name: '等于', key: 'EQ' }, { name: '不等于', key: 'NE' }, { name: '包含', key: 'LIKE' }, { 'name': '不包含', 'key': 'NOT_LIKE' }, { 'name': '列表内', 'key': 'IN' }, { 'name': '列表外', 'key': 'NOT_IN' }],
    'TEXT': [{ name: '等于', key: 'EQ' }, { name: '不等于', key: 'NE' }, { name: '包含', key: 'LIKE' }, { 'name': '不包含', 'key': 'NOT_LIKE' }, { 'name': '列表内', 'key': 'IN' }, { 'name': '列表外', 'key': 'NOT_IN' }],
    'LONGTEXT': [{ name: '等于', key: 'EQ' }, { name: '不等于', key: 'NE' }, { name: '包含', key: 'LIKE' }, { 'name': '不包含', 'key': 'NOT_LIKE' }, { 'name': '列表内', 'key': 'IN' }, { 'name': '列表外', 'key': 'NOT_IN' }],
    'CHAR': [{ name: '等于', key: 'EQ' }, { name: '不等于', key: 'NE' }],
    'DECIMAL': [{ name: '等于', key: 'EQ' }, { name: '不等于', key: 'NE' }, { name: '大于', key: 'GT' }, { name: '大于等于', key: 'GE' }, { name: '小于', key: 'LT' }, { name: '小于等于', key: 'LE' }],
    'BIGINT': [{ name: '等于', key: 'EQ' }, { name: '不等于', key: 'NE' }, { name: '大于', key: 'GT' }, { name: '大于等于', key: 'GE' }, { name: '小于', key: 'LT' }, { name: '小于等于', key: 'LE' }],
    'INT': [{ name: '等于', key: 'EQ' }, { name: '不等于', key: 'NE' }, { name: '大于', key: 'GT' }, { name: '大于等于', key: 'GE' }, { name: '小于', key: 'LT' }, { name: '小于等于', key: 'LE' }],
    'TINYINT': [{ name: '等于', key: 'EQ' }, { name: '不等于', key: 'NE' }, { name: '大于', key: 'GT' }, { name: '大于等于', key: 'GE' }, { name: '小于', key: 'LT' }, { name: '小于等于', key: 'LE' }],
    'DATE': date,
    'DATETIME': date,
    'TIME': [{ name: '范围', key: 'BETWEEN' }, { name: '大于', key: 'GT' }, { name: '大于等于', key: 'GE' }, { name: '小于', key: 'LT' }, { name: '小于等于', key: 'LE' }],
    'BIT': [{ name: '等于', key: 'EQ' }, { name: '不等于', key: 'NE' }],
    'BIGINT UNSIGNED': [{ name: '等于', key: 'EQ' }, { name: '不等于', key: 'NE' }, { name: '大于', key: 'GT' }, { name: '大于等于', key: 'GE' }, { name: '小于', key: 'LT' }, { name: '小于等于', key: 'LE' }]
}

export const dateGroup = ['DATETIME', 'DATE', 'TIME']

export const intGroup = ['BIGINT', 'INT', 'DECIMAL', 'BIGINT UNSIGNED']

export const listGroup = ['IN', 'NOT_IN']

export const componentList = [{
    'alias': '文本框',
    'key': 'text'
}, {
    'alias': '下拉选择框',
    'key': 'select'
}, {
    'alias': '单选框',
    'key': 'radio'
}, {
    'alias': '多选框',
    'key': 'checkbox'
}, {
    'alias': '日期选择框',
    'key': 'datetime'
}]

export const componentSource = [{
    'name': '常量',
    'key': 'final'
}, {
    'name': '筛选数据来源配置',
    'key': 'dataset'
}]

export const datetimeFormatList = [{
    'name': 'yyyy',
    'key': 'yyyy'
}, {
    'name': 'yyyy-MM',
    'key': 'yyyy-MM'
}, {
    'name': 'yyyy-MM-dd',
    'key': 'yyyy-MM-dd'
}, {
    'name': 'yyyy-MM-dd HH:mm:ss',
    'key': 'yyyy-MM-dd HH:mm:ss'
}]

export const dynamicDateValue = [{
    name: '近七天', key: 'week'
}, {
    name: '近30天内', key: 'month'
}, {
    name: '近3个月', key: 'quarter'
}, {
    name: '近一年', key: 'year'
}]
