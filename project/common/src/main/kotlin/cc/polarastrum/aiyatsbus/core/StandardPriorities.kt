package cc.polarastrum.aiyatsbus.core

/**
 * 标准优先级常量
 * 
 * 定义了系统中各个组件的加载和执行优先级。
 * 数值越小优先级越高，确保依赖关系正确的加载顺序。
 *
 * @author mical
 * @since 2024/2/26 23:47
 */
object StandardPriorities {

    /** 稀有度优先级，最高优先级 */
    const val RARITY = 0
    /** 附魔目标优先级 */
    const val TARGET = 1
    /** 事件执行器优先级 */
    const val EVENT_EXECUTORS = 2
    /** 内置触发器优先级 */
    const val INTERNAL_TRIGGERS = 3
    /** 定时器优先级，与内置触发器同级 */
    const val TICKERS = 3
    /** 附魔效果优先级 */
    const val ENCHANTMENT = 4
    /** 附魔组优先级 */
    const val GROUP = 5
    /** 限制条件优先级 */
    const val LIMITATIONS = 6
    /** 玩家数据优先级 */
    const val PLAYER_DATA = 7
    /** 菜单系统优先级 */
    const val MENU = 8
    /** 反非法物品检测优先级，最低优先级 */
    const val ANTI_ILLEGAL_ITEM = 9

    /**
     * 获取数据属性优先级
     * 
     * 根据配置 ID 返回对应的优先级值。
     * 只支持品质、附魔组、附魔对象、内置触发器四种类型。
     * 
     * @param id 配置项 ID，不区分大小写
     * @return 对应的优先级值，如果 ID 不匹配则返回 -1
     */
    fun getDataProperty(id: String): Int {
        return when (id.lowercase()) {
            "rarity" -> RARITY
            "target" -> TARGET
            "internal_triggers" -> INTERNAL_TRIGGERS
            "group" -> GROUP
            else -> -1
        }
    }
}