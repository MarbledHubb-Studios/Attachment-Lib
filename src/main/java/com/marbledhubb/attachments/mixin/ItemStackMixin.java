package com.marbledhubb.attachments.mixin;

import com.marbledhubb.attachments.world.inventory.tooltip.AttachmentHolderTooltip;
import com.marbledhubb.attachments.world.inventory.tooltip.CompositeTooltip;
import com.marbledhubb.attachments.world.item.AttachmentHolderItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
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
        if (((ItemStack) (Object) this).getItem() instanceof AttachmentHolderItem item && item.attachmentHolderStackedOnOther((ItemStack) (Object) this, slot, action, player))
            cir.setReturnValue(true);
    }

    @Inject(method = "overrideOtherStackedOnMe", at = @At("HEAD"), cancellable = true)
    private void injectToOverrideOtherStackedOnMe(ItemStack stack, Slot slot, ClickAction action, Player player, SlotAccess access, CallbackInfoReturnable<Boolean> cir) {
        if (((ItemStack) (Object) this).getItem() instanceof AttachmentHolderItem item && item.otherStackedOnAttachmentHolder((ItemStack) (Object) this, stack, slot, action, player, access))
            cir.setReturnValue(true);
    }

    @Inject(method = "inventoryTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;inventoryTick(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;IZ)V", shift = At.Shift.AFTER))
    private void injectToInventoryTick(Level level, Entity entity, int inventorySlot, boolean isCurrentItem, CallbackInfo ci) {
        if (((ItemStack) (Object) this).getItem() instanceof AttachmentHolderItem item)
            item.attachmentHolderInventoryTick((ItemStack) (Object) this, level, entity, inventorySlot, isCurrentItem);
    }

    @Inject(method = "hurtAndBreak", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;damageItem(Lnet/minecraft/world/item/ItemStack;ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)I"))
    private <T extends LivingEntity> void damageAttachments(int amount, T entity, Consumer<T> onBroken, CallbackInfo ci) {
        if (((ItemStack) (Object) this).getItem() instanceof AttachmentHolderItem item)
            item.damageAttachments((ItemStack) (Object) this, amount, entity, onBroken);
    }

    @Inject(method = "hurtAndBreak", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", shift = At.Shift.AFTER))
    private <T extends LivingEntity> void onItemBroken(int amount, T entity, Consumer<T> onBroken, CallbackInfo ci) {
        if (((ItemStack) (Object) this).getItem() instanceof AttachmentHolderItem) {
            if (entity instanceof Player player) {
                AttachmentHolderItem.getAttachments((ItemStack) (Object) this).forEach((slotIndex, attachmentStack) -> {
                    if (!player.getInventory().add(attachmentStack)) {
                        player.drop(attachmentStack, false);
                    }
                });
            } else {
                AttachmentHolderItem.getAttachments((ItemStack) (Object) this).forEach((slotIndex, attachmentStack) ->
                        entity.level().addFreshEntity(new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), attachmentStack))
                );
            }
        }
    }

    @Inject(method = "getTooltipImage", at = @At("RETURN"), cancellable = true)
    private void injectToGetTooltipImage(CallbackInfoReturnable<Optional<TooltipComponent>> cir) {
        if (((ItemStack) (Object) this).getItem() instanceof AttachmentHolderItem item) {
            AttachmentHolderTooltip attachmentHolderTooltip = new AttachmentHolderTooltip(
                    item.getAttachmentSlotSettings(),
                    AttachmentHolderItem.getAttachments((ItemStack) (Object) this),
                    AttachmentHolderItem.getSelectedSlotIndex((ItemStack) (Object) this)
            );
            Optional<TooltipComponent> originalTooltip = cir.getReturnValue();

            cir.setReturnValue(Optional.of(
                    originalTooltip.isEmpty()
                            ? attachmentHolderTooltip
                            : new CompositeTooltip(
                                    new TooltipComponent[]{originalTooltip.get(), attachmentHolderTooltip},
                                    1,
                                    CompositeTooltip.Layout.VERTICAL
                            )
            ));
        }
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void use(Level level, Player player, InteractionHand usedHand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
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
