package com.marbledhubb.attachments.util;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class EquipmentUtil {

    public static @Nullable EquipmentSlot findEquipmentSlot(LivingEntity entity, ItemStack itemStack) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (entity.getItemBySlot(slot) == itemStack) {
                return slot;
            }
        }

        return null;
    }
}