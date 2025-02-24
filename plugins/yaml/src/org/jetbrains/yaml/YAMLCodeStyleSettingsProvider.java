// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.yaml;

import com.intellij.application.options.CodeStyleAbstractConfigurable;
import com.intellij.application.options.CodeStyleAbstractPanel;
import com.intellij.application.options.TabbedLanguageCodeStylePanel;
import com.intellij.openapi.options.Configurable;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsProvider;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.formatter.YAMLCodeStyleSettings;

/**
 * @deprecated This logic is moved to YAMLLanguageCodeStyleSettingsProvider.
 * This class is still needed for compatibility for Raml plugin.
 */
@Deprecated(forRemoval = true)
public class YAMLCodeStyleSettingsProvider extends CodeStyleSettingsProvider {
  @NotNull
  @Override
  public Configurable createSettingsPage(final @NotNull CodeStyleSettings settings, final @NotNull CodeStyleSettings originalSettings) {
    return new CodeStyleAbstractConfigurable(settings, originalSettings, YAMLLanguage.INSTANCE.getDisplayName()) {
      @Override
      protected @NotNull CodeStyleAbstractPanel createPanel(final @NotNull CodeStyleSettings settings) {
        final CodeStyleSettings currentSettings = getCurrentSettings();
        return new TabbedLanguageCodeStylePanel(YAMLLanguage.INSTANCE, currentSettings, settings) {
          @Override
          protected void initTabs(final CodeStyleSettings settings) {
            addIndentOptionsTab(settings);
            addSpacesTab(settings);
            addWrappingAndBracesTab(settings);
          }
        };
      }

      @Override
      public String getHelpTopic() {
        return "reference.settingsdialog.codestyle.yaml";
      }
    };
  }

  @Override
  public String getConfigurableDisplayName() {
    return YAMLLanguage.INSTANCE.getDisplayName();
  }

  @Nullable
  @Override
  public CustomCodeStyleSettings createCustomSettings(@NotNull CodeStyleSettings settings) {
    return new YAMLCodeStyleSettings(settings);
  }
}
