package mod.chloeprime.gunsmithlib.common.util;

import mod.chloeprime.gunsmithlib.api.util.AmmoInfo;
import mod.chloeprime.gunsmithlib.api.util.AttachmentInfo;
import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.common.scripting.attachment.AttachmentScripting;

import javax.annotation.Nonnull;
import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public final class GunPartIterator {
    public static <T, D> Stream<T> iterate(
            GunInfo gun,
            Function<GunInfo, Optional<D>> gunMapper,
            Function<AmmoInfo, Optional<D>> ammoMapper,
            Function<AttachmentInfo, Optional<D>> attachMapper,
            Function<D, T> dataMapper
    ) {
        var optDataMapper = (Function<Optional<D>, Optional<T>>) d -> d.map(dataMapper);
        return iterate(
                gun,
                gunMapper.andThen(optDataMapper),
                ammoMapper.andThen(optDataMapper),
                attachMapper.andThen(optDataMapper));
    }

    public static <T> Stream<T> iterate(
            GunInfo gun,
            Function<GunInfo, Optional<T>> gunMapper,
            Function<AmmoInfo, Optional<T>> ammoMapper,
            Function<AttachmentInfo, Optional<T>> attachMapper
    ) {
        final int ID_GUN  = 0;
        final int ID_AMMO = 1;
        final var attaches = AttachmentScripting.SCRIPT_ORDER;
        final int total = 2 + attaches.size();

        var iterable = new AbstractCollection<Optional<T>>() {
            @Override
            public @Nonnull Iterator<Optional<T>> iterator() {
                return new Iterator<>() {
                    private int i = 0;

                    @Override
                    public boolean hasNext() {
                        return i < total;
                    }

                    @Override
                    public Optional<T> next() {
                        var id = i++;
                        return switch (id) {
                            case ID_GUN -> gunMapper.apply(gun);
                            case ID_AMMO -> Gunsmith
                                    .getAmmoInfo(Gunsmith.createAmmoItemFromId(gun.index().getGunData().getAmmoId(), gun.getTotalAmmo()))
                                    .flatMap(ammoMapper);
                            default -> {
                                var attachTypeI = id - 2;
                                if (attachTypeI >= attaches.size()) {
                                    yield Optional.<T>empty();
                                }
                                yield Gunsmith
                                        .getAttachmentInfo(gun.gunItem().getAttachment(gun.gunStack(), attaches.get(attachTypeI)))
                                        .flatMap(attachMapper);
                            }
                        };
                    }
                };
            }

            @Override
            public int size() {
                return total;
            }
        };

        return iterable.stream().flatMap(Optional::stream);
    }

    private GunPartIterator() {
    }
}
