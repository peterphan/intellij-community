// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.ide.projectView.impl

import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.ui.UISettings
import com.intellij.ide.util.treeView.NodeRenderer
import com.intellij.openapi.fileEditor.impl.IdeDocumentHistoryImpl
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileSystemItem
import com.intellij.ui.SimpleTextAttributes
import com.intellij.util.text.JBDateFormat
import com.intellij.util.ui.tree.TreeUtil
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import javax.swing.JTree

open class ProjectViewRenderer : NodeRenderer() {
  init {
    isOpaque = false
    isIconOpaque = false
    isTransparentIconBackground = true
  }

  override fun customizeCellRenderer(tree: JTree,
                                     value: Any?,
                                     selected: Boolean,
                                     expanded: Boolean,
                                     leaf: Boolean,
                                     row: Int,
                                     hasFocus: Boolean) {
    super.customizeCellRenderer(tree, value, selected, expanded, leaf, row, hasFocus)

    val userObject = TreeUtil.getUserObject(value)
    if (userObject is ProjectViewNode<*> && UISettings.getInstance().showInplaceComments) {
      appendInplaceComments(userObject)
    }
  }

  // used in Rider
  @Suppress("MemberVisibilityCanBePrivate")
  fun appendInplaceComments(project: Project?, file: VirtualFile?) {
    val ioFile = if (file == null || file.isDirectory || !file.isInLocalFileSystem) null else file.toNioPath()
    val fileAttributes = try {
      if (ioFile == null) null else Files.readAttributes(ioFile, BasicFileAttributes::class.java)
    }
    catch (ignored: Exception) {
      null
    }

    if (fileAttributes != null) {
      append("  ")
      val attributes = SimpleTextAttributes.GRAYED_SMALL_ATTRIBUTES
      append(JBDateFormat.getFormatter().formatDateTime(fileAttributes.lastModifiedTime().toMillis()), attributes)
      append(", " + StringUtil.formatFileSize(fileAttributes.size()), attributes)
    }

    if (Registry.`is`("show.last.visited.timestamps") && file != null && project != null) {
      IdeDocumentHistoryImpl.appendTimestamp(project, this, file)
    }
  }

  private fun appendInplaceComments(node: ProjectViewNode<*>) {
    val parentNode = node.parent
    val content = node.value
    if (content is PsiFileSystemItem || content !is PsiElement || parentNode != null && parentNode.value is PsiDirectory) {
      appendInplaceComments(node.project, node.virtualFile)
    }
  }
}