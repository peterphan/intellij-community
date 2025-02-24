// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package org.jetbrains.kotlin.idea.base.projectStructure.fileTypes

import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.kotlin.idea.base.projectStructure.KotlinBaseProjectStructureBundle
import org.jetbrains.kotlin.serialization.js.KotlinJavascriptSerializationUtil

object KotlinJavaScriptMetaFileType : FileType {
    override fun getName() = "KJSM"
    override fun getDescription() = KotlinBaseProjectStructureBundle.message("kotlin.javascript.meta.file")
    override fun getDefaultExtension() = KotlinJavascriptSerializationUtil.CLASS_METADATA_FILE_EXTENSION
    override fun getIcon() = null
    override fun isBinary() = true
    override fun isReadOnly() = true
    override fun getCharset(file: VirtualFile, content: ByteArray) = null
}
