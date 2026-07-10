package com.marbledhubb.attachments.tooltip;

import com.marbledhubb.attachments.item.AttachmentSlotSettings;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public record AttachmentHolderTooltip(AttachmentSlotSettings[] slotSettings, Map<Integer, ItemStack> attachments, int selectedSlotIndex) implements TooltipComponent {
}
