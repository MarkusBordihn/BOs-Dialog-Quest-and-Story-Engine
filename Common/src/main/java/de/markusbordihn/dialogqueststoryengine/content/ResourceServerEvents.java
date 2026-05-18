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

package de.markusbordihn.dialogqueststoryengine.content;

import de.markusbordihn.dialogqueststoryengine.Constants;
import de.markusbordihn.dialogqueststoryengine.content.dialog.DialogContentLoader;
import de.markusbordihn.dialogqueststoryengine.content.interaction.InteractionContentLoader;
import de.markusbordihn.dialogqueststoryengine.content.quest.QuestContentLoader;
import de.markusbordihn.dialogqueststoryengine.content.story.InteractiveStoryContentLoader;
import java.util.function.Consumer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ResourceServerEvents {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  private ResourceServerEvents() {}

  public static void registerReloadListeners(Consumer<PreparableReloadListener> registrar) {
    log.info("{} Data Pack loaders ...", Constants.LOG_REGISTER_PREFIX);
    registrar.accept(new DialogContentLoader());
    registrar.accept(new QuestContentLoader());
    registrar.accept(new InteractiveStoryContentLoader());
    registrar.accept(new InteractionContentLoader());
  }
}
