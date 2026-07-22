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

import com.google.gson.JsonObject;
import de.markusbordihn.dialogqueststoryengine.Constants;
import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssue;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import de.markusbordihn.dialogqueststoryengine.logic.action.Action;
import de.markusbordihn.dialogqueststoryengine.logic.action.ActionContext;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;

public record SendMessageAction(String message, Optional<String> speaker) implements Action {

  public static final ResourceLocation TYPE_ID =
      new ResourceLocation(Constants.MOD_NAMESPACE, "send_message");

  public static Action parse(
      JsonObject jsonObject,
      ContentType contentType,
      ResourceLocation contentId,
      String filePath,
      List<ContentIssue> issues) {
    if (!jsonObject.has("message") || !jsonObject.get("message").isJsonPrimitive()) {
      issues.add(
          ContentIssue.of(IssueCode.MISSING_FIELD, contentType, contentId, filePath, "message"));
      return Action.NOOP;
    }

    String message = jsonObject.get("message").getAsString();
    if (message.isEmpty()) {
      issues.add(
          ContentIssue.of(IssueCode.MISSING_FIELD, contentType, contentId, filePath, "message"));
      return Action.NOOP;
    }

    Optional<String> speaker =
        jsonObject.has("speaker") && jsonObject.get("speaker").isJsonPrimitive()
            ? Optional.of(jsonObject.get("speaker").getAsString()).filter(value -> !value.isBlank())
            : Optional.empty();

    return new SendMessageAction(message, speaker);
  }

  private static boolean isPlayerName(MinecraftServer server, String name) {
    return server != null && server.getPlayerList().getPlayerByName(name) != null;
  }

  @Override
  public void execute(ActionContext actionContext) {
    if (actionContext.player() == null) {
      return;
    }

    String resolved =
        this.message.replace("{player}", actionContext.player().getName().getString());
    Optional<String> safeSpeaker =
        this.speaker.filter(name -> !isPlayerName(actionContext.server(), name));
    Component component =
        safeSpeaker.isPresent()
            ? Component.literal("<" + safeSpeaker.get() + "> " + resolved)
            : Component.literal(resolved);
    actionContext.player().sendSystemMessage(component);
  }
}
