package com.marbledhubb.attachments.world.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public interface AttachmentItem {
    default void attachmentTick(ItemStack attachmentStack, ItemStack holderStack, int slotIndex, Level level, Entity entity) {}
    default void attachmentStorageTick(ItemStack itemStack, ItemStack holder, int slotIndex, Level level, Entity entity) {}

    default void onEquip(EquipmentSlot slot, ItemStack attachmentStack, ItemStack holderStack, int slotIndex, LivingEntity entity) {}
    default void onUnequip(EquipmentSlot slot, ItemStack attachmentStack, ItemStack holderStack, int slotIndex, LivingEntity entity) {}

    default void onAttach(AttachmentSlotSettings slotSettings, ItemStack attachmentStack, ItemStack holderStack, int slotIndex, LivingEntity entity) {}
    default void onDetach(AttachmentSlotSettings slotSettings, ItemStack attachmentStack, ItemStack holderStack, int slotIndex, LivingEntity entity) {}

    default boolean canAttach(AttachmentSlotSettings slotSettings, ItemStack attachmentStack, ItemStack holderStack, int slotIndex, LivingEntity entity) {
        return true;
    }

    default boolean canDetach(AttachmentSlotSettings slotSettings, ItemStack attachmentStack, ItemStack holderStack, int slotIndex, LivingEntity entity) {
        return true;
    }

    // TODO?
    //default List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack attachmentStack) {
    //    return tooltips;
    //}

    default Multimap<Attribute, AttributeModifier> getAttributeModifiers(AttachmentSlotSettings slotSettings, ItemStack attachmentStack, ItemStack holderStack, int slotIndex) {
        return HashMultimap.create();
    }

    default void onAttachFromUse(AttachmentSlotSettings slotSettings, ItemStack attachmentStack, ItemStack holderStack, int slotIndex, LivingEntity entity) {
        onAttach(slotSettings, attachmentStack, holderStack, slotIndex, entity);
    }

    default SoundEvent getAttachSound() {
        return SoundEvents.ARMOR_EQUIP_GENERIC;
    }

    default boolean canAttachFromUse(AttachmentSlotSettings slotSettings, ItemStack attachmentStack, ItemStack holderStack, int slotIndex, LivingEntity entity) {
        return canAttach(slotSettings, attachmentStack, holderStack, slotIndex, entity);
    }

    default void onAttachmentBreak(AttachmentSlotSettings slotSettings, ItemStack attachmentStack, ItemStack holderStack, int slotIndex, LivingEntity entity) {
    }

    default List<Component> getAttributesTooltip(List<Component> tooltips, ItemStack attachmentStack) {
        return tooltips;
    }
}
