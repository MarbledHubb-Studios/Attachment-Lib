package com.marbledhubb.attachments.mixin.client;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@OnlyIn(Dist.CLIENT)
@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenInvoker {
    @Invoker("findSlot")
    Slot invokeFindSlot(double x, double y);
}
