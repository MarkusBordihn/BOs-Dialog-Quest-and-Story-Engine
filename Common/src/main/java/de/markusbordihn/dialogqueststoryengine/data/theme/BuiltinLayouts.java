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

package de.markusbordihn.dialogqueststoryengine.data.theme;

import de.markusbordihn.dialogqueststoryengine.Constants;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

public final class BuiltinLayouts {

  public static final ResourceLocation DIALOG = id("dialog");
  public static final ResourceLocation DIALOG_LETTERBOX = id("dialog_letterbox");
  public static final ResourceLocation HOLOPAD = id("holopad");
  public static final ResourceLocation JOURNAL_BOOK = id("journal_book");
  public static final ResourceLocation JOURNAL_PANEL = id("journal_panel");
  public static final ResourceLocation QUEST_FLOW = id("quest_flow");
  public static final ResourceLocation REWARD_CLAIM = id("reward_claim");

  public static final List<ResourceLocation> ALL =
      List.of(
          DIALOG, DIALOG_LETTERBOX, HOLOPAD, JOURNAL_BOOK, JOURNAL_PANEL, QUEST_FLOW, REWARD_CLAIM);

  private BuiltinLayouts() {}

  private static ResourceLocation id(String path) {
    return new ResourceLocation(Constants.MOD_NAMESPACE, path);
  }
}
