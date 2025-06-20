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

import com.google.gson.JsonParser
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import taboolib.module.chat.ComponentText
import taboolib.module.chat.component

/**
 * 文本组件工具类
 * 
 * 提供文本格式化和组件转换功能。
 * 支持旧版文本格式转换、复合文本构建等操作。
 *
 * @author mical
 * @since 2024/2/17 17:07
 */

/**
 * 旧版 JSON 解析器
 * 
 * 旧版 Gson 没有 parseString 静态方法，使用实例方法
 */
val JSON_PARSER = JsonParser()

/**
 * 旧版文本序列化器
 * 
 * 配置了颜色代码、十六进制颜色等功能的序列化器
 */
val LEGACY_COMPONENT_SERIALIZER = LegacyComponentSerializer.builder()
    .character('\u00a7')
    .useUnusualXRepeatedCharacterHexFormat()
    .hexColors()
    .build()

/**
 * 将旧版文本转换为 Adventure Component
 * 
 * 支持 &a 等颜色代码格式的文本转换
 * 
 * @return 转换后的 Adventure Component
 */
fun String.legacyToAdventure(): Component {
    return LEGACY_COMPONENT_SERIALIZER.deserialize(this)
}

/**
 * 将字符串列表构建为复合文本并上色
 * 
 * @return 构建后的复合文本列表，如果原列表为 null 则返回空列表
 */
fun List<String>?.toBuiltComponent(): List<ComponentText> {
    return this?.map { it.component().buildColored() } ?: emptyList()
}