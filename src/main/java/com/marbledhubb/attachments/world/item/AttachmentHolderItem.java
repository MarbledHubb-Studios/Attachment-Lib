package com.marbledhubb.attachments.world.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.marbledhubb.attachments.client.model.AttachmentHolderArmorModel;
import com.marbledhubb.attachments.world.inventory.tooltip.AttachmentHolderTooltip;
import com.marbledhubb.attachments.util.EquipmentUtil;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
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
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

public interface AttachmentHolderItem {
    AttachmentSlotSettings[] getAttachmentSlotSettings();

    //TODO ammunition like arrows for bows (and actual ammo e.g tacz guns) consumed from generic storage slots. refer to https://discord.com/channels/1459427496784298150/1491653851093401858/1519550589246111805 from ar task post - mn

    default boolean tryToEquipAttachment(ItemStack itemStack, ItemStack possibleAttachment, LivingEntity entity) {
        Map<Integer, ItemStack> attachments = getAttachments(itemStack);

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

        CompoundTag compoundTag = itemStack.getOrCreateTag();
        if (!compoundTag.contains("Attachments")) {
            compoundTag.put("Attachments", new ListTag());
        }

        ListTag listTag = compoundTag.getList("Attachments", Tag.TAG_COMPOUND);

        CompoundTag pairTag = new CompoundTag();
        pairTag.putInt("Slot", slotIndex);
        pairTag.put("Attachment", attachment.save(new CompoundTag()));

        listTag.add(pairTag);
    }

    default Optional<ItemStack> tryToRemoveAttachment(boolean removeFirst, ItemStack itemStack, LivingEntity entity) {
        CompoundTag compoundTag = itemStack.getOrCreateTag();
        if (!compoundTag.contains("Attachments")) {
            return Optional.empty();
        } else {
            ListTag listTag = compoundTag.getList("Attachments", 10);
            if (listTag.isEmpty()) {
                return Optional.empty();
            } else {
                int targetSlotIndex = removeFirst ? getAttachments(itemStack).firstKey() : getSelectedSlotIndex(itemStack);

                int i = 0;
                for (Tag tag : listTag) {
                    CompoundTag pairTag = (CompoundTag) tag;

                    int slotIndex = pairTag.getInt("Slot");
                    if (slotIndex == targetSlotIndex) {
                        ItemStack attachment = ItemStack.of(pairTag.getCompound("Attachment"));

                        if (attachment.getItem() instanceof AttachmentItem attachmentItem) {
                            attachmentItem.onUnequipFromAttachmentSlot(attachment, itemStack, entity);
                            EquipmentSlot equipmentSlot = EquipmentUtil.findEquipmentSlot(entity, itemStack);
                            if (equipmentSlot != null) attachmentItem.onAttachmentUnequipFromEquipmentSlot(equipmentSlot, attachment, itemStack, slotIndex, entity);
                        }
                        playUnequipAttachmentSound(itemStack, attachment, entity);

                        listTag.remove(i);
                        if (listTag.isEmpty()) {
                            itemStack.removeTagKey("Attachments");
                        }

                        compoundTag.remove("SelectedSlot");

                        return Optional.of(attachment);
                    }
                    i++;
                }
                return Optional.empty();
            }
        }
    }

    default void updateAttachment(ItemStack itemStack, ItemStack attachment, int slotIndex) {
        CompoundTag compoundTag = itemStack.getOrCreateTag();
        if (!compoundTag.contains("Attachments")) return;

        ListTag listTag = compoundTag.getList("Attachments", Tag.TAG_COMPOUND);
        if (listTag.isEmpty()) return;

        for (Tag tag : listTag) {
            CompoundTag pairTag = (CompoundTag) tag;
            if (pairTag.getInt("Slot") == slotIndex) {
                listTag.remove(pairTag);

                CompoundTag newPairTag = new CompoundTag();
                newPairTag.putInt("Slot", slotIndex);
                newPairTag.put("Attachment", attachment.save(new CompoundTag()));
                listTag.add(newPairTag);
                break;
            }
        }
    }

