/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.internal

import net.mamoe.mirai.Bot
import net.mamoe.mirai.mock.MockBot
import net.mamoe.mirai.mock.MockBotFactory
import net.mamoe.mirai.mock.database.MessageDatabase
import net.mamoe.mirai.mock.fsserver.TmpFsServer
import net.mamoe.mirai.mock.userprofile.UserProfileService
import net.mamoe.mirai.mock.utils.NameGenerator
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.lateinitMutableProperty
import kotlin.math.absoluteValue
import kotlin.random.Random

internal class MockBotFactoryImpl : MockBotFactory {
    override fun newMockBotBuilder(): MockBotFactory.BotBuilder {
        return object : MockBotFactory.BotBuilder {
            var id: Long = Random.nextLong().absoluteValue
            var nick_: String by lateinitMutableProperty {
                "Mock Bot $id"
            }
            var configuration_: BotConfiguration by lateinitMutableProperty { BotConfiguration { } }
            var nameGenerator: NameGenerator = NameGenerator.DEFAULT
            var tmpFsServer_: TmpFsServer by lateinitMutableProperty {
                TmpFsServer.newInMemoryFsServer()
            }
            var msgDb: MessageDatabase by lateinitMutableProperty {
                MessageDatabase.newDefaultDatabase()
            }
            var userProfileService: UserProfileService by lateinitMutableProperty {
                UserProfileService.newDefaultInstance()
            }

            override fun id(value: Long): MockBotFactory.BotBuilder = apply {
                this.id = value
            }

            override fun nick(value: String): MockBotFactory.BotBuilder = apply {
                this.nick_ = value
            }

            override fun configuration(value: BotConfiguration): MockBotFactory.BotBuilder = apply {
                this.configuration_ = value
            }

            override fun nameGenerator(value: NameGenerator): MockBotFactory.BotBuilder = apply {
                this.nameGenerator = value
            }

            override fun tmpFsServer(server: TmpFsServer): MockBotFactory.BotBuilder = apply {
                tmpFsServer_ = server
            }

            override fun msgDatabase(db: MessageDatabase): MockBotFactory.BotBuilder = apply {
                msgDb = db
            }

            override fun userProfileService(service: UserProfileService): MockBotFactory.BotBuilder = apply {
                userProfileService = service
            }

            override fun createNoInstanceRegister(): MockBot {
                return MockBotImpl(
                    configuration_,
                    id,
                    nick_,
                    nameGenerator,
                    tmpFsServer_,
                    msgDb,
                    userProfileService,
                )
            }

            @Suppress("INVISIBLE_MEMBER")
            override fun create(): MockBot {
                return createNoInstanceRegister().also {
                    Bot._instances[id] = it
                }
            }
        }
    }

    override fun newBot(qq: Long, password: String, configuration: BotConfiguration): Bot {
        return newMockBotBuilder()
            .id(qq)
            .configuration(configuration)
            .create()
    }

    override fun newBot(qq: Long, passwordMd5: ByteArray, configuration: BotConfiguration): Bot {
        return newMockBotBuilder()
            .id(qq)
            .configuration(configuration)
            .create()
    }
}