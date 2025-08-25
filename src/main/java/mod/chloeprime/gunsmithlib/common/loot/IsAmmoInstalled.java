package mod.chloeprime.gunsmithlib.common.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tacz.guns.api.TimelessAPI;
import mod.chloeprime.gunsmithlib.api.common.GunLootFunctions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class IsAmmoInstalled implements LootItemCondition {
    private final ResourceLocation ammoId;

    public static final MapCodec<IsAmmoInstalled> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            ResourceLocation.CODEC.fieldOf("ammo_id").forGetter(cond -> cond.ammoId)
    ).apply(builder, IsAmmoInstalled::new));

    public IsAmmoInstalled(ResourceLocation ammoId) {
        this.ammoId = ammoId;
    }

    public static IsAmmoInstalled.Builder isAmmoInstalled(ResourceLocation ammoId) {
        return new Builder().ammoId(ammoId);
    }

    public static class Builder implements LootItemCondition.Builder {
        private ResourceLocation ammoId;

        public Builder ammoId(ResourceLocation ammoId) {
            this.ammoId = ammoId;
            return this;
        }

        @Nonnull
        public IsAmmoInstalled build() {
            return new IsAmmoInstalled(this.ammoId);
        }
    }

    public ResourceLocation getAmmoId() {
        return this.ammoId;
    }

    @Override
    public boolean test(LootContext context) {
        return TimelessAPI.getCommonAmmoIndex(this.ammoId).isPresent();
    }

    @Override
    public @Nonnull LootItemConditionType getType() {
        return GunLootFunctions.IS_AMMO_INSTALLED.get();
    }
}
