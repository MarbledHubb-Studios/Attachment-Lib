package com.marbledhubb.attachments.item;

import com.google.common.collect.Multimap;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Consumer;

public class DefaultedAttachmentHolderItem extends Item implements AttachmentHolderItem {
    private final AttachmentSlotSettings[] attachmentSlotSettings;

    public DefaultedAttachmentHolderItem(Properties properties, AttachmentSlotSettings[] attachmentSlotSettings) {
        super(properties);
        this.attachmentSlotSettings = attachmentSlotSettings;
    }

    @Override
    public boolean overrideStackedOnOther(@NotNull ItemStack itemStack, @NotNull Slot slot, @NotNull ClickAction clickAction, @NotNull Player player) {
        return AttachmentHolderItem.super.overrideStackedOnOther(itemStack, slot, clickAction, player);
    }

    @Override
    public boolean overrideOtherStackedOnMe(@NotNull ItemStack itemStack, @NotNull ItemStack possibleAttachment, @NotNull Slot slot, @NotNull ClickAction clickAction, @NotNull Player player, @NotNull SlotAccess slotAccess) {
        return AttachmentHolderItem.super.overrideOtherStackedOnMe(itemStack, possibleAttachment, slot, clickAction, player, slotAccess);
    }

    @Override
    public @NotNull Multimap<Attribute, AttributeModifier> getAttributeModifiers(@NotNull EquipmentSlot equipmentSlot, ItemStack itemStack) {
        return AttachmentHolderItem.super.getAttributeModifiers(super.getAttributeModifiers(equipmentSlot, itemStack), equipmentSlot, itemStack);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack itemStack, @NotNull Level level, @NotNull Entity entity, int slotIndex, boolean selected) {
        super.inventoryTick(itemStack, level, entity, slotIndex, selected);
        AttachmentHolderItem.super.inventoryTick(itemStack, level, entity, slotIndex, selected);
    }

    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
        AttachmentHolderItem.super.damageAttachments(stack, amount, entity, onBroken);
        return super.damageItem(stack, amount, entity, onBroken);
    }

    @Override
    public void onDestroyed(ItemEntity itemEntity, DamageSource damageSource) {
        super.onDestroyed(itemEntity, damageSource);
        AttachmentHolderItem.super.onDestroyed(itemEntity, damageSource);
    }

    @Override
    public @NotNull Optional<TooltipComponent> getTooltipImage(@NotNull ItemStack itemStack) {
        return AttachmentHolderItem.super.getTooltipImage(itemStack);
    }

    @Override
    public AttachmentSlotSettings[] getAttachmentSlotSettings() {
        return attachmentSlotSettings;
    }
}
