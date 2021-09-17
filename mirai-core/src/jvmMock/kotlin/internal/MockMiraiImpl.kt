/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.internal

import kotlinx.coroutines.currentCoroutineContext
import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.data.FriendInfo
import net.mamoe.mirai.data.StrangerInfo
import net.mamoe.mirai.data.UserProfile
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.internal.AbstractBot
import net.mamoe.mirai.internal.MiraiImpl
import net.mamoe.mirai.internal.network.components.EventDispatcher
import net.mamoe.mirai.internal.network.components.EventDispatcherScopeFlag
import net.mamoe.mirai.message.action.Nudge
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.mock.MockBotFactory
import net.mamoe.mirai.mock.contact.MockGroup
import net.mamoe.mirai.mock.database.queryMessageInfo
import net.mamoe.mirai.mock.internal.contact.AQQ_RECALL_FAILED_MESSAGE
import net.mamoe.mirai.mock.internal.contact.MockFriendImpl
import net.mamoe.mirai.mock.internal.contact.MockImage
import net.mamoe.mirai.mock.internal.contact.MockStrangerImpl
import net.mamoe.mirai.mock.utils.mock
import net.mamoe.mirai.mock.utils.simpleMemberInfo
import net.mamoe.mirai.utils.currentTimeSeconds

internal class MockMiraiImpl : MiraiImpl() {
    override suspend fun solveBotInvitedJoinGroupRequestEvent(
        bot: Bot,
        eventId: Long,
        invitorId: Long,
        groupId: Long,
        accept: Boolean
    ) {
        bot.mock()
        if (accept) {
            val group = bot.addGroup(groupId, bot.nameGenerator.nextGroupName())
            group.addMember(
                simpleMemberInfo(
                    uin = 111111111,
                    permission = MemberPermission.OWNER,
                    name = "MockMember - Owner",
                    nameCard = "Custom NameCard",
                )
            ).addMember(
                simpleMemberInfo(
                    uin = 222222222,
                    permission = MemberPermission.ADMINISTRATOR,
                    name = "MockMember - Administrator",
                    nameCard = "root",
                )
            )

            group.addMember(
                simpleMemberInfo(
                    uin = bot.id,
                    permission = MemberPermission.MEMBER,
                    name = bot.nick,
                )
            )


            if (invitorId != 0L) {
                val invitor = group[invitorId] ?: kotlin.run {
                    group.addMember0(
                        simpleMemberInfo(
                            uin = invitorId,
                            permission = MemberPermission.ADMINISTRATOR,
                            name = bot.getFriend(invitorId)?.nick ?: "A random invitor",
                            nameCard = "invitor",
                        )
                    )
                }
                BotJoinGroupEvent.Invite(invitor)
            } else {
                BotJoinGroupEvent.Active(group)
            }.broadcast()
        }
    }

    override suspend fun solveMemberJoinRequestEvent(
        bot: Bot,
        eventId: Long,
        fromId: Long,
        fromNick: String,
        groupId: Long,
        accept: Boolean?,
        blackList: Boolean,
        message: String
    ) {
        if (accept == null || !accept) return // ignore

        val member = bot.getGroupOrFail(groupId).mock().addMember0(
            simpleMemberInfo(
                uin = fromId,
                name = fromNick,
                permission = MemberPermission.MEMBER
            )
        )
        MemberJoinEvent.Active(member).broadcast()
    }

    override suspend fun solveNewFriendRequestEvent(
        bot: Bot,
        eventId: Long,
        fromId: Long,
        fromNick: String,
        accept: Boolean,
        blackList: Boolean
    ) {
        if (!accept) return

        FriendAddEvent(bot.mock().addFriend(fromId, fromNick)).broadcast()
    }

    override fun getUin(contactOrBot: ContactOrBot): Long {
        if (contactOrBot is MockGroup) return contactOrBot.uin

        return super.getUin(contactOrBot)
    }

    override suspend fun muteAnonymousMember(
        bot: Bot,
        anonymousId: String,
        anonymousNick: String,
        groupId: Long,
        seconds: Int
    ) {
        // noop
    }

    override suspend fun recallFriendMessageRaw(
        bot: Bot,
        targetId: Long,
        messageIds: IntArray,
        messageInternalIds: IntArray,
        time: Int
    ): Boolean {
        val info = bot.mock().msgDatabase.queryMessageInfo(messageIds, messageInternalIds) ?: return false
        if (info.kind != MessageSourceKind.FRIEND) return false
        if (info.sender != bot.id) return false
        if (currentTimeSeconds() - info.time > 120) return false
        bot.msgDatabase.removeMessageInfo(info.msgId)

        // MessageRecallEvent.FriendRecall() // TODO: Unknown Logic

        return true
    }

