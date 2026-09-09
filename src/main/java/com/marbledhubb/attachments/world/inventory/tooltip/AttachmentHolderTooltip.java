package com.marbledhubb.attachments.world.inventory.tooltip;

import com.marbledhubb.attachments.world.item.AttachmentSlotSettings;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public record AttachmentHolderTooltip(AttachmentSlotSettings[] slotSettings, Map<Integer, ItemStack> attachments, int selectedSlotIndex) implements TooltipComponent {
}
