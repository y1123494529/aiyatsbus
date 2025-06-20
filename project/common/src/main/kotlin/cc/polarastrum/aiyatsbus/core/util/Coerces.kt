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

import taboolib.common5.Coerce

/**
 * 类型转换工具类
 * 
 * 提供安全的类型转换功能，支持默认值处理。
 * 基于 TabooLib 的 Coerce 工具，提供更便捷的转换方法。
 *
 * @author Lanscarlos
 * @since 2022-02-27 16:45
 */

/**
 * 转换为布尔值
 * 
 * @param def 默认值，当原值为 null 时使用
 * @return 转换后的布尔值
 */
fun Any?.coerceBoolean(def: Boolean? = null): Boolean {
    if (this == null && def != null) return def
    return Coerce.toBoolean(this)
}

/**
 * 转换为短整型
 * 
 * @param def 默认值，当原值为 null 时使用
 * @return 转换后的短整型值
 */
fun Any?.coerceShort(def: Short? = null): Short {
    if (this == null && def != null) return def
    return Coerce.toShort(this)
}

/**
 * 转换为整型
 * 
 * @param def 默认值，当原值为 null 时使用
 * @return 转换后的整型值
 */
fun Any?.coerceInt(def: Int? = null): Int {
    if (this == null && def != null) return def
    return Coerce.toInteger(this)
}

/**
 * 转换为长整型
 * 
 * @param def 默认值，当原值为 null 时使用
 * @return 转换后的长整型值
 */
fun Any?.coerceLong(def: Long? = null): Long {
    if (this == null && def != null) return def
    return Coerce.toLong(this)
}

/**
 * 转换为单精度浮点数
 * 
 * @param def 默认值，当原值为 null 时使用
 * @return 转换后的单精度浮点数值
 */
fun Any?.coerceFloat(def: Float? = null): Float {
    if (this == null && def != null) return def
    return Coerce.toFloat(this)
}

/**
 * 转换为双精度浮点数
 * 
 * @param def 默认值，当原值为 null 时使用
 * @return 转换后的双精度浮点数值
 */
fun Any?.coerceDouble(def: Double? = null): Double {
    if (this == null && def != null) return def
    return Coerce.toDouble(this)
}

/**
 * 转换为列表
 * 
 * @param def 默认列表，当转换失败时使用
 * @param transfer 转换函数，将元素转换为目标类型
 * @return 转换后的列表
 */
inline fun <reified T> Any?.coerceList(def: List<T> = emptyList(), transfer: (Any?) -> T): List<T> {
    return when (this) {
        is T -> listOf(this)
        is Array<*> -> this.map(transfer)
        is Collection<*> -> this.map(transfer)
        else -> def
    }
}

/**
 * 转换为非空列表
 * 
 * @param def 默认列表，当转换失败时使用
 * @param transfer 转换函数，将元素转换为目标类型，返回 null 表示过滤该元素
 * @return 转换后的非空列表
 */
inline fun <reified T: Any> Any?.coerceListNotNull(def: List<T> = emptyList(), transfer: (Any?) -> T?): List<T> {
    return when (this) {
        is T -> listOf(this)
        is Array<*> -> this.mapNotNull(transfer)
        is Collection<*> -> this.mapNotNull(transfer)
        else -> def
    }
}