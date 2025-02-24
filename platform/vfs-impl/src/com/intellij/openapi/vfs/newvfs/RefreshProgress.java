// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.openapi.vfs.newvfs;

import com.intellij.ide.impl.ProjectUtilCore;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.util.ProgressIndicatorBase;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

final class RefreshProgress extends ProgressIndicatorBase {
  static @NotNull ProgressIndicator create() {
    return ApplicationManager.getApplication().isUnitTestMode() ? new EmptyProgressIndicator() : new RefreshProgress();
  }

  @Override
  public void start() {
    super.start();
    scheduleUiUpdate();
  }

  @Override
  public void stop() {
    super.stop();
    scheduleUiUpdate();
  }

  private void scheduleUiUpdate() {
    // wrapping in `invokeLater` here reduces the number of events posted to EDT in the case of multiple IDE frames
    UIUtil.invokeLaterIfNeeded(() -> {
      if (ApplicationManager.getApplication().isDisposed()) {
        return;
      }

      Project[] projects = ProjectUtilCore.getOpenProjects();
      WindowManager windowManager = WindowManager.getInstance();
      if (projects.length != 0 && windowManager != null) {
        for (Project project : projects) {
          StatusBar statusBar = windowManager.getStatusBar(project);
          if (statusBar != null) {
            if (isRunning()) {
              statusBar.startRefreshIndication(getText());
            }
            else {
              statusBar.stopRefreshIndication();
            }
          }
        }
      }
    });
  }
}
