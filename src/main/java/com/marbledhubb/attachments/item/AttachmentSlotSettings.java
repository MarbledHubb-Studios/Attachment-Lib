package com.marbledhubb.attachments.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Function;

public class AttachmentSlotSettings {
    private final Function<ItemStack, Boolean> placementCondition;
    private final boolean isStorageSlot;
    private final ResourceLocation iconLocation;

    public static AttachmentSlotSettings genericSlot() {
        return genericSlot(false, true);
    }

    public static AttachmentSlotSettings genericSlot(boolean allowNestedAttachments, boolean isStorageSlot) {
        return new AttachmentSlotSettings(allowNestedAttachments ?
                itemStack -> true :
                AttachmentSlotSettings::doesItemHaveNoAttachments,
                isStorageSlot,
                null);
    }

    public static AttachmentSlotSettings fromTag(TagKey<Item> itemTag) {
        return fromTag(itemTag, false, false);
    }

    public static AttachmentSlotSettings fromTag(TagKey<Item> itemTag, boolean allowNestedAttachments, boolean isStorageSlot) {
        return new AttachmentSlotSettings(allowNestedAttachments ?
                itemStack -> itemStack.is(itemTag) :
                itemStack -> itemStack.is(itemTag) && doesItemHaveNoAttachments(itemStack),
                isStorageSlot,
                ResourceLocation.fromNamespaceAndPath(itemTag.location().getNamespace(), "textures/gui/container/attachment_slot/%s.png".formatted(itemTag.location().getPath())));
    }

    public AttachmentSlotSettings(Function<ItemStack, Boolean> placementCondition, boolean renderArmorModel, ResourceLocation iconLocation) {
        this.placementCondition = placementCondition;
        this.isStorageSlot = renderArmorModel;
        this.iconLocation = iconLocation;
    }

    public static boolean doesItemHaveNoAttachments(ItemStack itemStack) {
        return !(itemStack.getItem() instanceof AttachmentHolderItem attachmentHolder) || attachmentHolder.getAttachments(itemStack).isEmpty();
    }

    public boolean mayPlace(ItemStack itemStack) {
        return placementCondition.apply(itemStack);
    }

    public boolean isStorageSlot() {
        return isStorageSlot;
    }

    public ResourceLocation getIconLocation() {
        return iconLocation;
    }
}
