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
public class IsGunInstalled implements LootItemCondition {
    private final ResourceLocation gunId;

    public static final MapCodec<IsGunInstalled> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            ResourceLocation.CODEC.fieldOf("gun_id").forGetter(cond -> cond.gunId)
    ).apply(builder, IsGunInstalled::new));

    public IsGunInstalled(ResourceLocation gunId) {
        this.gunId = gunId;
    }

    public static IsGunInstalled.Builder isGunInstalled(ResourceLocation gunId) {
        return new Builder().gunId(gunId);
    }

    /**
     * Builder
     */
    public static class Builder implements LootItemCondition.Builder {
        private ResourceLocation gunId;

        public Builder gunId(ResourceLocation gunId) {
            this.gunId = gunId;
            return this;
        }

        @Nonnull
        public IsGunInstalled build() {
            return new IsGunInstalled(this.gunId);
        }
    }

    public ResourceLocation getGunId() {
        return this.gunId;
    }

    @Override
    public boolean test(LootContext context) {
        return TimelessAPI.getCommonGunIndex(this.gunId).isPresent();
    }

    @Override
    public @Nonnull LootItemConditionType getType() {
        return GunLootFunctions.IS_GUN_INSTALLED.get();
    }

}
