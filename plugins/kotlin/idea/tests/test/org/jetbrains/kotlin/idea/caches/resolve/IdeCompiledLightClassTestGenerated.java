// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package org.jetbrains.kotlin.idea.caches.resolve;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.idea.test.JUnit3RunnerWithInners;
import org.jetbrains.kotlin.idea.test.KotlinTestUtils;
import org.jetbrains.kotlin.test.TestMetadata;
import org.jetbrains.kotlin.idea.test.TestRoot;
import org.junit.runner.RunWith;
import static org.jetbrains.kotlin.idea.base.plugin.artifacts.TestKotlinArtifacts.compilerTestData;

/**
 * This class is generated by {@link org.jetbrains.kotlin.testGenerator.generator.TestGenerator}.
 * DO NOT MODIFY MANUALLY.
 */
@SuppressWarnings("all")
@TestRoot("idea/tests")
@TestDataPath("$CONTENT_ROOT")
@RunWith(JUnit3RunnerWithInners.class)
@TestMetadata("../../../../out/kotlinc-testdata-2/compiler/testData/asJava/lightClasses")
public abstract class IdeCompiledLightClassTestGenerated extends AbstractIdeCompiledLightClassTest {
    @RunWith(JUnit3RunnerWithInners.class)
    @TestMetadata("../../../../out/kotlinc-testdata-2/compiler/testData/asJava/lightClasses/delegation")
    public static class Delegation extends AbstractIdeCompiledLightClassTest {
        private void runTest(String testDataFilePath) throws Exception {
            KotlinTestUtils.runTest(this::doTest, this, testDataFilePath);
        }

        @Override
        protected void setUp() {
            compilerTestData("compiler/testData/asJava/lightClasses/delegation");
            super.setUp();
        }

        @TestMetadata("Function.kt")
        public void testFunction() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/delegation/Function.kt"));
        }

