package mod.chloeprime.gunsmithlib.api.common;

import com.mojang.serialization.MapCodec;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.common.loot.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

/**
 * @since 3.2.0
 */
public final class GunLootFunctions {
    public static final @ApiStatus.Internal DeferredRegister<LootItemFunctionType<?>> DFR_FUNC = DeferredRegister.create(BuiltInRegistries.LOOT_FUNCTION_TYPE, GunsmithLib.MOD_ID);
    public static final @ApiStatus.Internal DeferredRegister<LootItemConditionType> DFR_COND = DeferredRegister.create(BuiltInRegistries.LOOT_CONDITION_TYPE, GunsmithLib.MOD_ID);
    /**
     * 设置枪械 id 并初始化枪械 NBT（内部弹药量，开火模式，热量等）
     */
    public static final Supplier<LootItemFunctionType<InitGunInfo>> INIT_GUN_INFO = registerFunction("init_gun_info",InitGunInfo.CODEC);

    /**
     * 设置子弹 id
     */
    public static final Supplier<LootItemFunctionType<InitAmmoInfo>> INIT_AMMO_INFO = registerFunction("init_ammo_info", InitAmmoInfo.CODEC);

    /**
     * 设置配件 id
     */
    public static final Supplier<LootItemFunctionType<InitAttachmentInfo>> INIT_ATTACHMENT_INFO = registerFunction("init_attachment_info", InitAttachmentInfo.CODEC);

    /**
     * 检测某个枪械 id 是否存在（是否有对应的 index）
     */
    public static final Supplier<LootItemConditionType> IS_GUN_INSTALLED = registerCondition("is_gun_installed", IsGunInstalled.CODEC);

    /**
     * 检测某个子弹 id 是否存在（是否有对应的 index）
     */
    public static final Supplier<LootItemConditionType> IS_AMMO_INSTALLED = registerCondition("is_ammo_installed", IsAmmoInstalled.CODEC);

    /**
     * 检测某个配件 id 是否存在（是否有对应的 index）
     */
    public static final Supplier<LootItemConditionType> IS_ATTACHMENT_INSTALLED_IN_DATABASE = registerCondition("is_gun_installed_in_database", IsAttachmentInstalledInDatabase.CODEC);

    /**
     * 设置枪械 id，默认枪内没有子弹
     * @param gunId 枪械 id
     */
    public static LootItemConditionalFunction.Builder<?> initGunInfo(ResourceLocation gunId) {
        return InitGunInfo.initGunInfo(gunId);
    }

    /**
     * 设置枪械 id
     * @param gunId 枪械 id
     * @param ammo 枪内总子弹数量（包含弹匣和膛内）
     */
    public static LootItemConditionalFunction.Builder<?> initGunInfo(ResourceLocation gunId, NumberProvider ammo) {
        return InitGunInfo.initGunInfo(gunId, ammo);
    }

    /**
     * 设置弹药 id
     * @param ammoId 弹药 id
     */
    public static LootItemConditionalFunction.Builder<?> initAmmoInfo(ResourceLocation ammoId) {
        return InitAmmoInfo.initAmmoInfo(ammoId);
    }

    /**
     * 设置配件 id
     * @param attachmentId 配件 id
     */
    public static LootItemConditionalFunction.Builder<?> initAttachmentInfo(ResourceLocation attachmentId) {
        return InitAttachmentInfo.initAttachmentInfo(attachmentId);
    }

    /**
     * 检测某个枪械 id 是否存在（是否有对应的 index）
     * @param gunId 待检测的枪械 id
     * @return 该枪械 id 对应的枪械是否存在
     */
    public static LootItemCondition.Builder isGunInstalled(ResourceLocation gunId) {
        return IsGunInstalled.isGunInstalled(gunId);
    }

    /**
     * 检测某个弹药 id 是否存在（是否有对应的 index）
     * @param ammoId 待检测的弹药 id
     * @return 该弹药 id 对应的弹药是否存在
     */
    public static LootItemCondition.Builder isAmmoInstalled(ResourceLocation ammoId) {
        return IsAmmoInstalled.isAmmoInstalled(ammoId);
    }

    /**
     * 检测某个配件 id 是否存在（是否有对应的 index）
     * @param attachmentId 待检测的配件 id
     * @return 该配件 id 对应的配件是否存在
     */
    public static LootItemCondition.Builder isAttachmentInstalledInDatabase(ResourceLocation attachmentId) {
        return IsAttachmentInstalledInDatabase.isAttachmentInstalledInDatabase(attachmentId);
    }

    private GunLootFunctions() {
    }

    private static
    <T extends LootItemFunction>
    Supplier<LootItemFunctionType<T>> registerFunction(String name, MapCodec<T> serializer) {
        return DFR_FUNC.register(name, () -> new LootItemFunctionType<>(serializer));
    }


    private static
    <T extends LootItemCondition>
    Supplier<LootItemConditionType> registerCondition(String name, MapCodec<T> serializer) {
        return DFR_COND.register(name, () -> new LootItemConditionType(serializer));
    }

    @ApiStatus.Internal
    public static void init() {
    }
}
