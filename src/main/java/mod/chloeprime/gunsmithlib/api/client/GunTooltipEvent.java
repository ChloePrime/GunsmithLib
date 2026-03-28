package mod.chloeprime.gunsmithlib.api.client;

import com.tacz.guns.client.tooltip.ClientGunTooltip;
import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import net.neoforged.bus.api.Event;

import java.util.Optional;

public abstract class GunTooltipEvent extends Event {
    private final GunTooltipContext context;

    protected GunTooltipEvent(GunTooltipContext context) {
        this.context = context;
    }

    public ClientGunTooltip getTooltipComponent() {
        return context.instance();
    }

    public Optional<GunInfo> getGunInfo() {
        return Optional.ofNullable(context.gunInfo());
    }

    public static class Initialize extends GunTooltipEvent {
        public Initialize(GunTooltipContext context) {
            super(context);
        }
    }


    public static sealed class ComputeSize extends GunTooltipEvent {
        public ComputeSize(GunTooltipContext context) {
            super(context);
        }
    }

    public static non-sealed class ComputeWidth extends ComputeSize {
        private final int originalWidth;
        private int width;

        public ComputeWidth(GunTooltipContext context, int width) {
            super(context);
            this.width = this.originalWidth = width;
        }

        public int getOriginalWidth() {
            return originalWidth;
        }

        public int getWidth() {
            return width;
        }

        public void pumpWidth(int widthIn) {
            this.width = Math.max(widthIn, this.width);
        }

        public void setWidth(int widthIn) {
            this.width = widthIn;
        }
    }

    public static non-sealed class ComputeHeight extends ComputeSize {
        private final int originalHeight;
        private int height;

        public ComputeHeight(GunTooltipContext context, int height) {
            super(context);
            this.height = this.originalHeight = height;
        }

        public int getOriginalHeight() {
            return originalHeight;
        }

        public int getHeight() {
            return height;
        }

        public void pumpHeight(int height) {
            this.height += height;
        }

        public void setHeight(int height) {
            this.height = height;
        }
    }
}
