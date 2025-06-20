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

/**
 * Aiyatsbus 核心对象
 * 
 * 提供 Aiyatsbus 系统的全局访问点。
 * 负责管理 API 实例的注册和获取，确保系统正常运行。
 *
 * @author mical
 * @since 2024/2/17 15:31
 */
object Aiyatsbus {

    /** API 实例，在系统启动时注册 */
    var api: AiyatsbusAPI? = null

    /**
     * 获取 API 实例
     * 
     * @return AiyatsbusAPI 实例
     * @throws IllegalStateException 当 API 未加载或加载失败时抛出异常
     */
    fun api(): AiyatsbusAPI {
        return api ?: error("AiyatsbusAPI has not finished loading, or failed to load!")
    }

    /**
     * 注册 API 实例
     * 
     * @param api 要注册的 API 实例
     */
    fun register(api: AiyatsbusAPI) {
        Aiyatsbus.api = api
    }
}