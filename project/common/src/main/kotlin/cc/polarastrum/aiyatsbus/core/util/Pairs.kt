package cc.polarastrum.aiyatsbus.core.util

/**
 * 键值对工具类
 * 
 * 提供简单的键值对数据结构，用于兼容 Java 代码。
 * 包含数据类和便捷的创建函数。
 *
 * @author mical
 * @since 2025/6/20 23:42
 */

/**
 * 键值对数据类
 *
 * @param A 第一个值的类型
 * @param B 第二个值的类型
 * @param first 第一个值
 * @param second 第二个值
 */
data class Pair1<A, B>(val first: A, val second: B)

/**
 * 创建键值对的便捷函数
 *
 * 使用中缀语法创建 Pair 对象
 *
 * @param A 第一个值的类型
 * @param B 第二个值的类型
 * @param that 第二个值
 * @return 包含两个值的 Pair 对象
 */
infix fun <A, B> A.to1(that: B): Pair1<A, B> = Pair1(this, that)