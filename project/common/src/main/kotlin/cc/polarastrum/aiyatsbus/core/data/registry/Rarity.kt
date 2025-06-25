package cc.polarastrum.aiyatsbus.core.data.registry

import cc.polarastrum.aiyatsbus.core.data.Dependencies
import cc.polarastrum.aiyatsbus.core.data.Dependency
import cc.polarastrum.aiyatsbus.core.data.Registry
import cc.polarastrum.aiyatsbus.core.data.RegistryItem
import cc.polarastrum.aiyatsbus.core.util.replace
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.chat.component
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration

/**
 * 稀有度类
 * 用于定义附魔的稀有度等级，包含稀有度的各种属性和配置信息。
 * 稀有度决定了附魔的获取难度、显示效果等。
 *
 * @author mical
 * @since 2025/6/20 19:59
 */
data class Rarity @JvmOverloads constructor(
    /** 配置根节点 */
    private val root: ConfigurationSection,
    /** 依赖项配置，定义该稀有度的前置条件 */
    override val dependencies: Dependencies = Dependencies(root.getConfigurationSection("dependencies")),
    /** 稀有度名称 */
    val name: String = root.getString("name")!!,
    /** 稀有度颜色代码，用于显示 */
    val color: String = root.getString("color")!!,
    /** 稀有度权重，影响获取概率，默认为 100 */
    val weight: Int = root.getInt("weight", 100),
    val skull: String = root.getString("skull", "")!!,
    /** 是否不可访问，为 true 时玩家无法获得该稀有度的附魔 */
    val inaccessible: Boolean = root.getBoolean("inaccessible", false),
    /** 菜单中的自定义模型 ID，-1 表示使用默认模型 */
    val customModelUI: Int = root.getInt("custom_model_ui"),
    /** 玩家附魔书中的自定义模型 ID，-1 表示使用默认模型 */
    val customModelBook: Int = root.getInt("custom_model_book")
) : RegistryItem(root), Dependency {

    /**
     * 是否启用了菜单自定义模型
     * @return true 表示启用了自定义模型，false 表示使用默认模型
     */
    val isCustomModelUIEnabled: Boolean
        get() = customModelUI != -1

    /**
     * 是否启用了附魔书自定义模型
     * @return true 表示启用了自定义模型，false 表示使用默认模型
     */
    val isCustomModelBookEnabled: Boolean
        get() = customModelBook != -1

    /**
     * 生成带颜色的显示名称
     * @param text 要显示的文本，默认为稀有度名称
     * @return 带有颜色格式的显示文本
     */
    fun displayName(text: String = name): String {
        return color.replace("text" to text).component().buildColored().toLegacyText()
    }

    /**
     * 稀有度注册器伴生对象
     * 负责从配置文件中加载和管理稀有度数据
     */
    companion object : Registry<Rarity>("rarity", { section -> Rarity(section) }) {

        /** 稀有度配置文件，自动重载配置变更 */
        @Config("enchants/rarity.yml", autoReload = true)
        override lateinit var config: Configuration
            private set
    }
}
