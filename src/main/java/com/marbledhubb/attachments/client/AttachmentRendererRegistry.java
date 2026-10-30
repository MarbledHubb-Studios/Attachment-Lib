package com.marbledhubb.attachments.client;

import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class AttachmentRendererRegistry {
    private static final Map<Item, Supplier<AttachmentRenderer>> RENDERER_REGISTRY = new ConcurrentHashMap<>();
    private static final Map<Item, AttachmentRenderer> RENDERERS = new HashMap<>();

    public static void register(Item item, Supplier<AttachmentRenderer> renderer) {
        RENDERER_REGISTRY.put(item, renderer);
    }

    public static Optional<AttachmentRenderer> getRenderer(Item item) {
        return Optional.ofNullable(RENDERERS.get(item));
    }

    public static void load() {
        for(Map.Entry<Item, Supplier<AttachmentRenderer>> entry : RENDERER_REGISTRY.entrySet()) {
            RENDERERS.put(entry.getKey(), entry.getValue().get());
        }
    }
}
