package mod.chloeprime.gunsmithlib.client.animation;

import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ReloadState;
import mod.chloeprime.gunsmithlib.api.common.GunAttributes;
import net.minecraft.client.Minecraft;

public class AnimationSpeedScaler {
    public static double getAnimationSpeedScale() {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return 1;
        }
        var isReloading = IGunOperator.fromLivingEntity(player).getSynReloadState().getStateType() != ReloadState.StateType.NOT_RELOADING;
        return isReloading ? player.getAttributeValue(GunAttributes.RELOAD_SPEED) : 1;
    }

    public static abstract sealed class TimeTracker {
        public static TimeTracker createMillisTracker() {
            return new Millis();
        }

        public static TimeTracker createNanosTracker() {
            return new Nanos();
        }

        private long lastUnscaled;
        private long lastScaled;

        public TimeTracker() {
            lastUnscaled = lastScaled = getUnscaledCurrentTime();
        }

        public long updateAndGet(long original, double scale) {
            var deltaUnscaled = original - lastUnscaled;
            lastUnscaled += deltaUnscaled;
            lastScaled += (long) (deltaUnscaled * scale);
            return lastScaled;
        }

        protected abstract long getUnscaledCurrentTime();

        public static final class Millis extends TimeTracker {
            @Override
            public long getUnscaledCurrentTime() {
                return System.currentTimeMillis();
            }
        }

        public static final class Nanos extends TimeTracker {
            @Override
            public long getUnscaledCurrentTime() {
                return System.nanoTime();
            }
        }
    }
}
