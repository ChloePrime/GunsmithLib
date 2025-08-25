package mod.chloeprime.gunsmithlib.common.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.gun.FireMode;
import mod.chloeprime.gunsmithlib.api.common.GunLootFunctions;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Objects;

@ParametersAreNonnullByDefault
public class InitGunInfo extends LootItemConditionalFunction {
    private final ResourceLocation gunId;
    private final NumberProvider ammo;

    public static final MapCodec<InitGunInfo> CODEC = RecordCodecBuilder.mapCodec(builder -> commonFields(builder).and(builder.group(
            ResourceLocation.CODEC.fieldOf("gun_id").forGetter(func -> func.gunId),
            NumberProviders.CODEC.fieldOf("ammo").forGetter(func -> func.ammo)
    )).apply(builder, InitGunInfo::new));

    public InitGunInfo(
            List<LootItemCondition> conditions,
            ResourceLocation gunId,
            NumberProvider ammoCount
    ) {
        super(conditions);
        this.gunId = gunId;
        this.ammo = ammoCount;
    }

    public static LootItemConditionalFunction.Builder<?> initGunInfo(ResourceLocation gunId) {
        return initGunInfo(gunId, ConstantValue.exactly(0));
    }

    public static LootItemConditionalFunction.Builder<?> initGunInfo(ResourceLocation gunId, NumberProvider ammo) {
        return simpleBuilder((conditions) -> new InitGunInfo(conditions, gunId, ammo));
    }

    public ResourceLocation getGunId() {
        return gunId;
    }

    public NumberProvider getInitialAmmoCount() {
        return ammo;
    }

    @Override
    public @Nonnull ItemStack run(ItemStack stack, LootContext context) {
        if (stack.getItem() instanceof IGun gunItem) {
            gunItem.setGunId(stack, this.gunId);
        }
        Gunsmith.getGunInfo(stack).ifPresent(gun -> {
            // 初始化开火模式
            gun.gunItem().setFireMode(gun.gunStack(), gun.index().getGunData().getFireModeSet()
                    .stream().findFirst()
                    .orElse(FireMode.UNKNOWN));
            // 填充弹药
            gun.setTotalAmmo(ammo.getInt(context));
            // 初始化热量
            if (gun.index().getGunData().hasHeatData()) {
                gun.gunItem().setHeatAmount(gun.gunStack(), 0);
            }
        });
        return stack;
    }

    @Override
    public @Nonnull LootItemFunctionType<InitGunInfo> getType() {
        return Objects.requireNonNull(GunLootFunctions.INIT_GUN_INFO);
    }
}