        @TestMetadata("Property.kt")
        public void testProperty() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/delegation/Property.kt"));
        }
    }

    @RunWith(JUnit3RunnerWithInners.class)
    @TestMetadata("../../../../out/kotlinc-testdata-2/compiler/testData/asJava/lightClasses/facades")
    public static class Facades extends AbstractIdeCompiledLightClassTest {
        private void runTest(String testDataFilePath) throws Exception {
            KotlinTestUtils.runTest(this::doTest, this, testDataFilePath);
        }

        @Override
        protected void setUp() {
            compilerTestData("compiler/testData/asJava/lightClasses/facades");
            super.setUp();
        }

        @TestMetadata("AllPrivate.kt")
        public void testAllPrivate() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/facades/AllPrivate.kt"));
        }

        @TestMetadata("MultiFile.kt")
        public void testMultiFile() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/facades/MultiFile.kt"));
        }

        @TestMetadata("SingleFile.kt")
        public void testSingleFile() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/facades/SingleFile.kt"));
        }

        @TestMetadata("SingleJvmClassName.kt")
        public void testSingleJvmClassName() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/facades/SingleJvmClassName.kt"));
        }
    }

    @RunWith(JUnit3RunnerWithInners.class)
    @TestMetadata("../../../../out/kotlinc-testdata-2/compiler/testData/asJava/lightClasses/nullabilityAnnotations")
    public static class NullabilityAnnotations extends AbstractIdeCompiledLightClassTest {
        private void runTest(String testDataFilePath) throws Exception {
            KotlinTestUtils.runTest(this::doTest, this, testDataFilePath);
        }

        @Override
        protected void setUp() {
            compilerTestData("compiler/testData/asJava/lightClasses/nullabilityAnnotations");
            super.setUp();
        }

        @TestMetadata("Class.kt")
        public void testClass() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/nullabilityAnnotations/Class.kt"));
        }

        @TestMetadata("ClassObjectField.kt")
        public void testClassObjectField() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/nullabilityAnnotations/ClassObjectField.kt"));
        }

        @TestMetadata("ClassWithConstructor.kt")
        public void testClassWithConstructor() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/nullabilityAnnotations/ClassWithConstructor.kt"));
        }

        @TestMetadata("ClassWithConstructorAndProperties.kt")
        public void testClassWithConstructorAndProperties() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/nullabilityAnnotations/ClassWithConstructorAndProperties.kt"));
        }

        @TestMetadata("FileFacade.kt")
        public void testFileFacade() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/nullabilityAnnotations/FileFacade.kt"));
        }

        @TestMetadata("Generic.kt")
        public void testGeneric() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/nullabilityAnnotations/Generic.kt"));
        }

        @TestMetadata("IntOverridesAny.kt")
        public void testIntOverridesAny() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/nullabilityAnnotations/IntOverridesAny.kt"));
        }

        @TestMetadata("JvmOverloads.kt")
        public void testJvmOverloads() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/nullabilityAnnotations/JvmOverloads.kt"));
        }

        @TestMetadata("NullableUnitReturn.kt")
        public void testNullableUnitReturn() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/nullabilityAnnotations/NullableUnitReturn.kt"));
        }

        @TestMetadata("OverrideAnyWithUnit.kt")
        public void testOverrideAnyWithUnit() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/nullabilityAnnotations/OverrideAnyWithUnit.kt"));
        }

        @TestMetadata("PlatformTypes.kt")
        public void testPlatformTypes() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/nullabilityAnnotations/PlatformTypes.kt"));
        }

        @TestMetadata("Primitives.kt")
        public void testPrimitives() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/nullabilityAnnotations/Primitives.kt"));
        }

        @TestMetadata("PrivateInClass.kt")
        public void testPrivateInClass() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/nullabilityAnnotations/PrivateInClass.kt"));
        }

        @TestMetadata("Synthetic.kt")
        public void testSynthetic() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/nullabilityAnnotations/Synthetic.kt"));
        }

        @TestMetadata("Trait.kt")
        public void testTrait() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/nullabilityAnnotations/Trait.kt"));
        }

        @TestMetadata("UnitAsGenericArgument.kt")
        public void testUnitAsGenericArgument() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/nullabilityAnnotations/UnitAsGenericArgument.kt"));
        }

        @TestMetadata("UnitParameter.kt")
        public void testUnitParameter() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/nullabilityAnnotations/UnitParameter.kt"));
        }

        @TestMetadata("VoidReturn.kt")
        public void testVoidReturn() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/nullabilityAnnotations/VoidReturn.kt"));
        }
    }

    @RunWith(JUnit3RunnerWithInners.class)
    @TestMetadata("../../../../out/kotlinc-testdata-2/compiler/testData/asJava/lightClasses/object")
    public static class Object extends AbstractIdeCompiledLightClassTest {
        private void runTest(String testDataFilePath) throws Exception {
            KotlinTestUtils.runTest(this::doTest, this, testDataFilePath);
        }

        @Override
        protected void setUp() {
            compilerTestData("compiler/testData/asJava/lightClasses/object");
            super.setUp();
        }

        @TestMetadata("SimpleObject.kt")
        public void testSimpleObject() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/object/SimpleObject.kt"));
        }
    }

    @RunWith(JUnit3RunnerWithInners.class)
    @TestMetadata("../../../../out/kotlinc-testdata-2/compiler/testData/asJava/lightClasses/publicField")
    public static class PublicField extends AbstractIdeCompiledLightClassTest {
        private void runTest(String testDataFilePath) throws Exception {
            KotlinTestUtils.runTest(this::doTest, this, testDataFilePath);
        }

        @Override
        protected void setUp() {
            compilerTestData("compiler/testData/asJava/lightClasses/publicField");
            super.setUp();
        }

        @TestMetadata("CompanionObject.kt")
        public void testCompanionObject() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/publicField/CompanionObject.kt"));
        }

        @TestMetadata("Simple.kt")
        public void testSimple() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/publicField/Simple.kt"));
        }
    }

    @RunWith(JUnit3RunnerWithInners.class)
    @TestMetadata("../../../../out/kotlinc-testdata-2/compiler/testData/asJava/lightClasses")
    public static class Uncategorized extends AbstractIdeCompiledLightClassTest {
        private void runTest(String testDataFilePath) throws Exception {
            KotlinTestUtils.runTest(this::doTest, this, testDataFilePath);
        }

        @Override
        protected void setUp() {
            compilerTestData("compiler/testData/asJava/lightClasses");
            super.setUp();
        }

        @TestMetadata("AnnotatedParameterInEnumConstructor.kt")
        public void testAnnotatedParameterInEnumConstructor() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/AnnotatedParameterInEnumConstructor.kt"));
        }

        @TestMetadata("AnnotatedParameterInInnerClassConstructor.kt")
        public void testAnnotatedParameterInInnerClassConstructor() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/AnnotatedParameterInInnerClassConstructor.kt"));
        }

        @TestMetadata("AnnotatedPropertyWithSites.kt")
        public void testAnnotatedPropertyWithSites() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/AnnotatedPropertyWithSites.kt"));
        }

        @TestMetadata("AnnotationClass.kt")
        public void testAnnotationClass() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/AnnotationClass.kt"));
        }

        @TestMetadata("AnnotationJvmRepeatable.kt")
        public void testAnnotationJvmRepeatable() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/AnnotationJvmRepeatable.kt"));
        }

        @TestMetadata("AnnotationKotlinAndJavaRepeatable.kt")
        public void testAnnotationKotlinAndJavaRepeatable() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/AnnotationKotlinAndJavaRepeatable.kt"));
        }

        @TestMetadata("AnnotationKotlinAndJvmRepeatable.kt")
        public void testAnnotationKotlinAndJvmRepeatable() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/AnnotationKotlinAndJvmRepeatable.kt"));
        }

        @TestMetadata("AnnotationRepeatable.kt")
        public void testAnnotationRepeatable() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/AnnotationRepeatable.kt"));
        }

        @TestMetadata("Constructors.kt")
        public void testConstructors() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/Constructors.kt"));
        }

        @TestMetadata("DataClassWithCustomImplementedMembers.kt")
        public void testDataClassWithCustomImplementedMembers() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/DataClassWithCustomImplementedMembers.kt"));
        }

        @TestMetadata("DelegatedNested.kt")
        public void testDelegatedNested() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/DelegatedNested.kt"));
        }

        @TestMetadata("Delegation.kt")
        public void testDelegation() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/Delegation.kt"));
        }

        @TestMetadata("DeprecatedEnumEntry.kt")
        public void testDeprecatedEnumEntry() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/DeprecatedEnumEntry.kt"));
        }

        @TestMetadata("DeprecatedNotHiddenInClass.kt")
        public void testDeprecatedNotHiddenInClass() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/DeprecatedNotHiddenInClass.kt"));
        }

        @TestMetadata("DollarsInName.kt")
        public void testDollarsInName() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/DollarsInName.kt"));
        }

        @TestMetadata("DollarsInNameNoPackage.kt")
        public void testDollarsInNameNoPackage() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/DollarsInNameNoPackage.kt"));
        }

        @TestMetadata("ExtendingInterfaceWithDefaultImpls.kt")
        public void testExtendingInterfaceWithDefaultImpls() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/ExtendingInterfaceWithDefaultImpls.kt"));
        }

        @TestMetadata("HiddenDeprecated.kt")
        public void testHiddenDeprecated() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/HiddenDeprecated.kt"));
        }

        @TestMetadata("HiddenDeprecatedInClass.kt")
        public void testHiddenDeprecatedInClass() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/HiddenDeprecatedInClass.kt"));
        }

        @TestMetadata("InheritingInterfaceDefaultImpls.kt")
        public void testInheritingInterfaceDefaultImpls() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/InheritingInterfaceDefaultImpls.kt"));
        }

        @TestMetadata("InlineReified.kt")
        public void testInlineReified() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/InlineReified.kt"));
        }

        @TestMetadata("JvmNameOnMember.kt")
        public void testJvmNameOnMember() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/JvmNameOnMember.kt"));
        }

        @TestMetadata("JvmStatic.kt")
        public void testJvmStatic() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/JvmStatic.kt"));
        }

        @TestMetadata("LocalFunctions.kt")
        public void testLocalFunctions() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/LocalFunctions.kt"));
        }

        @TestMetadata("NestedObjects.kt")
        public void testNestedObjects() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/NestedObjects.kt"));
        }

        @TestMetadata("NonDataClassWithComponentFunctions.kt")
        public void testNonDataClassWithComponentFunctions() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/NonDataClassWithComponentFunctions.kt"));
        }

        @TestMetadata("OnlySecondaryConstructors.kt")
        public void testOnlySecondaryConstructors() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/OnlySecondaryConstructors.kt"));
        }

        @TestMetadata("PublishedApi.kt")
        public void testPublishedApi() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/PublishedApi.kt"));
        }

        @TestMetadata("SpecialAnnotationsOnAnnotationClass.kt")
        public void testSpecialAnnotationsOnAnnotationClass() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/SpecialAnnotationsOnAnnotationClass.kt"));
        }

        @TestMetadata("StubOrderForOverloads.kt")
        public void testStubOrderForOverloads() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/StubOrderForOverloads.kt"));
        }

        @TestMetadata("TypePararametersInClass.kt")
        public void testTypePararametersInClass() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/TypePararametersInClass.kt"));
        }

        @TestMetadata("VarArgs.kt")
        public void testVarArgs() throws Exception {
            runTest(compilerTestData("compiler/testData/asJava/lightClasses/VarArgs.kt"));
        }
    }
}
