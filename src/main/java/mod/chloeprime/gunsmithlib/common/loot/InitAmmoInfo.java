package mod.chloeprime.gunsmithlib.common.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tacz.guns.api.item.IAmmo;
import mod.chloeprime.gunsmithlib.api.common.GunLootFunctions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Objects;

@ParametersAreNonnullByDefault
public class InitAmmoInfo extends LootItemConditionalFunction {
    private final ResourceLocation ammoId;

    public static final MapCodec<InitAmmoInfo> CODEC = RecordCodecBuilder.mapCodec(builder -> commonFields(builder).and(
            ResourceLocation.CODEC.fieldOf("ammo_id").forGetter(func -> func.ammoId)
    ).apply(builder, InitAmmoInfo::new));

    public InitAmmoInfo(
            List<LootItemCondition> conditions,
            ResourceLocation ammoId
    ) {
        super(conditions);
        this.ammoId = ammoId;
    }

    public static Builder<?> initAmmoInfo(ResourceLocation ammoId) {
        return simpleBuilder((conditions) -> new InitAmmoInfo(conditions, ammoId));
    }

    public ResourceLocation getAmmoId() {
        return ammoId;
    }

    @Override
    public @Nonnull ItemStack run(ItemStack stack, LootContext context) {
        if (stack.getItem() instanceof IAmmo ammoItem) {
            ammoItem.setAmmoId(stack, this.ammoId);
        }
        return stack;
    }

    @Override
    public @Nonnull LootItemFunctionType<InitAmmoInfo> getType() {
        return Objects.requireNonNull(GunLootFunctions.INIT_AMMO_INFO);
    }
}
