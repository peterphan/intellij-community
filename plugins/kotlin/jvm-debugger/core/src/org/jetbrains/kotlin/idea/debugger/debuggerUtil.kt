// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package org.jetbrains.kotlin.idea.debugger

import com.intellij.debugger.engine.DebugProcessImpl
import com.intellij.debugger.engine.DebuggerManagerThreadImpl
import com.intellij.debugger.engine.events.DebuggerCommandImpl
import com.intellij.debugger.impl.DebuggerContextImpl
import com.intellij.debugger.impl.DebuggerUtilsAsync
import com.intellij.debugger.jdi.StackFrameProxyImpl
import com.intellij.psi.PsiElement
import com.sun.jdi.*
import org.jetbrains.kotlin.base.util.KOTLIN_FILE_EXTENSIONS
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.codegen.coroutines.INVOKE_SUSPEND_METHOD_NAME
import org.jetbrains.kotlin.codegen.inline.KOTLIN_STRATA_NAME
import org.jetbrains.kotlin.codegen.topLevelClassAsmType
import org.jetbrains.kotlin.idea.caches.resolve.analyze
import org.jetbrains.kotlin.idea.core.util.CodeInsightUtils
import org.jetbrains.kotlin.idea.core.util.getLineEndOffset
import org.jetbrains.kotlin.idea.core.util.getLineStartOffset
import org.jetbrains.kotlin.idea.debugger.DebuggerUtils.isKotlinFakeLineNumber
import org.jetbrains.kotlin.idea.util.application.runReadAction
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.load.java.JvmAbi
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.inline.InlineUtil
import org.jetbrains.kotlin.resolve.lazy.BodyResolveMode
import java.util.concurrent.CompletableFuture

fun Location.isInKotlinSources(): Boolean {
    return declaringType().isInKotlinSources()
}

fun ReferenceType.isInKotlinSources(): Boolean {
    val fileExtension = safeSourceName()?.substringAfterLast('.')?.toLowerCase() ?: ""
    return fileExtension in KOTLIN_FILE_EXTENSIONS || containsKotlinStrata()
}

fun ReferenceType.isInKotlinSourcesAsync(): CompletableFuture<Boolean> {
    return DebuggerUtilsAsync.sourceName(this)
        .thenApply {
            val fileExtension = it?.substringAfterLast('.')?.toLowerCase() ?: ""
            fileExtension in KOTLIN_FILE_EXTENSIONS
        }
        .exceptionally {
            if (DebuggerUtilsAsync.unwrap(it) is AbsentInformationException) {
                false
            }
            else {
                throw it
            }
        }
        .thenCombine(containsKotlinStrataAsync()) { kotlinExt, kotlinStrata -> kotlinExt || kotlinStrata }
}

fun ReferenceType.containsKotlinStrata() = availableStrata().contains(KOTLIN_STRATA_NAME)

fun ReferenceType.containsKotlinStrataAsync(): CompletableFuture<Boolean> =
    DebuggerUtilsAsync.availableStrata(this).thenApply { it.contains(KOTLIN_STRATA_NAME) }

fun isInsideInlineArgument(inlineArgument: KtFunction, location: Location, debugProcess: DebugProcessImpl): Boolean {
    val visibleVariables = location.visibleVariables(debugProcess)
    val markerLocalVariables = visibleVariables.filter { it.name().startsWith(JvmAbi.LOCAL_VARIABLE_NAME_PREFIX_INLINE_ARGUMENT) }

    return runReadAction {
        val lambdaOrdinal = lambdaOrdinalByArgument(inlineArgument)
        val functionName = functionNameByArgument(inlineArgument, inlineArgument.analyze(BodyResolveMode.PARTIAL))

        markerLocalVariables
            .map { it.name().drop(JvmAbi.LOCAL_VARIABLE_NAME_PREFIX_INLINE_ARGUMENT.length) }
            .any { variableName ->
                if (variableName.startsWith("-")) {
                    val lambdaClassName = ClassNameCalculator.getClassNameCompat(inlineArgument)?.substringAfterLast('.') ?: return@any false
                    dropInlineSuffix(variableName) == "-$functionName-$lambdaClassName"
                } else {
                    // For Kotlin up to 1.3.10
                    lambdaOrdinalByLocalVariable(variableName) == lambdaOrdinal
                            && functionNameByLocalVariable(variableName) == functionName
                }
            }
    }
}

fun <T : Any> DebugProcessImpl.invokeInManagerThread(f: (DebuggerContextImpl) -> T?): T? {
    var result: T? = null
    val command: DebuggerCommandImpl = object : DebuggerCommandImpl() {
        override fun action() {
            result = f(debuggerContext)
        }
    }

    when {
        DebuggerManagerThreadImpl.isManagerThread() ->
            managerThread.invoke(command)
        else ->
            managerThread.invokeAndWait(command)
    }

    return result
}

private fun lambdaOrdinalByArgument(elementAt: KtFunction): Int {
    val className = ClassNameCalculator.getClassNameCompat(elementAt) ?: return 0
    return className.substringAfterLast("$").toInt()
}

private fun functionNameByArgument(elementAt: KtFunction, context: BindingContext): String {
    val inlineArgumentDescriptor = InlineUtil.getInlineArgumentDescriptor(elementAt, context)
    return inlineArgumentDescriptor?.containingDeclaration?.name?.asString() ?: "unknown"
}

private fun Location.visibleVariables(debugProcess: DebugProcessImpl): List<LocalVariable> {
    val stackFrame = MockStackFrame(this, debugProcess.virtualMachineProxy.virtualMachine)
    return stackFrame.visibleVariables()
}

