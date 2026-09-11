package com.marbledhubb.attachments.client;

import com.marbledhubb.attachments.AttachmentLib;
import com.marbledhubb.attachments.mixin.client.AbstractContainerScreenInvoker;
import com.marbledhubb.attachments.network.ShiftSelectedAttachmentSlotMessage;
import com.marbledhubb.attachments.world.item.AttachmentHolderItem;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = AttachmentLib.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class AttachmentHolderScrollHandler {
    private static double accumulativeScrollDelta = 0;

    @SubscribeEvent
    public static void onMouseScrolled(ScreenEvent.MouseScrolled.Pre event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) return;
        double x = event.getMouseX();
        double y = event.getMouseY();
        double scrollDelta = event.getScrollDelta();

        Slot slot = ((AbstractContainerScreenInvoker) screen).invokeFindSlot(x, y);
        if (slot == null) {
            return;
        }
        ItemStack itemStack = slot.getItem();
        if (!(itemStack.getItem() instanceof AttachmentHolderItem attachmentHolder)) {
            return;
        }
        if (accumulativeScrollDelta * scrollDelta < 0) {
            accumulativeScrollDelta = 0;
        }
        accumulativeScrollDelta += scrollDelta;
        int shiftAmount = (int) accumulativeScrollDelta;
        accumulativeScrollDelta -= shiftAmount;
        if (shiftAmount == 0) {
            return;
        }

        if (attachmentHolder.shiftSelectedSlot(itemStack, shiftAmount))
            AttachmentLib.PACKET_HANDLER.sendToServer(new ShiftSelectedAttachmentSlotMessage(
                    screen.getMenu().containerId,
                    screen.getMenu().getStateId(),
                    slot.index,
                    shiftAmount)
            );
    }
}
