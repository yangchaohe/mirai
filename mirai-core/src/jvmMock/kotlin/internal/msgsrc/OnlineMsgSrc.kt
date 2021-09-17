/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.internal.msgsrc

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSourceKind
import net.mamoe.mirai.message.data.OnlineMessageSource
import net.mamoe.mirai.mock.internal.contact.AbstractMockContact

internal class OnlineMsgSrcToGroup(
    override val ids: IntArray,
    override val internalIds: IntArray,
    override val time: Int,
    override val originalMessage: MessageChain,
    override val bot: Bot,
    override val sender: Bot,
    override val target: Group
) : OnlineMessageSource.Outgoing.ToGroup()

internal class OnlineMsgSrcToFriend(
    override val ids: IntArray,
    override val internalIds: IntArray,
    override val time: Int,
    override val originalMessage: MessageChain,
    override val bot: Bot,
    override val sender: Bot,
    override val target: Friend
) : OnlineMessageSource.Outgoing.ToFriend()

internal class OnlineMsgSrcToStranger(
    override val ids: IntArray,
    override val internalIds: IntArray,
    override val time: Int,
    override val originalMessage: MessageChain,
    override val bot: Bot,
    override val sender: Bot,
    override val target: Stranger
) : OnlineMessageSource.Outgoing.ToStranger()

internal class OnlineMsgSrcToTemp(
    override val ids: IntArray,
    override val internalIds: IntArray,
    override val time: Int,
    override val originalMessage: MessageChain,
    override val bot: Bot,
    override val sender: Bot,
    override val target: Member
) : OnlineMessageSource.Outgoing.ToTemp()

internal class OnlineMsgFromGroup(
    override val ids: IntArray,
    override val internalIds: IntArray,
    override val time: Int,
    override val originalMessage: MessageChain,
    override val bot: Bot,
    override val sender: Member
) : OnlineMessageSource.Incoming.FromGroup()

internal class OnlineMsgSrcFromFriend(
    override val ids: IntArray,
    override val internalIds: IntArray,
    override val time: Int,
    override val originalMessage: MessageChain,
    override val bot: Bot,
    override val sender: Friend
) : OnlineMessageSource.Incoming.FromFriend()

internal class OnlineMsgSrcFromStranger(
    override val ids: IntArray,
    override val internalIds: IntArray,
    override val time: Int,
    override val originalMessage: MessageChain,
    override val bot: Bot,
    override val sender: Stranger
) : OnlineMessageSource.Incoming.FromStranger()

internal class OnlineMsgSrcFromTemp(
    override val ids: IntArray,
    override val internalIds: IntArray,
    override val time: Int,
    override val originalMessage: MessageChain,
    override val bot: Bot,
    override val sender: Member
) : OnlineMessageSource.Incoming.FromTemp()

internal class OnlineMsgSrcFromGroup(
    override val ids: IntArray,
    override val internalIds: IntArray,
    override val time: Int,
    override val originalMessage: MessageChain,
    override val bot: Bot,
    override val sender: Member
) : OnlineMessageSource.Incoming.FromGroup()

internal typealias MsgSrcConstructor<R> = (
    ids: IntArray,
    internalIds: IntArray,
    time: Int,
) -> R

internal inline fun <R> AbstractMockContact.newMsgSrc(
    isSaying: Boolean,
    constructor: MsgSrcConstructor<R>,
): R {
    val db = bot.msgDatabase
    val info = if (isSaying) {
        db.newMessageInfo(
            sender = id,
            subject = when (this) {
                is Member -> group.id
                is Stranger,
                is Friend,
                -> bot.id
                else -> error("Invalid contact: $this")
            },
            kind = when (this) {
                is Member -> MessageSourceKind.GROUP
                is Stranger -> MessageSourceKind.STRANGER
                is Friend -> MessageSourceKind.FRIEND
                else -> error("Invalid contact: $this")
            }
        )
    } else {
        db.newMessageInfo(
            sender = bot.id,
            subject = this.id,
            kind = when (this) {
                is NormalMember -> MessageSourceKind.TEMP
                is Stranger -> MessageSourceKind.STRANGER
                is Friend -> MessageSourceKind.FRIEND
                is Group -> MessageSourceKind.GROUP
                else -> error("Invalid contact: $this")
            }
        )
    }
    return constructor(
        intArrayOf(info.id1),
        intArrayOf(info.id2),
        info.time.toInt(),
    )
}

