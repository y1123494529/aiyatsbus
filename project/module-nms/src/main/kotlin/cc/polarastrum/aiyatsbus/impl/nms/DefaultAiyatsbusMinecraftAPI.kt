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
@file:Suppress("PrivatePropertyName", "SameParameterValue", "unused")

package cc.polarastrum.aiyatsbus.impl.nms

import cc.polarastrum.aiyatsbus.core.AiyatsbusMinecraftAPI
import cc.polarastrum.aiyatsbus.core.toDisplayMode
import cc.polarastrum.aiyatsbus.core.util.isNull
import cc.polarastrum.aiyatsbus.impl.nms.v12005_nms.NMS12005
import io.papermc.paper.adventure.PaperAdventure
import net.kyori.adventure.util.Codec
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.library.reflex.Reflex.Companion.getProperty
import taboolib.library.reflex.Reflex.Companion.setProperty
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.NMSItemTag
import java.io.IOException

/**
 * Aiyatsbus
 * com.mcstarrysky.aiyatsbus.impl.nms.DefaultAiyatsbusMinecraftAPI
 *
 * @author mical
 * @since 2024/2/18 00:21
 */
class DefaultAiyatsbusMinecraftAPI : AiyatsbusMinecraftAPI {

    init {
        // 预热
        if (MinecraftVersion.versionId >= 12005) {
            NMS12005.instance
        }
    }

    private val NBT_CODEC: Codec<Any, String, IOException, IOException> = PaperAdventure::class.java.getProperty("NBT_CODEC", isStatic = true)!!

    override fun getRepairCost(item: ItemStack): Int {
        return if (MinecraftVersion.isUniversal) {
            if (MinecraftVersion.versionId >= 12005) {
                NMS12005.instance.getRepairCost(item)
            } else {
                (NMSItemTag.asNMSCopy(item) as NMSItemStack).baseRepairCost
            }
        } else (NMSItemTag.asNMSCopy(item) as NMS16ItemStack).repairCost
    }

    override fun setRepairCost(item: ItemStack, cost: Int) {
        if (MinecraftVersion.isUniversal) {
            if (MinecraftVersion.versionId >= 12005) {
                NMS12005.instance.setRepairCost(item, cost)
            } else {
                (NMSItemTag.asNMSCopy(item) as NMSItemStack).setRepairCost(cost)
            }
        } else {
            (NMSItemTag.asNMSCopy(item) as NMS16ItemStack).repairCost = cost
        }
    }

    override fun createItemStack(material: String, tag: String?): ItemStack {
        return try {
            if (MinecraftVersion.versionId >= 11802) {
                Bukkit.getItemFactory().createItemStack(material + tag)
            } else {
                val mat = material.split(":")[1].uppercase()
                val bkItem = ItemStack(Material.valueOf(mat), 1)
                if (tag.isNullOrEmpty()) {
                    bkItem
                } else {
                    val nmsItem = NMSItemTag.asNMSCopy(bkItem)
                    if (MinecraftVersion.isUniversal) {
                        val nbt = NBT_CODEC.decode(tag) as NMSNBTTagCompound
                        (nmsItem as NMSItemStack).tag = nbt
                    } else {
                        val nbt = NBT_CODEC.decode(tag) as NMS16NBTTagCompound
                        (nmsItem as NMS16ItemStack).tag = nbt
                    }
                    NMSItemTag.asBukkitCopy(nmsItem)
                }
            }
        } catch (t: Throwable) {
            throw IllegalStateException(t)
        }
    }

    override fun adaptMerchantRecipe(merchantRecipeList: Any, player: Player) {

        if (MinecraftVersion.versionId >= 12005) {
            return NMS12005.instance.adaptMerchantRecipe(merchantRecipeList, player)
        }

        fun adapt(item: Any, player: Player): Any {
            val bkItem = NMSItemTag.asBukkitCopy(item)
            if (bkItem.isNull) return item
            return NMSItemTag.asNMSCopy(bkItem.toDisplayMode(player))
        }

        if (MinecraftVersion.isUniversal) {
            val previous = merchantRecipeList as NMSMerchantRecipeList
            for (i in 0 until previous.size) {
                with(previous[i]!!) {
                    setProperty("baseCostA", adapt(baseCostA, player) as NMSItemStack)
                    setProperty("costB", adapt(costB, player) as NMSItemStack)
                    setProperty("result", adapt(result, player) as NMSItemStack)
                }
            }
        } else {
            val previous = merchantRecipeList as NMS16MerchantRecipeList
            for (i in 0 until previous.size) {
                with (previous[i]!!) {
                    setProperty("buyingItem1", adapt(buyingItem1, player) as NMS16ItemStack)
                    setProperty("buyingItem2", adapt(buyingItem2, player) as NMS16ItemStack)
                    setProperty("sellingItem", adapt(sellingItem, player) as NMS16ItemStack)
                }
            }
        }
    }

