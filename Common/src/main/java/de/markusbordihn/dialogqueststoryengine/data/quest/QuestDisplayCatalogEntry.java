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

package de.markusbordihn.dialogqueststoryengine.data.quest;

import de.markusbordihn.dialogqueststoryengine.data.quest.content.PrerequisiteMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record QuestDisplayCatalogEntry(
    ResourceLocation questId,
    String titleKey,
    String descriptionKey,
    Optional<ResourceLocation> categoryId,
    String categoryKey,
    Optional<ResourceLocation> arcId,
    Optional<String> arcKey,
    int arcOrder,
    Optional<ResourceLocation> chapterId,
    Optional<String> chapterKey,
    int chapterOrder,
    Optional<ResourceLocation> icon,
    QuestAvailability derivedAvailability,
    PrerequisiteMode prerequisiteMode,
    List<ResourceLocation> visiblePrerequisiteIds,
    Optional<String> rewardTitleKey,
    Optional<String> rewardDescriptionKey,
    List<RewardDisplayEntry> rewardEntries,
    int sortOrder,
    List<QuestStepDisplayEntry> steps) {

  public static QuestDisplayCatalogEntry read(FriendlyByteBuf buffer) {
    ResourceLocation questId = buffer.readResourceLocation();
    String titleKey = buffer.readUtf();
    String descriptionKey = buffer.readUtf();
    Optional<ResourceLocation> categoryId =
        buffer.readOptional(FriendlyByteBuf::readResourceLocation);
    String categoryKey = buffer.readUtf();
    Optional<ResourceLocation> arcId = buffer.readOptional(FriendlyByteBuf::readResourceLocation);
    Optional<String> arcKey = buffer.readOptional(FriendlyByteBuf::readUtf);
    int arcOrder = buffer.readVarInt();
    Optional<ResourceLocation> chapterId =
        buffer.readOptional(FriendlyByteBuf::readResourceLocation);
    Optional<String> chapterKey = buffer.readOptional(FriendlyByteBuf::readUtf);
    int chapterOrder = buffer.readVarInt();
    Optional<ResourceLocation> icon = buffer.readOptional(FriendlyByteBuf::readResourceLocation);
    QuestAvailability availability = buffer.readEnum(QuestAvailability.class);
    PrerequisiteMode prerequisiteMode = buffer.readEnum(PrerequisiteMode.class);
    List<ResourceLocation> visiblePrerequisiteIds =
        buffer.readCollection(ArrayList::new, FriendlyByteBuf::readResourceLocation);
    Optional<String> rewardTitleKey = buffer.readOptional(FriendlyByteBuf::readUtf);
    Optional<String> rewardDescriptionKey = buffer.readOptional(FriendlyByteBuf::readUtf);
    List<RewardDisplayEntry> rewardEntries =
        buffer.readCollection(ArrayList::new, RewardDisplayEntry::read);
    int sortOrder = buffer.readVarInt();
    List<QuestStepDisplayEntry> steps =
        buffer.readCollection(ArrayList::new, QuestStepDisplayEntry::read);
    return new QuestDisplayCatalogEntry(
        questId,
        titleKey,
        descriptionKey,
        categoryId,
        categoryKey,
        arcId,
        arcKey,
        arcOrder,
        chapterId,
        chapterKey,
        chapterOrder,
        icon,
        availability,
        prerequisiteMode,
        visiblePrerequisiteIds,
        rewardTitleKey,
        rewardDescriptionKey,
        rewardEntries,
        sortOrder,
        steps);
  }

  public void write(FriendlyByteBuf buffer) {
    buffer.writeResourceLocation(this.questId);
    buffer.writeUtf(this.titleKey);
    buffer.writeUtf(this.descriptionKey);
    buffer.writeOptional(this.categoryId, FriendlyByteBuf::writeResourceLocation);
    buffer.writeUtf(this.categoryKey);
    buffer.writeOptional(this.arcId, FriendlyByteBuf::writeResourceLocation);
    buffer.writeOptional(this.arcKey, FriendlyByteBuf::writeUtf);
    buffer.writeVarInt(this.arcOrder);
    buffer.writeOptional(this.chapterId, FriendlyByteBuf::writeResourceLocation);
    buffer.writeOptional(this.chapterKey, FriendlyByteBuf::writeUtf);
    buffer.writeVarInt(this.chapterOrder);
    buffer.writeOptional(this.icon, FriendlyByteBuf::writeResourceLocation);
    buffer.writeEnum(this.derivedAvailability);
    buffer.writeEnum(this.prerequisiteMode);
    buffer.writeCollection(this.visiblePrerequisiteIds, FriendlyByteBuf::writeResourceLocation);
    buffer.writeOptional(this.rewardTitleKey, FriendlyByteBuf::writeUtf);
    buffer.writeOptional(this.rewardDescriptionKey, FriendlyByteBuf::writeUtf);
    buffer.writeCollection(this.rewardEntries, (targetBuffer, entry) -> entry.write(targetBuffer));
    buffer.writeVarInt(this.sortOrder);
    buffer.writeCollection(this.steps, (targetBuffer, entry) -> entry.write(targetBuffer));
  }
}
