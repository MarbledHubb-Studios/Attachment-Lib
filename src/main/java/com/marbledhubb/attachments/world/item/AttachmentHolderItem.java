package com.marbledhubb.attachments.world.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.marbledhubb.attachments.client.AttachmentRenderer;
import com.marbledhubb.attachments.client.AttachmentRendererRegistry;
import com.marbledhubb.attachments.client.model.AttachmentHolderArmorModel;
import com.marbledhubb.attachments.world.inventory.tooltip.AttachmentHolderTooltip;
import com.marbledhubb.attachments.util.EquipmentUtil;
import net.minecraft.client.model.Model;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
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
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

public interface AttachmentHolderItem {
    AttachmentSlotSettings[] getAttachmentSlotSettings();

    //TODO ammunition like arrows for bows (and actual ammo e.g tacz guns) consumed from generic storage slots. refer to https://discord.com/channels/1459427496784298150/1491653851093401858/1519550589246111805 from ar task post - mn

    // TODO allow quick attachment swapping -aimi
    default boolean tryAttachItem(ItemStack holderStack, ItemStack possibleAttachmentStack, LivingEntity entity, boolean fromUse) {
        boolean isAttachmentItem = possibleAttachmentStack.getItem() instanceof AttachmentItem;

        Map<Integer, ItemStack> attachments = getAttachments(holderStack);

        int slotIndex = 0;
        for (AttachmentSlotSettings slotSettings : getAttachmentSlotSettings()) {
            if (!attachments.containsKey(slotIndex)) {
                if (slotSettings.mayPlace(possibleAttachmentStack) &&
                        (!isAttachmentItem ||
                                (fromUse ?
                                        ((AttachmentItem) possibleAttachmentStack.getItem()).canAttachFromUse(slotSettings, possibleAttachmentStack, holderStack, slotIndex, entity) :
                                        ((AttachmentItem) possibleAttachmentStack.getItem()).canAttach(slotSettings, possibleAttachmentStack, holderStack, slotIndex, entity)
                                )
                        )
                ) {
                    attachItem(holderStack, possibleAttachmentStack, slotSettings, slotIndex, entity, fromUse);
                    possibleAttachmentStack.setCount(0);
                    return true;
                }
            }
            slotIndex++;
        }
        return false;
    }

    private static void attachItem(ItemStack holderStack, ItemStack attachmentStack, AttachmentSlotSettings slotSettings, int slotIndex, LivingEntity entity, boolean fromUse) {
        if (attachmentStack.getItem() instanceof AttachmentItem attachmentItem) {
            if (fromUse) {
                attachmentItem.onAttachFromUse(slotSettings, attachmentStack, holderStack, slotIndex, entity);
            } else {
                attachmentItem.onAttach(slotSettings, attachmentStack, holderStack, slotIndex, entity);
            }
            EquipmentSlot equipmentSlot = EquipmentUtil.findEquipmentSlot(entity, holderStack);
            if (equipmentSlot != null) attachmentItem.onEquip(equipmentSlot, attachmentStack, holderStack, slotIndex, entity);
        }
        playAttachSound(holderStack, attachmentStack, entity);

        CompoundTag compoundTag = holderStack.getOrCreateTag();
        if (!compoundTag.contains("Attachments")) {
            compoundTag.put("Attachments", new ListTag());
        }

        ListTag listTag = compoundTag.getList("Attachments", Tag.TAG_COMPOUND);

        CompoundTag pairTag = new CompoundTag();
        pairTag.putInt("Slot", slotIndex);
        pairTag.put("Attachment", attachmentStack.save(new CompoundTag()));

        listTag.add(pairTag);
    }

    default Optional<ItemStack> tryDetachItem(boolean detachFirst, ItemStack holderStack, LivingEntity entity) {
        TreeMap<Integer, ItemStack> attachments = getAttachments(holderStack);
        if (attachments.isEmpty()) return Optional.empty();

        int slotIndex = detachFirst ? attachments.firstKey() : getSelectedSlotIndex(holderStack);
        AttachmentSlotSettings slotSettings = getAttachmentSlotSettings()[slotIndex];

        ItemStack attachmentStack = attachments.get(slotIndex);
        if (attachmentStack == null || (attachmentStack.getItem() instanceof AttachmentItem item && !item.canDetach(slotSettings, attachmentStack, holderStack, slotIndex, entity))) return Optional.empty();

        return detachItem(holderStack, slotSettings, slotIndex, attachmentStack, entity);
    }

