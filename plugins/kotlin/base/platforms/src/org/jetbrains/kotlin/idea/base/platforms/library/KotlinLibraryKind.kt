// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.kotlin.idea.base.platforms

import com.intellij.openapi.roots.libraries.DummyLibraryProperties
import com.intellij.openapi.roots.libraries.PersistentLibraryKind
import com.intellij.openapi.vfs.JarFileSystem
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.kotlin.idea.base.platforms.library.KnownLibraryKindForIndex
import org.jetbrains.kotlin.idea.base.platforms.library.getLibraryKindForJar
import org.jetbrains.kotlin.platform.CommonPlatforms
import org.jetbrains.kotlin.platform.DefaultIdeTargetPlatformKindProvider
import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.platform.idePlatformKind
import org.jetbrains.kotlin.platform.js.JsPlatforms
import org.jetbrains.kotlin.platform.konan.NativePlatforms

sealed interface KotlinLibraryKind {
    // TODO: Drop this property. See https://youtrack.jetbrains.com/issue/KT-38233
    //  This property returns approximate library platform, as the real platform can be evaluated only for concrete library.
    val compilerPlatform: TargetPlatform
}

object KotlinJavaScriptLibraryKind : PersistentLibraryKind<DummyLibraryProperties>("kotlin.js"), KotlinLibraryKind {
    override val compilerPlatform: TargetPlatform
        get() = JsPlatforms.defaultJsPlatform

    override fun createDefaultProperties(): DummyLibraryProperties {
        return DummyLibraryProperties.INSTANCE
    }
}

object KotlinCommonLibraryKind : PersistentLibraryKind<DummyLibraryProperties>("kotlin.common"), KotlinLibraryKind {
    override val compilerPlatform: TargetPlatform
        get() = CommonPlatforms.defaultCommonPlatform

    override fun createDefaultProperties(): DummyLibraryProperties {
        return DummyLibraryProperties.INSTANCE
    }
}

object KotlinNativeLibraryKind : PersistentLibraryKind<DummyLibraryProperties>("kotlin.native"), KotlinLibraryKind {
    override val compilerPlatform: TargetPlatform
        get() = NativePlatforms.unspecifiedNativePlatform

    override fun createDefaultProperties(): DummyLibraryProperties {
        return DummyLibraryProperties.INSTANCE
    }
}

// TODO: Drop this property. See https://youtrack.jetbrains.com/issue/KT-38233
//  It returns approximate library platform, as the real platform can be evaluated only for concrete library.
val PersistentLibraryKind<*>?.platform: TargetPlatform
    get() = when (this) {
        is KotlinLibraryKind -> this.compilerPlatform
        else -> DefaultIdeTargetPlatformKindProvider.defaultPlatform
    }

fun detectLibraryKind(roots: Array<VirtualFile>): PersistentLibraryKind<*>? {
    val jarFile = roots.firstOrNull() ?: return null
    if (jarFile.fileSystem is JarFileSystem) {
        // TODO: Detect library kind for Jar file using IdePlatformKindResolution.
        when (jarFile.getLibraryKindForJar()) {
            KnownLibraryKindForIndex.COMMON -> return KotlinCommonLibraryKind
            KnownLibraryKindForIndex.JS -> return KotlinJavaScriptLibraryKind
            KnownLibraryKindForIndex.UNKNOWN -> {
                /* Continue detection of library kind via IdePlatformKindResolution. */
            }
        }
    }

    val matchingPlatformKind = IdePlatformKindProjectStructure.getLibraryPlatformKind(jarFile)
        ?: DefaultIdeTargetPlatformKindProvider.defaultPlatform.idePlatformKind

    return IdePlatformKindProjectStructure.getLibraryKind(matchingPlatformKind)
}