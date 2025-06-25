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
package cc.polarastrum.aiyatsbus.core.script

import org.bukkit.command.CommandSender
import java.util.concurrent.CompletableFuture

/**
 * Aiyatsbus
 * cc.polarastrum.aiyatsbus.core.script.ScriptHandler
 *
 * @author mical
 * @since 2025/6/22 13:16
 */
interface ScriptHandler {

    fun invoke(source: String, sender: CommandSender?, variables: Map<String, Any?> = emptyMap()): CompletableFuture<Any?>?

    fun invoke(source: List<String>, sender: CommandSender?, variables: Map<String, Any?> = emptyMap()): CompletableFuture<Any?>?

    fun preheat(source: String)

    fun preheat(source: List<String>)
}