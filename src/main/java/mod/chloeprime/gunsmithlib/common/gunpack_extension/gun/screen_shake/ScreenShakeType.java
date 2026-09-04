package mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.screen_shake;

import java.util.Optional;
import java.util.function.Function;

/**
 * 主要用于 RPC
 *
 * @since 7.0.0
 */
public enum ScreenShakeType {
    SHOOT(ScreenShakeData::getOnShoot),
    HIT(ScreenShakeData::getOnHit),
    IMPACT(ScreenShakeData::getOnImpact),
    EXPLOSION(ScreenShakeData::getOnExplosion);

    @FunctionalInterface
    public interface Field extends Function<ScreenShakeData, Optional<ScreenShakeEntryData>> {
    }

    public Field getField() {
        return field;
    }

    private final Field field;

    ScreenShakeType(Field field) {
        this.field = field;
    }
}
