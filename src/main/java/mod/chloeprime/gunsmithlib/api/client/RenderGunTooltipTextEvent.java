package mod.chloeprime.gunsmithlib.api.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.eventbus.api.Cancelable;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

public abstract class RenderGunTooltipTextEvent extends GunTooltipEvent {
    public record RenderContext(
            Font font,
            int x, MutableInt y0,
            Matrix4f matrix,
            MultiBufferSource.BufferSource buffer
    ) {
        public int y() {
            return y0.getValue();
        }

        public RenderContext(
                Font font,
                int x, int y,
                Matrix4f matrix,
                MultiBufferSource.BufferSource buffer
        ) {
            this(font, x, new MutableInt(y), matrix, buffer);
        }
    }

    public interface RenderFunc extends ToIntFunction<RenderContext> {
    }

    private final RenderContext renderContext;
    private @Nullable List<RenderFunc> renderers;
    private int height;

    public RenderGunTooltipTextEvent(GunTooltipContext context, RenderContext renderContext) {
        super(context);
        this.renderContext = renderContext;
    }

    public int getHeight() {
        return height;
    }

    public void pumpHeight(int height) {
        enqueue(ctx -> height);
    }

    public void enqueue(RenderFunc renderer) {
        if (renderers == null) {
            renderers = new ArrayList<>();
        }
        renderers.add(renderer);
    }

    @ApiStatus.Internal
    public void doRender() {
        if (renderers == null) {
            return;
        }
        for (var renderer : renderers) {
            var lineHeight = renderer.applyAsInt(this.renderContext);
            height += lineHeight;
            renderContext.y0().add(lineHeight);
        }
    }

    @Cancelable
    public static class BeforeDescription extends RenderGunTooltipTextEvent {
        public BeforeDescription(GunTooltipContext context, RenderContext renderContext) {
            super(context, renderContext);
        }
    }

    @Cancelable
    public static class AfterDescription extends RenderGunTooltipTextEvent {
        public AfterDescription(GunTooltipContext context, RenderContext renderContext) {
            super(context, renderContext);
        }
    }

    @Cancelable
    public static class AfterAmmoInfo extends RenderGunTooltipTextEvent {
        public AfterAmmoInfo(GunTooltipContext context, RenderContext renderContext) {
            super(context, renderContext);
        }
    }

    @Cancelable
    public static class AfterBaseInfo extends RenderGunTooltipTextEvent {
        public AfterBaseInfo(GunTooltipContext context, RenderContext renderContext) {
            super(context, renderContext);
        }
    }

    @Cancelable
    public static class AfterExtraDamageInfo extends RenderGunTooltipTextEvent {
        public AfterExtraDamageInfo(GunTooltipContext context, RenderContext renderContext) {
            super(context, renderContext);
        }
    }

    @Cancelable
    public static class AfterUpgradeTip extends RenderGunTooltipTextEvent {
        public AfterUpgradeTip(GunTooltipContext context, RenderContext renderContext) {
            super(context, renderContext);
        }
    }

    public static class AfterPackInfo extends RenderGunTooltipTextEvent {
        public AfterPackInfo(GunTooltipContext context, RenderContext renderContext) {
            super(context, renderContext);
        }
    }
}
