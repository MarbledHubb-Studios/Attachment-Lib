package com.marbledhubb.attachments.world.item;

import com.marbledhubb.attachments.AttachmentLib;
import com.marbledhubb.attachments.client.model.AttachmentHolderArmorModel;
import com.marbledhubb.attachments.component.ModDataComponents;
import com.marbledhubb.attachments.world.inventory.tooltip.AttachmentHolderTooltip;
import com.marbledhubb.attachments.util.EquipmentUtil;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

public interface AttachmentHolderItem {
    AttachmentSlotSettings[] getAttachmentSlotSettings();

    //TODO ammunition like arrows for bows (and actual ammo e.g tacz guns) consumed from generic storage slots. refer to https://discord.com/channels/1459427496784298150/1491653851093401858/1519550589246111805 from ar task post - mn

    default boolean tryToEquipAttachment(ItemStack itemStack, ItemStack possibleAttachment, LivingEntity entity) {
        TreeMap<Integer, ItemStack> attachments = getAttachments(itemStack);

        int slotIndex = 0;
        for (AttachmentSlotSettings slotSettings : getAttachmentSlotSettings()) {
            if (!attachments.containsKey(slotIndex)) {
                if (slotSettings.mayPlace(possibleAttachment)) {
                    equipAttachment(itemStack, possibleAttachment, slotIndex, entity);
                    possibleAttachment.setCount(0);
                    return true;
                }
            }
            slotIndex++;
        }
        return false;
    }

    private void equipAttachment(ItemStack itemStack, ItemStack attachment, int slotIndex, LivingEntity entity) {
        if (attachment.getItem() instanceof AttachmentItem attachmentItem) {
            attachmentItem.onEquipInAttachmentSlot(attachment, itemStack, entity);
            EquipmentSlot equipmentSlot = EquipmentUtil.findEquipmentSlot(entity, itemStack);
            if (equipmentSlot != null) attachmentItem.onAttachmentEquipInEquipmentSlot(equipmentSlot, attachment, itemStack, slotIndex, entity);
        }
        playEquipAttachmentSound(itemStack, attachment, entity);

        TreeMap<Integer, ItemStack> attachments = new TreeMap<>(getAttachments(itemStack));
        attachments.put(slotIndex, attachment);
        itemStack.set(ModDataComponents.ATTACHMENTS, attachments);
    }

