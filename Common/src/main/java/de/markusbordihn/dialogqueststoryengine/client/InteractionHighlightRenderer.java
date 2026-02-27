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

package de.markusbordihn.dialogqueststoryengine.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionDataEntry;
import de.markusbordihn.dialogqueststoryengine.data.interaction.TargetKind;
import de.markusbordihn.dialogqueststoryengine.item.InteractionWandItem;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class InteractionHighlightRenderer {

  private static final float ALPHA = 1.0f;
  private static final double BLOCK_INFLATE = 0.005;
  private static final double MAX_RENDER_DISTANCE_SQ = 64.0 * 64.0;
  private static final float LABEL_SCALE = 0.025f;
  private static final int LABEL_BG_COLOR = 0x80000000;

  private InteractionHighlightRenderer() {}

  public static void renderHighlights(PoseStack poseStack, Camera camera) {
    Minecraft mc = Minecraft.getInstance();
    if (mc.player == null || mc.level == null) {
      return;
    }

    if (!(mc.player.getMainHandItem().getItem() instanceof InteractionWandItem)
        && !(mc.player.getOffhandItem().getItem() instanceof InteractionWandItem)) {
      return;
    }

    List<InteractionDataEntry> entries = InteractionClientData.getEntries();
    if (entries.isEmpty()) {
      return;
    }

    Vec3 cam = camera.getPosition();
    Player player = mc.player;

    boolean hasEntityEntries = false;
    for (InteractionDataEntry entry : entries) {
      if (entry.kind() == TargetKind.ENTITY) {
        hasEntityEntries = true;
        break;
      }
    }
    Map<UUID, Entity> entityMap;
    if (hasEntityEntries) {
      entityMap = new HashMap<>();
      for (Entity entity : mc.level.entitiesForRendering()) {
        entityMap.put(entity.getUUID(), entity);
      }
    } else {
      entityMap = Map.of();
    }

    MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
    VertexConsumer consumer = bufferSource.getBuffer(RenderType.lines());

    AABB[] cachedAABBs = new AABB[entries.size()];
    for (int i = 0; i < entries.size(); i++) {
      InteractionDataEntry entry = entries.get(i);
      AABB aabb = getAABB(entry, entityMap, player);
      cachedAABBs[i] = aabb;
      if (aabb == null) {
        continue;
      }
      AABB relative = aabb.move(-cam.x, -cam.y, -cam.z).inflate(BLOCK_INFLATE);
      TargetKind kind = entry.kind();
      LevelRenderer.renderLineBox(
          poseStack, consumer, relative, kind.getRed(), kind.getGreen(), kind.getBlue(), ALPHA);
    }

    bufferSource.endBatch(RenderType.lines());

    Font font = mc.font;
    for (int i = 0; i < entries.size(); i++) {
      AABB aabb = cachedAABBs[i];
      if (aabb == null) {
        continue;
      }
      renderLabel(poseStack, bufferSource, font, camera, aabb, entries.get(i));
    }
  }

  private static void renderLabel(
      PoseStack poseStack,
      MultiBufferSource.BufferSource bufferSource,
      Font font,
      Camera camera,
      AABB aabb,
      InteractionDataEntry entry) {
    Component line1 = Component.literal(entry.type().name() + " | " + entry.kind().name());
    String detail =
        entry.label() != null && !entry.label().isEmpty()
            ? entry.label()
            : entry.targetId().toString().substring(0, 8);
    Component line2 = Component.literal(detail);

    double cx = aabb.getCenter().x;
    double cy = aabb.maxY + 0.5;
    double cz = aabb.getCenter().z;
    Vec3 cam = camera.getPosition();

    poseStack.pushPose();
    poseStack.translate(cx - cam.x, cy - cam.y, cz - cam.z);
    poseStack.mulPose(Axis.YP.rotationDegrees(-camera.getYRot()));
    poseStack.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
    poseStack.scale(-LABEL_SCALE, -LABEL_SCALE, LABEL_SCALE);

    int textColor = entry.kind().getLabelColor();
    float halfWidth1 = font.width(line1) / 2.0f;
    font.drawInBatch(
        line1,
        -halfWidth1,
        0,
        textColor,
        false,
        poseStack.last().pose(),
        bufferSource,
        Font.DisplayMode.SEE_THROUGH,
        LABEL_BG_COLOR,
        0xF000F0);
    float halfWidth2 = font.width(line2) / 2.0f;
    font.drawInBatch(
        line2,
        -halfWidth2,
        font.lineHeight + 1,
        0xFFCCCCCC,
        false,
        poseStack.last().pose(),
        bufferSource,
        Font.DisplayMode.SEE_THROUGH,
        LABEL_BG_COLOR,
        0xF000F0);
    bufferSource.endBatch();
    poseStack.popPose();
  }

  private static AABB getAABB(
      InteractionDataEntry entry, Map<UUID, Entity> entityMap, Player player) {
    if ((entry.kind() == TargetKind.BLOCK || entry.kind() == TargetKind.BLOCK_ENTITY)
        && entry.blockPos() != null) {
      BlockPos pos = entry.blockPos();
      if (player.blockPosition().distSqr(pos) > MAX_RENDER_DISTANCE_SQ) {
        return null;
      }
      return new AABB(pos);
    } else if (entry.kind() == TargetKind.ENTITY) {
      Entity entity = entityMap.get(entry.targetId());
      if (entity != null) {
        return entity.getBoundingBox();
      }
    }
    return null;
  }
}
