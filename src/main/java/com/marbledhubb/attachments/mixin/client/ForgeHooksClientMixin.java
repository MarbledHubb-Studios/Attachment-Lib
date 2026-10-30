package com.marbledhubb.attachments.mixin.client;

import com.marbledhubb.attachments.world.item.AttachmentHolderItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ForgeHooksClient.class)
public class ForgeHooksClientMixin {
    @Redirect(method = "getArmorModel", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/extensions/common/IClientItemExtensions;getGenericArmorModel(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/client/model/HumanoidModel;)Lnet/minecraft/client/model/Model;"))
    private static @NotNull Model modifyArmorModel(IClientItemExtensions instance, LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
        HumanoidModel<?> result = original;

        if (itemStack.getItem() instanceof AttachmentHolderItem) {
            result = AttachmentHolderItem.getArmorModel(original, livingEntity, itemStack, equipmentSlot);
            ForgeHooksClient.copyModelProperties(original, result);
        }

        return result;
    }
}
