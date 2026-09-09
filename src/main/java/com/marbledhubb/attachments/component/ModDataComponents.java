package com.marbledhubb.attachments.component;

import com.marbledhubb.attachments.AttachmentLib;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.TreeMap;
import java.util.function.UnaryOperator;

public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, AttachmentLib.MOD_ID);

    public static DeferredHolder<DataComponentType<?>, DataComponentType<TreeMap<Integer, ItemStack>>> ATTACHMENTS =
            register("attachments", builder -> builder
                    .persistent(Codec.list(Codec.pair(Codec.INT, ItemStack.CODEC)).xmap(list -> {
                        TreeMap<Integer, ItemStack> map = new TreeMap<>();
                        for (Pair<Integer, ItemStack> entry : list) {
                            map.put(entry.getFirst(), entry.getSecond());
                        }
                        return map;
                    }, map -> map.entrySet().stream().map(entry -> Pair.of(entry.getKey(), entry.getValue())).toList()))
                    .networkSynchronized(ByteBufCodecs.map(size -> new TreeMap<>(), ByteBufCodecs.INT, ItemStack.STREAM_CODEC)));

    public static DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> SELECTED_ATTACHMENT_SLOT_INDEX =
            register("selected_attachment_slot_index", builder -> builder
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.INT));

    private static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(String name, UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        return DATA_COMPONENT_TYPES.register(name, () -> builderOperator.apply(DataComponentType.builder()).build());
    }

    public static void register(IEventBus eventBus) {
        DATA_COMPONENT_TYPES.register(eventBus);
    }
}
