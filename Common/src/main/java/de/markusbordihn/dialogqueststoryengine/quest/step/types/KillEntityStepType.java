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
import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssue;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import de.markusbordihn.dialogqueststoryengine.data.json.RegistryReferenceValidator;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.RawQuestStep;
import de.markusbordihn.dialogqueststoryengine.quest.step.QuestStepContext;
import de.markusbordihn.dialogqueststoryengine.registry.QuestStepHandler;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public final class KillEntityStepType implements QuestStepHandler {

  public static final ResourceLocation TYPE_ID =
      new ResourceLocation(Constants.MOD_NAMESPACE, "kill_entity");

  static final String FIELD_ENTITY = "entity";

  @Override
  public void validate(
      ResourceLocation questId, RawQuestStep step, String filePath, List<ContentIssue> issues) {
    JsonObject jsonObject = step.typeSpecificJson();
    String fieldPath = "logic.steps." + step.id() + "." + FIELD_ENTITY;
    if (!jsonObject.has(FIELD_ENTITY) || !jsonObject.get(FIELD_ENTITY).isJsonPrimitive()) {
      issues.add(
          ContentIssue.of(
              IssueCode.MISSING_FIELD, ContentType.QUEST, questId, filePath, fieldPath));
      return;
    }

    String entity = jsonObject.get(FIELD_ENTITY).getAsString();
    ResourceLocation entityId = ResourceLocation.tryParse(entity);
    if (entityId == null) {
      issues.add(
          ContentIssue.of(
              IssueCode.INVALID_RESOURCE_LOCATION,
              ContentType.QUEST,
              questId,
              filePath,
              fieldPath,
              Map.of("value", entity)));
      return;
    }

    RegistryReferenceValidator.requireRegistered(
        BuiltInRegistries.ENTITY_TYPE,
        entityId,
        ContentType.QUEST,
        questId,
        filePath,
        fieldPath,
        issues);
  }

  @Override
  public void onEntityKilled(QuestStepContext context, LivingEntity killed) {
    JsonObject jsonObject = context.step().typeSpecificJson();
    if (!jsonObject.has(FIELD_ENTITY) || !jsonObject.get(FIELD_ENTITY).isJsonPrimitive()) {
      return;
    }

    ResourceLocation targetTypeId =
        ResourceLocation.tryParse(jsonObject.get(FIELD_ENTITY).getAsString());
    if (targetTypeId != null
        && targetTypeId.equals(BuiltInRegistries.ENTITY_TYPE.getKey(killed.getType()))) {
      context.progress(1);
    }
  }
}
