package com.marbledhubb.attachments.mixin;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.text.DecimalFormat;

@Mixin(ItemStack.class)
public interface ItemStackAccessor {
    @Accessor("ATTRIBUTE_MODIFIER_FORMAT")
    static DecimalFormat getAttributeModifierFormat() {
        throw new AssertionError();
    }
}
