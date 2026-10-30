package com.marbledhubb.attachments.event;

import com.marbledhubb.attachments.AttachmentLib;
import com.marbledhubb.attachments.world.item.AttachmentHolderItem;
import com.marbledhubb.attachments.world.item.AttachmentItem;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AttachmentLib.MOD_ID)
public class ModEvents {
    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        // TODO perhaps check if the itemstacks are even attachment holders in the first place? unsure it's even necessary -aimi
        AttachmentHolderItem.getAttachments(event.getFrom()).forEach((slotIndex, attachment) -> {
            if (attachment.getItem() instanceof AttachmentItem attachmentItem)
                attachmentItem.onUnequip(event.getSlot(), attachment, event.getFrom(), slotIndex, event.getEntity());
        });

        AttachmentHolderItem.getAttachments(event.getTo()).forEach((slotIndex, attachment) -> {
            if (attachment.getItem() instanceof AttachmentItem attachmentItem)
                attachmentItem.onEquip(event.getSlot(), attachment, event.getTo(), slotIndex, event.getEntity());
        });
    }

    @SubscribeEvent
    public static void modifyItemAttributeModifiers(ItemAttributeModifierEvent event) {
        if (event.getItemStack().getItem() instanceof AttachmentHolderItem item) {
            event.clearModifiers();
            item.getAttributeModifiers(event.getOriginalModifiers(), event.getSlotType(), event.getItemStack()).forEach(event::addModifier);
        }
    }
}
