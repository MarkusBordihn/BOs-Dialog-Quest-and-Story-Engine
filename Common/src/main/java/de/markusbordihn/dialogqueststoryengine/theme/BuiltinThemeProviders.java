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

import de.markusbordihn.dialogqueststoryengine.registry.Registries;
import de.markusbordihn.dialogqueststoryengine.registry.ThemeProvider;
import de.markusbordihn.dialogqueststoryengine.theme.provider.DialogLayoutProvider;
import de.markusbordihn.dialogqueststoryengine.theme.provider.DialogLetterboxLayoutProvider;
import de.markusbordihn.dialogqueststoryengine.theme.provider.HolopadLayoutProvider;
import de.markusbordihn.dialogqueststoryengine.theme.provider.JournalBookLayoutProvider;
import de.markusbordihn.dialogqueststoryengine.theme.provider.JournalPanelLayoutProvider;
import de.markusbordihn.dialogqueststoryengine.theme.provider.QuestFlowLayoutProvider;
import de.markusbordihn.dialogqueststoryengine.theme.provider.RewardClaimLayoutProvider;

public final class BuiltinThemeProviders {

  private BuiltinThemeProviders() {}

  public static void register() {
    register(new DialogLayoutProvider());
    register(new DialogLetterboxLayoutProvider());
    register(new HolopadLayoutProvider());
    register(new JournalBookLayoutProvider());
    register(new JournalPanelLayoutProvider());
    register(new QuestFlowLayoutProvider());
    register(new RewardClaimLayoutProvider());
  }

  private static void register(ThemeProvider provider) {
    Registries.THEMES.register(provider.layoutId(), provider);
  }
}
