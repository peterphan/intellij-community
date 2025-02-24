// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.execution.codeInspection

import com.intellij.codeInsight.TestFrameworks
import com.intellij.codeInsight.intention.FileModifier
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.debugger.DebuggerManagerEx
import com.intellij.execution.*
import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.executors.DefaultDebugExecutor
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.runners.ExecutionUtil
import com.intellij.execution.stacktrace.StackTraceLine
import com.intellij.execution.testframework.sm.runner.states.TestStateInfo
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Iconable
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.util.ClassUtil
import com.intellij.testIntegration.TestFailedLineManager
import com.intellij.util.containers.FactoryMap
import com.intellij.util.ui.UIUtil
import org.jetbrains.annotations.Nls
import org.jetbrains.uast.*
import javax.swing.Icon

class TestFailedLineManagerImpl(project: Project) : TestFailedLineManager, FileEditorManagerListener {
  private val testStorage = TestStateStorage.getInstance(project)

  private val cache = FactoryMap.create<VirtualFile, MutableMap<String, TestInfoCache>> { hashMapOf() }

  init {
    project.messageBus.connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, this)
  }

  override fun fileClosed(source: FileEditorManager, file: VirtualFile) {
    cache.remove(file)?.forEach { (s: String, info: TestInfoCache) -> testStorage.writeState(s, info.record) }
  }

  override fun getTestInfo(element: PsiElement): TestFailedLineManager.TestInfo? {
    val call = element.toUElementOfType<UCallExpression>() ?: return null
    val containingMethod = call.getContainingUMethod() ?: return null
    val callSourcePsi = call.sourcePsi ?: return null
    val file = call.getContainingUFile()?.sourcePsi ?: return null
    val info = getTestInfo(containingMethod) ?: return null
    val document = PsiDocumentManager.getInstance(callSourcePsi.project).getDocument(file) ?: return null
    info.pointer?.element?.let { pointerElem ->
      if (callSourcePsi == pointerElem) {
        info.record.failedLine = document.getLineNumber(callSourcePsi.textOffset) + 1
        return info
      }
    }
    if (info.record.failedLine == -1 || StringUtil.isEmpty(info.record.failedMethod)) return null
    if (info.record.failedLine != document.getLineNumber(callSourcePsi.textOffset) + 1) return null
    if (info.record.failedMethod != call.methodName) return null
    info.pointer = SmartPointerManager.createPointer(callSourcePsi)
    return if (info.record.magnitude <= TestStateInfo.Magnitude.IGNORED_INDEX.value) null else info
  }

  private class TestInfoCache(
    var record: TestStateStorage.Record,
    var pointer: SmartPsiElementPointer<PsiElement>? = null
  ) : TestFailedLineManager.TestInfo {
    override fun getErrorMessage(): String = record.errorMessage

    override fun getTopStackTraceLine(): String = record.topStacktraceLine
  }

  private fun getTestInfo(method: UMethod): TestInfoCache? {
    val containingClass = method.getContainingUClass() ?: return null
    val javaClazz = containingClass.javaPsi
    val framework = TestFrameworks.detectFramework(javaClazz) ?: return null
    if (!framework.isTestMethod(method.javaPsi, false)) return null
    val url = "java:test://" + ClassUtil.getJVMClassName(javaClazz) + "/" + method.name
    val state = testStorage.getState(url) ?: return null
    val vFile = method.getContainingUFile()?.sourcePsi?.virtualFile ?: return null
    val infoInFile = cache[vFile] ?: return null
    var info = infoInFile[url]
    if (info == null || state.date != info.record.date) {
      info = TestInfoCache(state)
      infoInFile[url] = info
    }
    return info
  }

  override fun getRunQuickFix(element: PsiElement): LocalQuickFix? {
    val executor = ExecutorRegistry.getInstance().getExecutorById(DefaultRunExecutor.EXECUTOR_ID) ?: return null
    val configuration = ConfigurationContext(element).configuration ?: return null
    return RunActionFix(executor, configuration)
  }

  override fun getDebugQuickFix(element: PsiElement, topStacktraceLine: String): LocalQuickFix? {
    val executor = ExecutorRegistry.getInstance().getExecutorById(DefaultDebugExecutor.EXECUTOR_ID) ?: return null
    val configuration = ConfigurationContext(element).configuration ?: return null
    return DebugActionFix(topStacktraceLine, executor, configuration)
  }

  private open class RunActionFix(
    @FileModifier.SafeFieldForPreview private val executor: Executor,
    @FileModifier.SafeFieldForPreview private val configuration: RunnerAndConfigurationSettings
  ) : LocalQuickFix, Iconable {
    override fun getFamilyName(): @Nls(capitalization = Nls.Capitalization.Sentence) String =
      UIUtil.removeMnemonic(executor.getStartActionText(ProgramRunnerUtil.shortenName(configuration.name, 0)))

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
      ExecutionUtil.runConfiguration(configuration, executor)
    }

    override fun getIcon(flags: Int): Icon = executor.icon
  }

  private class DebugActionFix(
    private val topStacktraceLine: String,
    executor: Executor,
    settings: RunnerAndConfigurationSettings
  ) : RunActionFix(executor, settings) {
    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
      val line = StackTraceLine(project, topStacktraceLine)
      line.getMethodLocation(project)?.let { location ->
        PsiDocumentManager.getInstance(project).getDocument(location.psiElement.containingFile)?.let { document ->
          DebuggerManagerEx.getInstanceEx(project).breakpointManager.addLineBreakpoint(document, line.lineNumber)
        }
      }
      super.applyFix(project, descriptor)
    }
  }
}