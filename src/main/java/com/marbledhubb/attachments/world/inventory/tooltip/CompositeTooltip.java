package com.marbledhubb.attachments.world.inventory.tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;

public record CompositeTooltip(TooltipComponent[] children, int spacing, Layout layout) implements TooltipComponent {
    public enum Layout {
        VERTICAL,
        HORIZONTAL
    }
}