// For Kotlin up to 1.3.10
private fun lambdaOrdinalByLocalVariable(name: String): Int = try {
    val nameWithoutPrefix = name.removePrefix(JvmAbi.LOCAL_VARIABLE_NAME_PREFIX_INLINE_ARGUMENT)
    Integer.parseInt(nameWithoutPrefix.substringBefore("$", nameWithoutPrefix))
} catch (e: NumberFormatException) {
    0
}

// For Kotlin up to 1.3.10
private fun functionNameByLocalVariable(name: String): String {
    val nameWithoutPrefix = name.removePrefix(JvmAbi.LOCAL_VARIABLE_NAME_PREFIX_INLINE_ARGUMENT)
    return nameWithoutPrefix.substringAfterLast("$", "unknown")
}

private class MockStackFrame(private val location: Location, private val vm: VirtualMachine) : StackFrame {
    private var visibleVariables: Map<String, LocalVariable>? = null

    override fun location() = location
    override fun thread() = null
    override fun thisObject() = null

    private fun createVisibleVariables() {
        if (visibleVariables == null) {
            val allVariables = location.method().safeVariables() ?: emptyList()
            val map = HashMap<String, LocalVariable>(allVariables.size)

            for (variable in allVariables) {
                if (variable.isVisible(this)) {
                    map[variable.name()] = variable
                }
            }
            visibleVariables = map
        }
    }

    override fun visibleVariables(): List<LocalVariable> {
        createVisibleVariables()
        val mapAsList = ArrayList(visibleVariables!!.values)
        mapAsList.sort()
        return mapAsList
    }

    override fun visibleVariableByName(name: String): LocalVariable? {
        createVisibleVariables()
        return visibleVariables!![name]
    }

    override fun getValue(variable: LocalVariable) = null
    override fun getValues(variables: List<LocalVariable>): Map<LocalVariable, Value> = emptyMap()
    override fun setValue(variable: LocalVariable, value: Value) {
    }

    override fun getArgumentValues(): List<Value> = emptyList()
    override fun virtualMachine() = vm
}

private const val INVOKE_SUSPEND_SIGNATURE = "(Ljava/lang/Object;)Ljava/lang/Object;"

fun StackFrameProxyImpl.isOnSuspensionPoint(): Boolean {
    val location = this.safeLocation() ?: return false

    if (isInSuspendMethod(location)) {
        val firstLocation = getFirstMethodLocation(location) ?: return false
        return firstLocation.safeLineNumber() == location.safeLineNumber() && firstLocation.codeIndex() != location.codeIndex()
    }

    return false
}

fun isInSuspendMethod(location: Location): Boolean {
    val method = location.method()
    val signature = method.signature()
    val continuationAsmType = continuationAsmType()
    return signature.contains(continuationAsmType.toString()) ||
          (method.name() == INVOKE_SUSPEND_METHOD_NAME && signature == INVOKE_SUSPEND_SIGNATURE)
}

private fun continuationAsmType() =
    StandardNames.COROUTINES_PACKAGE_FQ_NAME.child(Name.identifier("Continuation")).topLevelClassAsmType()

private fun getFirstMethodLocation(location: Location): Location? {
    val firstLocation = location.safeMethod()?.location() ?: return null
    if (firstLocation.safeLineNumber() < 0) {
        return null
    }

    return firstLocation
}

fun isOnSuspendReturnOrReenter(location: Location): Boolean {
    val firstLocation = getFirstMethodLocation(location) ?: return false
    return firstLocation.safeLineNumber() == location.safeLineNumber()
}

fun isOneLineMethod(location: Location): Boolean {
    val method = location.safeMethod() ?: return false
    val allLineLocations = method.safeAllLineLocations()
    if (allLineLocations.isEmpty()) return false
    if (allLineLocations.size == 1) return true
    
    val inlineFunctionBorders = method.getInlineFunctionAndArgumentVariablesToBordersMap().values
    return allLineLocations
        .mapNotNull { loc ->
            if (!isKotlinFakeLineNumber(loc) &&
                !inlineFunctionBorders.any { loc in it })
                loc.lineNumber()
            else
                null
        }
        .toHashSet()
        .size == 1
}

fun findElementAtLine(file: KtFile, line: Int): PsiElement? {
    val lineStartOffset = file.getLineStartOffset(line) ?: return null
    val lineEndOffset = file.getLineEndOffset(line) ?: return null

    return runReadAction {
        var topMostElement: PsiElement? = null
        var elementAt: PsiElement?
        for (offset in lineStartOffset until lineEndOffset) {
            elementAt = file.findElementAt(offset)
            if (elementAt != null) {
                topMostElement = CodeInsightUtils.getTopmostElementAtOffset(elementAt, offset)
                if (topMostElement is KtElement) {
                    break
                }
            }
        }

        topMostElement
    }
}

fun findCallByEndToken(element: PsiElement): KtCallExpression? {
    if (element is KtElement) return null

    return when (element.node.elementType) {
        KtTokens.RPAR -> (element.parent as? KtValueArgumentList)?.parent as? KtCallExpression
        KtTokens.RBRACE -> when (val braceParent = CodeInsightUtils.getTopParentWithEndOffset(element, KtCallExpression::class.java)) {
            is KtCallExpression -> braceParent
            is KtLambdaArgument -> braceParent.parent as? KtCallExpression
            is KtValueArgument -> (braceParent.parent as? KtValueArgumentList)?.parent as? KtCallExpression
            else -> null
        }
        else -> null
    }
}

val DebuggerContextImpl.canRunEvaluation: Boolean
    get() = debugProcess?.canRunEvaluation ?: false

val DebugProcessImpl.canRunEvaluation: Boolean
    get() = suspendManager.pausedContext != null