    override fun componentFromJson(json: String): Any {
        return CraftChatMessage16.fromJSON(json)
    }

    override fun componentToJson(iChatBaseComponent: Any): String {
        return CraftChatMessage16.toJSON(iChatBaseComponent as NMS16IChatBaseComponent)
    }

    override fun breakBlock(player: Player, block: Block): Boolean {
        return if (MinecraftVersion.isUniversal) {
            (player as CraftPlayer20).handle.gameMode.destroyBlock(NMSBlockPosition(block.x, block.y, block.z))
        } else {
            (player as CraftPlayer16).handle.playerInteractManager.breakBlock(NMS16BlockPosition(block.x, block.y, block.z))
        }
    }

    override fun damageItemStack(item: ItemStack, amount: Int, entity: LivingEntity): ItemStack {
        var stack = item
        val nmsStack: Any = if (MinecraftVersion.isUniversal) {
            if (stack is CraftItemStack20) {
                val handle = stack.getProperty<NMSItemStack>("handle")
                if (handle == null || handle.isEmpty) {
                    return stack
                }
                handle
            } else {
                CraftItemStack20.asNMSCopy(stack).also {
                    stack = CraftItemStack20.asCraftMirror(it)
                }
            }
        } else {
            if (stack is CraftItemStack16) {
                val handle = stack.getProperty<NMS16ItemStack>("handle")
                if (handle == null || handle.isEmpty) {
                    return stack
                }
                handle
            } else {
                CraftItemStack16.asNMSCopy(stack).also {
                    stack = CraftItemStack16.asCraftMirror(it)
                }
            }
        }
        damageItemStack(nmsStack, amount, null, entity)
        return stack
    }

    /*
    /**
     * CraftLivingEntity&damageItemStack(EquipmentSlot, int)
     */
    fun damageItemStack(slot: EquipmentSlot, amount: Int, entity: LivingEntity) {
        if (MinecraftVersion.isUniversal) {
            val nmsSlot = CraftEquipmentSlot20.getNMS(slot)
            damageItemStack((entity as CraftLivingEntity20).handle.getItemBySlot(nmsSlot), amount, nmsSlot, entity)
        } else {
            val nmsSlot = CraftEquipmentSlot16.getNMS(slot)
            damageItemStack((entity as CraftLivingEntity16).handle.getEquipment(nmsSlot), amount, nmsSlot, entity)
        }
    }

     */

    /**
     * CraftLivingEntity#damageItemStack0
     */
    private fun damageItemStack(nmsStack: Any, amount: Int, enumItemSlot: Any?, entity: LivingEntity) {
        if (MinecraftVersion.isUniversal) {
            nmsStack as NMSItemStack
            // 1.20.4 -> hurtAndBreak(int, EntityLiving, Consumer<EntityLiving>)
            // 1.20.5, 1.21 -> hurtAndBreak(int, EntityLiving, EnumItemSlot), 自动广播事件
            if (MinecraftVersion.versionId >= 12005) {
                NMS12005.instance.hurtAndBreak(nmsStack, amount, entity)
            } else {
                nmsStack.hurtAndBreak(amount, (entity as CraftLivingEntity20).handle) { entityLiving ->
                    (enumItemSlot as? NMSEnumItemSlot)?.let { entityLiving.broadcastBreakEvent(it) }
                }
            }
        } else {
            nmsStack as NMS16ItemStack
            nmsStack.damage(amount, (entity as CraftLivingEntity16).handle) { entityLiving ->
                (enumItemSlot as? NMS16EnumItemSlot)?.let { entityLiving.broadcastItemBreak(it) }
            }
        }
    }
}