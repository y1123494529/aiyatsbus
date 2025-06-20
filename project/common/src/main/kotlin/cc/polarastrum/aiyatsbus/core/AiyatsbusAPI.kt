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
package cc.polarastrum.aiyatsbus.core

import cc.polarastrum.aiyatsbus.core.registration.AiyatsbusEnchantmentRegisterer

/**
 * Aiyatsbus API 接口
 * 
 * 定义 Aiyatsbus 系统的核心 API 功能。
 * 提供附魔管理、注册和语言系统的访问接口。
 *
 * @author mical
 * @since 2024/2/17 15:31
 */
interface AiyatsbusAPI {

    /**
     * 获取附魔管理器
     * 
     * @return 附魔管理器实例，负责附魔的注册、加载和查询
     */
    fun getEnchantmentManager(): AiyatsbusEnchantmentManager

    /**
     * 获取附魔注册器
     * 
     * @return 附魔注册器实例，负责附魔的注册和注销
     */
    fun getEnchantmentRegisterer(): AiyatsbusEnchantmentRegisterer

    /**
     * 获取语言系统
     * 
     * @return 语言系统实例，负责多语言支持和文本管理
     */
    fun getLanguage(): AiyatsbusLanguage
}