    private void addAttachmentAttributeModifiers(ItemStack itemStack, ItemStack attachment, int attachmentSlotIndex) {
        ItemAttributeModifiers modifiers = itemStack.getAttributeModifiers();

        for (ItemAttributeModifiers.Entry attachmentEntry : attachment.getAttributeModifiers().modifiers()) {
            boolean addNewModifier = true;

            AttributeModifier attachmentModifier = attachmentEntry.modifier();
            Holder<Attribute> attribute = attachmentEntry.attribute();

            for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
                if (entry.attribute() != attribute) continue;

                AttributeModifier modifier = entry.modifier();

                if (modifier.operation() == AttributeModifier.Operation.ADD_VALUE && attachmentModifier.operation() == AttributeModifier.Operation.ADD_VALUE) {
                    modifiers.modifiers().remove(entry);
                    modifiers.modifiers().add(new ItemAttributeModifiers.Entry(attribute, new AttributeModifier(modifier.id(), modifier.amount() + attachmentModifier.amount(), AttributeModifier.Operation.ADD_VALUE), entry.slot()));
                    addNewModifier = false;
                    break;
                } else if (modifier.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_BASE && attachmentModifier.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_BASE) {
                    modifiers.modifiers().remove(entry);
                    modifiers.modifiers().add(new ItemAttributeModifiers.Entry(attribute, new AttributeModifier(modifier.id(), modifier.amount() + attachmentModifier.amount(), AttributeModifier.Operation.ADD_MULTIPLIED_BASE), entry.slot()));
                    addNewModifier = false;
                    break;
                }
            }

            if (addNewModifier)
                modifiers.modifiers().add(new ItemAttributeModifiers.Entry(
                        attribute,
                        new AttributeModifier(ResourceLocation.fromNamespaceAndPath(AttachmentLib.MOD_ID, UUID.nameUUIDFromBytes((attachmentEntry.slot() + ":" + attachmentSlotIndex + ":" + attribute + ":" + attachmentModifier).getBytes(StandardCharsets.UTF_8)).toString()), attachmentModifier.amount(), attachmentModifier.operation()),
                        attachmentEntry.slot()));
        }
    }

    default Optional<ItemStack> tryToRemoveAttachment(boolean removeFirst, ItemStack itemStack, LivingEntity entity) {
        TreeMap<Integer, ItemStack> attachments = new TreeMap<>(getAttachments(itemStack));
        if (attachments.isEmpty()) {
            return Optional.empty();
        }

        int targetSlotIndex = removeFirst ? Collections.min(getAttachments(itemStack).keySet()) : getSelectedSlotIndex(itemStack);

        for (Map.Entry<Integer, ItemStack> entry : attachments.entrySet()) {
            if (entry.getKey() == targetSlotIndex) {
                if (entry.getValue().getItem() instanceof AttachmentItem attachmentItem) {
                    attachmentItem.onUnequipFromAttachmentSlot(entry.getValue(), itemStack, entity);
                    EquipmentSlot equipmentSlot = EquipmentUtil.findEquipmentSlot(entity, itemStack);
                    if (equipmentSlot != null) attachmentItem.onAttachmentUnequipFromEquipmentSlot(equipmentSlot, entry.getValue(), itemStack, entry.getKey(), entity);
                }
                playUnequipAttachmentSound(itemStack, entry.getValue(), entity);

                attachments.remove(entry.getKey());
                if (attachments.isEmpty()) {
                    itemStack.remove(ModDataComponents.ATTACHMENTS);
                }

                itemStack.remove(ModDataComponents.SELECTED_ATTACHMENT_SLOT_INDEX);

                return Optional.of(entry.getValue());
            }
        }
        return Optional.empty();
    }

    default TreeMap<Integer, ItemStack> getAttachments(ItemStack itemStack) {
        return itemStack.getOrDefault(ModDataComponents.ATTACHMENTS, new TreeMap<>());
    }

    default boolean shiftSelectedSlot(ItemStack itemStack, int shiftAmount) {
        TreeMap<Integer, ItemStack> attachments = getAttachments(itemStack);
        if (attachments.size() <= 1) return false;

        int slotIndex = getSelectedSlotIndex(itemStack);
        int slotAmount = getAttachmentSlotSettings().length;

        for (int i = 0; i < Math.abs(shiftAmount); ++i) {
            for (int j = 0; j < slotAmount; ++j) {
                if (shiftAmount > 0) {
                    if (++slotIndex >= slotAmount) slotIndex = 0;
                } else {
                    if (--slotIndex < 0) slotIndex = slotAmount - 1;
                }

                if (attachments.containsKey(slotIndex)) {
                    itemStack.set(ModDataComponents.SELECTED_ATTACHMENT_SLOT_INDEX, slotIndex);
                    break;
                }
            }
        }
        return true;
    }

    default int getSelectedSlotIndex(ItemStack itemStack) {
        if (itemStack.has(ModDataComponents.SELECTED_ATTACHMENT_SLOT_INDEX))
            return itemStack.get(ModDataComponents.SELECTED_ATTACHMENT_SLOT_INDEX);

        Map<Integer, ItemStack> attachments = getAttachments(itemStack);
        return attachments.isEmpty() ? -1 : Collections.min(attachments.keySet());
    }

    default void playEquipAttachmentSound(ItemStack itemStack, ItemStack attachment, Entity entity) {
        if (attachment.getItem() instanceof Equipable equipable) {
            entity.playSound(equipable.getEquipSound().value(), 1, 1);
        } else if (itemStack.getItem() instanceof Equipable equipable) {
            entity.playSound(equipable.getEquipSound().value(), 1, 1);
        }
    }

    default void playUnequipAttachmentSound(ItemStack itemStack, ItemStack attachment, Entity entity) {
    }

    default void onEquipInEquipmentSlot(EquipmentSlot slot, ItemStack previous, ItemStack itemStack, LivingEntity entity) {
        getAttachments(itemStack).forEach((slotIndex, attachment) -> {
            if (attachment.getItem() instanceof AttachmentItem attachmentItem)
                attachmentItem.onAttachmentEquipInEquipmentSlot(slot, attachment, itemStack, slotIndex, entity);
        });
    }

    default void onUnequipFromEquipmentSlot(EquipmentSlot slot, ItemStack itemStack, ItemStack next, LivingEntity entity) {
        getAttachments(itemStack).forEach((slotIndex, attachment) -> {
            if (attachment.getItem() instanceof AttachmentItem attachmentItem)
                attachmentItem.onAttachmentUnequipFromEquipmentSlot(slot, attachment, itemStack, slotIndex, entity);
        });
    }



    default boolean overrideStackedOnOther(@NotNull ItemStack itemStack, @NotNull Slot slot, @NotNull ClickAction clickAction, @NotNull Player player) {
        if (clickAction == ClickAction.SECONDARY) {
            if (slot.getItem().isEmpty()) {
                Optional<ItemStack> removedAttachment = tryToRemoveAttachment(true, itemStack, player);
                if (removedAttachment.isPresent()) {
                    slot.safeInsert(removedAttachment.get());
                    return true;
                }
                return false;
            }
            return tryToEquipAttachment(itemStack, slot.getItem(), player);
        }

        return false;
    }

    default boolean overrideOtherStackedOnMe(@NotNull ItemStack itemStack, @NotNull ItemStack possibleAttachment, @NotNull Slot slot, @NotNull ClickAction clickAction, @NotNull Player player, @NotNull SlotAccess slotAccess) {
        if (clickAction == ClickAction.SECONDARY && slot.allowModification(player)) {
            if (possibleAttachment.isEmpty()) {
                Optional<ItemStack> removedAttachment = tryToRemoveAttachment(false, itemStack, player);
                if (removedAttachment.isPresent()) {
                    slotAccess.set(removedAttachment.get());
                    return true;
                }
                return false;
            }
            return tryToEquipAttachment(itemStack, possibleAttachment, player);
        }

        return false;
    }

    default ItemAttributeModifiers getAttributeModifiers(ItemAttributeModifiers modifiers, ItemStack itemStack) {
        getAttachments(itemStack).forEach((attachmentSlotIndex, attachment) -> {
            for (ItemAttributeModifiers.Entry attachmentEntry : attachment.getAttributeModifiers().modifiers()) {
                boolean addNewModifier = true;

                AttributeModifier attachmentModifier = attachmentEntry.modifier();
                Holder<Attribute> attribute = attachmentEntry.attribute();

                for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
                    if (entry.attribute() != attribute) continue;

                    AttributeModifier modifier = entry.modifier();

                    if (modifier.operation() == AttributeModifier.Operation.ADD_VALUE && attachmentModifier.operation() == AttributeModifier.Operation.ADD_VALUE) {
                        modifiers.modifiers().remove(entry);
                        modifiers.modifiers().add(new ItemAttributeModifiers.Entry(attribute, new AttributeModifier(modifier.id(), modifier.amount() + attachmentModifier.amount(), AttributeModifier.Operation.ADD_VALUE), entry.slot()));
                        addNewModifier = false;
                        break;
                    } else if (modifier.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_BASE && attachmentModifier.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_BASE) {
                        modifiers.modifiers().remove(entry);
                        modifiers.modifiers().add(new ItemAttributeModifiers.Entry(attribute, new AttributeModifier(modifier.id(), modifier.amount() + attachmentModifier.amount(), AttributeModifier.Operation.ADD_MULTIPLIED_BASE), entry.slot()));
                        addNewModifier = false;
                        break;
                    }
                }

                if (addNewModifier)
                    modifiers.modifiers().add(new ItemAttributeModifiers.Entry(
                            attribute,
                            new AttributeModifier(ResourceLocation.fromNamespaceAndPath(AttachmentLib.MOD_ID, UUID.nameUUIDFromBytes((attachmentEntry.slot() + ":" + attachmentSlotIndex + ":" + attribute + ":" + attachmentModifier).getBytes(StandardCharsets.UTF_8)).toString()), attachmentModifier.amount(), attachmentModifier.operation()),
                            attachmentEntry.slot()));
            }
        });
        return modifiers;
    }

    default void inventoryTick(@NotNull ItemStack itemStack, @NotNull Level level, @NotNull Entity entity, int slotIndex, boolean selected) {
        getAttachments(itemStack).forEach((attachmentSlotIndex, attachment) ->
        {
            if (attachment.getItem() instanceof AttachmentItem attachmentItem) {
                if (getAttachmentSlotSettings()[attachmentSlotIndex].isStorageSlot()) {
                    attachmentItem.attachmentStorageTick(attachment, itemStack, attachmentSlotIndex, level, entity);
                } else {
                    attachmentItem.attachmentTick(attachment, itemStack, attachmentSlotIndex, level, entity);
                }
            }
        });
    }

    default <T extends LivingEntity> void damageAttachments(@NotNull ItemStack stack, int amount, @Nullable T entity, @NotNull Consumer<Item> onBroken) {
        getAttachments(stack).forEach((slotIndex, attachment) -> {
            if (!getAttachmentSlotSettings()[slotIndex].isStorageSlot())
                attachment.hurtAndBreak(amount, (ServerLevel) entity.level(), entity, onBroken);
        });
    }

    default void onDestroyed(ItemEntity itemEntity, DamageSource damageSource) {
        List<ItemStack> list = new ArrayList<>();
        getAttachments(itemEntity.getItem()).forEach((slotIndex, attachment) ->
                list.add(attachment));

        ItemUtils.onContainerDestroyed(itemEntity, list);
    }

    default @NotNull Optional<TooltipComponent> getTooltipImage(@NotNull ItemStack itemStack) {
        return Optional.of(new AttachmentHolderTooltip(getAttachmentSlotSettings(), getAttachments(itemStack), getSelectedSlotIndex(itemStack)));
    }



    static AttachmentHolderArmorModel<?> getArmorModel(Model baseModel, LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
        Map<ItemStack, Model> attachments = new HashMap<>();
        if (itemStack.getItem() instanceof AttachmentHolderItem attachmentHolder) {
            attachmentHolder.getAttachments(itemStack).forEach((slotIndex, attachmentStack) -> {
                if (attachmentStack.getItem() instanceof ArmorItem && !attachmentHolder.getAttachmentSlotSettings()[slotIndex].isStorageSlot() &&
                        (!(attachmentStack.getItem() instanceof AttachmentItem attachmentItem) || attachmentItem.mayRenderArmorModel(livingEntity, attachmentStack, equipmentSlot)))
                    attachments.put(attachmentStack, IClientItemExtensions.of(attachmentStack).getGenericArmorModel(livingEntity, attachmentStack, equipmentSlot, original));
            });
        }

        return new AttachmentHolderArmorModel<>(baseModel, livingEntity, equipmentSlot, attachments);
    }
}
