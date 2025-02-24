// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package org.jetbrains.kotlin.idea.inspections.migration

import com.intellij.util.text.VersionComparatorUtil
import org.jetbrains.annotations.Nls
import org.jetbrains.kotlin.config.LanguageVersion
import org.jetbrains.kotlin.idea.KotlinBundle
import org.jetbrains.kotlin.idea.versions.LibInfo

interface VersionUpdater {
    fun updateVersion(currentVersion: String): String
}

object KotlinxVersionUpdater : VersionUpdater {
    override fun updateVersion(currentVersion: String): String {
        return when {
            currentVersion.contains("eap13") -> return currentVersion
            currentVersion.contains("rc13") -> return currentVersion
            (VersionComparatorUtil.compare(currentVersion, "0.99.0") >= 0) -> return currentVersion // Do not update starting from 1.0.0
            (VersionComparatorUtil.compare(currentVersion, "0.27.0") < 0) -> return "0.27.0"
            else -> "$currentVersion-eap13"
        }
    }
}

data class DeprecatedForKotlinLibInfo(
    val lib: LibInfo,
    val sinceKotlinLanguageVersion: LanguageVersion,
    val versionUpdater: VersionUpdater,
    @Nls val message: String
)

private fun kotlinxCoroutinesDeprecation(name: String): DeprecatedForKotlinLibInfo {
    return DeprecatedForKotlinLibInfo(
        lib = LibInfo("org.jetbrains.kotlinx", name),
        sinceKotlinLanguageVersion = LanguageVersion.KOTLIN_1_3,
        versionUpdater = KotlinxVersionUpdater,
        message = KotlinBundle.message("library.should.be.updated.to.be.compatible.with.kotlin.1.3")
    )
}

val DEPRECATED_COROUTINES_LIBRARIES_INFORMATION = listOf(
    kotlinxCoroutinesDeprecation("kotlinx-coroutines"),
    kotlinxCoroutinesDeprecation("kotlinx-coroutines-android"),
    kotlinxCoroutinesDeprecation("kotlinx-coroutines-core"),
    kotlinxCoroutinesDeprecation("kotlinx-coroutines-core-common"),
    kotlinxCoroutinesDeprecation("kotlinx-coroutines-core-js"),
    kotlinxCoroutinesDeprecation("kotlinx-coroutines-guava"),
    kotlinxCoroutinesDeprecation("kotlinx-coroutines-io"),
    kotlinxCoroutinesDeprecation("kotlinx-coroutines-javafx"),
    kotlinxCoroutinesDeprecation("kotlinx-coroutines-jdk8"),
    kotlinxCoroutinesDeprecation("kotlinx-coroutines-nio"),
    kotlinxCoroutinesDeprecation("kotlinx-coroutines-quasar"),
    kotlinxCoroutinesDeprecation("kotlinx-coroutines-reactive"),
    kotlinxCoroutinesDeprecation("kotlinx-coroutines-reactor"),
    kotlinxCoroutinesDeprecation("kotlinx-coroutines-rx1"),
    kotlinxCoroutinesDeprecation("kotlinx-coroutines-rx2"),
    kotlinxCoroutinesDeprecation("kotlinx-coroutines-swing")
)