package com.marbledhubb.attachments.world.item;

import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

public class DefaultedAttachmentHolderArmorItem extends ArmorItem implements AttachmentHolderItem {
    private final AttachmentSlotSettings[] attachmentSlotSettings;

    public DefaultedAttachmentHolderArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties, AttachmentSlotSettings[] attachmentSlotSettings) {
        super(material, type, properties);
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
    public void inventoryTick(@NotNull ItemStack itemStack, @NotNull Level level, @NotNull Entity entity, int slotIndex, boolean selected) {
        super.inventoryTick(itemStack, level, entity, slotIndex, selected);
        AttachmentHolderItem.super.inventoryTick(itemStack, level, entity, slotIndex, selected);
    }

    @Override
    public <T extends LivingEntity> int damageItem(@NotNull ItemStack stack, int amount, @Nullable T entity, @NotNull Consumer<Item> onBroken) {
        AttachmentHolderItem.super.damageAttachments(stack, amount, entity, onBroken);
        return super.damageItem(stack, amount, entity, onBroken);
    }

    @Override
    public void onDestroyed(@NotNull ItemEntity itemEntity, @NotNull DamageSource damageSource) {
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
