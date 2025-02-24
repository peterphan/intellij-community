// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package org.jetbrains.kotlin.idea.quickfix

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.RootsChangeRescanningInfo
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.util.NlsContexts
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.base.util.invalidateProjectRoots
import org.jetbrains.kotlin.cli.common.arguments.CliArgumentStringBuilder.replaceLanguageFeature
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.idea.KotlinJvmBundle
import org.jetbrains.kotlin.idea.compiler.configuration.KotlinCompilerSettings
import org.jetbrains.kotlin.idea.configuration.findApplicableConfigurator
import org.jetbrains.kotlin.idea.facet.getRuntimeLibraryVersion
import org.jetbrains.kotlin.base.util.module
import org.jetbrains.kotlin.config.TestSourceKotlinRootType
import org.jetbrains.kotlin.idea.base.projectStructure.getKotlinSourceRootType
import org.jetbrains.kotlin.psi.KtFile

sealed class ChangeGeneralLanguageFeatureSupportFix(
    element: PsiElement,
    feature: LanguageFeature,
    featureSupport: LanguageFeature.State
) : AbstractChangeFeatureSupportLevelFix(element, feature, featureSupport, feature.presentableName) {

    class InModule(
        element: PsiElement,
        feature: LanguageFeature,
        featureSupport: LanguageFeature.State
    ) : ChangeGeneralLanguageFeatureSupportFix(element, feature, featureSupport) {
        override fun getText() = KotlinJvmBundle.message("fix.0.in.current.module", super.getText())

        override fun invoke(project: Project, editor: Editor?, file: KtFile) {
            val module = ModuleUtilCore.findModuleForPsiElement(file) ?: return
            val fileIndex = ModuleRootManager.getInstance(module).fileIndex
            val forTests = fileIndex.getKotlinSourceRootType(file.virtualFile) == TestSourceKotlinRootType

            findApplicableConfigurator(module).changeGeneralFeatureConfiguration(module, feature, featureSupport, forTests)
        }
    }

    class InProject(
        element: PsiElement,
        feature: LanguageFeature,
        featureSupport: LanguageFeature.State
    ) : ChangeGeneralLanguageFeatureSupportFix(element, feature, featureSupport) {
        override fun getText() = KotlinJvmBundle.message("fix.0.in.the.project", super.getText())

        override fun invoke(project: Project, editor: Editor?, file: KtFile) {
            if (featureSupportEnabled) {
                if (!checkUpdateRuntime(project, feature.sinceApiVersion)) return
            }
            KotlinCompilerSettings.getInstance(project).update {
                additionalArguments = additionalArguments.replaceLanguageFeature(
                    feature,
                    featureSupport,
                    file.module?.let { getRuntimeLibraryVersion(it) },
                    separator = " ",
                    quoted = false
                )
            }
            project.invalidateProjectRoots(RootsChangeRescanningInfo.NO_RESCAN_NEEDED)
        }

    }

    companion object : FeatureSupportIntentionActionsFactory() {
        private val supportedFeatures = listOf(LanguageFeature.InlineClasses)

        @NlsContexts.DialogTitle
        fun getFixText(feature: LanguageFeature, state: LanguageFeature.State) = getFixText(state, feature.presentableName)

        override fun doCreateActions(diagnostic: Diagnostic): List<IntentionAction> {
            val module = ModuleUtilCore.findModuleForPsiElement(diagnostic.psiElement) ?: return emptyList()

            return supportedFeatures.flatMap { feature ->
                doCreateActions(
                    diagnostic, feature, allowWarningAndErrorMode = false,
                    quickFixConstructor = if (shouldConfigureInProject(module)) ::InProject else ::InModule
                )
            }
        }
    }
}
