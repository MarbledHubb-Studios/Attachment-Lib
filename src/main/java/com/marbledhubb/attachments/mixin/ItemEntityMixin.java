package com.marbledhubb.attachments.mixin;

import com.marbledhubb.attachments.world.item.AttachmentHolderItem;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {
    @Inject(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;onDestroyed(Lnet/minecraft/world/entity/item/ItemEntity;Lnet/minecraft/world/damagesource/DamageSource;)V", shift = At.Shift.AFTER))
    private void onDestroyed(DamageSource pSource, float pAmount, CallbackInfoReturnable<Boolean> cir) {
        List<ItemStack> list = new ArrayList<>();
        AttachmentHolderItem.getAttachments(((ItemEntity) (Object) this).getItem()).forEach((slotIndex, attachment) -> list.add(attachment));

        ItemUtils.onContainerDestroyed((ItemEntity) (Object) this, list.stream());
    }
}
