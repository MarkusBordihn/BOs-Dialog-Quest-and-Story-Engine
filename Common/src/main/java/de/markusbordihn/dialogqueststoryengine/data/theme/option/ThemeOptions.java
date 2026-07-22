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

package de.markusbordihn.dialogqueststoryengine.data.theme.option;

import de.markusbordihn.dialogqueststoryengine.data.theme.ThemeTextAlignment;

public final class ThemeOptions {

  public static final String TITLE_ALIGNMENT = "title_alignment";
  public static final String TEXT_ALIGNMENT = "text_alignment";
  public static final String SPEAKER_ALIGNMENT = "speaker_alignment";
  public static final String PORTRAIT_SIDE = "portrait_side";
  public static final String PAGE_TURN_STYLE = "page_turn_style";
  public static final String BUTTON_STYLE = "button_style";
  public static final String BACKGROUND_MODE = "background_mode";
  public static final String SHOW_CLOSE_BUTTON = "show_close_button";
  public static final String SHOW_PAGE_NUMBERS = "show_page_numbers";

  private ThemeOptions() {}

  public static ThemeOption<ThemeTextAlignment> titleAlignment(ThemeTextAlignment defaultValue) {
    return ThemeOption.ofEnum(TITLE_ALIGNMENT, defaultValue, ThemeTextAlignment::fromKey);
  }

  public static ThemeOption<ThemeTextAlignment> textAlignment(ThemeTextAlignment defaultValue) {
    return ThemeOption.ofEnum(TEXT_ALIGNMENT, defaultValue, ThemeTextAlignment::fromKey);
  }

  public static ThemeOption<ThemeTextAlignment> speakerAlignment(ThemeTextAlignment defaultValue) {
    return ThemeOption.ofEnum(SPEAKER_ALIGNMENT, defaultValue, ThemeTextAlignment::fromKey);
  }

  public static ThemeOption<PortraitSide> portraitSide(PortraitSide defaultValue) {
    return ThemeOption.ofEnum(PORTRAIT_SIDE, defaultValue, PortraitSide::fromKey);
  }

  public static ThemeOption<PageTurnStyle> pageTurnStyle(PageTurnStyle defaultValue) {
    return ThemeOption.ofEnum(PAGE_TURN_STYLE, defaultValue, PageTurnStyle::fromKey);
  }

  public static ThemeOption<ButtonStyle> buttonStyle(ButtonStyle defaultValue) {
    return ThemeOption.ofEnum(BUTTON_STYLE, defaultValue, ButtonStyle::fromKey);
  }

  public static ThemeOption<BackgroundMode> backgroundMode(BackgroundMode defaultValue) {
    return ThemeOption.ofEnum(BACKGROUND_MODE, defaultValue, BackgroundMode::fromKey);
  }

  public static ThemeOption<Boolean> showCloseButton(boolean defaultValue) {
    return ThemeOption.ofBoolean(SHOW_CLOSE_BUTTON, defaultValue);
  }

  public static ThemeOption<Boolean> showPageNumbers(boolean defaultValue) {
    return ThemeOption.ofBoolean(SHOW_PAGE_NUMBERS, defaultValue);
  }
}
