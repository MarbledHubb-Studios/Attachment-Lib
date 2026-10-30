package com.marbledhubb.attachments.mixin.client;

import com.marbledhubb.attachments.client.model.AttachmentHolderArmorModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin<T extends LivingEntity> {
    @Unique
    private float attachments$partialTick;

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V", at = @At("HEAD"))
    private void capturePartialTick(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        this.attachments$partialTick = partialTick;
    }

    @ModifyVariable(method = "renderArmorPiece", at = @At(value = "STORE", target = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;getArmorModelHook(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/client/model/HumanoidModel;)Lnet/minecraft/client/model/Model;"))
    private Model passContext(Model model) {
        if (model instanceof AttachmentHolderArmorModel<?> holder) {
            holder.partialTick = this.attachments$partialTick;
            holder.renderLayerParent = ((RenderLayerAccessor) ((HumanoidArmorLayer<?, ?, ?>) (Object) this)).getRenderer();
        }

        return model;
    }
}
