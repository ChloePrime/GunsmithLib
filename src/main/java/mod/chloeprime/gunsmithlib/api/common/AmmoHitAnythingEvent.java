package mod.chloeprime.gunsmithlib.api.common;

import com.tacz.guns.entity.EntityKineticBullet;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

public class AmmoHitAnythingEvent extends Event {
    private final Level level;
    private final HitResult hitResult;
    private final EntityKineticBullet ammo;

    public AmmoHitAnythingEvent(Level level, HitResult hitResult, EntityKineticBullet ammo) {
        this.level = level;
        this.hitResult = hitResult;
        this.ammo = ammo;
    }

    public final Level getLevel() {
        return level;
    }

    public final HitResult getHitResult() {
        return hitResult;
    }

    public final EntityKineticBullet getAmmo() {
        return ammo;
    }

    @Cancelable
    public static class Pre extends AmmoHitAnythingEvent {
        public Pre(Level level, HitResult hitResult, EntityKineticBullet ammo) {
            super(level, hitResult, ammo);
        }
    }

    public static class Post extends AmmoHitAnythingEvent {
        public Post(Level level, HitResult hitResult, EntityKineticBullet ammo) {
            super(level, hitResult, ammo);
        }
    }
}
