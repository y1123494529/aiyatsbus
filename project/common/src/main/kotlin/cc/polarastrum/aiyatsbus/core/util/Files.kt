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

import taboolib.common5.FileWatcher
import java.io.File

/**
 * 文件操作工具类
 * 
 * 提供文件读取、监听等操作功能。
 * 支持递归读取文件夹、文件变更监听等高级功能。
 *
 * @author mical
 * @date 2024/8/27 17:21
 */

/**
 * 递归读取文件夹内的所有指定后缀名文件
 * 
 * @param extension 文件扩展名（不包含点号）
 * @return 所有匹配的文件列表
 */
fun File.deepRead(extension: String): List<File> {
    val files = mutableListOf<File>()
    listFiles()?.forEach {
        if (it.isDirectory) {
            files.addAll(it.deepRead(extension))
        } else if (it.extension == extension) {
            files.add(it)
        }
    }
    return files
}

/**
 * 文件监听器对象
 * 
 * 提供文件变更监听功能，支持添加和移除监听器
 */
object FileWatcher {

    /** TabooLib 文件监听器实例 */
    private val fileWatcher = FileWatcher.INSTANCE
    /** 已添加监听器的文件集合 */
    private val fileListeners = LinkedHashSet<File>()
    /** 正在被监听器处理的文件集合 */
    private val watching = LinkedHashSet<File>()

    /**
     * 监听文件改动
     * 
     * @param callback 文件变更时的回调函数
     */
    fun File.watch(callback: (File) -> Unit) {
        if (!hasListener) {
            fileWatcher.addSimpleListener(this) {
                callback(this)
            }
            hasListener = true
        }
    }

    /**
     * 取消监听
     */
    fun File.unwatch() {
        if (hasListener) {
            fileWatcher.removeListener(this)
            hasListener = false
        }
    }

    /**
     * 检测文件是否正在被监听器处理
     * 
     * 防止重复处理同一个文件的变更事件
     */
    var File.isProcessingByWatcher: Boolean
        get() = this in watching && hasListener
        set(value) {
            if (value) watching += this else watching -= this
        }

    /**
     * 文件是否已添加监听器
     */
    private var File.hasListener: Boolean
        get() = this in fileListeners
        set(value) {
            if (value) fileListeners += this else fileListeners -= this
        }
}