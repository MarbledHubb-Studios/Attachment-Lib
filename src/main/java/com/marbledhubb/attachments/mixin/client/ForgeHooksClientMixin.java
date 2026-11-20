package com.marbledhubb.attachments.mixin.client;

import com.marbledhubb.attachments.world.item.AttachmentHolderItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ForgeHooksClient.class)
public class ForgeHooksClientMixin {
    @Inject(method = "getArmorModel", at = @At("RETURN"), cancellable = true)
    private static void modifyArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original, CallbackInfoReturnable<Model> cir) {
        if (itemStack.getItem() instanceof AttachmentHolderItem) {
            Model result = AttachmentHolderItem.getArmorModel(cir.getReturnValue(), livingEntity, itemStack, equipmentSlot);
            //ForgeHooksClient.copyModelProperties(original, result);
            cir.setReturnValue(result);
        }
    }
}
