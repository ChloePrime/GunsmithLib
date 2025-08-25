package mod.chloeprime.gunsmithlib.api.common;

import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * 装弹动作填充弹匣时触发
 */
public class GunReloadFeedEvent extends LivingEvent {
    public GunInfo getGunInfo() {
        return gunInfo;
    }

    @Deprecated(since = "tacz 1.1.4")
    public boolean willLoadBarrel() {
        return willLoadBarrel;
    }

    private final GunInfo gunInfo;
    private final boolean willLoadBarrel;

    protected GunReloadFeedEvent(LivingEntity entity, GunInfo gunInfo, boolean willLoadBarrel) {
        super(entity);
        this.gunInfo = gunInfo;
        this.willLoadBarrel = willLoadBarrel;
    }

    public static class Pre extends GunReloadFeedEvent implements ICancellableEvent {
        @ApiStatus.Internal
        public Pre(LivingEntity entity, GunInfo gunInfo, boolean willLoadBarrel) {
            super(entity, gunInfo, willLoadBarrel);
        }
    }

    public static class Post extends GunReloadFeedEvent {
        @ApiStatus.Internal
        public Post(LivingEntity entity, GunInfo gunInfo, boolean willLoadBarrel) {
            super(entity, gunInfo, willLoadBarrel);
        }
    }
}
