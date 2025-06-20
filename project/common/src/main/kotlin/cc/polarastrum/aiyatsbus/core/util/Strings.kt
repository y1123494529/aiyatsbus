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

import cc.polarastrum.aiyatsbus.core.util.MathUtils.calculate
import org.bukkit.entity.Player
import taboolib.platform.compat.replacePlaceholder
import kotlin.math.roundToInt

/**
 * 字符串工具类
 * 
 * 提供字符串处理的实用工具函数，包括占位符替换、变量计算、数学表达式处理等。
 * 扩展了 String 类的功能，提供更便捷的字符串操作方法。
 *
 * @author HamsterYDS
 * @since 2024/2/17 22:53
 */

/**
 * 替换玩家占位符
 * 
 * 如果玩家不为 null，则使用 TabooLib 的占位符替换功能处理字符串
 * 
 * @param player 玩家对象，可能为 null
 * @return 替换后的字符串
 */
fun String.replacePlaceholder(player: Player?): String {
    return if (player != null) replacePlaceholder(player) else this
}

/**
 * 替换字符串中的占位符
 * 
 * 使用键值对列表替换字符串中的占位符
 * 
 * @param holders 占位符和值的键值对列表
 * @param tagged 是否使用标签格式（默认 true，使用 {key} 格式）
 * @return 替换后的字符串
 */
fun String.replace(holders: List<Pair<String, Any>>, tagged: Boolean = true): String {
    var tmp = this
    holders.forEach { (holder, value) -> tmp = tmp.replace(if (tagged) "{$holder}" else holder, "$value") }
    return tmp
}

/**
 * 替换字符串中的占位符（使用 Map）
 * 
 * 使用 Map 对象替换字符串中的占位符
 * 
 * @param holders 占位符和值的映射
 * @param tagged 是否使用标签格式（默认 true，使用 {key} 格式）
 * @return 替换后的字符串
 */
fun String.replace(holders: Map<String, Any>, tagged: Boolean = true): String = replace(holders.toList(), tagged)

/**
 * 替换字符串中的占位符（使用可变参数）
 * 
 * 使用可变参数形式的键值对替换字符串中的占位符
 * 
 * @param holders 占位符和值的键值对（可变参数）
 * @param tagged 是否使用标签格式（默认 true，使用 {key} 格式）
 * @return 替换后的字符串
 */
fun String.replace(vararg holders: Pair<String, Any>, tagged: Boolean = true): String = replace(holders.toList(), tagged)

/**
 * 计算字符串中的数学表达式
 * 
 * 将字符串中的变量替换为实际值，然后计算数学表达式
 * 
 * @param holders 变量名和值的键值对（可变参数）
 * @return 计算结果的字符串表示
 */
fun String.calculate(vararg holders: Pair<String, Any>): String {
    val result = calcToDouble(*holders)
    return if (result.isInteger()) result.toInt().toString() else result.toString()
}

/**
 * 计算字符串中的数学表达式并返回双精度浮点数
 * 
 * 将字符串中的变量替换为实际值，然后计算数学表达式
 * 
 * @param holders 变量名和值的键值对（可变参数）
 * @return 计算结果的双精度浮点数值
 */
fun String.calcToDouble(vararg holders: Pair<String, Any>): Double = calculate(holders.toList())

/**
 * 计算字符串中的数学表达式并返回整数
 * 
 * 将字符串中的变量替换为实际值，然后计算数学表达式并四舍五入为整数
 * 
 * @param holders 变量名和值的键值对（可变参数）
 * @return 计算结果的整数值
 */
fun String.calcToInt(vararg holders: Pair<String, Any>): Int = calcToDouble(*holders).roundToInt()