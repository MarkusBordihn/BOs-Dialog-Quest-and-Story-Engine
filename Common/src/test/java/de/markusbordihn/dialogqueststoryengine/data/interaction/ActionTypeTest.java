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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.dialogqueststoryengine.Constants;
import de.markusbordihn.dialogqueststoryengine.logic.action.BuiltinActions;
import de.markusbordihn.dialogqueststoryengine.registry.Registries;
import java.util.Arrays;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ActionTypeTest {

  @BeforeAll
  static void registerActions() {
    BuiltinActions.register();
  }

  @Test
  void fromNameIsCaseInsensitiveAndDefaultsToNone() {
    assertEquals(ActionType.GIVE_ITEM, ActionType.fromName("give_item"));
    assertEquals(ActionType.GIVE_ITEM, ActionType.fromName("GIVE_ITEM"));
    assertEquals(ActionType.NONE, ActionType.fromName("does_not_exist"));
    assertEquals(ActionType.NONE, ActionType.fromName(null));
  }

  @Test
  void typeIdMapsToNamespacedRegistryId() {
    assertEquals(
        Optional.of(new ResourceLocation(Constants.MOD_NAMESPACE, "give_item")),
        ActionType.GIVE_ITEM.typeId());
  }

  @Test
  void sentinelAndInteractiveStoryHaveNoTypeId() {
    assertTrue(ActionType.NONE.typeId().isEmpty());
    assertTrue(ActionType.OPEN_INTERACTIVE_STORY.typeId().isEmpty());
  }

  @Test
  void shorthandKeysDeclareFamilyAndSingleOwners() {
    assertTrue(ActionType.GIVE_ITEM.shorthandKeys().contains("item"));
    assertTrue(ActionType.OPEN_DIALOG.shorthandKeys().contains("dialog"));
    assertTrue(ActionType.START_QUEST.shorthandKeys().contains("quest"));
    assertTrue(ActionType.COMPLETE_QUEST.shorthandKeys().contains("quest"));
    assertTrue(ActionType.SET_FACT.shorthandKeys().contains("fact"));
    assertTrue(ActionType.GIVE_EXPERIENCE.shorthandKeys().isEmpty());
  }

  @Test
  void everyTypeIdResolvesInActionRegistry() {
    for (ActionType type : ActionType.values()) {
      type.typeId()
          .ifPresent(
              id ->
                  assertTrue(
                      Registries.ACTIONS.contains(id),
                      "ActionType " + type + " has typeId " + id + " that is not registered"));
    }
  }

  @Test
  void closeSessionActionHasNoActionType() {
    assertTrue(
        Arrays.stream(ActionType.values())
            .map(ActionType::typeId)
            .flatMap(Optional::stream)
            .noneMatch(id -> id.getPath().equals("close_session")));
  }
}
