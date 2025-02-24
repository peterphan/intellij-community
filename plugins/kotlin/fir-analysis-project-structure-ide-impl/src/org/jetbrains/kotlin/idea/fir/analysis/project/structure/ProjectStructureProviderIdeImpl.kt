// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package org.jetbrains.kotlin.idea.fir.analysis.project.structure

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.analysis.project.structure.KtBinaryModule
import org.jetbrains.kotlin.analysis.project.structure.KtLibraryModule
import org.jetbrains.kotlin.analysis.project.structure.KtModule
import org.jetbrains.kotlin.analysis.project.structure.ProjectStructureProvider
import org.jetbrains.kotlin.analyzer.ModuleInfo
import org.jetbrains.kotlin.idea.base.projectStructure.ModuleInfoProvider
import org.jetbrains.kotlin.idea.base.projectStructure.firstOrNull
import org.jetbrains.kotlin.idea.base.projectStructure.moduleInfo.*
import org.jetbrains.kotlin.idea.base.projectStructure.moduleInfo.ModuleSourceInfo
import org.jetbrains.kotlin.idea.base.projectStructure.moduleInfo.NotUnderContentRootModuleInfo
import org.jetbrains.kotlin.idea.caches.project.*

@OptIn(FE10ApiUsage::class)
internal class ProjectStructureProviderIdeImpl : ProjectStructureProvider() {
    override fun getKtModuleForKtElement(element: PsiElement): KtModule {
        val config = ModuleInfoProvider.Configuration(createSourceLibraryInfoForLibraryBinaries = false)
        val moduleInfo = ModuleInfoProvider.getInstance(element.project).firstOrNull(element, config)
            ?: NotUnderContentRootModuleInfo

        return getKtModuleByModuleInfo(moduleInfo)
    }

    // TODO maybe introduce some cache?
    fun getKtModuleByModuleInfo(moduleInfo: ModuleInfo): KtModule =
        createKtModuleByModuleInfo(moduleInfo)


    private fun createKtModuleByModuleInfo(moduleInfo: ModuleInfo): KtModule = when (moduleInfo) {
        is ModuleSourceInfo -> KtSourceModuleByModuleInfo(moduleInfo, this)
        is LibraryInfo -> KtLibraryModuleByModuleInfo(moduleInfo, this)
        is SdkInfo -> SdkKtModuleByModuleInfo(moduleInfo, this)
        is LibrarySourceInfo -> KtLibrarySourceModuleByModuleInfo(moduleInfo, this)
        is NotUnderContentRootModuleInfo -> NotUnderContentRootModuleByModuleInfo(moduleInfo, this)
        else -> TODO("Unsupported module info ${moduleInfo::class} $moduleInfo")
    }

    override fun getKtBinaryModules(): Collection<KtBinaryModule> {
        TODO("This is a temporary function used for Android LINT, and should not be called in the IDE")
    }

    override fun getStdlibWithBuiltinsModule(module: KtModule): KtLibraryModule? {
        val stdlibLibraryInfo = module.moduleInfo.findJvmStdlibAcrossDependencies() ?: return null
        return getKtModuleByModuleInfo(stdlibLibraryInfo) as KtLibraryModule
    }

    companion object {
        fun getInstance(project: Project):ProjectStructureProviderIdeImpl {
            return project.getService(ProjectStructureProvider::class.java) as ProjectStructureProviderIdeImpl
        }
    }
}
