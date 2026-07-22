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

package de.markusbordihn.dialogqueststoryengine.data.interaction;

import de.markusbordihn.dialogqueststoryengine.Constants;
import de.markusbordihn.dialogqueststoryengine.data.json.EnumKeys;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;

public enum ActionType {
  NONE(false),
  OPEN_STORY("story"),
  OPEN_INTERACTIVE_STORY(false),
  OPEN_DIALOG("dialog"),
  START_QUEST("quest"),
  COMPLETE_QUEST("quest"),
  FAIL_QUEST("quest"),
  ADVANCE_QUEST_STEP("quest"),
  UNLOCK_STORY("story"),
  MARK_STORY_READ("story"),
  SET_FACT("fact"),
  REMOVE_FACT("fact"),
  GIVE_ITEM("item"),
  GIVE_EXPERIENCE(),
  RUN_COMMAND("command"),
  RUN_FUNCTION("function"),
  SEND_MESSAGE("message");

  private final Set<String> shorthandKeys;
  private final String key;
  private final Optional<ResourceLocation> typeId;

  ActionType(String... shorthandKeys) {
    this(true, shorthandKeys);
  }

  ActionType(boolean registeredAction, String... shorthandKeys) {
    this.shorthandKeys = Set.of(shorthandKeys);
    this.key = this.name().toLowerCase(Locale.ROOT);
    this.typeId =
        registeredAction
            ? Optional.of(new ResourceLocation(Constants.MOD_NAMESPACE, this.key))
            : Optional.empty();
  }

  public static ActionType fromName(String name) {
    return EnumKeys.byName(ActionType.class, name).orElse(NONE);
  }

  public String key() {
    return this.key;
  }

  public Set<String> shorthandKeys() {
    return this.shorthandKeys;
  }

  public Optional<ResourceLocation> typeId() {
    return this.typeId;
  }
}
