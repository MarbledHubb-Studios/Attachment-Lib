package com.marbledhubb.attachments.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public interface AttachmentRenderer {
    <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack itemStack, LivingEntity entity, PoseStack poseStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource bufferSource, int packedLight, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch);

    static void renderModel(Model model, ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource renderTypeBuffer, int light, @Nullable RenderType glintRender) {
        RenderType renderType = model.renderType(textureLocation);
        VertexConsumer vertexConsumer;
        if (glintRender != null) {
            vertexConsumer = VertexMultiConsumer.create(renderTypeBuffer.getBuffer(glintRender), renderTypeBuffer.getBuffer(renderType));
        } else {
            vertexConsumer = renderTypeBuffer.getBuffer(renderType);
        }

        model.renderToBuffer(poseStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
    }
}
