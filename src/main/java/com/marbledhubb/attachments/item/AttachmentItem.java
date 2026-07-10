package com.marbledhubb.attachments.item;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface AttachmentItem {
    default void onEquipInAttachmentSlot(ItemStack itemStack, ItemStack holder, LivingEntity entity) {}
    default void onUnequipFromAttachmentSlot(ItemStack itemStack, ItemStack holder, LivingEntity entity) {}

    default void onAttachmentEquipInEquipmentSlot(EquipmentSlot slot, ItemStack itemStack, ItemStack holder, int slotIndex, LivingEntity entity) {}
    default void onAttachmentUnequipFromEquipmentSlot(EquipmentSlot slot, ItemStack itemStack, ItemStack holder, int slotIndex, LivingEntity entity) {}

    default void attachmentTick(ItemStack itemStack, ItemStack holder, int slotIndex, Level level, Entity entity) {}
    default void attachmentStorageTick(ItemStack itemStack, ItemStack holder, int slotIndex, Level level, Entity entity) {}

    default boolean mayRenderArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot) {
        return true;
    }
}
