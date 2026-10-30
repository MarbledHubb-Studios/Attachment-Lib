package com.marbledhubb.attachments.mixin;

import com.marbledhubb.attachments.world.item.AttachmentHolderItem;
import com.marbledhubb.attachments.world.item.AttachmentItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.function.Consumer;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(method = "overrideStackedOnOther", at = @At("HEAD"), cancellable = true)
    private void injectToOverrideStackedOnOther(Slot slot, ClickAction action, Player player, CallbackInfoReturnable<Boolean> cir) {
        if (((ItemStack) (Object) this).getItem() instanceof AttachmentHolderItem item && item.overrideStackedOnOther((ItemStack) (Object) this, slot, action, player))
            cir.setReturnValue(true);
    }

    @Inject(method = "overrideOtherStackedOnMe", at = @At("HEAD"), cancellable = true)
    private void injectToOverrideOtherStackedOnMe(ItemStack stack, Slot slot, ClickAction action, Player player, SlotAccess access, CallbackInfoReturnable<Boolean> cir) {
        if (((ItemStack) (Object) this).getItem() instanceof AttachmentHolderItem item && item.overrideOtherStackedOnMe((ItemStack) (Object) this, stack, slot, action, player, access))
            cir.setReturnValue(true);
    }

    @Inject(method = "inventoryTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;inventoryTick(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;IZ)V", shift = At.Shift.AFTER))
    private void injectToInventoryTick(Level level, Entity entity, int inventorySlot, boolean isCurrentItem, CallbackInfo ci) {
        if (((ItemStack) (Object) this).getItem() instanceof AttachmentHolderItem item)
            item.inventoryTick((ItemStack) (Object) this, level, entity, inventorySlot, isCurrentItem);
    }

    @Inject(method = "hurtAndBreak", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;damageItem(Lnet/minecraft/world/item/ItemStack;ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)I"))
    private <T extends LivingEntity> void injectToHurtAndBreak(int amount, T entity, Consumer<T> onBroken, CallbackInfo ci) {
        if (((ItemStack) (Object) this).getItem() instanceof AttachmentHolderItem item)
            item.damageAttachments((ItemStack) (Object) this, amount, entity, onBroken);
    }

    @Inject(method = "getTooltipImage", at = @At("HEAD"), cancellable = true)
    private void injectToGetTooltipImage(CallbackInfoReturnable<Optional<TooltipComponent>> cir) {
        if (((ItemStack) (Object) this).getItem() instanceof AttachmentHolderItem item) // TODO make it so it can keep the original image as well -aimi
            cir.setReturnValue(item.getTooltipImage((ItemStack) (Object) this));
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void use(Level level, Player player, InteractionHand usedHand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        if (((ItemStack) (Object) this).getItem() instanceof AttachmentItem) {
            for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                if ((equipmentSlot == EquipmentSlot.MAINHAND && usedHand == InteractionHand.MAIN_HAND) || (equipmentSlot == EquipmentSlot.OFFHAND && usedHand == InteractionHand.OFF_HAND))
                    continue;

                ItemStack holderStack = player.getItemBySlot(equipmentSlot);
                if (holderStack.getItem() instanceof AttachmentHolderItem holderItem) {
                    if (holderItem.tryAttachItem(holderStack, (ItemStack) (Object) this, player, true)) {
                        cir.setReturnValue(InteractionResultHolder.sidedSuccess(ItemStack.EMPTY, level.isClientSide())); // TODO change from empty to the swapped stack once that's a thing -aimi
                        return;
                    }
                }
            }
        }
    }
}
