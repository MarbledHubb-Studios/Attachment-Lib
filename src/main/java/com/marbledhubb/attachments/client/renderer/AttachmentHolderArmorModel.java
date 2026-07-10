package com.marbledhubb.attachments.client.renderer;

import com.marbledhubb.attachments.mixin.client.HumanoidArmorLayerInvoker;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.armortrim.ArmorTrim;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class AttachmentHolderArmorModel<T extends LivingEntity> extends HumanoidModel<T> {
    private final Model baseModel;
    private final T entity;
    private final EquipmentSlot equipmentSlot;
    private final Map<ItemStack, Model> attachments;

    public HumanoidArmorLayer<?, ?, ?> armorLayer;

    public AttachmentHolderArmorModel(Model baseModel, T entity, EquipmentSlot equipmentSlot, Map<ItemStack, Model> attachments) {
        super(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER_INNER_ARMOR));
        this.baseModel = baseModel;
        this.entity = entity;
        this.equipmentSlot = equipmentSlot;
        this.attachments = attachments;
    }

    @Override
    public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        baseModel.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);

        MultiBufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        attachments.forEach((itemStack, model) -> renderArmorPiece(poseStack, bufferSource, entity, equipmentSlot, packedLight, itemStack, model));
    }

    private void renderArmorPiece(PoseStack poseStack, MultiBufferSource bufferSource, T entity, EquipmentSlot equipmentSlot, int packedLight, ItemStack itemStack, Model model) {
        HumanoidArmorLayerInvoker armorLayerInvoker = (HumanoidArmorLayerInvoker) armorLayer;
        ArmorItem item = (ArmorItem) itemStack.getItem();

        boolean usesInnerModel = armorLayerInvoker.invokeUsesInnerModel(equipmentSlot);
        if (item instanceof DyeableLeatherItem dyeableLeatherItem) {
            int i = dyeableLeatherItem.getColor(itemStack);
            float f = (float)(i >> 16 & 255) / 255.0F;
            float f1 = (float)(i >> 8 & 255) / 255.0F;
            float f2 = (float)(i & 255) / 255.0F;
            armorLayerInvoker.invokeRenderModel(poseStack, bufferSource, packedLight, item, model, usesInnerModel, f, f1, f2, armorLayer.getArmorResource(entity, itemStack, equipmentSlot, null));
            armorLayerInvoker.invokeRenderModel(poseStack, bufferSource, packedLight, item, model, usesInnerModel, 1.0F, 1.0F, 1.0F, armorLayer.getArmorResource(entity, itemStack, equipmentSlot, "overlay"));
        } else {
            armorLayerInvoker.invokeRenderModel(poseStack, bufferSource, packedLight, item, model, usesInnerModel, 1.0F, 1.0F, 1.0F, armorLayer.getArmorResource(entity, itemStack, equipmentSlot, null));
        }
        ArmorTrim.getTrim(entity.level().registryAccess(), itemStack).ifPresent((armorTrim) ->
                armorLayerInvoker.invokeRenderTrim(item.getMaterial(), poseStack, bufferSource, packedLight, armorTrim, model, usesInnerModel));
        if (itemStack.hasFoil()) {
            armorLayerInvoker.invokeRenderGlint(poseStack, bufferSource, packedLight, model);
        }
    }
}
