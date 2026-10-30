package com.marbledhubb.attachments.mixin;

import com.marbledhubb.attachments.world.item.AttachmentHolderItem;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.common.extensions.IForgeItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IForgeItemStack.class)
public interface IForgeItemStackMixin {
    @Inject(method = "onDestroyed", at = @At("RETURN"))
    default void injectToOnDestroyed(ItemEntity itemEntity, DamageSource damageSource, CallbackInfo ci) {
        AttachmentHolderItem.onDestroyed(itemEntity);
    }
}
