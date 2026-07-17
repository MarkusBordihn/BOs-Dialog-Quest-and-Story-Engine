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

package de.markusbordihn.dialogqueststoryengine.logic.action.types;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.dialogqueststoryengine.config.SecurityConfig;
import de.markusbordihn.dialogqueststoryengine.logic.action.ActionContext;
import de.markusbordihn.dialogqueststoryengine.state.PlayerState;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class RunCommandActionTest {

  @AfterEach
  void resetSecurityConfig() {
    SecurityConfig.configure(false, List.of(), 2);
  }

  @Test
  void execute_commandActionsDisabled_doesNotCrash() {
    ActionContext ctx = ActionContext.ofTest(new PlayerState(UUID.randomUUID()));

    assertDoesNotThrow(() -> new RunCommandAction("say hello", 2).execute(ctx));
  }

  @Test
  void execute_commandActionsEnabled_nullServerDoesNotCrash() {
    SecurityConfig.configure(true, List.of(), 2);
    ActionContext ctx = ActionContext.ofTest(new PlayerState(UUID.randomUUID()));

    assertDoesNotThrow(() -> new RunCommandAction("say hello", 2).execute(ctx));
  }

  @Test
  void execute_commandActionsEnabled_nullPlayerDoesNotCrash() {
    SecurityConfig.configure(true, List.of(), 2);
    ActionContext ctx = ActionContext.ofTest(new PlayerState(UUID.randomUUID()));

    assertDoesNotThrow(() -> new RunCommandAction("say hello", 2).execute(ctx));
  }

  @Test
  void execute_commandActionsEnabledWithWhitelist_unlistedCommandDoesNotCrash() {
    SecurityConfig.configure(true, List.of("say"), 2);
    ActionContext ctx = ActionContext.ofTest(new PlayerState(UUID.randomUUID()));

    assertDoesNotThrow(() -> new RunCommandAction("give @s diamond 1", 2).execute(ctx));
  }

  @Test
  void whitelistMatchesCommandBoundaries() {
    SecurityConfig.configure(true, List.of("say"), 2);

    assertTrue(SecurityConfig.isCommandAllowed("/say hello"));
    assertFalse(SecurityConfig.isCommandAllowed("say_private hello"));
    assertFalse(SecurityConfig.isCommandAllowed("give @s diamond"));
  }
}
