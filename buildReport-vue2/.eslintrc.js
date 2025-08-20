module.exports = {
    env: {
        browser: true,
        es2021: true
    },
    globals: {
        process: true,
        require: true,
        plus: true
    },
    extends: [
        'plugin:vue/strongly-recommended',
        'plugin:vue/essential',
        'eslint:recommended'
    ],
    overrides: [
        {
            env: {
                node: true
            },
            files: [
                '.eslintrc.{js,cjs}'
            ],
            parserOptions: {
                sourceType: 'script'
            }
        }
    ],
    parserOptions: {
        ecmaVersion: 2021,
        sourceType: 'module',
        ecmaFeatures: {
            jsx: true,
            tsx: true
        }
    },
    plugins: [
        'vue'
    ],
    rules: {
        // 代码风格
        'block-spacing': [2, 'always'],
        'brace-style': [2, '1tbs', {
            'allowSingleLine': true
        }],
        'comma-spacing': [2, {
            'before': false,
            'after': true
        }],
        'comma-dangle': [2, 'never'],
        'comma-style': [2, 'last'],
        'computed-property-spacing': [2, 'never'],
        'indent': [2, 4, {
            'SwitchCase': 1
        }],
        'key-spacing': [2, {
            'beforeColon': false,
            'afterColon': true
        }],
        'keyword-spacing': [2, {
            'before': true,
            'after': true
        }],
        'linebreak-style': 0,
        'multiline-ternary': [2, 'always-multiline'],
        'no-multiple-empty-lines': [2, {
            'max': 1
        }],
        'no-unneeded-ternary': [2, {
            'defaultAssignment': false
        }],
        'quotes': [2, 'single'],
        'semi': [2, 'never'],
        'space-before-blocks': [2, 'always'],
        'space-before-function-paren': [2, 'never'],
        'space-in-parens': [2, 'never'],
        'space-infix-ops': 2,
        'space-unary-ops': [2, {
            'words': true,
            'nonwords': false
        }],
        'spaced-comment': [2, 'always', {
            'markers': ['global', 'globals', 'eslint', 'eslint-disable', '*package', '!', ',']
        }],
        'switch-colon-spacing': [2, {
            'after': true,
            'before': false
        }],
        // ES6
        'arrow-parens': [2, 'as-needed'],
        'arrow-spacing': [2, {
            'before': true,
            'after': true
        }],
        // Vue - https://github.com/vuejs/eslint-plugin-vue
        'vue/html-indent': [2, 4],
        'vue/max-attributes-per-line': 0,
        'vue/require-default-prop': 0,
        'vue/singleline-html-element-content-newline': 0,
        'vue/attributes-order': 2,
        'vue/order-in-components': 2,
        'vue/this-in-template': 2,
        'vue/script-indent': [2, 4, {
            'switchCase': 1
        }],
        // 临时禁用多单词组件名检查
        'vue/multi-word-component-names': 'off',
        'no-inline-comments ': 'off'
    }
}
