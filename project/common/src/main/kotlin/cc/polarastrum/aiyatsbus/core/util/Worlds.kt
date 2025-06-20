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

import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.metadata.Metadatable
import org.bukkit.persistence.PersistentDataHolder
import org.bukkit.persistence.PersistentDataType
import taboolib.platform.util.bukkitPlugin
import taboolib.platform.util.removeMeta
import taboolib.platform.util.setMeta
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * 世界工具类
 * 
 * 提供世界相关的实用工具函数，包括时间判断、PDC 操作、元数据管理等。
 * 扩展了 Bukkit 的世界、位置、方块等类的功能。
 *
 * @author mical
 * @date 2024/8/27 17:23
 */

/**
 * 世界是否为白天
 * 
 * 根据世界时间判断是否为白天（时间在 0-12300 或 23850-24000 之间）
 */
val World.isDay: Boolean
    get() = time < 12300 || time > 23850

/**
 * 世界是否为黑夜
 * 
 * 根据世界时间判断是否为黑夜（时间在 12301-23849 之间）
 */
val World.isNight: Boolean
    get() = time in 12301..23849

/**
 * 从 PDC 获取内容
 * 
 * 使用操作符重载简化 PDC 数据获取
 * 
 * @param key 数据键名
 * @param type 数据类型
 * @param namespace 命名空间，默认为 "aiyatsbus"
 * @return 获取的数据，如果不存在则返回 null
 */
operator fun <T, Z> PersistentDataHolder.get(key: String, type: PersistentDataType<T, Z>, namespace: String = "aiyatsbus"): Z? {
    return persistentDataContainer.get(NamespacedKey(namespace, key), type)
}

/**
 * 向 PDC 设置内容
 * 
 * 使用操作符重载简化 PDC 数据设置
 * 
 * @param namespace 命名空间
 * @param key 数据键名
 * @param type 数据类型
 * @param value 要设置的值
 */
operator fun <T, Z : Any> PersistentDataHolder.set(namespace: String, key: String, type: PersistentDataType<T, Z>, value: Z) {
    persistentDataContainer.set(NamespacedKey(namespace, key), type, value)
}

/**
 * 向 PDC 设置内容（使用默认命名空间）
 * 
 * 使用操作符重载简化 PDC 数据设置，使用默认的 "aiyatsbus" 命名空间
 * 
 * @param key 数据键名
 * @param type 数据类型
 * @param value 要设置的值
 */
operator fun <T, Z : Any> PersistentDataHolder.set(key: String, type: PersistentDataType<T, Z>, value: Z) {
    set("aiyatsbus", key, type, value)
}

/**
 * 判断 PDC 是否包含某个键
 * 
 * @param key 数据键名
 * @param type 数据类型
 * @param namespace 命名空间，默认为 "aiyatsbus"
 * @return 如果包含该键则返回 true
 */
fun <T, Z : Any> PersistentDataHolder.has(key: String, type: PersistentDataType<T, Z>, namespace: String = "aiyatsbus"): Boolean {
    return persistentDataContainer.has(NamespacedKey(namespace, key), type)
}

/**
 * 从 PDC 移除内容
 * 
 * @param key 要移除的数据键名
 * @param namespace 命名空间，默认为 "aiyatsbus"
 */
fun PersistentDataHolder.remove(key: String, namespace: String = "aiyatsbus") {
    return persistentDataContainer.remove(NamespacedKey(namespace, key))
}

/**
 * 标记对象
 * 
 * 为对象设置元数据标记
 * 
 * @param key 标记键名
 */
fun Metadatable.mark(key: String) {
    setMeta(key, bukkitPlugin)
}

/**
 * 移除标记
 * 
 * 移除对象的元数据标记
 * 
 * @param key 要移除的标记键名
 */
fun Metadatable.unmark(key: String) {
    removeMeta(key)
}

/**
 * 将位置序列化为文本字符串
 * 
 * 将 Location 的世界名称和坐标信息序列化为 "世界名,X,Y,Z" 格式的字符串
 */
val Location.serialized get() = "${world.name},$blockX,$blockY,$blockZ"