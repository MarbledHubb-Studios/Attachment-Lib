package com.marbledhubb.attachments.client;

import com.marbledhubb.attachments.world.item.AttachmentHolderItem;
import com.marbledhubb.attachments.mixin.client.AbstractContainerScreenInvoker;
import com.marbledhubb.attachments.network.payload.ShiftSelectedAttachmentSlotPayload;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@OnlyIn(Dist.CLIENT)
public class AttachmentHolderScrollHandler {
    private static double accumulativeScrollDelta = 0;

    public static void onMouseScrolled(ScreenEvent.MouseScrolled.Pre event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) return;
        double x = event.getMouseX();
        double y = event.getMouseY();
        double scrollDelta = event.getScrollDeltaY();

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
            PacketDistributor.sendToServer(new ShiftSelectedAttachmentSlotPayload(
                    screen.getMenu().containerId,
                    screen.getMenu().getStateId(),
                    slot.index,
                    shiftAmount)
            );
    }
}
