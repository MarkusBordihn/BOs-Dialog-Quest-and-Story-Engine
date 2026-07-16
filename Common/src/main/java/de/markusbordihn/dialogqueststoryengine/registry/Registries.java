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

package de.markusbordihn.dialogqueststoryengine.registry;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;

public final class Registries {

  public static final ExtensionRegistry<ConditionHandler> CONDITIONS =
      new ExtensionRegistry<>(RegistryType.CONDITIONS);
  public static final ExtensionRegistry<ActionHandler> ACTIONS =
      new ExtensionRegistry<>(RegistryType.ACTIONS);
  public static final ExtensionRegistry<InteractionHandler> INTERACTIONS =
      new ExtensionRegistry<>(RegistryType.INTERACTIONS);
  public static final ExtensionRegistry<QuestStepHandler> QUEST_STEPS =
      new ExtensionRegistry<>(RegistryType.QUEST_STEPS);
  public static final ExtensionRegistry<RewardHandler> REWARDS =
      new ExtensionRegistry<>(RegistryType.REWARDS);
  public static final ExtensionRegistry<ThemeProvider> THEMES =
      new ExtensionRegistry<>(RegistryType.THEMES);
  public static final ExtensionRegistry<ContextValueProvider> CONTEXT_VALUES =
      new ExtensionRegistry<>(RegistryType.CONTEXT_VALUES);
  public static final ExtensionRegistry<ContentValidator> VALIDATORS =
      new ExtensionRegistry<>(RegistryType.VALIDATORS);

  private static final EnumMap<RegistryType, ExtensionRegistry<?>> ALL =
      new EnumMap<>(RegistryType.class);

  static {
    ALL.put(RegistryType.CONDITIONS, CONDITIONS);
    ALL.put(RegistryType.ACTIONS, ACTIONS);
    ALL.put(RegistryType.INTERACTIONS, INTERACTIONS);
    ALL.put(RegistryType.QUEST_STEPS, QUEST_STEPS);
    ALL.put(RegistryType.REWARDS, REWARDS);
    ALL.put(RegistryType.THEMES, THEMES);
    ALL.put(RegistryType.CONTEXT_VALUES, CONTEXT_VALUES);
    ALL.put(RegistryType.VALIDATORS, VALIDATORS);
  }

  private Registries() {}

  public static Collection<ExtensionRegistry<?>> all() {
    return Collections.unmodifiableCollection(ALL.values());
  }

  public static void freezeAll() {
    ALL.values().forEach(ExtensionRegistry::freeze);
  }
}
