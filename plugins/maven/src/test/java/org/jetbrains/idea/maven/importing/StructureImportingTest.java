/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.idea.maven.importing;

import com.intellij.compiler.CompilerConfiguration;
import com.intellij.compiler.impl.javaCompiler.javac.JavacConfiguration;
import com.intellij.maven.testFramework.MavenMultiVersionImportingTestCase;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.LanguageLevelUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.testFramework.PlatformTestUtil;
import org.jetbrains.idea.maven.project.MavenGeneralSettings;
import org.jetbrains.idea.maven.project.MavenProject;
import org.junit.Assume;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class StructureImportingTest extends MavenMultiVersionImportingTestCase {
  @Test
  public void testInheritProjectJdkForModules() {
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>");

    assertTrue(ModuleRootManager.getInstance(getModule("project")).isSdkInherited());
  }

  @Test
  public void testDoNotResetSomeSettingsAfterReimport() {
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>");

    Sdk sdk = setupJdkForModule("project");

    importProject();

    if (supportsKeepingManualChanges()) {
      assertFalse(ModuleRootManager.getInstance(getModule("project")).isSdkInherited());
      assertEquals(sdk, ModuleRootManager.getInstance(getModule("project")).getSdk());
    }
    else {
      assertTrue(ModuleRootManager.getInstance(getModule("project")).isSdkInherited());
    }
  }

  @Test
  public void testMarkModulesAsMavenized() {
    createModule("userModule");

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<packaging>pom</packaging>" +
                     "<version>1</version>" +

                     "<modules>" +
                     "  <module>m1</module>" +
                     "</modules>");

    createModulePom("m1", "<groupId>test</groupId>" +
                          "<artifactId>m1</artifactId>" +
                          "<version>1</version>");

    importProject();
    assertModules("project", "m1", "userModule");
    assertMavenizedModule("project");
    assertMavenizedModule("m1");
    assertNotMavenizedModule("userModule");

    configConfirmationForYesAnswer();
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<packaging>pom</packaging>" +
                     "<version>1</version>" +

                     "<modules>" +
                     "  <module>m2</module>" +
                     "</modules>");

    createModulePom("m2", "<groupId>test</groupId>" +
                          "<artifactId>m2</artifactId>" +
                          "<version>1</version>");

    importProject();
    assertModules("project", "m2", "userModule");
    assertMavenizedModule("project");
    assertMavenizedModule("m2");
    assertNotMavenizedModule("userModule");
  }


  @Test
  public void testModulesWithSlashesRegularAndBack() {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<packaging>pom</packaging>" +
                     "<version>1</version>" +

                     "<modules>" +
                     "  <module>dir\\m1</module>" +
                     "  <module>dir/m2</module>" +
                     "</modules>");

    createModulePom("dir/m1", "<groupId>test</groupId>" +
                              "<artifactId>m1</artifactId>" +
                              "<version>1</version>");

    createModulePom("dir/m2", "<groupId>test</groupId>" +
                              "<artifactId>m2</artifactId>" +
                              "<version>1</version>");

    importProject();
    assertModules("project", "m1", "m2");

    List<MavenProject> roots = getProjectsTree().getRootProjects();
    assertEquals(1, roots.size());
    assertEquals("project", roots.get(0).getMavenId().getArtifactId());

    List<MavenProject> modules = getProjectsTree().getModules(roots.get(0));
    assertEquals(2, modules.size());
    assertEquals("m1", modules.get(0).getMavenId().getArtifactId());
    assertEquals("m2", modules.get(1).getMavenId().getArtifactId());
  }

  @Test
  public void testModulesAreNamedAfterArtifactIds() {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<packaging>pom</packaging>" +
                     "<version>1</version>" +
                     "<name>name</name>" +

                     "<modules>" +
                     "  <module>dir1</module>" +
                     "  <module>dir2</module>" +
                     "</modules>");

    createModulePom("dir1", "<groupId>test</groupId>" +
                            "<artifactId>m1</artifactId>" +
                            "<version>1</version>" +
                            "<name>name1</name>");

    createModulePom("dir2", "<groupId>test</groupId>" +
                            "<artifactId>m2</artifactId>" +
                            "<version>1</version>" +
                            "<name>name2</name>");
    importProject();
    assertModules("project", "m1", "m2");
  }

  @Test
  public void testModulesWithSlashesAtTheEnds() {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<packaging>pom</packaging>" +
                     "<version>1</version>" +

                     "<modules>" +
                     "  <module>m1/</module>" +
                     "  <module>m2\\</module>" +
                     "  <module>m3//</module>" +
                     "</modules>");

    createModulePom("m1", "<groupId>test</groupId>" +
                          "<artifactId>m1</artifactId>" +
                          "<version>1</version>");

    createModulePom("m2", "<groupId>test</groupId>" +
                          "<artifactId>m2</artifactId>" +
                          "<version>1</version>");

    createModulePom("m3", "<groupId>test</groupId>" +
                          "<artifactId>m3</artifactId>" +
                          "<version>1</version>");

    importProject();
    assertModules("project", "m1", "m2", "m3");
  }

  @Test
  public void testModulesWithSameArtifactId() {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<packaging>pom</packaging>" +
                     "<version>1</version>" +

                     "<modules>" +
                     "  <module>dir1/m</module>" +
                     "  <module>dir2/m</module>" +
                     "</modules>");

    createModulePom("dir1/m", "<groupId>test.group1</groupId>" +
                              "<artifactId>m</artifactId>" +
                              "<version>1</version>");

    createModulePom("dir2/m", "<groupId>test.group2</groupId>" +
                              "<artifactId>m</artifactId>" +
                              "<version>1</version>");

    importProject();
    assertModules("project", "m (1) (test.group1)", "m (2) (test.group2)");
  }

  @Test
  public void testModulesWithSameArtifactIdAndGroup() {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<packaging>pom</packaging>" +
                     "<version>1</version>" +

                     "<modules>" +
                     "  <module>dir1/m</module>" +
                     "  <module>dir2/m</module>" +
                     "</modules>");

    createModulePom("dir1/m", "<groupId>test</groupId>" +
                              "<artifactId>m</artifactId>" +
                              "<version>1</version>");

    createModulePom("dir2/m", "<groupId>test</groupId>" +
                              "<artifactId>m</artifactId>" +
                              "<version>1</version>");

    importProject();
    assertModules("project", "m (1)", "m (2)");
  }

  @Test
  public void testModuleWithRelativePath() {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<packaging>pom</packaging>" +
                     "<version>1</version>" +

                     "<modules>" +
                     "  <module>../m</module>" +
                     "</modules>");

    createModulePom("../m", "<groupId>test</groupId>" +
                            "<artifactId>m</artifactId>" +
                            "<version>1</version>");

    importProject();
    assertModules("project", "m");
  }

  @Test
  public void testModuleWithRelativeParent() {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<parent>" +
                     "  <groupId>test</groupId>" +
                     "  <artifactId>parent</artifactId>" +
                     "  <version>1</version>" +
                     "  <relativePath>../parent</relativePath>" +
                     "</parent>");

    createModulePom("../parent", "<groupId>test</groupId>" +
                                 "<artifactId>parent</artifactId>" +
                                 "<version>1</version>" +
                                 "<packaging>pom</packaging>");

    importProject();
    assertModules("project");
  }

  @Test
  public void testModulePathsAsProperties() {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<packaging>pom</packaging>" +
                     "<version>1</version>" +

                     "<properties>" +
                     "  <module1>m1</module1>" +
                     "  <module2>m2</module2>" +
                     "</properties>" +

                     "<modules>" +
                     "  <module>${module1}</module>" +
                     "  <module>${module2}</module>" +
                     "</modules>");

    createModulePom("m1", "<groupId>test</groupId>" +
                          "<artifactId>m1</artifactId>" +
                          "<version>1</version>");

    createModulePom("m2", "<groupId>test</groupId>" +
                          "<artifactId>m2</artifactId>" +
                          "<version>1</version>");

    importProject();
    assertModules("project", "m1", "m2");

    List<MavenProject> roots = getProjectsTree().getRootProjects();
    assertEquals(1, roots.size());
    assertEquals("project", roots.get(0).getMavenId().getArtifactId());

    List<MavenProject> modules = getProjectsTree().getModules(roots.get(0));
    assertEquals(2, modules.size());
    assertEquals("m1", modules.get(0).getMavenId().getArtifactId());
    assertEquals("m2", modules.get(1).getMavenId().getArtifactId());
  }

  @Test
  public void testRecursiveParent() {
    createProjectPom("<parent>" +
                     "  <groupId>org.apache.maven.archetype.test</groupId>" +
                     "  <artifactId>test-create-2</artifactId>" +
                     "  <version>1.0-SNAPSHOT</version>" +
                     "</parent>" +

                     "<artifactId>test-create-2</artifactId>" +
                     "<name>Maven archetype Test create-2-subModule</name>" +
                     "<packaging>pom</packaging>");
    importProjectWithErrors();
  }

  @Test
  public void testParentWithoutARelativePath() {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<packaging>pom</packaging>" +
                     "<version>1</version>" +

                     "<properties>" +
                     "  <moduleName>m1</moduleName>" +
                     "</properties>" +

                     "<modules>" +
                     "  <module>modules/m</module>" +
                     "</modules>");

    createModulePom("modules/m", "<groupId>test</groupId>" +
                                 "<artifactId>${moduleName}</artifactId>" +
                                 "<version>1</version>" +

                                 "<parent>" +
                                 "  <groupId>test</groupId>" +
                                 "  <artifactId>project</artifactId>" +
                                 "  <version>1</version>" +
                                 "</parent>");

    importProject();
    assertModules("project", mn("project", "m1"));

    List<MavenProject> roots = getProjectsTree().getRootProjects();
    assertEquals(1, roots.size());
    assertEquals("project", roots.get(0).getMavenId().getArtifactId());

    List<MavenProject> modules = getProjectsTree().getModules(roots.get(0));
    assertEquals(1, modules.size());
    assertEquals("m1", modules.get(0).getMavenId().getArtifactId());
  }

  @Test
  public void testModuleWithPropertiesWithParentWithoutARelativePath() {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<packaging>pom</packaging>" +
                     "<version>1</version>" +

                     "<properties>" +
                     "  <moduleName>m1</moduleName>" +
                     "</properties>" +

                     "<modules>" +
                     "  <module>modules/m</module>" +
                     "</modules>");

    createModulePom("modules/m", "<groupId>test</groupId>" +
                                 "<artifactId>${moduleName}</artifactId>" +
                                 "<version>1</version>" +

                                 "<parent>" +
                                 "  <groupId>test</groupId>" +
                                 "  <artifactId>project</artifactId>" +
                                 "  <version>1</version>" +
                                 "</parent>");

    importProject();
    assertModules("project", mn("project", "m1"));

    List<MavenProject> roots = getProjectsTree().getRootProjects();
    assertEquals(1, roots.size());
    assertEquals("project", roots.get(0).getMavenId().getArtifactId());

    List<MavenProject> modules = getProjectsTree().getModules(roots.get(0));
    assertEquals(1, modules.size());
    assertEquals("m1", modules.get(0).getMavenId().getArtifactId());
  }

  @Test
  public void testParentInLocalRepository() throws Exception {
    if (!hasMavenInstallation()) return;

    final VirtualFile parent = createModulePom("parent",
                                               "<groupId>test</groupId>" +
                                               "<artifactId>parent</artifactId>" +
                                               "<version>1</version>" +
                                               "<packaging>pom</packaging>" +

                                               "<dependencies>" +
                                               "  <dependency>" +
                                               "    <groupId>junit</groupId>" +
                                               "    <artifactId>junit</artifactId>" +
                                               "    <version>4.0</version>" +
                                               "  </dependency>" +
                                               "</dependencies>");
    executeGoal("parent", "install");

    WriteAction.runAndWait(() -> parent.delete(null));


    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>m</artifactId>" +
                     "<version>1</version>" +

                     "<parent>" +
                     "  <groupId>test</groupId>" +
                     "  <artifactId>parent</artifactId>" +
                     "  <version>1</version>" +
                     "</parent>");

    importProject();
    assertModules("m");
    assertModuleLibDeps("m", "Maven: junit:junit:4.0");
  }

  @Test
  public void testParentInRemoteRepository() {
    String pathToJUnit = "asm/asm-parent/3.0";
    File parentDir = new File(getRepositoryPath(), pathToJUnit);

    removeFromLocalRepository(pathToJUnit);
    assertFalse(parentDir.exists());

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<parent>" +
                     "  <groupId>asm</groupId>" +
                     "  <artifactId>asm-parent</artifactId>" +
                     "  <version>3.0</version>" +
                     "</parent>");

    importProject();
    assertModules("project");

    assertTrue(parentDir.exists());

    assertEquals("asm-parent", getProjectsTree().getRootProjects().get(0).getParentId().getArtifactId());
    assertTrue(new File(parentDir, "asm-parent-3.0.pom").exists());
  }

  @Test
  public void testCreatingModuleGroups() {
    VirtualFile p1 = createModulePom("project1",
                                     "<groupId>test</groupId>" +
                                     "<artifactId>project1</artifactId>" +
                                     "<version>1</version>" +
                                     "<packaging>pom</packaging>" +

                                     "<modules>" +
                                     "  <module>m1</module>" +
                                     "</modules>");

    createModulePom("project1/m1",
                    "<groupId>test</groupId>" +
                    "<artifactId>m1</artifactId>" +
                    "<version>1</version>");

    VirtualFile p2 = createModulePom("project2",
                                     "<groupId>test</groupId>" +
                                     "<artifactId>project2</artifactId>" +
                                     "<version>1</version>" +
                                     "<packaging>pom</packaging>" +

                                     "<modules>" +
                                     "  <module>m2</module>" +
                                     "</modules>");

    createModulePom("project2/m2",
                    "<groupId>test</groupId>" +
                    "<artifactId>m2</artifactId>" +
                    "<version>1</version>" +
                    "<packaging>pom</packaging>" +

                    "<modules>" +
                    "  <module>m3</module>" +
                    "</modules>");

    createModulePom("project2/m2/m3",
                    "<groupId>test</groupId>" +
                    "<artifactId>m3</artifactId>" +
                    "<version>1</version>");

    getMavenImporterSettings().setCreateModuleGroups(true);
    importProjects(p1, p2);
    assertModules("project1", "project2", "m1", "m2", "m3");

    assertModuleGroupPath("project1", "project1 and modules");
    assertModuleGroupPath("m1", "project1 and modules");
    assertModuleGroupPath("project2", "project2 and modules");
    assertModuleGroupPath("m2", "project2 and modules", "m2 and modules");
    assertModuleGroupPath("m3", "project2 and modules", "m2 and modules");
  }

  @Test
  public void testDoesNotCreateUnnecessaryTopLevelModuleGroup() {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>" +

                     "<modules>" +
                     "  <module>m1</module>" +
                     "</modules>");

    createModulePom("m1",
                    "<groupId>test</groupId>" +
                    "<artifactId>m1</artifactId>" +
                    "<version>1</version>" +
                    "<packaging>pom</packaging>" +

                    "<modules>" +
                    "  <module>m2</module>" +
                    "</modules>");

    createModulePom("m1/m2",
                    "<groupId>test</groupId>" +
                    "<artifactId>m2</artifactId>" +
                    "<version>1</version>");

    getMavenImporterSettings().setCreateModuleGroups(true);
    importProject();
    assertModules("project", "m1", "m2");

    assertModuleGroupPath("project");
    assertModuleGroupPath("m1", "m1 and modules");
    assertModuleGroupPath("m2", "m1 and modules");
  }

  @Test
  public void testModuleGroupsWhenNotCreatingModulesForAggregatorProjects() {
    if (!supportsCreateAggregatorOption() || !supportModuleGroups()) return;

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>" +

                     "<modules>" +
                     "  <module>module1</module>" +
                     "</modules>");

    createModulePom("module1",
                    "<groupId>test</groupId>" +
                    "<artifactId>module1</artifactId>" +
                    "<version>1</version>" +
                    "<packaging>pom</packaging>" +

                    "<modules>" +
                    "  <module>module2</module>" +
                    "</modules>");

    createModulePom("module1/module2",
                    "<groupId>test</groupId>" +
                    "<artifactId>module2</artifactId>" +
                    "<version>1</version>");

    getMavenImporterSettings().setCreateModuleGroups(true);
    getMavenImporterSettings().setCreateModulesForAggregators(false);
    importProject();
    assertModules("module2");

    assertModuleGroupPath("module2", "module1 and modules");
  }

  @Test
  public void testReimportingProjectWhenCreatingModuleGroupsSettingChanged() {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>" +

                     "<modules>" +
                     "  <module>module1</module>" +
                     "</modules>");

    createModulePom("module1",
                    "<groupId>test</groupId>" +
                    "<artifactId>module1</artifactId>" +
                    "<version>1</version>" +
                    "<packaging>pom</packaging>" +

                    "<modules>" +
                    "  <module>module2</module>" +
                    "</modules>");

    createModulePom("module1/module2",
                    "<groupId>test</groupId>" +
                    "<artifactId>module2</artifactId>" +
                    "<version>1</version>");
    importProject();
    assertModules("project", "module1", "module2");

    assertModuleGroupPath("module2");

    getMavenImporterSettings().setCreateModuleGroups(true);
    myProjectsManager.performScheduledImportInTests();
    assertModuleGroupPath("module2", "module1 and modules");
  }

  @Test
  public void testModuleGroupsWhenProjectWithDuplicateNameEmerges() {
    VirtualFile p1 = createModulePom("project1",
                                     "<groupId>test</groupId>" +
                                     "<artifactId>project1</artifactId>" +
                                     "<version>1</version>" +
                                     "<packaging>pom</packaging>" +

                                     "<modules>" +
                                     "  <module>m1</module>" +
                                     "</modules>");

    createModulePom("project1/m1",
                    "<groupId>test</groupId>" +
                    "<artifactId>module</artifactId>" +
                    "<version>1</version>");

    VirtualFile p2 = createModulePom("project2",
                                     "<groupId>test</groupId>" +
                                     "<artifactId>project2</artifactId>" +
                                     "<version>1</version>" +
                                     "<packaging>pom</packaging>");

    //createModulePom("m2",
    //                "<groupId>test</groupId>" +
    //                "<artifactId>m2</artifactId>" +
    //                "<version>1</version>" +
    //                "<packaging>pom</packaging>");

    getMavenImporterSettings().setCreateModuleGroups(true);
    importProjects(p1, p2);
    assertModules("project1", "project2", "module");

    if (supportModuleGroups()) {
      assertModuleGroupPath("project1", "project1 and modules");
      assertModuleGroupPath("module", "project1 and modules");
    }

    p2 = createModulePom("project2",
                         "<groupId>test</groupId>" +
                         "<artifactId>project2</artifactId>" +
                         "<version>1</version>" +
                         "<packaging>pom</packaging>" +

                         "<modules>" +
                         "  <module>m2</module>" +
                         "</modules>");

    createModulePom("project2/m2",
                    "<groupId>test</groupId>" +
                    "<artifactId>module</artifactId>" +
                    "<version>1</version>");

    updateProjectsAndImport(p2); // should not fail to map module names. 

    if (supportsKeepingModulesFromPreviousImport()) {
      assertModules("project1", "project2", "module", "module (1)");
    }
    else {
      assertModules("project1", "project2", "module (1)", "module (2)");
    }

    if (supportModuleGroups()) {
      assertModuleGroupPath("project1", "project1 and modules");
      assertModuleGroupPath("module", "project1 and modules");
      assertModuleGroupPath("project2", "project2 and modules");
      assertModuleGroupPath("module (1)", "project2 and modules");
    }
  }

  @Test
  public void testLanguageLevel() {
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>" +

                  "<build>" +
                  "  <plugins>" +
                  "    <plugin>" +
                  "      <groupId>org.apache.maven.plugins</groupId>" +
                  "      <artifactId>maven-compiler-plugin</artifactId>" +
                  "      <configuration>" +
                  "        <source>1.4</source>" +
                  "      </configuration>" +
                  "    </plugin>" +
                  "  </plugins>" +
                  "</build>");

    assertModules("project");
    assertEquals(LanguageLevel.JDK_1_4, getLanguageLevelForModule());
  }

  @Test
  public void testLanguageLevelFromDefaultCompileExecutionConfiguration() {
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>" +

                  "<build>" +
                  "  <plugins>" +
                  "    <plugin>" +
                  "      <groupId>org.apache.maven.plugins</groupId>" +
                  "      <artifactId>maven-compiler-plugin</artifactId>" +
                  "      <executions>" +
                  "        <execution>" +
                  "          <id>default-compile</id>" +
                  "             <configuration>" +
                  "                <source>1.8</source>" +
                  "             </configuration>" +
                  "        </execution>" +
                  "      </executions>" +
                  "    </plugin>" +
                  "  </plugins>" +
                  "</build>");

    assertModules("project");
    assertEquals(LanguageLevel.JDK_1_8, getLanguageLevelForModule());
  }

  @Test
  public void testLanguageLevel6() {
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>" +

                  "<build>" +
                  "  <plugins>" +
                  "    <plugin>" +
                  "      <groupId>org.apache.maven.plugins</groupId>" +
                  "      <artifactId>maven-compiler-plugin</artifactId>" +
                  "      <configuration>" +
                  "        <source>1.6</source>" +
                  "      </configuration>" +
                  "    </plugin>" +
                  "  </plugins>" +
                  "</build>");

    assertModules("project");
    assertEquals(LanguageLevel.JDK_1_6, getLanguageLevelForModule());
  }

  @Test
  public void testLanguageLevelX() {
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>" +
                  "<build>" +
                  "  <plugins>" +
                  "    <plugin>" +
                  "      <groupId>org.apache.maven.plugins</groupId>" +
                  "      <artifactId>maven-compiler-plugin</artifactId>" +
                  "      <configuration>" +
                  "        <source>99</source>" +
                  "      </configuration>" +
                  "    </plugin>" +
                  "  </plugins>" +
                  "</build>");

    assertModules("project");
    assertEquals(LanguageLevel.HIGHEST, getLanguageLevelForModule());
  }

  @Test
  public void testLanguageLevelWhenCompilerPluginIsNotSpecified() {
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>");

    assertModules("project");
    assertEquals(LanguageLevel.JDK_1_5, getLanguageLevelForModule());
  }

  @Test
  public void testLanguageLevelWhenConfigurationIsNotSpecified() {
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>" +

                  "<build>" +
                  "  <plugins>" +
                  "    <plugin>" +
                  "      <groupId>org.apache.maven.plugins</groupId>" +
                  "      <artifactId>maven-compiler-plugin</artifactId>" +
                  "    </plugin>" +
                  "  </plugins>" +
                  "</build>");

    assertModules("project");
    assertEquals(LanguageLevel.JDK_1_5, getLanguageLevelForModule());
  }

  @Test
  public void testLanguageLevelWhenSourceLanguageLevelIsNotSpecified() {
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>" +

                  "<build>" +
                  "  <plugins>" +
                  "    <plugin>" +
                  "      <groupId>org.apache.maven.plugins</groupId>" +
                  "      <artifactId>maven-compiler-plugin</artifactId>" +
                  "      <configuration>" +
                  "      </configuration>" +
                  "    </plugin>" +
                  "  </plugins>" +
                  "</build>");

    assertModules("project");
    assertEquals(LanguageLevel.JDK_1_5, getLanguageLevelForModule());
  }

  @Test
  public void testLanguageLevelFromPluginManagementSection() {
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>" +

                  "<build>" +
                  "  <pluginManagement>" +
                  "    <plugins>" +
                  "      <plugin>" +
                  "        <groupId>org.apache.maven.plugins</groupId>" +
                  "        <artifactId>maven-compiler-plugin</artifactId>" +
                  "        <configuration>" +
                  "          <source>1.4</source>" +
                  "        </configuration>" +
                  "      </plugin>" +
                  "    </plugins>" +
                  "  </pluginManagement>" +
                  "</build>");

    assertModules("project");
    assertEquals(LanguageLevel.JDK_1_4, getLanguageLevelForModule());
  }

  @Test
  public void testLanguageLevelFromParentPluginManagementSection() {
    createModulePom("parent",
                    "<groupId>test</groupId>" +
                    "<artifactId>parent</artifactId>" +
                    "<version>1</version>" +
                    "<packaging>pom</packaging>" +

                    "<build>" +
                    "  <pluginManagement>" +
                    "    <plugins>" +
                    "      <plugin>" +
                    "        <groupId>org.apache.maven.plugins</groupId>" +
                    "        <artifactId>maven-compiler-plugin</artifactId>" +
                    "        <configuration>" +
                    "          <source>1.4</source>" +
                    "        </configuration>" +
                    "      </plugin>" +
                    "    </plugins>" +
                    "  </pluginManagement>" +
                    "</build>");

    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>" +

                  "<parent>" +
                  "  <groupId>test</groupId>" +
                  "  <artifactId>parent</artifactId>" +
                  "  <version>1</version>" +
                  "  <relativePath>parent/pom.xml</relativePath>" +
                  "</parent>");

    assertModules("project");
    assertEquals(LanguageLevel.JDK_1_4, getLanguageLevelForModule());
  }

  @Test
  public void testOverridingLanguageLevelFromPluginManagementSection() {
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>" +

                  "<build>" +
                  "  <pluginManagement>" +
                  "    <plugins>" +
                  "      <plugin>" +
                  "        <groupId>org.apache.maven.plugins</groupId>" +
                  "        <artifactId>maven-compiler-plugin</artifactId>" +
                  "        <configuration>" +
                  "          <source>1.4</source>" +
                  "        </configuration>" +
                  "      </plugin>" +
                  "    </plugins>" +
                  "  </pluginManagement>" +
                  "  <plugins>" +
                  "    <plugin>" +
                  "      <groupId>org.apache.maven.plugins</groupId>" +
                  "      <artifactId>maven-compiler-plugin</artifactId>" +
                  "      <configuration>" +
                  "        <source>1.3</source>" +
                  "      </configuration>" +
                  "    </plugin>" +
                  "  </plugins>" +
                  "</build>");

    assertModules("project");
    assertEquals(LanguageLevel.JDK_1_3, getLanguageLevelForModule());
  }

  @Test
  public void testPreviewLanguageLevelOneLine() {
    doTestPreview("<compilerArgs>--enable-preview</compilerArgs>\n");
  }

  @Test
  public void testPreviewLanguageLevelArg() {
    doTestPreview("<compilerArgs><arg>--enable-preview</arg></compilerArgs>\n");
  }

  @Test
  public void testPreviewLanguageLevelCompilerArg() {
    doTestPreview("<compilerArgs><compilerArg>--enable-preview</compilerArg></compilerArgs>\n");
  }

  private void doTestPreview(String compilerArgs) {
    int feature = LanguageLevel.HIGHEST.toJavaVersion().feature;
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>" +

                  "<build>" +
                  "  <plugins>" +
                  "    <plugin>\n" +
                  "      <groupId>org.apache.maven.plugins</groupId>\n" +
                  "      <artifactId>maven-compiler-plugin</artifactId>\n" +
                  "      <version>3.8.0</version>\n" +
                  "      <configuration>\n" +
                  "          <release>" + feature + "</release>\n" +
                  compilerArgs +
                  "          <forceJavacCompilerUse>true</forceJavacCompilerUse>\n" +
                  "      </configuration>\n" +
                  "    </plugin>" +
                  "  </plugins>" +
                  "</build>");

    assertModules("project");
    assertEquals(LanguageLevel.values()[LanguageLevel.HIGHEST.ordinal() + 1], getLanguageLevelForModule());
  }

  @Test
  public void testInheritingLanguageLevelFromPluginManagementSection() {
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>" +

                  "<build>" +
                  "  <pluginManagement>" +
                  "    <plugins>" +
                  "      <plugin>" +
                  "        <groupId>org.apache.maven.plugins</groupId>" +
                  "        <artifactId>maven-compiler-plugin</artifactId>" +
                  "        <configuration>" +
                  "          <source>1.4</source>" +
                  "        </configuration>" +
                  "      </plugin>" +
                  "    </plugins>" +
                  "  </pluginManagement>" +
                  "  <plugins>" +
                  "    <plugin>" +
                  "      <groupId>org.apache.maven.plugins</groupId>" +
                  "      <artifactId>maven-compiler-plugin</artifactId>" +
                  "      <configuration>" +
                  "          <target>1.5</target>" +
                  "      </configuration>" +
                  "    </plugin>" +
                  "  </plugins>" +
                  "</build>");

    assertModules("project");
    assertEquals(LanguageLevel.JDK_1_4, getLanguageLevelForModule());
  }

  private LanguageLevel getLanguageLevelForModule() {
    return LanguageLevelUtil.getCustomLanguageLevel(getModule("project"));
  }

  @Test
  public void testSettingTargetLevel() {
    JavacConfiguration.getOptions(myProject, JavacConfiguration.class).ADDITIONAL_OPTIONS_STRING = "-Xmm500m -Xms128m -target 1.5";

    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>" +

                  "<build>" +
                  "  <plugins>" +
                  "    <plugin>" +
                  "      <artifactId>maven-compiler-plugin</artifactId>" +
                  "        <configuration>" +
                  "          <target>1.3</target>" +
                  "        </configuration>" +
                  "     </plugin>" +
                  "  </plugins>" +
                  "</build>");

    assertEquals("-Xmm500m -Xms128m", JavacConfiguration.getOptions(myProject, JavacConfiguration.class).ADDITIONAL_OPTIONS_STRING.trim());

    Module module = getModule("project");

    String targetLevel = CompilerConfiguration.getInstance(myProject).getBytecodeTargetLevel(module);

    assertEquals("1.3", targetLevel);
  }

  @Test
  public void testSettingTargetLevelFromDefaultCompileExecutionConfiguration() {
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>" +

                  "<build>" +
                  "  <plugins>" +
                  "    <plugin>" +
                  "      <groupId>org.apache.maven.plugins</groupId>" +
                  "      <artifactId>maven-compiler-plugin</artifactId>" +
                  "      <executions>" +
                  "        <execution>" +
                  "          <id>default-compile</id>" +
                  "             <configuration>" +
                  "                <target>1.9</target>" +
                  "             </configuration>" +
                  "        </execution>" +
                  "      </executions>" +
                  "    </plugin>" +
                  "  </plugins>" +
                  "</build>");

    assertModules("project");
    Module module = getModule("project");
    String targetLevel = CompilerConfiguration.getInstance(myProject).getBytecodeTargetLevel(module);
    assertEquals(LanguageLevel.JDK_1_9, LanguageLevel.parse(targetLevel));
  }

  @Test
  public void testSettingTargetLevelFromParent() {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<packaging>pom</packaging>" +
                     "<version>1</version>" +

                     "<modules>" +
                     "  <module>m1</module>" +
                     "  <module>m2</module>" +
                     "</modules>" +

                     "<properties>" +
                     "<maven.compiler.target>1.3</maven.compiler.target>" +
                     "</properties>");

    createModulePom("m1", "<groupId>test</groupId>" +
                          "<artifactId>m1</artifactId>" +
                          "<version>1</version>" +

                          "<parent>" +
                          "<groupId>test</groupId>" +
                          "<artifactId>project</artifactId>" +
                          "<version>1</version>" +
                          "</parent>");

    createModulePom("m2", "<groupId>test</groupId>" +
                          "<artifactId>m2</artifactId>" +
                          "<version>1</version>" +

                          "<parent>" +
                          "<groupId>test</groupId>" +
                          "<artifactId>project</artifactId>" +
                          "<version>1</version>" +
                          "</parent>" +

                          "<build>" +
                          "  <plugins>" +
                          "    <plugin>" +
                          "      <artifactId>maven-compiler-plugin</artifactId>" +
                          "        <configuration>" +
                          "          <target>1.5</target>" +
                          "        </configuration>" +
                          "     </plugin>" +
                          "  </plugins>" +
                          "</build>");

    importProject();

    assertEquals("1.3", CompilerConfiguration.getInstance(myProject).getBytecodeTargetLevel(getModule("project")));
    assertEquals("1.3", CompilerConfiguration.getInstance(myProject).getBytecodeTargetLevel(getModule(mn("project", "m1"))));
    assertEquals("1.5", CompilerConfiguration.getInstance(myProject).getBytecodeTargetLevel(getModule(mn("project", "m2"))));
  }

  @Test
  public void testReleaseCompilerPropertyInPerSourceTypeModules() {
    Assume.assumeTrue(MavenProjectImporter.isImportToWorkspaceModelEnabled());

    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>" +

                  "<properties>" +
                  "  <maven.compiler.release>8</maven.compiler.release>" +
                  "  <maven.compiler.testRelease>11</maven.compiler.testRelease>" +
                  "</properties>" +
                  "" +
                  " <build>\n" +
                  "  <plugins>" +
                  "    <plugin>" +
                  "      <artifactId>maven-compiler-plugin</artifactId>" +
                  "      <version>3.10.0</version>" +
                  "    </plugin>" +
                  "  </plugins>" +
                  "</build>"
    );

    assertModules("project", "project.main", "project.test");
  }

  @Test
  public void testProjectWithBuiltExtension() {
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>" +

                  "<build>" +
                  " <extensions>" +
                  "   <extension>" +
                  "     <groupId>org.apache.maven.wagon</groupId>" +
                  "     <artifactId>wagon-webdav</artifactId>" +
                  "     <version>1.0-beta-2</version>" +
                  "    </extension>" +
                  "  </extensions>" +
                  "</build>");
    assertModules("project");
  }

  @Test
  public void testUsingPropertyInBuildExtensionsOfChildModule() {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>" +

                     "<properties>" +
                     "  <xxx>1.0-beta-2</xxx>" +
                     "</properties>" +

                     "<modules>" +
                     "  <module>m</module>" +
                     "</modules>");

    createModulePom("m", "<groupId>test</groupId>" +
                         "<artifactId>m</artifactId>" +

                         "<parent>" +
                         "  <groupId>test</groupId>" +
                         "  <artifactId>project</artifactId>" +
                         "  <version>1</version>" +
                         "</parent>" +

                         "<build>" +
                         "  <extensions>" +
                         "    <extension>" +
                         "      <groupId>org.apache.maven.wagon</groupId>" +
                         "      <artifactId>wagon-webdav</artifactId>" +
                         "      <version>${xxx}</version>" +
                         "    </extension>" +
                         "  </extensions>" +
                         "</build>");

    importProject();
    assertModules("project", mn("project", "m"));
  }

  @Test
  public void testFileProfileActivationInParentPom() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>" +

                     "  <profiles>" +
                     "    <profile>" +
                     "      <id>xxx</id>" +
                     "      <dependencies>" +
                     "        <dependency>" +
                     "          <groupId>junit</groupId>" +
                     "          <artifactId>junit</artifactId>" +
                     "          <version>4.0</version>" +
                     "        </dependency>" +
                     "      </dependencies>" +
                     "      <activation>" +
                     "        <file>" +
                     "          <exists>src/io.properties</exists>" +
                     "        </file>" +
                     "      </activation>" +
                     "    </profile>" +
                     "  </profiles>" +

                     "<modules>" +
                     "  <module>m1</module>" +
                     "  <module>m2</module>" +
                     "</modules>");

    createModulePom("m1", "<groupId>test</groupId>" +
                          "<artifactId>m1</artifactId>" +

                          "<parent>" +
                          "  <groupId>test</groupId>" +
                          "  <artifactId>project</artifactId>" +
                          "  <version>1</version>" +
                          "</parent>");

    createModulePom("m2", "<groupId>test</groupId>" +
                          "<artifactId>m2</artifactId>" +

                          "<parent>" +
                          "  <groupId>test</groupId>" +
                          "  <artifactId>project</artifactId>" +
                          "  <version>1</version>" +
                          "</parent>");
    createProjectSubFile("m2/src/io.properties", "");

    importProject();

    assertModules("project", mn("project", "m1"), mn("project", "m2"));
    assertModuleLibDeps(mn("project", "m1"));
    assertModuleLibDeps(mn("project", "m2"), "Maven: junit:junit:4.0");
  }

  @Test
  public void testProjectWithProfiles() {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<profiles>" +
                     "  <profile>" +
                     "    <id>one</id>" +
                     "    <activation>" +
                     "      <activeByDefault>false</activeByDefault>" +
                     "    </activation>" +
                     "    <properties>" +
                     "      <junit.version>4.0</junit.version>" +
                     "    </properties>" +
                     "  </profile>" +
                     "  <profile>" +
                     "    <id>two</id>" +
                     "    <activation>" +
                     "      <activeByDefault>false</activeByDefault>" +
                     "    </activation>" +
                     "    <properties>" +
                     "      <junit.version>3.8.1</junit.version>" +
                     "    </properties>" +
                     "  </profile>" +
                     "</profiles>" +

                     "<dependencies>" +
                     "  <dependency>" +
                     "    <groupId>junit</groupId>" +
                     "    <artifactId>junit</artifactId>" +
                     "    <version>${junit.version}</version>" +
                     "  </dependency>" +
                     "</dependencies>");

    importProjectWithProfiles("one");
    assertModules("project");

    assertModuleLibDeps("project", "Maven: junit:junit:4.0");

    importProjectWithProfiles("two");
    assertModules("project");

    assertModuleLibDeps("project", "Maven: junit:junit:3.8.1");
  }

  @Test
  public void testProjectWithOldProfilesXmlFile() {
    ignore(); // not supported by 2.2
  }

  @Test
  public void testProjectWithDefaultProfile() {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<profiles>" +
                     "  <profile>" +
                     "    <id>one</id>" +
                     "    <activation>" +
                     "      <activeByDefault>true</activeByDefault>" +
                     "    </activation>" +
                     "    <properties>" +
                     "      <junit.version>4.0</junit.version>" +
                     "    </properties>" +
                     "  </profile>" +
                     "</profiles>" +

                     "<dependencies>" +
                     "  <dependency>" +
                     "    <groupId>junit</groupId>" +
                     "    <artifactId>junit</artifactId>" +
                     "    <version>${junit.version}</version>" +
                     "  </dependency>" +
                     "</dependencies>");

    importProject();
    assertModules("project");

    assertModuleLibDeps("project", "Maven: junit:junit:4.0");
  }

  @Test
  public void testRefreshFSAfterImport() {
    myProjectRoot.getChildren(); // make sure fs is cached
    new File(myProjectRoot.getPath(), "foo").mkdirs();

    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>");
    if(isNewImportingProcess){
      PlatformTestUtil.waitForPromise(myImportingResult.getVfsRefreshPromise());
    }

    assertNotNull(myProjectRoot.findChild("foo"));
  }

  @Test
  public void  testErrorImportArtifactVersionCannotBeEmpty() {
    assumeVersionMoreThan("3.0.5");
    createProjectPom("<groupId>test</groupId>\n" +
                     "  <artifactId>parent</artifactId>\n" +
                     "  <packaging>pom</packaging>\n" +
                     "  <version>1</version>\n" +
                     "  <modules>\n" +
                     "   <module>m1</module>\n" +
                     "  </modules>\n" +
                     "  <properties>\n" +
                     "   <junit.group.id>junit</junit.group.id>\n" +
                     "   <junit.artifact.id>junit</junit.artifact.id>\n" +
                     "  </properties>\n" +
                     "  <profiles>\n" +
                     "    <profile>\n" +
                     "      <id>profile-test</id>\n" +
                     "      <dependencies>\n" +
                     "        <dependency>\n" +
                     "          <groupId>${junit.group.id}</groupId>\n" +
                     "          <artifactId>${junit.artifact.id}</artifactId>\n" +
                     "        </dependency>\n" +
                     "      </dependencies>\n" +
                     "    </profile>\n" +
                     "  </profiles>\n" +
                     "  \n" +
                     "  <dependencyManagement>\n" +
                     "    <dependencies>\n" +
                     "      <dependency>\n" +
                     "        <groupId>junit</groupId>\n" +
                     "        <artifactId>junit</artifactId>\n" +
                     "        <version>4.0</version> \n" +
                     "      </dependency>\n" +
                     "    </dependencies>\n" +
                     "  </dependencyManagement>");

    createModulePom("m1", "<parent>\n" +
                          "<groupId>test</groupId>\n" +
                          "<artifactId>parent</artifactId>\n" +
                          "<version>1</version>\t\n" +
                          "</parent>\n" +
                          "<artifactId>m1</artifactId>\t\n" +
                          "<dependencies>\n" +
                          "  <dependency>\n" +
                          "    <groupId>junit</groupId>\n" +
                          "    <artifactId>junit</artifactId>\n" +
                          "  </dependency>\n" +
                          "</dependencies>");

    doImportProjects(Collections.singletonList(myProjectPom), false, "profile-test");
  }

  @Test
  public void testProjectWithMavenConfigCustomUserSettingsXml() throws IOException {
    createProjectSubFile(".mvn/maven.config", "-s .mvn/custom-settings.xml");
    createProjectSubFile(".mvn/custom-settings.xml",
                         "<settings>\n" +
                         "    <profiles>\n" +
                         "        <profile>\n" +
                         "            <id>custom1</id>\n" +
                         "            <properties>\n" +
                         "                <projectName>customName</prop>\n" +
                         "            </properties>\n" +
                         "        </profile>\n" +
                         "    </profiles>\n" +
                         "    <activeProfiles>\n" +
                         "        <activeProfile>custom1</activeProfile>\n" +
                         "    </activeProfiles>" +
                         "</settings>");
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>${projectName}</artifactId>" +
                     "<version>1</version>");

    MavenGeneralSettings settings = getMavenGeneralSettings();
    settings.setUserSettingsFile("");
    settings.setUseMavenConfig(true);
    importProject();
    assertModules("customName");
  }

  @Test
  public void testProjectWithActiveProfilesFromSettingsXml() throws IOException {
    updateSettingsXml("<activeProfiles>\n" +
                      "  <activeProfile>one</activeProfile>\n" +
                      "</activeProfiles>");

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>${projectName}</artifactId>" +
                     "<version>1</version>" +

                     "<profiles>" +
                     "  <profile>" +
                     "    <id>one</id>" +
                     "    <properties>" +
                     "      <projectName>project-one</projectName>" +
                     "    </properties>" +
                     "  </profile>" +
                     "</profiles>");

    importProject();
    assertModules("project-one");
  }

  @Test
  public void testProjectWithActiveProfilesAndInnactiveFromSettingsXml() throws IOException {
    updateSettingsXml("<activeProfiles>\n" +
                      "  <activeProfile>one</activeProfile>\n" +
                      "  <activeProfile>two</activeProfile>\n" +
                      "</activeProfiles>");

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>${projectName}</artifactId>" +
                     "<version>1</version>" +

                     "<profiles>" +
                     "  <profile>" +
                     "    <id>one</id>" +
                     "    <properties>" +
                     "      <projectName>project-one</projectName>" +
                     "    </properties>" +
                     "  </profile>" +
                     "  <profile>" +
                     "    <id>two</id>" +
                     "    <properties>" +
                     "      <projectName>project-two</projectName>" +
                     "    </properties>" +
                     "  </profile>" +
                     "</profiles>");

    List<String> disabledProfiles = Collections.singletonList("one");
    doImportProjects(Collections.singletonList(myProjectPom), true, disabledProfiles);
    assertModules("project-two");
  }

  @Test
  public void testOverrideLanguageLevelFromParentPom() {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<packaging>pom</packaging>" +
                     "<version>1</version>" +

                     "<modules>" +
                     "  <module>m1</module>" +
                     "</modules>" +

                     "<build>" +
                     "  <plugins>" +
                     "    <plugin>" +
                     "      <groupId>org.apache.maven.plugins</groupId>" +
                     "      <artifactId>maven-compiler-plugin</artifactId>" +
                     "      <version>3.6.0</version>" +
                     "      <configuration>" +
                     "       <source>7</source>" +
                     "      </configuration>" +
                     "    </plugin>" +
                     "  </plugins>" +
                     "</build>"
    );

    createModulePom("m1",
                    "<artifactId>m1</artifactId>" +
                    "<version>1</version>" +

                    "<parent>" +
                    "  <groupId>test</groupId>" +
                    "  <artifactId>project</artifactId>" +
                    "  <version>1</version>" +
                    "</parent>" +

                    "<build>" +
                    "  <plugins>" +
                    "    <plugin>" +
                    "      <groupId>org.apache.maven.plugins</groupId>" +
                    "      <artifactId>maven-compiler-plugin</artifactId>" +
                    "      <configuration>" +
                    "        <release>11</release>" +
                    "      </configuration>" +
                    "    </plugin>" +
                    "  </plugins>" +
                    "</build>");

    importProject();

    assertEquals(LanguageLevel.JDK_11, LanguageLevelUtil.getCustomLanguageLevel(getModule(mn("project", "m1"))));
    assertEquals(LanguageLevel.JDK_11.toJavaVersion().toString(),
                 CompilerConfiguration.getInstance(myProject).getBytecodeTargetLevel(getModule(mn("project", "m1"))));
  }

  @Test
  public void testReleaseHasPriorityInParentPom() {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<packaging>pom</packaging>" +
                     "<version>1</version>" +

                     "<modules>" +
                     "  <module>m1</module>" +
                     "</modules>" +

                     "<build>" +
                     "  <plugins>" +
                     "    <plugin>" +
                     "      <groupId>org.apache.maven.plugins</groupId>" +
                     "      <artifactId>maven-compiler-plugin</artifactId>" +
                     "      <version>3.6.0</version>" +
                     "      <configuration>" +
                     "       <release>9</release>" +
                     "      </configuration>" +
                     "    </plugin>" +
                     "  </plugins>" +
                     "</build>"
    );

    createModulePom("m1",
                    "<artifactId>m1</artifactId>" +
                    "<version>1</version>" +

                    "<parent>" +
                    "  <groupId>test</groupId>" +
                    "  <artifactId>project</artifactId>" +
                    "  <version>1</version>" +
                    "</parent>" +

                    "<build>" +
                    "  <plugins>" +
                    "    <plugin>" +
                    "      <groupId>org.apache.maven.plugins</groupId>" +
                    "      <artifactId>maven-compiler-plugin</artifactId>" +
                    "      <configuration>" +
                    "        <source>11</source>" +
                    "      </configuration>" +
                    "    </plugin>" +
                    "  </plugins>" +
                    "</build>");

    importProject();

    assertEquals(LanguageLevel.JDK_1_9, LanguageLevelUtil.getCustomLanguageLevel(getModule(mn("project", "m1"))));
    assertEquals(LanguageLevel.JDK_1_9.toJavaVersion().toString(),
                 CompilerConfiguration.getInstance(myProject).getBytecodeTargetLevel(getModule(mn("project", "m1"))));
  }

  @Test
  public void testReleasePropertyNotSupport() {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<packaging>pom</packaging>" +
                     "<version>1</version>" +

                     "<modules>" +
                     "  <module>m1</module>" +
                     "</modules>" +

                     "<build>" +
                     "  <plugins>" +
                     "    <plugin>" +
                     "      <groupId>org.apache.maven.plugins</groupId>" +
                     "      <artifactId>maven-compiler-plugin</artifactId>" +
                     "      <configuration>" +
                     "       <release>9</release>" +
                     "      </configuration>" +
                     "    </plugin>" +
                     "  </plugins>" +
                     "</build>"
    );

    createModulePom("m1",
                    "<artifactId>m1</artifactId>" +
                    "<version>1</version>" +

                    "<parent>" +
                    "  <groupId>test</groupId>" +
                    "  <artifactId>project</artifactId>" +
                    "  <version>1</version>" +
                    "</parent>" +

                    "<build>" +
                    "  <plugins>" +
                    "    <plugin>" +
                    "      <groupId>org.apache.maven.plugins</groupId>" +
                    "      <artifactId>maven-compiler-plugin</artifactId>" +
                    "      <configuration>" +
                    "        <source>11</source>" +
                    "        <target>11</target>" +
                    "      </configuration>" +
                    "    </plugin>" +
                    "  </plugins>" +
                    "</build>");

    importProject();

    assertEquals(LanguageLevel.JDK_11, LanguageLevelUtil.getCustomLanguageLevel(getModule(mn("project", "m1"))));
    assertEquals(LanguageLevel.JDK_11.toJavaVersion().toString(),
                 CompilerConfiguration.getInstance(myProject).getBytecodeTargetLevel(getModule(mn("project", "m1"))));
  }

  @Test
  public void testCompilerPluginExecutionBlockProperty() {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<profiles>" +
                     "  <profile>" +
                     "    <id>target-jdk8</id>" +
                     "    <activation><jdk>[1.8,)</jdk></activation>" +
                     "    <build>" +
                     "      <plugins>" +
                     "        <plugin>" +
                     "          <groupId>org.apache.maven.plugins</groupId>" +
                     "          <artifactId>maven-compiler-plugin</artifactId>" +
                     "          <executions>" +
                     "            <execution>" +
                     "              <id>compile-jdk8</id>" +
                     "              <goals>" +
                     "                <goal>compile</goal>" +
                     "              </goals>" +
                     "              <configuration>" +
                     "                <source>1.8</source>" +
                     "                <target>1.8</target>" +
                     "              </configuration>" +
                     "            </execution>" +
                     "          </executions>" +
                     "        </plugin>" +
                     "      </plugins>" +
                     "    </build>" +
                     "  </profile>" +
                     "  <profile>" +
                     "    <id>target-jdk11</id>" +
                     "    <activation><jdk>[11,)</jdk></activation>" +
                     "    <build>" +
                     "      <plugins>" +
                     "        <plugin>" +
                     "          <groupId>org.apache.maven.plugins</groupId>" +
                     "          <artifactId>maven-compiler-plugin</artifactId>" +
                     "          <executions>" +
                     "            <execution>" +
                     "              <id>compile-jdk11</id>" +
                     "              <goals>" +
                     "                <goal>compile</goal>" +
                     "              </goals>" +
                     "              <configuration>" +
                     "                <source>11</source>" +
                     "                <target>11</target>" +
                     "              </configuration>" +
                     "            </execution>" +
                     "          </executions>" +
                     "        </plugin>" +
                     "      </plugins>" +
                     "    </build>" +
                     "  </profile>" +
                     "</profiles>" +

                     "<build>" +
                     "  <plugins>" +
                     "    <plugin>" +
                     "      <groupId>org.apache.maven.plugins</groupId>" +
                     "      <artifactId>maven-compiler-plugin</artifactId>" +
                     "      <version>3.8.1</version>" +
                     "    </plugin>" +
                     "  </plugins>" +
                     "</build>"
    );


    importProject();

    assertEquals(LanguageLevel.JDK_11, LanguageLevelUtil.getCustomLanguageLevel(getModule("project")));
    assertEquals(LanguageLevel.JDK_11.toJavaVersion().toString(),
                 CompilerConfiguration.getInstance(myProject).getBytecodeTargetLevel(getModule("project")));
  }
}
