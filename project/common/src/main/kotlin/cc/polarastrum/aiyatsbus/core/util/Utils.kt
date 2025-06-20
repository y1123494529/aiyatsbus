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

import com.google.common.base.Enums
import com.google.gson.Gson
import taboolib.library.reflex.Reflex.Companion.invokeMethod
import taboolib.library.reflex.ReflexClass
import taboolib.library.reflex.UnsafeAccess
import java.lang.reflect.Field

/**
 * 通用工具类
 * 
 * 提供各种通用工具方法，包括枚举转换、反射操作、JSON 验证等。
 *
 * @author mical
 * @since 2024/2/17 17:07
 */

/** Gson 实例，用于 JSON 操作 */
val GSON = Gson()

/**
 * 字符串转枚举
 * 
 * @param transfer 转换函数，默认将字符串转为大写
 * @return 对应的枚举值，如果不存在则返回 null
 */
inline fun <reified T : Enum<T>> String?.enumOf(transfer: (String) -> String = { it.uppercase() }): T? {
    return if (this == null) null else Enums.getIfPresent(T::class.java, transfer(this)).orNull()
}

/**
 * 深度调用方法
 * 
 * 支持通过路径调用嵌套方法，如 "getA/getB/getC"
 * 
 * @param name 方法路径，用 "/" 分隔
 * @return 调用结果，如果调用失败则返回 null
 */
@Suppress("UNCHECKED_CAST")
fun <T> Any?.invokeMethodDeep(name: String): T? {
    var result: Any? = this
    for (method in name.split('/')) {
        result = result?.invokeMethod(method)
    }
    return result as? T
}

/**
 * 获取反射类的包名
 * 
 * @return 包名字符串，如果类名为 null 则返回 null
 */
fun ReflexClass.packageName(): String? {
    return name?.substringBeforeLast('.')
}

/**
 * 验证字符串是否为有效的 JSON
 * 
 * @return true 表示是有效的 JSON，false 表示无效
 */
fun String.isValidJson(): Boolean {
    if (trim().isEmpty() || !startsWith("{")) return false
    return kotlin.runCatching {
        GSON.fromJson(this, Any::class.java)
    }.isSuccess
}

/**
 * 设置 static final 字段的值
 * 
 * 使用 Unsafe 操作直接修改 final 字段的值
 * 
 * @param value 要设置的新值
 */
fun Field.setStaticFinal(value: Any) {
    val offset = UnsafeAccess.unsafe.staticFieldOffset(this)
    UnsafeAccess.unsafe.putObject(UnsafeAccess.unsafe.staticFieldBase(this), offset, value)
}

/**
 * 检查 ItemsAdder 插件是否存在
 * 
 * 通过尝试加载 ItemsAdder 类来判断插件是否已安装
 */
internal val itemsAdderEnabled = runCatching { Class.forName("dev.lone.itemsadder.api.ItemsAdder") }.isSuccess