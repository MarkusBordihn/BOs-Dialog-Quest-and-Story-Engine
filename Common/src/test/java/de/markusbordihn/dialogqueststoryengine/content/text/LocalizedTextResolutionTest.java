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

package de.markusbordihn.dialogqueststoryengine.content.text;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.dialogqueststoryengine.logic.context.BuiltinContextValueProviders;
import de.markusbordihn.dialogqueststoryengine.logic.context.NarrativeContextResolution;
import de.markusbordihn.dialogqueststoryengine.state.FactScope;
import de.markusbordihn.dialogqueststoryengine.state.FactValue;
import de.markusbordihn.dialogqueststoryengine.state.PlayerState;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class LocalizedTextResolutionTest {

  private static final ResourceLocation CONTEXT = new ResourceLocation("dqse", "context");
  private static final ResourceLocation FACT = new ResourceLocation("dqse", "fact");

  @BeforeAll
  static void registerProviders() {
    BuiltinContextValueProviders.register();
  }

  @Test
  void literalResolvesToPlainComponent() {
    LocalizedText resolved =
        LocalizedTextSource.literal("Hello traveler!")
            .resolve(NarrativeContextResolution.ofTest(new PlayerState(UUID.randomUUID())));

    assertTrue(resolved.literal());
    assertEquals("Hello traveler!", resolved.toComponent().getString());
  }

  @Test
  void factArgumentResolvesToFactDisplayValue() {
    PlayerState playerState = new PlayerState(UUID.randomUUID());
    playerState.setFact(FactScope.PLAYER, "example:alert_level", FactValue.of(3L));

    LocalizedTextSource source =
        LocalizedTextSource.keyed(
            "dialog.example",
            List.of(
                new ContextArgument(
                    FACT, Map.of("scope", "player", "fact", "example:alert_level"))));

    LocalizedText resolved = source.resolve(NarrativeContextResolution.ofTest(playerState));

    assertFalse(resolved.literal());
    assertEquals(1, resolved.arguments().size());
    assertEquals("3", resolved.arguments().get(0).getString());
  }

  @Test
  void missingFactResolvesToEmptyComponent() {
    PlayerState playerState = new PlayerState(UUID.randomUUID());
    LocalizedTextSource source =
        LocalizedTextSource.keyed(
            "dialog.example",
            List.of(
                new ContextArgument(FACT, Map.of("scope", "player", "fact", "example:absent"))));

    LocalizedText resolved = source.resolve(NarrativeContextResolution.ofTest(playerState));

    assertEquals("", resolved.arguments().get(0).getString());
  }

  @Test
  void unavailableTargetContextResolvesToEmptyComponent() {
    PlayerState playerState = new PlayerState(UUID.randomUUID());
    LocalizedTextSource source =
        LocalizedTextSource.keyed(
            "dialog.example", List.of(new ContextArgument(CONTEXT, Map.of("key", "target_name"))));

    LocalizedText resolved = source.resolve(NarrativeContextResolution.ofTest(playerState));

    assertEquals("", resolved.arguments().get(0).getString());
  }

  @Test
  void unknownProviderResolvesToEmptyComponent() {
    PlayerState playerState = new PlayerState(UUID.randomUUID());
    LocalizedTextSource source =
        LocalizedTextSource.keyed(
            "dialog.example",
            List.of(new ContextArgument(new ResourceLocation("other_mod", "custom"), Map.of())));

    LocalizedText resolved = source.resolve(NarrativeContextResolution.ofTest(playerState));

    assertEquals("", resolved.arguments().get(0).getString());
  }
}
