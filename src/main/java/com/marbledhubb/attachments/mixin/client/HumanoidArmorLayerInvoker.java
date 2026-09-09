package com.marbledhubb.attachments.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@OnlyIn(Dist.CLIENT)
@Mixin(HumanoidArmorLayer.class)
public interface HumanoidArmorLayerInvoker {
    @Invoker("usesInnerModel")
    boolean invokeUsesInnerModel(EquipmentSlot equipmentSlot);



    @Invoker("renderModel")
    void invokeRenderModel(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, Model model, int dyeColor, ResourceLocation textureLocation);

    @Invoker("renderTrim")
    void invokeRenderTrim(Holder<ArmorMaterial> armorMaterial, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, ArmorTrim trim, Model model, boolean innerTexture);

    @Invoker("renderGlint")
    void invokeRenderGlint(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, Model model);
}
