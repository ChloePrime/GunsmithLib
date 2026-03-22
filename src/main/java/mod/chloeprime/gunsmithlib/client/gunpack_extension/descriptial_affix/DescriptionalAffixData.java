package mod.chloeprime.gunsmithlib.client.gunpack_extension.descriptial_affix;

import mod.chloeprime.gunsmithlib.client.gunpack_extension.GunsmithLibGunDisplayExtension;
import mod.chloeprime.gunsmithlib.common.util.GunpackProperty;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class DescriptionalAffixData {
    @GunpackProperty
    private @Nullable String[] before;

    @GunpackProperty
    private @Nullable String[] after;

    @GunpackProperty
    private @Nullable String[] replace;

    public Optional<List<String>> getBefore() {
        return Optional.ofNullable(before).map(List::of);
    }

    public Optional<List<String>> getAfter() {
        return Optional.ofNullable(after).map(List::of);
    }

    public Optional<List<String>> getReplace() {
        return Optional.ofNullable(replace).map(List::of);
    }

    public static Optional<DescriptionalAffixData> fromGun(ItemStack gunItem) {
        return GunsmithLibGunDisplayExtension
                .of(gunItem)
                .map(GunsmithLibGunDisplayExtension::getDescriptionalAffixData);
    }
}
