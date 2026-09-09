package com.marbledhubb.attachments.network;

import com.marbledhubb.attachments.network.payload.ShiftSelectedAttachmentSlotPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModNetworking {
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(
                ShiftSelectedAttachmentSlotPayload.TYPE,
                ShiftSelectedAttachmentSlotPayload.STREAM_CODEC,
                ShiftSelectedAttachmentSlotPayload::handle
        );
    }
}
