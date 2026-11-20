package com.marbledhubb.attachments.client.model;

import com.marbledhubb.attachments.client.AttachmentRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class AttachmentHolderArmorModel<T extends LivingEntity> extends HumanoidModel<T> {
    private final Model baseModel;
    private final T entity;
    private final Map<ItemStack, AttachmentRenderer> attachments;

    public RenderLayerParent<T, ? extends HumanoidModel<T>> renderLayerParent;
    private float limbSwing;
    private float limbSwingAmount;
    public float partialTick;
    private float ageInTicks;
    private float netHeadYaw;
    private float headPitch;

    public AttachmentHolderArmorModel(Model baseModel, T entity, Map<ItemStack, AttachmentRenderer> attachments) {
        super(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER_INNER_ARMOR));
        this.baseModel = baseModel;
        this.entity = entity;
        this.attachments = attachments;
    }

    @Override
    public void setupAnim(@NotNull T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        this.limbSwing = limbSwing;
        this.limbSwingAmount = limbSwingAmount;
        this.ageInTicks = ageInTicks;
        this.netHeadYaw = netHeadYaw;
        this.headPitch = headPitch;
    }

    @Override
    public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        baseModel.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);

        MultiBufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        attachments.forEach((itemStack, renderer) -> renderer.render(itemStack, entity, poseStack, renderLayerParent, bufferSource, packedLight, limbSwing, limbSwingAmount, partialTick, ageInTicks, netHeadYaw, headPitch));
    }
}
