/*
 * Copyright 2025 Markus Bordihn
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package de.markusbordihn.dialogqueststoryengine.theme;

import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssue;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import de.markusbordihn.dialogqueststoryengine.data.theme.Theme;
import de.markusbordihn.dialogqueststoryengine.data.theme.ThemeArea;
import de.markusbordihn.dialogqueststoryengine.data.theme.ThemeLayoutContract;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ThemeContractValidator {

  private ThemeContractValidator() {}

  public static List<ContentIssue> validate(
      Theme theme, ThemeLayoutContract contract, String file) {
    List<ContentIssue> issues = new ArrayList<>();

    for (String name : contract.requiredAreas()) {
      if (!theme.areas().containsKey(name)) {
        issues.add(missingSlot(theme, file, "areas." + name, name, "area"));
      }
    }
    for (Map.Entry<String, ThemeArea> entry : theme.areas().entrySet()) {
      String name = entry.getKey();
      if (!contract.knowsArea(name)) {
        issues.add(unknownSlot(theme, file, "areas." + name, name, "area"));
        continue;
      }
      checkBounds(theme, file, name, entry.getValue(), issues);
    }

    for (String name : contract.requiredSprites()) {
      if (!theme.sprites().containsKey(name)) {
        issues.add(missingSlot(theme, file, "sprites." + name, name, "sprite"));
      }
    }
    for (String name : theme.sprites().keySet()) {
      if (!contract.knowsSprite(name)) {
        issues.add(unknownSlot(theme, file, "sprites." + name, name, "sprite"));
      }
    }

    for (String name : theme.colors().keySet()) {
      if (!contract.knowsColor(name)) {
        issues.add(
            ContentIssue.of(
                IssueCode.INVALID_THEME_COLOR,
                ContentType.THEME,
                theme.id(),
                file,
                "colors." + name,
                Map.of("name", name, "reason", "unknown color name for this layout")));
      }
    }

    return issues;
  }

  private static void checkBounds(
      Theme theme, String file, String name, ThemeArea area, List<ContentIssue> issues) {
    if (area.x() < 0
        || area.y() < 0
        || area.right() > theme.logicalWidth()
        || area.bottom() > theme.logicalHeight()) {
      issues.add(
          ContentIssue.of(
              IssueCode.THEME_AREA_OUT_OF_BOUNDS,
              ContentType.THEME,
              theme.id(),
              file,
              "areas." + name,
              Map.of(
                  "area",
                  area.x() + "," + area.y() + " " + area.width() + "x" + area.height(),
                  "logical",
                  theme.logicalWidth() + "x" + theme.logicalHeight())));
    }
  }

  private static ContentIssue missingSlot(
      Theme theme, String file, String path, String name, String kind) {
    return ContentIssue.of(
        IssueCode.MISSING_THEME_SLOT,
        ContentType.THEME,
        theme.id(),
        file,
        path,
        Map.of("slot", name, "kind", kind));
  }

  private static ContentIssue unknownSlot(
      Theme theme, String file, String path, String name, String kind) {
    return ContentIssue.of(
        IssueCode.UNKNOWN_THEME_SLOT,
        ContentType.THEME,
        theme.id(),
        file,
        path,
        Map.of("slot", name, "kind", kind));
  }
}
