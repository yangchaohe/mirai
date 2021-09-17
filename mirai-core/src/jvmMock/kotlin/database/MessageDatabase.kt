/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.database

import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.message.data.MessageSourceKind
import net.mamoe.mirai.mock.internal.db.MsgDatabaseImpl
import net.mamoe.mirai.mock.MockBot
import net.mamoe.mirai.utils.toLongUnsigned

/**
 * 一个消息数据库
 *
 * 该数据库用于存储发送者, 发送目标, 发送类型 等数据,
 * 用于支持 撤回/消息获取 等相关的功能的实现
 *
 * 一般在测试结束后销毁整个数据库
 */
public interface MessageDatabase {
    /**
     * implementation note: 该方法可能同时被多个线程同时调用
     */
    public fun newMessageInfo(
        sender: Long, subject: Long, kind: MessageSourceKind
    ): MessageInfo

    public fun queryMessageInfo(msgId: Long): MessageInfo?

    /**
     * implementation note: 该方法可能同时被多个线程同时调用
     */
    public fun removeMessageInfo(msgId: Long)

    /**
     * 断开与数据库的连接, 在 [MockBot.close] 时会自动调用
     */
    public fun disconnect()

    /**
     * 建立与数据库的连接, 在 [MockBot] 构造后马上调用,
     * 抛出任何错误都会中断 [MockBot] 的初始化
     */
    public fun connect()

    public companion object {
        @JvmStatic
        public fun newDefaultDatabase(): MessageDatabase {
            return MsgDatabaseImpl()
        }
    }
}

public data class MessageInfo(
    public val msgId: Long,
    public val sender: Long,
    public val subject: Long,
    public val kind: MessageSourceKind,
    public val time: Long, // seconds
) {
    // ids
    public val id1: Int get() = (msgId shr 32).toInt()

    // internalId
    public val id2: Int get() = msgId.toInt()
}

public fun mockMsgDatabaseId(id1: Int, id2: Int): Long {
    return (id1.toLongUnsigned() shl Int.SIZE_BITS) or (id2.toLongUnsigned())
}

public fun MessageDatabase.removeMessageInfo(id1: Int, id2: Int) {
    removeMessageInfo(mockMsgDatabaseId(id1, id2))
}

public fun MessageDatabase.queryMessageInfo(ids: IntArray, internalIds: IntArray): MessageInfo? {
    return queryMessageInfo(mockMsgDatabaseId(ids[0], internalIds[0]))
}

public fun MessageDatabase.removeMessageInfo(source: MessageSource) {
    removeMessageInfo(source.ids[0], source.internalIds[0])
}