    private static Optional<ItemStack> detachItem(ItemStack holderStack, AttachmentSlotSettings slotSettings, int slotIndex, ItemStack attachmentStack, LivingEntity entity) {
        CompoundTag compoundTag = holderStack.getOrCreateTag();
        ListTag listTag = compoundTag.getList("Attachments", Tag.TAG_COMPOUND);

        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag pairTag = listTag.getCompound(i);

            if (pairTag.getInt("Slot") != slotIndex) continue;

            if (attachmentStack.getItem() instanceof AttachmentItem attachmentItem) {
                attachmentItem.onDetach(slotSettings, attachmentStack, holderStack, slotIndex, entity);

                EquipmentSlot equipmentSlot = EquipmentUtil.findEquipmentSlot(entity, holderStack);
                if (equipmentSlot != null) attachmentItem.onUnequip(equipmentSlot, attachmentStack, holderStack, slotIndex, entity);
            }

            listTag.remove(i);
            if (listTag.isEmpty()) compoundTag.remove("Attachments");

            compoundTag.remove("SelectedSlot");

            return Optional.of(attachmentStack);
        }

        return Optional.empty();
    }

    static void updateAttachment(ItemStack holderStack, ItemStack attachmentStack, int slotIndex) {
        CompoundTag compoundTag = holderStack.getOrCreateTag();
        if (!compoundTag.contains("Attachments")) return;

        ListTag listTag = compoundTag.getList("Attachments", Tag.TAG_COMPOUND);
        if (listTag.isEmpty()) return;

        for (Tag tag : listTag) {
            CompoundTag pairTag = (CompoundTag) tag;
            if (pairTag.getInt("Slot") == slotIndex) {
                listTag.remove(pairTag);

                CompoundTag newPairTag = new CompoundTag();
                newPairTag.putInt("Slot", slotIndex);
                newPairTag.put("Attachment", attachmentStack.save(new CompoundTag()));
                listTag.add(newPairTag);
                break;
            }
        }
    }

    static TreeMap<Integer, ItemStack> getAttachments(ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.getTag();
        if (compoundTag == null) return new TreeMap<>();

        ListTag listTag = compoundTag.getList("Attachments", Tag.TAG_COMPOUND);
        if (listTag.isEmpty()) return new TreeMap<>();

        TreeMap<Integer, ItemStack> attachments = new TreeMap<>();

        for (Tag tag : listTag) {
            CompoundTag pairTag = (CompoundTag) tag;

            int slotIndex = pairTag.getInt("Slot");
            ItemStack attachment = ItemStack.of(pairTag.getCompound("Attachment"));

            attachments.put(slotIndex, attachment);
        }

        return attachments;
    }

    default boolean shiftSelectedSlot(ItemStack itemStack, int shiftAmount) {
        Map<Integer, ItemStack> attachments = getAttachments(itemStack);
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
                    itemStack.getOrCreateTag().putInt("SelectedSlot", slotIndex);
                    break;
                }
            }
        }
        return true;
    }

    static int getSelectedSlotIndex(ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.getOrCreateTag();

        if (compoundTag.contains("SelectedSlot")) {
            return compoundTag.getInt("SelectedSlot");
        }

        TreeMap<Integer, ItemStack> attachments = getAttachments(itemStack);
        return attachments.isEmpty() ? -1 : attachments.firstKey();
    }

    private static void playAttachSound(ItemStack holderStack, ItemStack attachmentStack, Entity entity) {
        if (attachmentStack.getItem() instanceof AttachmentItem item) {
            entity.playSound(item.getAttachSound(), 1, 1);
        } else if (holderStack.getItem() instanceof Equipable item) {
            entity.playSound(item.getEquipSound(), 1, 1);
        }
    }

    default boolean overrideStackedOnOther(@NotNull ItemStack itemStack, @NotNull Slot slot, @NotNull ClickAction clickAction, @NotNull Player player) {
        if (clickAction == ClickAction.SECONDARY) {
            if (slot.getItem().isEmpty()) {
                Optional<ItemStack> removedAttachment = tryDetachItem(true, itemStack, player);
                if (removedAttachment.isPresent()) {
                    slot.safeInsert(removedAttachment.get());
                    return true;
                }
                return false;
            }
            return tryAttachItem(itemStack, slot.getItem(), player, false);
        }

        return false;
    }

    default boolean overrideOtherStackedOnMe(@NotNull ItemStack itemStack, @NotNull ItemStack possibleAttachment, @NotNull Slot slot, @NotNull ClickAction clickAction, @NotNull Player player, @NotNull SlotAccess slotAccess) {
        if (clickAction == ClickAction.SECONDARY && slot.allowModification(player)) {
            if (possibleAttachment.isEmpty()) {
                Optional<ItemStack> removedAttachment = tryDetachItem(false, itemStack, player);
                if (removedAttachment.isPresent()) {
                    slotAccess.set(removedAttachment.get());
                    return true;
                }
                return false;
            }
            return tryAttachItem(itemStack, possibleAttachment, player, false);
        }

        return false;
    }

    default @NotNull Multimap<Attribute, AttributeModifier> getAttributeModifiers(Multimap<Attribute, AttributeModifier> holderAttributeModifiers, @NotNull EquipmentSlot equipmentSlot, ItemStack itemStack) {
        Multimap<Attribute, AttributeModifier> map = HashMultimap.create(holderAttributeModifiers);
        getAttachments(itemStack).forEach((slotIndex, attachmentStack) -> {
            AttachmentSlotSettings slotSettings = getAttachmentSlotSettings()[slotIndex];
            if (!slotSettings.isStorageSlot() && attachmentStack.getItem() instanceof AttachmentItem item) {
                item.getAttributeModifiers(slotSettings, attachmentStack, itemStack, slotIndex).forEach((attribute, attachmentModifier) -> {
                    boolean addNewModifier = true;

                    for (AttributeModifier holderModifier : map.get(attribute)) {
                        if (attachmentModifier.getOperation() == AttributeModifier.Operation.ADDITION && holderModifier.getOperation() == AttributeModifier.Operation.ADDITION) {
                            map.remove(attribute, holderModifier);
                            map.put(attribute, new AttributeModifier(holderModifier.getId(), holderModifier.getName(), attachmentModifier.getAmount() + holderModifier.getAmount(), AttributeModifier.Operation.ADDITION));
                            addNewModifier = false;
                            break;
                        } else if (attachmentModifier.getOperation() == AttributeModifier.Operation.MULTIPLY_BASE && holderModifier.getOperation() == AttributeModifier.Operation.MULTIPLY_BASE) {
                            map.remove(attribute, holderModifier);
                            map.put(attribute, new AttributeModifier(holderModifier.getId(), holderModifier.getName(), attachmentModifier.getAmount() + holderModifier.getAmount(), AttributeModifier.Operation.MULTIPLY_BASE));
                            addNewModifier = false;
                            break;
                        }
                    }

                    if (addNewModifier)
                        map.put(attribute, new AttributeModifier(UUID.nameUUIDFromBytes((equipmentSlot + ":" + slotIndex + ":" + attribute + ":" + attachmentModifier).getBytes(StandardCharsets.UTF_8)), attachmentModifier.getName(), attachmentModifier.getAmount(), attachmentModifier.getOperation()));
                });
            }
        });
        return map;
    }

    default void inventoryTick(@NotNull ItemStack itemStack, @NotNull Level level, @NotNull Entity entity, int slotIndex, boolean selected) {
        getAttachments(itemStack).forEach((attachmentSlotIndex, attachment) -> {
            if (attachment.getItem() instanceof AttachmentItem attachmentItem) {
                if (getAttachmentSlotSettings()[attachmentSlotIndex].isStorageSlot()) {
                    attachmentItem.attachmentStorageTick(attachment, itemStack, attachmentSlotIndex, level, entity);
                } else {
                    attachmentItem.attachmentTick(attachment, itemStack, attachmentSlotIndex, level, entity);
                }
            }
        });
    }

    default <T extends LivingEntity> void damageAttachments(ItemStack itemStack, int amount, T entity, Consumer<T> onBroken) {
        getAttachments(itemStack).forEach((slotIndex, attachmentStack) -> {
            AttachmentSlotSettings slotSettings = getAttachmentSlotSettings()[slotIndex];
            if (!slotSettings.isStorageSlot()) {
                attachmentStack.hurtAndBreak(amount, entity, onBroken);
                if (attachmentStack.getItem() instanceof AttachmentItem item) item.onAttachmentBreak(slotSettings, attachmentStack, itemStack, slotIndex, entity);
                updateAttachment(itemStack, attachmentStack, slotIndex);
            }
        });
    }

    static void onDestroyed(ItemEntity itemEntity) {
        List<ItemStack> list = new ArrayList<>();
        getAttachments(itemEntity.getItem()).forEach((slotIndex, attachment) -> list.add(attachment));

        ItemUtils.onContainerDestroyed(itemEntity, list.stream());
    }

    default @NotNull Optional<TooltipComponent> getTooltipImage(@NotNull ItemStack itemStack) {
        return Optional.of(new AttachmentHolderTooltip(getAttachmentSlotSettings(), getAttachments(itemStack), getSelectedSlotIndex(itemStack)));
    }



    static AttachmentHolderArmorModel<?> getArmorModel(Model baseModel, LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot) {
        Map<ItemStack, AttachmentRenderer> attachments = new HashMap<>();
        if (itemStack.getItem() instanceof AttachmentHolderItem holderItem) {
            AttachmentHolderItem.getAttachments(itemStack).forEach((slotIndex, attachmentStack) -> {
                AttachmentSlotSettings slotSettings = holderItem.getAttachmentSlotSettings()[slotIndex];
                if (!slotSettings.isStorageSlot() &&
                        (!(attachmentStack.getItem() instanceof AttachmentItem attachmentItem) || attachmentItem.canRenderArmorModel(slotSettings, attachmentStack, itemStack, slotIndex, livingEntity)))
                    AttachmentRendererRegistry.getRenderer(attachmentStack.getItem()).ifPresent(value -> attachments.put(attachmentStack, value));
            });
        }

        return new AttachmentHolderArmorModel<>(baseModel, livingEntity, equipmentSlot, attachments);
    }
}
