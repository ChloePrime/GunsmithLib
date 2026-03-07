package mod.chloeprime.gunsmithlib.api.common;

import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import com.tacz.guns.entity.EntityKineticBullet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * 子弹命中任何实体时触发。
 * 比 {@link EntityHurtByGunEvent.Pre} 先触发。
 * 取消这个事件只能阻止后续对直接命中目标的伤害，而不能阻止子弹消失。
 *
 * @since 5.2.0
 */
public class AmmoHitEntityEvent extends Event implements ICancellableEvent {
    private final Level level;
    private final EntityHitResult hitResult;
    private final Entity hitTarget;
    private final EntityKineticBullet ammo;
    private final boolean isHeadshot;

    public AmmoHitEntityEvent(
            Level level,
            EntityHitResult hitResult,
            Entity hitTarget,
            EntityKineticBullet ammo,
            boolean isHeadshot
    ) {
        this.level = level;
        this.hitResult = hitResult;
        this.hitTarget = hitTarget;
        this.ammo = ammo;
        this.isHeadshot = isHeadshot;
    }

    public Level getLevel() {
        return level;
    }

    public EntityHitResult getHitResult() {
        return hitResult;
    }

    public Entity getHitTarget() {
        return hitTarget;
    }

    public EntityKineticBullet getAmmo() {
        return ammo;
    }

    public boolean isHeadshot() {
        return isHeadshot;
    }
}
