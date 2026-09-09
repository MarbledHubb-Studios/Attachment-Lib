package com.marbledhubb.attachments;

import com.marbledhubb.attachments.client.AttachmentHolderScrollHandler;
import com.marbledhubb.attachments.client.gui.screens.inventory.tooltip.ModClientTooltipComponents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = AttachmentLib.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = AttachmentLib.MOD_ID, value = Dist.CLIENT)
public class AttachmentLibClient {
    public AttachmentLibClient(ModContainer container) {
        NeoForge.EVENT_BUS.addListener(AttachmentHolderScrollHandler::onMouseScrolled);
        NeoForge.EVENT_BUS.addListener(ModClientTooltipComponents::register);
    }
}
