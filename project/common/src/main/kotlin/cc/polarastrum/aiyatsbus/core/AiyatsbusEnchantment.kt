package cc.polarastrum.aiyatsbus.core

import cc.polarastrum.aiyatsbus.core.data.registry.Rarity
import cc.polarastrum.aiyatsbus.core.data.registry.Target
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import java.io.File

/**
 * Aiyatsbus
 * cc.polarastrum.aiyatsbus.AiyatsbusEnchantment
 *
 * @author mical
 * @since 2025/6/20 18:33
 */
interface AiyatsbusEnchantment {

    /**
     * 附魔标识
     */
    val id: String

    /**
     * 附魔的 Key
     */
    val enchantmentKey: NamespacedKey

    /**
     * 附魔文件
     */
    val file: File

    /**
     * Bukkit 附魔实例, 在注册后赋值, 一般是 CraftEnchantment
     *
     * 在 1.20.2 及以下版本中, 这个是 LegacyCraftEnchantment
     * 在 1.20.2 以上版本中, Bukkit 更改了注册附魔的方式, 这个一般是 AiyatsbusCraftEnchantment
     */
    val enchantment: Enchantment

    /**
     * 附魔品质
     */
    val rarity: Rarity

    /**
     * 附魔对象
     */
    val targets: List<Target>
}