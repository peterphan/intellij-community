// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package org.jetbrains.kotlin.idea.debugger

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.components.service
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.impl.XDebuggerUtilImpl
import org.jetbrains.kotlin.base.util.KOTLIN_FILE_EXTENSIONS

class ToggleKotlinVariablesState {
    companion object {
        private const val KOTLIN_VARIABLE_VIEW = "debugger.kotlin.variable.view"

        fun getService(): ToggleKotlinVariablesState = service()
    }

    var kotlinVariableView = PropertiesComponent.getInstance().getBoolean(KOTLIN_VARIABLE_VIEW, true)
        set(newValue) {
            field = newValue
            PropertiesComponent.getInstance().setValue(KOTLIN_VARIABLE_VIEW, newValue)
        }
}

class ToggleKotlinVariablesView : ToggleAction() {
    private val kotlinVariableViewService = ToggleKotlinVariablesState.getService()

    override fun update(e: AnActionEvent) {
        super.update(e)
        val session = XDebugSession.DATA_KEY.getData(e.dataContext)
        e.presentation.isEnabledAndVisible = session != null && session.isInKotlinFile()
    }

    private fun XDebugSession.isInKotlinFile(): Boolean {
        val fileExtension = currentPosition?.file?.extension ?: return false
        return fileExtension in KOTLIN_FILE_EXTENSIONS
    }

    override fun isSelected(e: AnActionEvent) = kotlinVariableViewService.kotlinVariableView

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        kotlinVariableViewService.kotlinVariableView = state
        XDebuggerUtilImpl.rebuildAllSessionsViews(e.project)
    }
}