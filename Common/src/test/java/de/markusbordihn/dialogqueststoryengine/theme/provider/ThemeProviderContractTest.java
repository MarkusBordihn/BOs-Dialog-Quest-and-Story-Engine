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

package de.markusbordihn.dialogqueststoryengine.theme.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssue;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueSeverity;
import de.markusbordihn.dialogqueststoryengine.data.theme.BuiltinLayouts;
import de.markusbordihn.dialogqueststoryengine.data.theme.ResolvedLayout;
import de.markusbordihn.dialogqueststoryengine.data.theme.Theme;
import de.markusbordihn.dialogqueststoryengine.data.theme.ThemeAnchor;
import de.markusbordihn.dialogqueststoryengine.data.theme.ThemeScaleLimits;
import de.markusbordihn.dialogqueststoryengine.data.theme.ThemeViewport;
import de.markusbordihn.dialogqueststoryengine.data.theme.layout.JournalLayout;
import de.markusbordihn.dialogqueststoryengine.registry.Registries;
import de.markusbordihn.dialogqueststoryengine.registry.ThemeProvider;
import de.markusbordihn.dialogqueststoryengine.theme.BuiltinThemeProviders;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ThemeProviderContractTest {

  @BeforeAll
  static void registerProviders() {
    if (!Registries.THEMES.contains(BuiltinLayouts.HOLOPAD)) {
      BuiltinThemeProviders.register();
    }
  }

  private static ThemeViewport viewport(Theme theme) {
    return ThemeViewport.of(
        theme.logicalWidth(),
        theme.logicalHeight(),
        theme.logicalWidth(),
        theme.logicalHeight(),
        ThemeScaleLimits.DEFAULT,
        ThemeAnchor.CENTER);
  }

  @Test
  void everyProviderFallbackValidatesAndResolves() {
    for (ThemeProvider provider : Registries.THEMES.values()) {
      Theme fallback = provider.fallbackTheme();
      assertEquals(provider.layoutId(), fallback.layoutId());

      List<ContentIssue> issues = provider.validate(fallback, "fallback");
      boolean hasError = issues.stream().anyMatch(issue -> issue.severity() == IssueSeverity.ERROR);
      assertTrue(!hasError, provider.layoutId() + " fallback has validation errors: " + issues);

      ResolvedLayout resolved = provider.resolve(fallback, viewport(fallback));
      assertNotNull(resolved);
    }
  }

  @Test
  void bothJournalLayoutsResolveToJournalView() {
    Theme book = Registries.THEMES.get(BuiltinLayouts.JOURNAL_BOOK).orElseThrow().fallbackTheme();
    Theme panel = Registries.THEMES.get(BuiltinLayouts.JOURNAL_PANEL).orElseThrow().fallbackTheme();

    JournalLayout bookView =
        new JournalLayout(
            Registries.THEMES
                .get(BuiltinLayouts.JOURNAL_BOOK)
                .orElseThrow()
                .resolve(book, viewport(book)));
    JournalLayout panelView =
        new JournalLayout(
            Registries.THEMES
                .get(BuiltinLayouts.JOURNAL_PANEL)
                .orElseThrow()
                .resolve(panel, viewport(panel)));

    // The same typed view serves both journal layouts: required slots resolve without branching.
    assertNotNull(bookView.tabs());
    assertNotNull(bookView.index());
    assertNotNull(bookView.detail());
    assertNotNull(panelView.tabs());
    assertNotNull(panelView.index());
    assertNotNull(panelView.detail());
  }
}
