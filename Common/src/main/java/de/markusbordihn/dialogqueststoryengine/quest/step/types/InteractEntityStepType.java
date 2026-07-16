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

package de.markusbordihn.dialogqueststoryengine.quest.step.types;

import com.google.gson.JsonObject;
import de.markusbordihn.dialogqueststoryengine.Constants;
import de.markusbordihn.dialogqueststoryengine.content.quest.RawQuestStep;
import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssue;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import de.markusbordihn.dialogqueststoryengine.quest.step.QuestStepContext;
import de.markusbordihn.dialogqueststoryengine.registry.QuestStepHandler;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public final class InteractEntityStepType implements QuestStepHandler {

  public static final ResourceLocation TYPE_ID =
      new ResourceLocation(Constants.MOD_NAMESPACE, "interact_entity");

  static final String FIELD_TARGET = "target";

  static boolean matches(String targetSpec, UUID entityUuid, ResourceLocation entityTypeId) {
    if (isUuid(targetSpec)) {
      return entityUuid != null && entityUuid.equals(UUID.fromString(targetSpec));
    }
    ResourceLocation targetTypeId = ResourceLocation.tryParse(targetSpec);
    return targetTypeId != null && targetTypeId.equals(entityTypeId);
  }

  private static boolean isUuid(String value) {
    try {
      UUID.fromString(value);
      return true;
    } catch (IllegalArgumentException ignored) {
      return false;
    }
  }

  @Override
  public void validate(
      ResourceLocation questId, RawQuestStep step, String filePath, List<ContentIssue> issues) {
    JsonObject jsonObject = step.jsonObject();
    String fieldPath = "logic.steps." + step.id() + "." + FIELD_TARGET;
    if (!jsonObject.has(FIELD_TARGET) || !jsonObject.get(FIELD_TARGET).isJsonPrimitive()) {
      issues.add(
          ContentIssue.of(
              IssueCode.MISSING_FIELD, ContentType.QUEST, questId, filePath, fieldPath));
      return;
    }

    String target = jsonObject.get(FIELD_TARGET).getAsString();
    if (!isUuid(target) && ResourceLocation.tryParse(target) == null) {
      issues.add(
          ContentIssue.of(
              IssueCode.INVALID_RESOURCE_LOCATION,
              ContentType.QUEST,
              questId,
              filePath,
              fieldPath,
              Map.of("value", target)));
    }
  }

  @Override
  public void onEntityInteract(QuestStepContext context, Entity target) {
    JsonObject jsonObject = context.step().jsonObject();
    if (!jsonObject.has(FIELD_TARGET) || !jsonObject.get(FIELD_TARGET).isJsonPrimitive()) {
      return;
    }

    String targetSpec = jsonObject.get(FIELD_TARGET).getAsString();
    ResourceLocation entityTypeId = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType());
    if (matches(targetSpec, target.getUUID(), entityTypeId)) {
      context.progress(1);
    }
  }
}
