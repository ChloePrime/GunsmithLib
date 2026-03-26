package mod.chloeprime.gunsmithlib.api.common;

import com.tacz.guns.entity.EntityKineticBullet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.eventbus.api.Cancelable;

public final class AmmoSelfExplodeEvent {
    @Cancelable
    public static class Pre extends AmmoHitAnythingEvent.Pre {
        public Pre(Level level, EntityKineticBullet ammo) {
            super(level, hitSelf(ammo), ammo);
        }
    }

    public static class Post extends AmmoHitAnythingEvent.Post {
        public Post(Level level, EntityKineticBullet ammo) {
            super(level, hitSelf(ammo), ammo);
        }
    }

    private static EntityHitResult hitSelf(Entity entity) {
        return new EntityHitResult(entity, entity.position());
    }

    private AmmoSelfExplodeEvent() {
    }
}
