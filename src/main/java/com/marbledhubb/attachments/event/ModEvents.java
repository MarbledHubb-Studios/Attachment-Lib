package com.marbledhubb.attachments.event;

import com.google.common.collect.Multimap;
import com.marbledhubb.attachments.AttachmentLib;
import com.marbledhubb.attachments.mixin.ItemAccessor;
import com.marbledhubb.attachments.mixin.ItemStackAccessor;
import com.marbledhubb.attachments.world.item.AttachmentHolderItem;
import com.marbledhubb.attachments.world.item.AttachmentItem;
import com.marbledhubb.attachments.world.item.AttachmentSlotSettings;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = AttachmentLib.MOD_ID)
public class ModEvents {
    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getFrom().getItem() instanceof AttachmentHolderItem) {
            AttachmentHolderItem.getAttachments(event.getFrom()).forEach((slotIndex, attachment) -> {
                if (attachment.getItem() instanceof AttachmentItem attachmentItem)
                    attachmentItem.onUnequip(event.getSlot(), attachment, event.getFrom(), slotIndex, event.getEntity());
            });
        }

        if (event.getTo().getItem() instanceof AttachmentHolderItem) {
            AttachmentHolderItem.getAttachments(event.getTo()).forEach((slotIndex, attachment) -> {
                if (attachment.getItem() instanceof AttachmentItem attachmentItem)
                    attachmentItem.onEquip(event.getSlot(), attachment, event.getTo(), slotIndex, event.getEntity());
            });
        }
    }

    @SubscribeEvent
    public static void modifyItemAttributeModifiers(ItemAttributeModifierEvent event) {
        Item item = event.getItemStack().getItem();
        if (item instanceof AttachmentHolderItem attachmentHolderItem && item instanceof Equipable equipable && equipable.getEquipmentSlot() == event.getSlotType()) {
            event.clearModifiers();
            attachmentHolderItem.getAttributeModifiers(event.getOriginalModifiers(), event.getSlotType(), event.getItemStack()).forEach(event::addModifier);
        }
    }

    @SubscribeEvent
    public static void modifyItemTooltip(ItemTooltipEvent event) {
        ItemStack itemStack = event.getItemStack();
        if (itemStack.getItem() instanceof AttachmentItem item) {
            Multimap<Attribute, AttributeModifier> attributeModifiers = item.getAttributeModifiers(AttachmentSlotSettings.genericSlot(false, false), event.getItemStack(), ItemStack.EMPTY, 0);
            if (attributeModifiers.isEmpty()) return;

            List<Component> tooltip = new ArrayList<>();

            tooltip.add(Component.empty());
            tooltip.add(Component.translatable("item.modifiers.attachment").withStyle(ChatFormatting.GOLD));

            for(Map.Entry<Attribute, AttributeModifier> entry : attributeModifiers.entries()) {
                AttributeModifier modifier = entry.getValue();
                double amount = modifier.getAmount();
                boolean flag = false;
                Player player = event.getEntity();
                if (player != null) {
                    if (modifier.getId() == ItemAccessor.getBaseAttackDamageUuid()) {
                        amount += player.getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
                        amount += EnchantmentHelper.getDamageBonus(itemStack, MobType.UNDEFINED);
                        flag = true;
                    } else if (modifier.getId() == ItemAccessor.getBaseAttackSpeedUuid()) {
                        amount += player.getAttributeBaseValue(Attributes.ATTACK_SPEED);
                        flag = true;
                    }
                }

                double d1;
                if (modifier.getOperation() != AttributeModifier.Operation.MULTIPLY_BASE && modifier.getOperation() != AttributeModifier.Operation.MULTIPLY_TOTAL) {
                    if (entry.getKey().equals(Attributes.KNOCKBACK_RESISTANCE)) {
                        d1 = amount * 10.0D;
                    } else {
                        d1 = amount;
                    }
                } else {
                    d1 = amount * 100.0D;
                }

                if (flag) {
                    tooltip.add(CommonComponents.space().append(Component.translatable("attribute.modifier.equals." + modifier.getOperation().toValue(), ItemStackAccessor.getAttributeModifierFormat().format(d1), Component.translatable(entry.getKey().getDescriptionId()))).withStyle(ChatFormatting.DARK_GREEN));
                } else if (amount > 0.0D) {
                    tooltip.add(Component.translatable("attribute.modifier.plus." + modifier.getOperation().toValue(), ItemStackAccessor.getAttributeModifierFormat().format(d1), Component.translatable(entry.getKey().getDescriptionId())).withStyle(ChatFormatting.BLUE));
                } else if (amount < 0.0D) {
                    d1 *= -1.0D;
                    tooltip.add(Component.translatable("attribute.modifier.take." + modifier.getOperation().toValue(), ItemStackAccessor.getAttributeModifierFormat().format(d1), Component.translatable(entry.getKey().getDescriptionId())).withStyle(ChatFormatting.RED));
                }
            }

            event.getToolTip().addAll(item.getAttributesTooltip(tooltip, itemStack));
        }
    }
}
