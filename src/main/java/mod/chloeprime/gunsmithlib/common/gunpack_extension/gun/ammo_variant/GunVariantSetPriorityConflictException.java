package mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.ammo_variant;

/**
 * @since 6.0.0
 */
@SuppressWarnings("unused")
public class GunVariantSetPriorityConflictException extends RuntimeException {
    public GunVariantSetPriorityConflictException() {
    }

    public GunVariantSetPriorityConflictException(String message) {
        super(message);
    }

    public GunVariantSetPriorityConflictException(String message, Throwable cause) {
        super(message, cause);
    }

    public GunVariantSetPriorityConflictException(Throwable cause) {
        super(cause);
    }

    public GunVariantSetPriorityConflictException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
