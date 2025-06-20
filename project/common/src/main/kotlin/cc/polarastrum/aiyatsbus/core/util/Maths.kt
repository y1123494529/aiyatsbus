/*
 *  Copyright (C) 2022-2024 PolarAstrumLab
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package cc.polarastrum.aiyatsbus.core.util

import redempt.crunch.CompiledExpression
import redempt.crunch.Crunch
import redempt.crunch.functional.EvaluationEnvironment
import redempt.crunch.functional.Function
import taboolib.common.platform.function.warning
import taboolib.common.util.random
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * 数学工具类
 * 
 * 提供数学计算、表达式解析、罗马数字转换等功能。
 * 基于 Crunch 库实现高性能的数学表达式计算。
 *
 * @author HamsterYDS
 * @since 2024/2/17 23:15
 */

/** 罗马数字单位数组 */
private val romanUnits = intArrayOf(1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1)
/** 罗马数字符号数组 */
private val romanSymbols = arrayOf("M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I")

/**
 * 将数字转换为罗马数字
 * 
 * @param simplified 是否简化模式，为 true 时数字 1 返回空字符串
 * @param blank 是否在结果前添加空格
 * @return 转换后的罗马数字字符串
 */
fun Int.roman(simplified: Boolean = false, blank: Boolean = false): String {
    if ((this == 1 && simplified) || this !in 1..3999) return ""
    var number = this
    val roman = StringBuilder()
    for (i in romanUnits.indices)
        while (number >= romanUnits[i]) {
            roman.append(romanSymbols[i])
            number -= romanUnits[i]
        }
    return if (blank) " $roman" else "$roman"
}

/**
 * 判断一个浮点数是否为整数
 * 
 * 例如：6.0 就是整数
 * 
 * @return true 表示是整数，false 表示是小数
 */
fun Double.isInteger(): Boolean {
    return this == toInt().toDouble()
}

/**
 * 数学工具对象
 * 
 * 提供表达式计算、权重选择等数学功能
 */
object MathUtils {

    /** 最小值函数 */
    private val min = Function("min", 2) {
        min(it[0], it[1])
    }

    /** 最大值函数 */
    private val max = Function("max", 2) {
        max(it[0], it[1])
    }

    /** 随机数函数 */
    private val rand = Function("random", 2) {
        random(it[0], it[1])
    }

    /** 表达式缓存，提高计算性能 */
    private val cache = mutableMapOf<String, CompiledExpression?>()

    /**
     * 预热表达式
     * 
     * 预编译表达式以提高后续计算性能
     * 
     * @return 原表达式字符串
     */
    fun String.preheatExpression(): String {
        val expression = replaceVariable()
        val variables = extractVariableNames()

        val env = EvaluationEnvironment()
        env.setVariableNames(*variables.toTypedArray())
        env.addFunctions(rand, min, max)
        val compiled = runCatching { Crunch.compileExpression(expression, env) }.getOrElse {
            error("compiling", this, variables, doubleArrayOf(), it)
            null
        }

        if (compiled != null) {
            cache[expression] = compiled
        }

        return this
    }

    /**
     * 计算表达式
     * 
     * 使用提供的变量值计算数学表达式
     * 
     * @param holders 变量名和值的键值对列表
     * @return 计算结果
     */
    fun String.calculate(holders: List<Pair<String, Any>>): Double {
        val expression = replaceVariable()
        val variables = extractVariableNames()

        val pairMap = holders.toMap()
        val values = variables.map { pairMap[it] }
            .map { it.toString().toDoubleOrNull() ?: 0.0 }
            .toDoubleArray()

        val compiled = cache.getOrPut(expression) {
            val env = EvaluationEnvironment()
            env.setVariableNames(*variables.toTypedArray())
            env.addFunctions(rand, min, max)
            runCatching { Crunch.compileExpression(expression, env) }.getOrElse {
                error("compiling", this, variables, values, it)
                null
            }
        }

        return runCatching { compiled?.evaluate(*values) }.getOrElse {
            error("evaluating", this, variables, values, it)
            0.0
        } ?: 0.0
    }

    /**
     * 按权重选择元素
     * 
     * 根据权重随机选择一个元素
     * 
     * @param random 随机数生成器
     * @return 选中的元素，如果没有有效权重则返回 null
     */
    fun <T> Map<T, Int>.selectByWeight(random: Random): T? {
        val total = values.sum()
        if (total <= 0) return null

        var accumulator = 0
        val target = random.nextInt(total)
        return entries.firstOrNull { entry ->
            accumulator += entry.value
            accumulator > target
        }?.key
    }

    /**
     * 错误处理
     * 
     * 输出表达式计算过程中的错误信息
     * 
     * @param action 操作类型（编译或计算）
     * @param expression 表达式字符串
     * @param variables 变量列表
     * @param values 变量值数组
     * @param error 错误对象
     */
    private fun error(action: String, expression: String, variables: List<String>, values: DoubleArray, error: Throwable) {
        warning("Error occurred while $action expression!")
        warning("|- Expression: $expression")
        warning("|- Variables: $variables")
        warning("|- Values: ${values.toList()}")
        warning("|- Error: $error")
    }

    /** 变量匹配正则表达式 */
    private val variableRegex = "\\{([^}]+)}".toRegex()

    /**
     * 提取表达式中的变量名
     * 
     * @return 变量名列表
     */
    private fun String.extractVariableNames(): List<String> {
        return (variableRegex.findAll(this).map { it.groupValues[1] }.toList())
    }

    /**
     * 替换变量格式
     * 
     * 将 {变量名} 格式替换为 $变量名 格式
     * 
     * @return 替换后的表达式
     */
    private fun String.replaceVariable(): String = replace(variableRegex, "\$1")
}