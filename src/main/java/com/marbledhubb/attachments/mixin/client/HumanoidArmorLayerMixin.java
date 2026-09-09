package com.marbledhubb.attachments.mixin.client;

import com.marbledhubb.attachments.client.model.AttachmentHolderArmorModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@OnlyIn(Dist.CLIENT)
@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> {
    @ModifyVariable(method = "renderArmorPiece", at = @At(value = "STORE", target = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;getArmorModelHook(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/client/model/HumanoidModel;)Lnet/minecraft/client/model/Model;"))
    private Model renderArmorPiece(Model model) {
        if (model instanceof AttachmentHolderArmorModel<?> attachmentHolderArmorModel) {
            attachmentHolderArmorModel.armorLayer = (HumanoidArmorLayer<?, ?, ?>) (Object) this;
        }
        return model;
    }
}
