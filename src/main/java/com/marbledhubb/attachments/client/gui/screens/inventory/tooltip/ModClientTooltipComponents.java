package com.marbledhubb.attachments.client.gui.screens.inventory.tooltip;

import com.marbledhubb.attachments.client.gui.screens.inventory.tooltip.custom.ClientAttachmentHolderTooltip;
import com.marbledhubb.attachments.world.inventory.tooltip.AttachmentHolderTooltip;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;

public class ModClientTooltipComponents {
    public static void register(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(AttachmentHolderTooltip.class, ClientAttachmentHolderTooltip::new);
    }
}
