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
package cc.polarastrum.aiyatsbus.impl

import cc.polarastrum.aiyatsbus.core.*
import cc.polarastrum.aiyatsbus.core.registration.AiyatsbusEnchantmentRegisterer
import cc.polarastrum.aiyatsbus.core.registration.modern.ModernEnchantmentRegisterer
import cc.polarastrum.aiyatsbus.impl.registration.legacy.DefaultLegacyEnchantmentRegisterer
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.function.registerLifeCycleTask
import taboolib.common.util.replaceWithOrder
import taboolib.common.util.t
import taboolib.module.nms.MinecraftVersion.versionId
import taboolib.module.nms.nmsProxy

/**
 * Aiyatsbus
 * com.mcstarrysky.aiyatsbus.impl.DefaultAiyatsbusAPI
 *
 * @author mical
 * @since 2024/2/17 16:20
 */
class DefaultAiyatsbusAPI : AiyatsbusAPI {

    private val enchantmentManager = PlatformFactory.getAPI<AiyatsbusEnchantmentManager>()

    private val language = PlatformFactory.getAPI<AiyatsbusLanguage>()

    private lateinit var enchantmentRegisterer: AiyatsbusEnchantmentRegisterer

    override fun getLanguage(): AiyatsbusLanguage {
        return language
    }

    override fun getEnchantmentManager(): AiyatsbusEnchantmentManager {
        return enchantmentManager
    }

    override fun getEnchantmentRegisterer(): AiyatsbusEnchantmentRegisterer {
        if (::enchantmentRegisterer.isInitialized) {
            enchantmentRegisterer = registerer
        }
        return registerer
    }

    companion object {

        private const val PACKAGE = "cc.polarastrum.aiyatsbus.impl.registration.v{0}_nms.DefaultModernEnchantmentRegisterer"

        lateinit var registerer: AiyatsbusEnchantmentRegisterer

        @Awake(LifeCycle.CONST)
        fun init() {
            registerer = when {
                versionId >= 12104 -> modern(12104)
                versionId >= 12102 -> modern(12103)
                versionId >= 12100 -> modern(12100)
                versionId >= 12005 -> error("""
                    Aiyatsbus 不支持 Minecraft 1.20.5 或 1.20.6。
                    Aiyatsbus doesn't support Minecraft 1.20.5 or 1.20.6.
                """.t())
                versionId >= 12003 -> modern(12004)
                else -> DefaultLegacyEnchantmentRegisterer
            }
        }

        private inline fun <reified T> proxy(bind: String, vararg parameter: Any): T {
            val time = System.currentTimeMillis()
            val proxy = nmsProxy(T::class.java, bind, *parameter)
            val cost = System.currentTimeMillis() - time
            println("""
            [Aiyatsbus] 代理类 ${T::class.java.simpleName} 已生成，用时 $cost 毫秒。
            [Aiyatsbus] Generated ${T::class.java.simpleName} in ${System.currentTimeMillis() - time}ms
        """.t())
            return proxy
        }

        private fun modern(versionId: Int): ModernEnchantmentRegisterer {
            return proxy(PACKAGE.replaceWithOrder(versionId))
        }
    }
}