package com.marbledhubb.attachments.network.payload;

import com.marbledhubb.attachments.AttachmentLib;
import com.marbledhubb.attachments.world.item.AttachmentHolderItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record ShiftSelectedAttachmentSlotPayload(int containerId, int stateId, int slotIndex, int shiftAmount) implements CustomPacketPayload {
    public static final Type<ShiftSelectedAttachmentSlotPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AttachmentLib.MOD_ID, "shift_selected_attachment_slot"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShiftSelectedAttachmentSlotPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    ShiftSelectedAttachmentSlotPayload::containerId,
                    ByteBufCodecs.VAR_INT,
                    ShiftSelectedAttachmentSlotPayload::stateId,
                    ByteBufCodecs.VAR_INT,
                    ShiftSelectedAttachmentSlotPayload::slotIndex,
                    ByteBufCodecs.VAR_INT,
                    ShiftSelectedAttachmentSlotPayload::shiftAmount,
                    ShiftSelectedAttachmentSlotPayload::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ShiftSelectedAttachmentSlotPayload payload, IPayloadContext context) {
        Player player = context.player();
        AbstractContainerMenu containerMenu = player.containerMenu;

        if (containerMenu.containerId != payload.containerId || !containerMenu.stillValid(player) || !containerMenu.isValidSlotIndex(payload.slotIndex)) return;

        if (player.isSpectator()) {
            containerMenu.sendAllDataToRemote();
            return;
        }

        Slot slot = player.containerMenu.getSlot(payload.slotIndex);
        ItemStack itemStack = slot.getItem();
        if (!(itemStack.getItem() instanceof AttachmentHolderItem attachmentHolder)) return;

        boolean flag = payload.stateId == containerMenu.getStateId();
        containerMenu.suppressRemoteUpdates();
        attachmentHolder.shiftSelectedSlot(itemStack, payload.shiftAmount);
        containerMenu.resumeRemoteUpdates();
        if (flag) {
            containerMenu.broadcastFullState();
        } else {
            containerMenu.broadcastChanges();
        }
    }
}
