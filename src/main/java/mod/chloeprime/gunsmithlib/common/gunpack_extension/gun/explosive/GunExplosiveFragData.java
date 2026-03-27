package mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.explosive;

import com.google.common.base.Suppliers;
import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.item.builder.GunItemBuilder;
import com.tacz.guns.api.item.gun.FireMode;
import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.common.util.GunpackProperty;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.function.Supplier;

public class GunExplosiveFragData {
    @GunpackProperty
    private ResourceLocation config_id = DefaultAssets.EMPTY_GUN_ID;

    @GunpackProperty
    private int count = 64;

    @GunpackProperty
    private double min_frag_velocity = 1;

    @GunpackProperty
    private double max_frag_velocity = 1;

    public final Optional<ItemStack> getConfigSource() {
        return Optional.ofNullable(item.get()).filter(item -> !item.isEmpty());
    }

    public final int getCount() {
        return count;
    }

    public final double getMinFragVelocity() {
        return min_frag_velocity;
    }

    public final double getMaxFragVelocity() {
        return max_frag_velocity;
    }

    public final double sampleFragVelocity(RandomSource rng) {
        return Mth.lerp(rng.nextDouble(), getMinFragVelocity(), getMaxFragVelocity());
    }

    private final Supplier<ItemStack> item = Suppliers.memoize(() -> GunItemBuilder.create()
            .setId(config_id)
            .setAmmoCount(Integer.MAX_VALUE)
            .setFireMode(FireMode.SEMI)
            .build());

    public static Optional<GunExplosiveFragData> of(ItemStack gun) {
        return Gunsmith.getGunInfo(gun).flatMap(GunExplosiveFragData::of);
    }

    public static Optional<GunExplosiveFragData> of(GunInfo gunInfo) {
        return GunExplosiveData.fromGun(gunInfo).map(GunExplosiveData::getFragData);
    }
}