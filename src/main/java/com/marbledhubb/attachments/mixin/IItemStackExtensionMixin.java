package com.marbledhubb.attachments.mixin;

import com.marbledhubb.attachments.world.item.AttachmentHolderItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.neoforge.common.extensions.IItemStackExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IItemStackExtension.class)
public interface IItemStackExtensionMixin {
    @Inject(method = "getAttributeModifiers", at = @At("RETURN"), cancellable = true)
    private void getAttributeModifiers(CallbackInfoReturnable<ItemAttributeModifiers> cir) {
        ItemStack self = (ItemStack) (Object) this;
        if (self.getItem() instanceof AttachmentHolderItem holder)
            cir.setReturnValue(holder.getAttributeModifiers(cir.getReturnValue(), self));
    }
}
