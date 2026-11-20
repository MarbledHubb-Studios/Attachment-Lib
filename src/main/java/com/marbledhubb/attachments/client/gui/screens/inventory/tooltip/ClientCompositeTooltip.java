package com.marbledhubb.attachments.client.gui.screens.inventory.tooltip;

import com.marbledhubb.attachments.world.inventory.tooltip.CompositeTooltip;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.Arrays;

@OnlyIn(Dist.CLIENT)
public class ClientCompositeTooltip implements ClientTooltipComponent {
    private final ClientTooltipComponent[] children;
    private final int spacing;
    private final CompositeTooltip.Layout layout;

    public ClientCompositeTooltip(CompositeTooltip component) {
        this.children = Arrays.stream(component.children()).map(ClientTooltipComponent::create).toArray(ClientTooltipComponent[]::new);
        this.spacing = component.spacing();
        this.layout = component.layout();
    }

    @Override
    public int getHeight() {
        int height = 0;

        for (ClientTooltipComponent child : children) {
            int childHeight = child.getHeight();

            if (layout == CompositeTooltip.Layout.VERTICAL) {
                height += childHeight;
            } else {
                height = Math.max(height, childHeight);
            }
        }

        if (layout == CompositeTooltip.Layout.VERTICAL && children.length > 1) {
            height += spacing * (children.length - 1);
        }

        return height;
    }

    @Override
    public int getWidth(@NotNull Font font) {
        int width = 0;

        for (ClientTooltipComponent child : children) {
            int childWidth = child.getWidth(font);

            if (layout == CompositeTooltip.Layout.HORIZONTAL) {
                width += childWidth;
            } else {
                width = Math.max(width, childWidth);
            }
        }

        if (layout == CompositeTooltip.Layout.HORIZONTAL && children.length > 1) {
            width += spacing * (children.length - 1);
        }

        return width;
    }

    @Override
    public void renderImage(@NotNull Font font, int x, int y, @NotNull GuiGraphics graphics) {
        for (ClientTooltipComponent child : children) {
            child.renderImage(font, x, y, graphics);

            if (layout == CompositeTooltip.Layout.HORIZONTAL) {
                x += child.getWidth(font) + spacing;
            } else {
                y += child.getHeight() + spacing;
            }
        }
    }

    @Override
    public void renderText(@NotNull Font font, int x, int y, @NotNull Matrix4f matrix, MultiBufferSource.@NotNull BufferSource buffer) {
        for (ClientTooltipComponent child : children) {
            child.renderText(font, x, y, matrix, buffer);

            if (layout == CompositeTooltip.Layout.HORIZONTAL) {
                x += child.getWidth(font) + spacing;
            } else {
                y += child.getHeight() + spacing;
            }
        }
    }
}
