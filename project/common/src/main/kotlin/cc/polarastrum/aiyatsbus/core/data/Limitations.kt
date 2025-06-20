package cc.polarastrum.aiyatsbus.core.data

import cc.polarastrum.aiyatsbus.core.*
import cc.polarastrum.aiyatsbus.core.data.LimitType.*
import cc.polarastrum.aiyatsbus.core.util.coerceBoolean
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.registerLifeCycleTask
import taboolib.module.kether.compileToJexl
import taboolib.platform.compat.replacePlaceholder

/**
 * 附魔限制管理类
 * 
 * 负责管理附魔的各种限制条件，包括冲突检查、依赖检查、权限检查等。
 * 支持多种检查类型：获取、商人交易、铁砧合成、使用等。
 * 
 * @param belonging 所属的附魔
 * @param lines 限制配置行列表
 * @author mical
 * @since 2024/2/18 10:15
 */
data class Limitations(
    private val belonging: AiyatsbusEnchantment,
    private val lines: List<String>
) {

    /** 是否与所有附魔都冲突 */
    var conflictsWithEverything: Boolean = false

    /** 限制条件集合 */
    val limitations = buildLimitations().toMutableSet()

    /**
     * 构建限制条件集合
     * 
     * @return 限制条件集合
     */
    private fun buildLimitations(): Set<Pair<LimitType, String>> {
        val result = mutableSetOf<Pair<LimitType, String>>()
        
        // 解析配置行
        lines.forEach { line ->
            val parts = line.split(":", limit = 2)
            if (parts.size < 2) return@forEach
            
            val type = try {
                LimitType.valueOf(parts[0])
            } catch (e: IllegalArgumentException) {
                return@forEach
            }
            
            val value = parts[1]
            
            when (type) {
                CONFLICT_ENCHANT -> {
                    if (value == "*") {
                        conflictsWithEverything = true
                    } else {
                        conflicts += belonging.basicData.name to value
                    }
                }
                CONFLICT_GROUP -> {
                    conflictGroups += belonging.basicData.name to value
                    result += type to value
                }
                else -> result += type to value
            }
        }
        
        // 添加默认限制
        result += MAX_CAPABILITY to ""
        result += TARGET to ""
        result += DISABLE_WORLD to ""
        result += SLOT to ""
        
        return result
    }

    /**
     * 检查操作是否被允许
     * 
     * 检查附魔是否可以应用到物品上、使用时是否可以生效、村民生成新交易等。
     * 
     * @param checkType 检查类型
     * @param item 相关物品（如正在被附魔的书、正在使用的剑等）
     * @param creature 生物实体（通常是玩家）
     * @param slot 装备槽位
     * @param ignoreSlot 是否忽略槽位检查（如烙印诅咒等）
     * @return 检查结果
     */
    fun checkAvailable(
        checkType: CheckType,
        item: ItemStack,
        creature: LivingEntity? = null,
        slot: EquipmentSlot? = null,
        ignoreSlot: Boolean = false
    ): CheckResult {
        return checkAvailable(checkType.limitTypes, item, checkType == CheckType.USE, creature, slot, ignoreSlot)
    }

    /**
     * 检查操作是否被允许（重载方法）
     * 
     * @param limits 要检查的限制类型集合
     * @param item 相关物品
     * @param use 是否为使用操作
     * @param creature 生物实体
     * @param slot 装备槽位
     * @param ignoreSlot 是否忽略槽位检查
     * @return 检查结果
     */
    fun checkAvailable(
        limits: Collection<LimitType>,
        item: ItemStack,
        use: Boolean = false,
        creature: LivingEntity? = null,
        slot: EquipmentSlot? = null,
        ignoreSlot: Boolean = false
    ): CheckResult {
        // 获取语言发送者
        val sender = creature as? Player ?: Bukkit.getConsoleSender()

        // 检查附魔是否启用
        if (!belonging.basicData.enable) {
            return CheckResult.Failed(sender.asLang("limitations-not-enable"))
        }

        // 检查所有相关限制
        for ((type, value) in limitations) {
            if (type !in limits) continue
            
            val result = when (type) {
                PAPI_EXPRESSION -> checkPapiExpression(value, creature)
                PERMISSION -> checkPermission(value, creature)
                DISABLE_WORLD -> checkDisableWorld(creature)
                else -> checkItem(type, item, value, slot, use, ignoreSlot)
            }
            
            if (!result) {
                return CheckResult.Failed(
                    sender.asLang(
                        "limitations-check-failed",
                        sender.asLang("limitations-typename-${type.name.lowercase()}") to "typename"
                    )
                )
            }
        }

        return CheckResult.Successful
    }

    /**
     * 检查 PAPI 表达式
     * 
     * @param expression 表达式
     * @param creature 生物实体
     * @return 检查结果
     */
    private fun checkPapiExpression(expression: String, creature: LivingEntity?): Boolean {
        if (creature !is Player) return true
        return try {
            expression.replacePlaceholder(creature).compileToJexl().eval().coerceBoolean()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 检查权限
     * 
     * @param permission 权限节点
     * @param creature 生物实体
     * @return 检查结果
     */
    private fun checkPermission(permission: String, creature: LivingEntity?): Boolean {
        return creature?.hasPermission(permission) ?: true
    }

    /**
     * 检查禁用世界
     * 
     * @param creature 生物实体
     * @return 检查结果
     */
    private fun checkDisableWorld(creature: LivingEntity?): Boolean {
        return creature?.world?.name !in belonging.basicData.disableWorlds
    }

    /**
     * 检查物品相关限制
     * 
     * @param type 限制类型
     * @param item 物品
     * @param value 限制值
     * @param slot 装备槽位
     * @param use 是否为使用操作
     * @param ignoreSlot 是否忽略槽位检查
     * @return 检查结果
     */
    private fun checkItem(
        type: LimitType, 
        item: ItemStack, 
        value: String, 
        slot: EquipmentSlot?, 
        use: Boolean, 
        ignoreSlot: Boolean
    ): Boolean {
        val itemType = item.type
        val enchants = item.fixedEnchants
        
        return when (type) {
            SLOT -> checkSlot(itemType, slot, ignoreSlot)
            TARGET -> checkTarget(itemType, use)
            MAX_CAPABILITY -> checkMaxCapability(itemType, enchants)
            DEPENDENCE_ENCHANT -> checkDependenceEnchant(value, enchants)
            CONFLICT_ENCHANT -> checkConflictEnchant(value, enchants)
            DEPENDENCE_GROUP -> checkDependenceGroup(value, enchants)
            CONFLICT_GROUP -> checkConflictGroup(value, enchants)
            else -> true
        }
    }

    /**
     * 检查槽位限制
     */
    private fun checkSlot(itemType: Material, slot: EquipmentSlot?, ignoreSlot: Boolean): Boolean {
        if (slot == null) return ignoreSlot
        return belonging.targets.find { itemType.isInTarget(it) }?.activeSlots?.contains(slot) ?: false
    }

    /**
     * 检查目标限制
     */
    private fun checkTarget(itemType: Material, use: Boolean): Boolean {
        return belonging.targets.any { itemType.isInTarget(it) } || 
               (!use && (itemType == Material.BOOK || itemType == Material.ENCHANTED_BOOK))
    }

    /**
     * 检查最大附魔数量限制
     */
    private fun checkMaxCapability(itemType: Material, enchants: Map<AiyatsbusEnchantment, Int>): Boolean {
        return itemType.capability > enchants.size
    }

    /**
     * 检查依赖附魔
     */
    private fun checkDependenceEnchant(value: String, enchants: Map<AiyatsbusEnchantment, Int>): Boolean {
        return enchants.containsKey(aiyatsbusEt(value))
    }

    /**
     * 检查冲突附魔
     */
    private fun checkConflictEnchant(value: String, enchants: Map<AiyatsbusEnchantment, Int>): Boolean {
        return !enchants.containsKey(aiyatsbusEt(value))
    }

    /**
     * 检查依赖附魔组
     */
    private fun checkDependenceGroup(value: String, enchants: Map<AiyatsbusEnchantment, Int>): Boolean {
        return enchants.any { (enchant, _) -> 
            enchant.enchantment.isInGroup(value) && enchant.enchantmentKey != belonging.enchantmentKey 
        }
    }

    /**
     * 检查冲突附魔组
     */
    private fun checkConflictGroup(value: String, enchants: Map<AiyatsbusEnchantment, Int>): Boolean {
        val group = aiyatsbusGroup(value)
        val maxCoexist = group?.maxCoexist ?: 10000
        val conflictCount = enchants.count { (enchant, _) -> 
            enchant.enchantment.isInGroup(value) && enchant.enchantmentKey != belonging.enchantmentKey 
        }
        return conflictCount < maxCoexist
    }

    /**
     * 检查是否与指定附魔冲突
     * 
     * @param other 要检查的附魔
     * @return 是否冲突
     */
    fun conflictsWith(other: Enchantment): Boolean {
        // 检查全局冲突
        if (conflictsWithEverything || other.aiyatsbusEt.limitations.conflictsWithEverything) {
            return true
        }
        
        // 检查具体冲突
        return limitations.any { (type, value) ->
            when (type) {
                CONFLICT_ENCHANT -> other.key == aiyatsbusEt(value)?.enchantmentKey
                CONFLICT_GROUP -> other.isInGroup(value)
                else -> false
            }
        }
    }

    companion object {

        /** 记录单向附魔冲突，开服后自动挂双向 */
        private val conflicts = mutableListOf<Pair<String, String>>()

        /** 记录单向附魔组冲突，开服后为附魔组的每一个此附魔添加此冲突附魔 */
        private val conflictGroups = mutableListOf<Pair<String, String>>()

        /**
         * 插件启用时的初始化
         */
        @Awake(LifeCycle.LOAD)
        fun onEnable() {
            registerLifeCycleTask(LifeCycle.ENABLE, StandardPriorities.LIMITATIONS) {
                // 处理附魔组冲突
                conflictGroups.forEach { (enchant, group) ->
                    aiyatsbusEt(enchant) ?: return@forEach
                    aiyatsbusGroup(group)?.enchantments?.forEach { 
                        it.limitations.limitations.add(CONFLICT_ENCHANT to enchant)
                    }
                }
                conflictGroups.clear()

                // 处理附魔冲突
                conflicts.forEach { (a, b) ->
                    val etA = aiyatsbusEt(a) ?: return@forEach
                    val etB = aiyatsbusEt(b) ?: return@forEach
                    etA.limitations.limitations.add(CONFLICT_ENCHANT to b)
                    etB.limitations.limitations.add(CONFLICT_ENCHANT to a)
                }
                conflicts.clear()
            }
        }
    }
}

/**
 * 检查结果密封类
 * 
 * @param isSuccess 是否成功
 * @param reason 失败原因
 */
sealed class CheckResult(val isSuccess: Boolean, val reason: String) {

    /** 是否失败 */
    val isFailure: Boolean = !isSuccess

    /** 成功结果 */
    object Successful : CheckResult(true, "")

    /** 失败结果 */
    class Failed(reason: String) : CheckResult(false, reason)
}

/**
 * 检查类型枚举
 * 
 * 定义了不同的检查场景和对应的限制类型
 */
enum class CheckType(vararg types: LimitType) {

    /** 从战利品/附魔台中获得附魔物品时 */
    ATTAIN(
        CONFLICT_GROUP,
        CONFLICT_ENCHANT,
        DEPENDENCE_GROUP,
        DEPENDENCE_ENCHANT,
        MAX_CAPABILITY,
        TARGET,
    ),
    
    /** 生成村民交易中的附魔时 */
    MERCHANT(
        CONFLICT_GROUP,
        CONFLICT_ENCHANT,
        DEPENDENCE_GROUP,
        DEPENDENCE_ENCHANT,
        MAX_CAPABILITY,
        TARGET
    ),
    
    /** 进行铁砧拼合物品附魔时 */
    ANVIL(
        CONFLICT_GROUP,
        CONFLICT_ENCHANT,
        DEPENDENCE_GROUP,
        DEPENDENCE_ENCHANT,
        MAX_CAPABILITY,
        TARGET
    ),
    
    /** 使用物品上的附魔时 */
    USE(
        PAPI_EXPRESSION,
        PERMISSION,
        DISABLE_WORLD,
        TARGET,
        SLOT
    );

    /** 该检查类型对应的限制类型集合 */
    val limitTypes = setOf(*types)
}

/**
 * 限制类型枚举
 * 
 * 定义了各种限制条件的类型
 */
enum class LimitType {

    /** 目标限制，如锋利只能应用于斧、剑 */
    TARGET,
    
    /** 最大附魔数量限制，如一把剑最多有 12 个附魔（默认） */
    MAX_CAPABILITY,
    
    /** PAPI 表达式限制，如 %player_level%>=30 */
    PAPI_EXPRESSION,
    
    /** 权限限制，如 aiyatsbus.use */
    PERMISSION,
    
    /** 冲突附魔限制，如锋利 */
    CONFLICT_ENCHANT,
    
    /** 冲突附魔组限制，如 "PVE类附魔" */
    CONFLICT_GROUP,
    
    /** 依赖附魔限制，如无限 */
    DEPENDENCE_ENCHANT,
    
    /** 依赖附魔组限制，如 "保护类附魔" */
    DEPENDENCE_GROUP,
    
    /** 禁用世界限制，如 world_the_end */
    DISABLE_WORLD,
    
    /** 装备槽位限制，如 HAND */
    SLOT
}