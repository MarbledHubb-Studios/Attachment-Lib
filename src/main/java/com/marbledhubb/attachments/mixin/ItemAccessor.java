package com.marbledhubb.attachments.mixin;

import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.UUID;

@Mixin(Item.class)
public interface ItemAccessor {
    @Accessor("BASE_ATTACK_DAMAGE_UUID")
    static UUID getBaseAttackDamageUuid() {
        throw new AssertionError();
    }

    @Accessor("BASE_ATTACK_SPEED_UUID")
    static UUID getBaseAttackSpeedUuid() {
        throw new AssertionError();
    }
}
