/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package net.mamoe.mirai.mock.contact

import net.mamoe.kjbb.JvmBlockingBridge
import net.mamoe.mirai.contact.UserOrBot
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.mock.MockBotDSL
import net.mamoe.mirai.mock.utils.MockActions
import net.mamoe.mirai.mock.utils.MockActions.mockFireRecalled
import net.mamoe.mirai.mock.utils.MockActions.nudged
import net.mamoe.mirai.mock.utils.NudgeDsl
import net.mamoe.mirai.utils.JavaFriendlyAPI
import java.util.function.Consumer
import kotlin.internal.LowPriorityInOverloadResolution

@JvmBlockingBridge
public interface MockUserOrBot : MockContactOrBot, UserOrBot {
    @JavaFriendlyAPI
    @LowPriorityInOverloadResolution
    public suspend fun nudgedBy(actor: MockUserOrBot, action: Consumer<NudgeDsl>) {
        actor.nudged(this) { action.accept(this) }
    }
}