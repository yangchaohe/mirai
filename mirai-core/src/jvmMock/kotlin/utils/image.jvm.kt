/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("FunctionName")

package net.mamoe.mirai.mock.utils

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO


public fun randomImage_awt_jvm(): BufferedImage {
    val width = (500..800).random()
    val height = (500..800).random()
    val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    val graphics = image.createGraphics()
    for (x in 0 until width) {
        for (y in 0 until height) {
            graphics.color = Color(
                (0..0xFFFFFF).random()
            )
            graphics.drawRect(x, y, 1, 1)
        }
    }
    graphics.dispose()
    return image
}

public fun BufferedImage.saveToBytes(): ByteArray = ByteArrayOutputStream().apply {
    ImageIO.write(this@saveToBytes, "png", this)
}.toByteArray()