    override suspend fun recallGroupMessageRaw(
        bot: Bot,
        groupCode: Long,
        messageIds: IntArray,
        messageInternalIds: IntArray
    ): Boolean {
        val info = bot.mock().msgDatabase.queryMessageInfo(messageIds, messageInternalIds) ?: return false
        if (info.kind != MessageSourceKind.GROUP) return false
        val group = bot.getGroup(info.subject) ?: return false
        val canDelete = when (group.botPermission) {
            MemberPermission.OWNER -> true
            MemberPermission.ADMINISTRATOR -> kotlin.run w@{
                val member = group.getMember(info.sender) ?: return@w true
                member.permission == MemberPermission.MEMBER
            }
            else -> kotlin.run w@{
                if (info.sender != bot.id) return@w false
                currentTimeSeconds() - info.time <= 120
            }
        }
        if (!canDelete) return false
        bot.msgDatabase.removeMessageInfo(info.msgId)

        MessageRecallEvent.GroupRecall(
            bot,
            info.sender,
            messageIds,
            messageInternalIds,
            info.time.toInt(),
            null,
            group,
            group[info.sender] ?: return true
        ).broadcast()

        return true
    }

    override suspend fun recallGroupTempMessageRaw(
        bot: Bot,
        groupUin: Long,
        targetId: Long,
        messageIds: IntArray,
        messageInternalIds: IntArray,
        time: Int
    ): Boolean = false // TODO: No recall event

    override suspend fun recallMessage(bot: Bot, source: MessageSource) {
        fun doFailed() {
            error("Failed to recall message #${source.ids.contentToString()}: $AQQ_RECALL_FAILED_MESSAGE")
        }
        if (source is OnlineMessageSource) {
            when (source) {
                is OnlineMessageSource.Incoming.FromFriend,
                is OnlineMessageSource.Outgoing.ToFriend,
                -> {
                    val resp = recallFriendMessageRaw(
                        bot,
                        source.subject.id,
                        source.ids,
                        source.internalIds,
                        source.time
                    )
                    if (!resp) doFailed()
                }
                is OnlineMessageSource.Incoming.FromGroup,
                is OnlineMessageSource.Outgoing.ToGroup,
                -> {
                    val resp = recallGroupMessageRaw(
                        bot,
                        source.subject.id,
                        source.ids,
                        source.internalIds
                    )
                    if (!resp) doFailed()
                }
                else -> {
                    // TODO: No Event
                }
            }
        } else {
            source as OfflineMessageSource
            when (source.kind) {
                MessageSourceKind.GROUP -> {
                    val resp = recallGroupMessageRaw(
                        bot,
                        source.targetId,
                        source.ids,
                        source.internalIds
                    )
                    if (!resp) doFailed()
                }
                MessageSourceKind.FRIEND -> {
                    val resp = recallFriendMessageRaw(
                        bot,
                        source.targetId,
                        source.ids,
                        source.internalIds,
                        source.time
                    )
                    if (!resp) doFailed()
                }
                MessageSourceKind.TEMP -> {
                    // TODO: No Event
                }
                MessageSourceKind.STRANGER -> {
                    // TODO: No Event
                }
            }
        }
    }

    override suspend fun sendNudge(bot: Bot, nudge: Nudge, receiver: Contact): Boolean {
        NudgeEvent(
            from = bot,
            target = nudge.target,
            subject = receiver,
            action = "戳了戳",
            suffix = ""
        ).broadcast()
        return true
    }

    override suspend fun queryProfile(bot: Bot, targetId: Long): UserProfile {
        return bot.mock().userProfileService.doQueryUserProfile(targetId)
    }

    override val BotFactory: BotFactory get() = MockBotFactory

    /*override suspend fun getGroupVoiceDownloadUrl(bot: Bot, md5: ByteArray, groupId: Long, dstUin: Long): String {
        return super.getGroupVoiceDownloadUrl(bot, md5, groupId, dstUin)
    }*/

    @Suppress("RETURN_TYPE_MISMATCH_ON_OVERRIDE")
    override fun newFriend(bot: Bot, friendInfo: FriendInfo): Friend {
        bot.mock()
        return MockFriendImpl(
            bot.coroutineContext,
            bot,
            friendInfo.uin,
            friendInfo.nick,
            friendInfo.remark,
        )
    }

    @Suppress("RETURN_TYPE_MISMATCH_ON_OVERRIDE")
    override fun newStranger(bot: Bot, strangerInfo: StrangerInfo): Stranger {
        bot.mock()
        return MockStrangerImpl(
            bot.coroutineContext,
            bot,
            strangerInfo.uin,
            strangerInfo.remark,
            strangerInfo.nick,
        )
    }

    override fun createImage(imageId: String): Image {
        if (imageId matches Image.IMAGE_ID_REGEX) {
            return MockImage(imageId, "image/" + imageId.substring(1..36))
        }
        //imageId.substring(1..36)
        return super.createImage(imageId)
    }

    override suspend fun broadcastEvent(event: Event) {
        if (currentCoroutineContext()[EventDispatcherScopeFlag] != null) {
            // called by [EventDispatcher]
            return super.broadcastEvent(event)
        }
        if (event is BotEvent) {
            val bot = event.bot
            if (bot is MockBotImpl) {
                bot.components[EventDispatcher].broadcast(event)
                return
            }
        }
        super.broadcastEvent(event)
    }
}
