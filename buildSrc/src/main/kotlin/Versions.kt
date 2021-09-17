/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("ObjectPropertyName", "ObjectPropertyName", "unused", "MemberVisibilityCanBePrivate")

import org.gradle.api.attributes.Attribute
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

object Versions {
    const val project = "2.8.0-M1"

    const val core = project
    const val console = project
    const val consoleTerminal = project

    const val kotlinCompiler = "1.5.30"
    const val kotlinStdlib = "1.5.30"
    const val dokka = "1.4.32"

    const val coroutines = "1.5.1"
    const val atomicFU = "0.16.3"
    const val serialization = "1.2.2"
    const val ktor = "1.5.4"

    const val binaryValidator = "0.4.0"

    const val io = "0.1.16"
    const val coroutinesIo = "0.1.16"

    const val blockingBridge = "1.10.6-1530.2"

    const val androidGradlePlugin = "4.1.1"
    const val android = "4.1.1.4"

    const val shadow = "6.1.0"

    const val logback = "1.2.5"
    const val slf4j = "1.7.32"
    const val log4j = "2.14.1"
    const val asm = "9.1"
    const val difflib = "1.3.0"
    const val netty = "4.1.63.Final"
    const val bouncycastle = "1.64"

    const val junit = "5.7.2"

    // https://github.com/google/jimfs
    // Java In Memory File System
    const val jimfs = "1.2"

    // If you the versions below, you need to sync changes to mirai-console/buildSrc/src/main/kotlin/Versions.kt

    const val yamlkt = "0.10.2"
    const val intellijGradlePlugin = "1.1"
    const val kotlinIntellijPlugin = "211-1.5.20-release-284-IJ7442.40" // keep to newest as kotlinCompiler
    const val intellij = "2021.1.3" // don't update easily unless you want your disk space -= 500MB

}

@Suppress("unused")
fun kotlinx(id: String, version: String) = "org.jetbrains.kotlinx:kotlinx-$id:$version"

@Suppress("unused")
fun ktor(id: String, version: String = Versions.ktor) = "io.ktor:ktor-$id:$version"


// Why using `-jvm`?
// All target platforms are JVM. Root modules like 'coroutines-core' will be resolved to 'coroutines-common' for commonMain,
// which make IDE code analysis not working.

val `kotlinx-coroutines-core-jvm` = kotlinx("coroutines-core-jvm", Versions.coroutines)
val `kotlinx-coroutines-jdk8` = kotlinx("coroutines-jdk8", Versions.coroutines)
val `kotlinx-coroutines-swing` = kotlinx("coroutines-swing", Versions.coroutines)
val `kotlinx-coroutines-debug` = kotlinx("coroutines-debug", Versions.coroutines)
val `kotlinx-serialization-core-jvm` = kotlinx("serialization-core-jvm", Versions.serialization)
val `kotlinx-serialization-json-jvm` = kotlinx("serialization-json-jvm", Versions.serialization)
val `kotlinx-serialization-protobuf-jvm` = kotlinx("serialization-protobuf-jvm", Versions.serialization)
const val `kotlinx-atomicfu-jvm` = "org.jetbrains.kotlinx:atomicfu-jvm:${Versions.atomicFU}"
val `kotlinx-io-jvm` = kotlinx("io-jvm", Versions.io)

fun KotlinDependencyHandler.implementationKotlinxIoJvm() {
    implementation(`kotlinx-io-jvm`) {
        /*
                    |    +--- org.jetbrains.kotlinx:kotlinx-io-jvm:0.1.16
                    |    |    +--- org.jetbrains.kotlin:kotlin-stdlib:1.3.60 -> 1.5.30 (*)
                    |    |    +--- org.jetbrains.kotlinx:atomicfu:0.14.1
                    |    |    +--- org.jetbrains.kotlinx:atomicfu-common:0.14.1
                    |    |    \--- org.jetbrains.kotlinx:kotlinx-io:0.1.16
                    |    |         \--- org.jetbrains.kotlinx:atomicfu-common:0.14.1
                     */
        exclude("org.jetbrains.kotlinx", "kotlinx-coroutines-core-common")
        exclude("org.jetbrains.kotlinx", "atomicfu")
        exclude("org.jetbrains.kotlinx", "atomicfu-common")
    }
}

val `kotlinx-coroutines-io-jvm` = kotlinx("coroutines-io-jvm", Versions.coroutinesIo)

val `ktor-serialization` = ktor("serialization", Versions.ktor)

val `ktor-client-core-jvm` = ktor("client-core-jvm", Versions.ktor)
val `ktor-client-cio-jvm` = ktor("client-cio-jvm", Versions.ktor)
val `ktor-client-okhttp` = ktor("client-okhttp", Versions.ktor)
val `ktor-client-android` = ktor("client-android", Versions.ktor)
val `ktor-client-logging-jvm` = ktor("client-logging-jvm", Versions.ktor)
val `ktor-network-jvm` = ktor("network-jvm", Versions.ktor)
val `ktor-client-serialization-jvm` = ktor("client-serialization-jvm", Versions.ktor)

val `ktor-server-core` = ktor("server-core", Versions.ktor)
val `ktor-server-netty` = ktor("server-netty", Versions.ktor)
const val `java-in-memory-file-system` = "com.google.jimfs:jimfs:" + Versions.jimfs

const val `logback-classic` = "ch.qos.logback:logback-classic:" + Versions.logback

const val `slf4j-api` = "org.slf4j:slf4j-api:" + Versions.slf4j
const val `slf4j-simple` = "org.slf4j:slf4j-simple:" + Versions.slf4j

const val `log4j-api` = "org.apache.logging.log4j:log4j-api:" + Versions.log4j
const val `log4j-core` = "org.apache.logging.log4j:log4j-core:" + Versions.log4j
const val `log4j-slf4j-impl` = "org.apache.logging.log4j:log4j-slf4j-impl:" + Versions.log4j
const val `log4j-to-slf4j` = "org.apache.logging.log4j:log4j-to-slf4j:" + Versions.log4j

val ATTRIBUTE_MIRAI_TARGET_PLATFORM: Attribute<String> = Attribute.of("mirai.target.platform", String::class.java)


const val `kotlin-compiler` = "org.jetbrains.kotlin:kotlin-compiler:${Versions.kotlinCompiler}"

const val `kotlin-stdlib-jdk8` = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlinStdlib}"
const val `kotlin-reflect` = "org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlinStdlib}"
const val `kotlin-test` = "org.jetbrains.kotlin:kotlin-test:${Versions.kotlinStdlib}"
const val `kotlin-test-junit5` = "org.jetbrains.kotlin:kotlin-test-junit5:${Versions.kotlinStdlib}"


const val `mirai-core-api` = "net.mamoe:mirai-core-api:${Versions.core}"
const val `mirai-core` = "net.mamoe:mirai-core:${Versions.core}"
const val `mirai-core-utils` = "net.mamoe:mirai-core-utils:${Versions.core}"

const val `yamlkt-jvm` = "net.mamoe.yamlkt:yamlkt:${Versions.yamlkt}"

const val `jetbrains-annotations` = "org.jetbrains:annotations:19.0.0"


const val `caller-finder` = "io.github.karlatemp:caller:1.1.1"

const val `android-runtime` = "com.google.android:android:${Versions.android}"
const val `netty-all` = "io.netty:netty-all:${Versions.netty}"
const val `bouncycastle` = "org.bouncycastle:bcprov-jdk15on:${Versions.bouncycastle}"