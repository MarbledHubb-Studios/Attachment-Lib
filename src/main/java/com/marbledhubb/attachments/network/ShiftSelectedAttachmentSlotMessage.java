package com.marbledhubb.attachments.network;

import com.marbledhubb.attachments.AttachmentLib;
import com.marbledhubb.attachments.item.AttachmentHolderItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public record ShiftSelectedAttachmentSlotMessage(int containerId, int stateId, int slotIndex, int shiftAmount) {
    public ShiftSelectedAttachmentSlotMessage(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt());
    }

    public void buffer(FriendlyByteBuf buf) {
        buf.writeInt(containerId);
        buf.writeInt(stateId);
        buf.writeInt(slotIndex);
        buf.writeInt(shiftAmount);
    }

    public void handler(Supplier<NetworkEvent.Context> supplier) {
        supplier.get().enqueueWork(() -> {
            ServerPlayer player = supplier.get().getSender();
            AbstractContainerMenu containerMenu = player.containerMenu;

            if (containerMenu.containerId != containerId || !containerMenu.stillValid(player) || !containerMenu.isValidSlotIndex(slotIndex)) return;

            if (player.isSpectator()) {
                containerMenu.sendAllDataToRemote();
                return;
            }

            Slot slot = player.containerMenu.getSlot(slotIndex);
            ItemStack itemStack = slot.getItem();
            if (!(itemStack.getItem() instanceof AttachmentHolderItem attachmentHolder)) return;

            boolean flag = stateId == containerMenu.getStateId();
            containerMenu.suppressRemoteUpdates();
            attachmentHolder.shiftSelectedSlot(itemStack, shiftAmount);
            containerMenu.resumeRemoteUpdates();
            if (flag) {
                containerMenu.broadcastFullState();
            } else {
                containerMenu.broadcastChanges();
            }
        });
        supplier.get().setPacketHandled(true);
    }

    @SubscribeEvent
    public static void registerMessage(FMLCommonSetupEvent event) {
        AttachmentLib.addNetworkMessage(ShiftSelectedAttachmentSlotMessage.class, ShiftSelectedAttachmentSlotMessage::buffer, ShiftSelectedAttachmentSlotMessage::new, ShiftSelectedAttachmentSlotMessage::handler);
    }
}
