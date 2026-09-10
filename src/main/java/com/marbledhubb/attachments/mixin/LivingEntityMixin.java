package com.marbledhubb.attachments.mixin;

import com.marbledhubb.attachments.component.ModDataComponents;
import com.marbledhubb.attachments.world.item.AttachmentHolderItem;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "onEquipItem", at = @At("HEAD"), cancellable = true)
    private void onEquipItem(EquipmentSlot slot, ItemStack oldItem, ItemStack newItem, CallbackInfo ci) {
        if (oldItem.getItem() instanceof AttachmentHolderItem && oldItem.getItem() == newItem.getItem()) {
            Map<DataComponentType<?>, Optional<?>> oldComponents = oldItem.getComponentsPatch().entrySet().stream()
                    .filter(entry ->
                            entry.getKey() != ModDataComponents.ATTACHMENTS.get() && entry.getKey() != ModDataComponents.SELECTED_ATTACHMENT_SLOT_INDEX.get())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            Map<DataComponentType<?>, Optional<?>> newComponents = newItem.getComponentsPatch().entrySet().stream()
                    .filter(entry ->
                            entry.getKey() != ModDataComponents.ATTACHMENTS.get() && entry.getKey() != ModDataComponents.SELECTED_ATTACHMENT_SLOT_INDEX.get())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));


            if (oldComponents.equals(newComponents)) ci.cancel();
        }
    }
}
