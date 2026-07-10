package com.marbledhubb.attachments.client.tooltip;

import com.marbledhubb.attachments.AttachmentLib;
import com.marbledhubb.attachments.item.AttachmentSlotSettings;
import com.marbledhubb.attachments.tooltip.AttachmentHolderTooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class ClientAttachmentHolderTooltip implements ClientTooltipComponent {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(AttachmentLib.MOD_ID, "textures/gui/container/attachment_holder.png");

    private final AttachmentSlotSettings[] slotSettings;
    private final Map<Integer, ItemStack> attachments;
    private final int selectedSlotIndex;

    public ClientAttachmentHolderTooltip(AttachmentHolderTooltip component) {
        this.slotSettings = component.slotSettings();
        this.attachments = component.attachments();
        this.selectedSlotIndex = component.selectedSlotIndex();
    }

    @Override
    public int getHeight() {
        return gridSizeY() * 20 + 2 + 4;
    }

    @Override
    public int getWidth(@NotNull Font font) {
        return gridSizeX() * 18 + 2;
    }

    @Override
    public void renderImage(@NotNull Font font, int x, int y, @NotNull GuiGraphics guiGraphics) {
        int sizeX = gridSizeX();
        int sizeY = gridSizeY();
        int slotIndex = 0;

        for(int gridY = 0; gridY < sizeY; ++gridY) {
            for(int gridX = 0; gridX < sizeX; ++gridX) {
                int slotX = x + gridX * 18 + 1;
                int slotY = y + gridY * 20 + 1;
                renderSlot(slotX, slotY, slotIndex++, guiGraphics, font);
            }
        }

        drawBorder(x, y, sizeX, sizeY, guiGraphics);
    }

    private void renderSlot(int x, int y, int slotIndex, GuiGraphics guiGraphics, Font font) {
        blit(guiGraphics, x, y, Texture.SLOT);

        ItemStack attachment = attachments.get(slotIndex);
        if (attachment == null) {
            ResourceLocation iconLocation = slotSettings[slotIndex].getIconLocation();
            if (iconLocation != null && Minecraft.getInstance().getResourceManager().getResource(iconLocation).isPresent())
                blitSlotOverlay(guiGraphics, x + 1, y + 1, iconLocation);
        } else {
            guiGraphics.renderItem(attachment, x + 1, y + 1, slotIndex);
            guiGraphics.renderItemDecorations(font, attachment, x + 1, y + 1);
        }

        if (slotIndex == selectedSlotIndex) {
            AbstractContainerScreen.renderSlotHighlight(guiGraphics, x + 1, y + 1, 0);
        }
    }

    private void drawBorder(int x, int y, int slotWidth, int slotHeight, GuiGraphics guiGraphics) {
        blit(guiGraphics, x, y, Texture.BORDER_CORNER_TOP);
        blit(guiGraphics, x + slotWidth * 18 + 1, y, Texture.BORDER_CORNER_TOP);

        for(int i = 0; i < slotWidth; ++i) {
            blit(guiGraphics, x + 1 + i * 18, y, Texture.BORDER_HORIZONTAL_TOP);
            blit(guiGraphics, x + 1 + i * 18, y + slotHeight * 20, Texture.BORDER_HORIZONTAL_BOTTOM);
        }

        for(int j = 0; j < slotHeight; ++j) {
            blit(guiGraphics, x, y + j * 20 + 1, Texture.BORDER_VERTICAL);
            blit(guiGraphics, x + slotWidth * 18 + 1, y + j * 20 + 1, Texture.BORDER_VERTICAL);
        }

        blit(guiGraphics, x, y + slotHeight * 20, Texture.BORDER_CORNER_BOTTOM);
        blit(guiGraphics, x + slotWidth * 18 + 1, y + slotHeight * 20, Texture.BORDER_CORNER_BOTTOM);
    }

    private void blit(GuiGraphics guiGraphics, int x, int y, Texture texture) {
        guiGraphics.blit(TEXTURE, x, y, 0, texture.x, texture.y, texture.w, texture.h, 128, 128);
    }

    private void blitSlotOverlay(GuiGraphics guiGraphics, int x, int y, ResourceLocation textureLocation) {
        guiGraphics.blit(textureLocation, x, y, 0, 0, 0, 16, 16, 16, 16);
    }

    private int gridSizeX() {
        return Math.min(5, slotSettings.length);
    }

    private int gridSizeY() {
        return (int) Math.ceil(slotSettings.length / (double) gridSizeX());
    }

    @OnlyIn(Dist.CLIENT)
    enum Texture {
        SLOT(0, 0, 18, 20),
        BORDER_VERTICAL(0, 18, 1, 20),
        BORDER_HORIZONTAL_TOP(0, 20, 18, 1),
        BORDER_HORIZONTAL_BOTTOM(0, 40, 18, 1),
        BORDER_CORNER_TOP(0, 20, 1, 1),
        BORDER_CORNER_BOTTOM(0, 40, 1, 1);

        public final int x;
        public final int y;
        public final int w;
        public final int h;

        Texture(int x, int y, int w, int h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }
}
