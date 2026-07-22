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

package de.markusbordihn.dialogqueststoryengine.commands.suggestion;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.markusbordihn.dialogqueststoryengine.content.dialog.DialogContentRegistry;
import de.markusbordihn.dialogqueststoryengine.content.quest.QuestContentRegistry;
import de.markusbordihn.dialogqueststoryengine.content.story.InteractiveStoryContentRegistry;
import de.markusbordihn.dialogqueststoryengine.data.dialog.DialogDefinition;
import de.markusbordihn.dialogqueststoryengine.story.entry.StoryEntryClientRegistry;
import de.markusbordihn.dialogqueststoryengine.theme.ThemeClientRegistry;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.resources.ResourceLocation;

public final class ContentIdSuggestions {

  public static final SuggestionProvider<CommandSourceStack> DIALOGS =
      (context, builder) ->
          SharedSuggestionProvider.suggestResource(
              DialogContentRegistry.all().stream().map(DialogDefinition::id).toList(), builder);
  public static final SuggestionProvider<CommandSourceStack> QUESTS =
      (context, builder) ->
          SharedSuggestionProvider.suggestResource(
              QuestContentRegistry.all().stream().map(definition -> definition.id()).toList(),
              builder);
  public static final SuggestionProvider<CommandSourceStack> INTERACTIVE_STORIES =
      (context, builder) ->
          SharedSuggestionProvider.suggestResource(
              InteractiveStoryContentRegistry.all().stream()
                  .map(definition -> definition.id())
                  .toList(),
              builder);
  public static final SuggestionProvider<CommandSourceStack> STORY_ENTRIES =
      (context, builder) -> SharedSuggestionProvider.suggestResource(storyEntryIds(), builder);
  public static final SuggestionProvider<CommandSourceStack> THEMES =
      (context, builder) -> SharedSuggestionProvider.suggestResource(themeIds(), builder);

  private ContentIdSuggestions() {}

  private static List<ResourceLocation> storyEntryIds() {
    return List.copyOf(StoryEntryClientRegistry.ids());
  }

  private static List<ResourceLocation> themeIds() {
    return List.copyOf(ThemeClientRegistry.ids());
  }
}
