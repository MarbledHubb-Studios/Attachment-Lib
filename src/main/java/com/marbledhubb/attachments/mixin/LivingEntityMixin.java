package com.marbledhubb.attachments.mixin;

import com.marbledhubb.attachments.item.AttachmentHolderItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "onEquipItem", at = @At("HEAD"), cancellable = true)
    private void onEquipItem(EquipmentSlot slot, ItemStack oldItem, ItemStack newItem, CallbackInfo ci) {
        if (oldItem.getItem() instanceof AttachmentHolderItem && oldItem.getItem() == newItem.getItem()) {
            CompoundTag oldTag = oldItem.getOrCreateTag().copy();
            oldTag.remove("Attachments");
            oldTag.remove("SelectedSlot");

            CompoundTag newTag = newItem.getOrCreateTag().copy();
            newTag.remove("Attachments");
            newTag.remove("SelectedSlot");

            if (oldTag.equals(newTag)) ci.cancel();
        }
    }
}