    default TreeMap<Integer, ItemStack> getAttachments(ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.getTag();
        if (compoundTag == null) {
            return new TreeMap<>();
        } else {
            TreeMap<Integer, ItemStack> attachments = new TreeMap<>();

            ListTag listTag = compoundTag.getList("Attachments", Tag.TAG_COMPOUND);

            for (Tag tag : listTag) {
                CompoundTag pairTag = (CompoundTag) tag;

                int slotIndex = pairTag.getInt("Slot");
                ItemStack attachment = ItemStack.of(pairTag.getCompound("Attachment"));

                attachments.put(slotIndex, attachment);
            }

            return attachments;
        }
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

    default int getSelectedSlotIndex(ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.getOrCreateTag();

        if (compoundTag.contains("SelectedSlot")) {
            return compoundTag.getInt("SelectedSlot");
        }

        TreeMap<Integer, ItemStack> attachments = getAttachments(itemStack);
        return attachments.isEmpty() ? -1 : attachments.firstKey();
    }

    default void playEquipAttachmentSound(ItemStack itemStack, ItemStack attachment, Entity entity) {
        if (attachment.getItem() instanceof Equipable equipable) {
            entity.playSound(equipable.getEquipSound(), 1.0F, 1.0F);
        } else if (itemStack.getItem() instanceof Equipable equipable) {
            entity.playSound(equipable.getEquipSound(), 1.0F, 1.0F);
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

    default @NotNull Multimap<Attribute, AttributeModifier> getAttributeModifiers(Multimap<Attribute, AttributeModifier> holderAttributeModifiers, @NotNull EquipmentSlot equipmentSlot, ItemStack itemStack) {
        Multimap<Attribute, AttributeModifier> map = HashMultimap.create(holderAttributeModifiers);
        getAttachments(itemStack).forEach((slotIndex, attachment) -> {
                if (!getAttachmentSlotSettings()[slotIndex].isStorageSlot()) {
                    attachment.getAttributeModifiers(equipmentSlot).forEach((attribute, modifier) -> {
                        boolean addNewModifier = true;

                        for (AttributeModifier modifier1 : map.get(attribute)) {
                            if (modifier.getOperation() == AttributeModifier.Operation.ADDITION && modifier1.getOperation() == AttributeModifier.Operation.ADDITION) {
                                map.remove(attribute, modifier1);
                                map.put(attribute, new AttributeModifier(modifier1.getId(), modifier1.getName(), modifier.getAmount() + modifier1.getAmount(), AttributeModifier.Operation.ADDITION));
                                addNewModifier = false;
                                break;
                            } else if (modifier.getOperation() == AttributeModifier.Operation.MULTIPLY_BASE && modifier1.getOperation() == AttributeModifier.Operation.MULTIPLY_BASE) {
                                map.remove(attribute, modifier1);
                                map.put(attribute, new AttributeModifier(modifier1.getId(), modifier1.getName(), modifier.getAmount() + modifier1.getAmount(), AttributeModifier.Operation.MULTIPLY_BASE));
                                addNewModifier = false;
                                break;
                            }
                        }

                        if (addNewModifier)
                            map.put(attribute, new AttributeModifier(UUID.nameUUIDFromBytes((equipmentSlot + ":" + slotIndex + ":" + attribute + ":" + modifier).getBytes(StandardCharsets.UTF_8)), modifier.getName(), modifier.getAmount(), modifier.getOperation()));
                    });
                }
        });
        return map;
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

    default <T extends LivingEntity> void damageAttachments(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
        getAttachments(stack).forEach((slotIndex, attachment) -> {
            if (!getAttachmentSlotSettings()[slotIndex].isStorageSlot())
                attachment.hurtAndBreak(amount, entity, onBroken);
        });
    }

    default void onDestroyed(ItemEntity itemEntity, DamageSource damageSource) {
        List<ItemStack> list = new ArrayList<>();
        getAttachments(itemEntity.getItem()).forEach((slotIndex, attachment) ->
                list.add(attachment));

        ItemUtils.onContainerDestroyed(itemEntity, list.stream());
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
