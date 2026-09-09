package com.marbledhubb.attachments;

import com.marbledhubb.attachments.component.ModDataComponents;
import com.marbledhubb.attachments.network.ModNetworking;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

@Mod(AttachmentLib.MOD_ID)
public class AttachmentLib {
    public static final String MOD_ID = "attachments";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AttachmentLib(IEventBus modEventBus, ModContainer modContainer) {
        ModDataComponents.register(modEventBus);
        modEventBus.addListener(ModNetworking::register);
    }
}
