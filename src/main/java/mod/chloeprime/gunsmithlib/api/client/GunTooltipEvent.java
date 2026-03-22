package mod.chloeprime.gunsmithlib.api.client;

import com.tacz.guns.client.tooltip.ClientGunTooltip;
import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import net.minecraftforge.eventbus.api.Event;

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

    public static class ComputeHeight extends GunTooltipEvent {
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
