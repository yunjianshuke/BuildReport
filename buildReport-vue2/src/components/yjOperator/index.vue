<script>
export default {
    name: 'OperatorBtn',
    inject: {
        preset_pageComponent: {
            default: null
        }
    },
    props: {
        permissions: {
            type: Object,
            default: () => ({
                add: '',
                edit: '',
                delete: '',
                view: ''
            })
        },
        showBtns: {
            type: Array,
            default: undefined
        },
        row: {
            type: Object,
            default: null
        },
        maxButtons: {
            type: Number,
            default: 1
        }
    },
    data() {
        return {
            currentRow: null,
            componentKey: Math.random().toString(36).substring(2, 10)
        }
    },
    mounted() {
        this.$eventBus.$on('preset_getGridMenuCustomBody', this.handleGetGridMenuCustomBody)
    },
    destroyed() {
        this.$eventBus.$off('preset_getGridMenuCustomBody', this.handleGetGridMenuCustomBody)
    },
    methods: {
        deleteEvent(noEmit) {
            if (noEmit) {
                return 'deleteEvent'
            }
            this.$emit('deleteEvent')
        },
        editEvent(noEmit) {
            if (noEmit) {
                return 'editEvent'
            }
            this.$emit('editEvent')
        },
        detailsEvent(noEmit) {
            if (noEmit) {
                return 'detailsEvent'
            }
            this.$emit('detailsEvent')
        },
        copyEvent(noEmit) {
            if (noEmit) {
                return 'copyEvent'
            }
            this.$emit('copyEvent')
        },
        getRowData() {
            this.currentRow = null
            if (this.row) {
                this.currentRow = this.row
                return
            }
            const pageComponent = this.preset_pageComponent
            if (pageComponent) {
                let slots = (this.showBtns && this.showBtns.length > 3 ? this.$children[0].$children[0].$children : this.$children) || []
                const visibleSlot = slots[0]
                if (visibleSlot) {
                    if (!visibleSlot.$listeners.click) return console.log('未设置click事件')
                    // 取到点击事件绑定的方法字符串，从中取到方法名
                    let clickFuncStr = visibleSlot.$listeners.click.fns.toString()
                    if (clickFuncStr.indexOf('row') === -1) {
                        let match = clickFuncStr.match(/return\s+\w+\.(\w+)\(/)
                        let funcName = match ? match[1] : null
                        const eventName = this[funcName](true)
                        if (!this.$listeners[eventName]) return console.error('请在使用组件的页面上绑定事件', eventName)
                        clickFuncStr = this.$listeners[eventName].fns.toString()
                    }

                    let match = clickFuncStr.match(/return\s+\w+\.(\w+)\(/)
                    let funcName = match ? match[1] : null
                    const clickFuncCache = pageComponent[funcName]
                    pageComponent[funcName] = currentRow => {
                        this.currentRow = currentRow
                    }
                    let element = visibleSlot.$el
                    let event = new Event('click')
                    element.dispatchEvent(event)
                    // 将原本的点击方法还原
                    pageComponent[funcName] = clickFuncCache
                }
            }
        },
        // preset_getGridMenuCustomBody事件的回调，通过传入的row和当前行row的特征进行判断，如果相同则返回当前行的操作按钮
        handleGetGridMenuCustomBody({clickRow, cb}) {
            this.getRowData()
            if (!this.currentRow || typeof this.currentRow !== 'object') return
            if ((clickRow.id && clickRow.id === this.currentRow.id) || (clickRow._X_ROW_KEY && clickRow._X_ROW_KEY === this.currentRow._X_ROW_KEY)) {
                let slots = (this.showBtns.length > 3 ? this.$children[0].$children[0].$children : this.$children) || []
                slots = slots.map(slot => {
                    let slotMap = {}
                    if (slot.$el && slot.$el.tagName === 'BUTTON') {
                        slotMap.code = Math.random().toString(36).substring(2, 12)
                        slotMap.name = slot.$el.innerText
                        slotMap.disabled = slot.$el.disabled
                        slotMap.clickMethod = slot.$listeners.click || (function(slot) {
                            return () => {
                                let element = slot.$el
                                let event = new Event('click')
                                element.dispatchEvent(event)
                            }
                        })(slot)
                    }

                    return slotMap
                })
                slots = slots.filter(slot => !!slot.code)
                cb(slots)
            }
        }
    },
    render() {
        let showBtnsLen = this.showBtns && this.showBtns.length || 0
        let slotsLen = 0
        if (this.showBtns) {
            if (this.$slots.begin) {
                slotsLen += this.$slots.begin.length
            }
            if (this.$slots.tail) {
                slotsLen += this.$slots.tail.length
            }
        } else {
            slotsLen = this.$slots.default && this.$slots.default.length || 0
        }
        const detailAuth = this.permissions.view ? this.$auth(this.permissions.view) : true
        const editAuth = this.permissions.edit ? this.$auth(this.permissions.edit) : true
        const copyAuth = this.permissions.add ? this.$auth(this.permissions.add) : true
        const delAuth = this.permissions.delete ? this.$auth(this.permissions.delete) : true
        const authAll = detailAuth && editAuth && copyAuth && delAuth
        return (
            <div class="operationActions">
                {this.showBtns && authAll &&
                    (showBtnsLen + slotsLen > this.maxButtons ?
                        <el-dropdown trigger="click" placement="bottom" class="operationDropdown">
                            <el-button type="text">...</el-button>
                            <el-dropdown-menu slot="dropdown" class="operationDropdownMenu">
                                {this.$slots.begin && this.$slots.begin.map(i => <el-dropdown-item>{i}</el-dropdown-item>)}
                                {detailAuth && <el-dropdown-item>{this.showBtns.includes('details') && <el-button size="small" type="text" onClick={() => this.detailsEvent()}>详情</el-button>}</el-dropdown-item>}
                                {editAuth && <el-dropdown-item>{this.showBtns.includes('update') && <el-button size="small" type="text" onClick={() => this.editEvent()}>修改</el-button>}</el-dropdown-item>}
                                {copyAuth && <el-dropdown-item>{this.showBtns.includes('copy') && <el-button size="small" type="text" onClick={() => this.copyEvent()}>复制</el-button>}</el-dropdown-item>}
                                {delAuth && <el-dropdown-item>{this.showBtns.includes('del') && <el-button size="small" type="text" on-click={() => this.deleteEvent()}>删除</el-button>}</el-dropdown-item>}
                                {this.$slots.tail && this.$slots.tail.map(i => <el-dropdown-item>{i}</el-dropdown-item>)}
                            </el-dropdown-menu>
                        </el-dropdown>
                        :
                        [
                            this.$slots.begin,
                            this.showBtns.includes('details') && detailAuth && <el-button class="re-el-button" size="small" onClick={() => this.detailsEvent()}>详情</el-button>,
                            this.showBtns.includes('update') && editAuth && <el-button class="re-el-button" size="small" on-click={() => this.editEvent()}>修改</el-button>,
                            this.showBtns.includes('copy') && copyAuth && <el-button class="re-el-button" size="small" onClick={() => this.copyEvent()}>复制</el-button>,
                            this.showBtns.includes('del') && delAuth && <el-button class="re-el-button" size="small" on-click={() => this.deleteEvent()}>删除</el-button>,
                            this.$slots.tail
                        ]
                    )
                }
                {!this.showBtns && slotsLen > 0 &&
                    (slotsLen > this.maxButtons ?
                        <div>
                            <el-dropdown trigger="click" placement="bottom" class="operationDropdown">
                                <el-button type="text">...</el-button>
                                <el-dropdown-menu slot="dropdown" class="operationDropdownMenu">
                                    {this.$slots.default && this.$slots.default.map(i => <el-dropdown-item>{i}</el-dropdown-item>)}
                                </el-dropdown-menu>
                            </el-dropdown>
                        </div>
                        :
                        this.$slots.default
                    )
                }
            </div>
        )
    }
}
</script>
<style lang="scss" scoped>
.operationDropdownMenu {
    ::v-deep {
        max-width: 135px;
        .el-button--text {
            font-size: 14px;
        }
        .el-dropdown-menu__item {
            padding: 0 !important;
        }
        .el-dropdown-menu__item .el-button {
            display: block;
            width: 100%;
            text-align: left;
            padding: 8px;
            font-size: 14px;
        }
    }
}
.operationActions {
    ::v-deep {
        .el-button--text {
            font-weight: 600;
        }
    }
}

</style>

