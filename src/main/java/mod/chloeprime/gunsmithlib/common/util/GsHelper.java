package mod.chloeprime.gunsmithlib.common.util;

import cn.chloeprime.commons.lang4.FloatSupplier;
import cn.chloeprime.commons.math.LinearAlgebraTypes;
import cn.chloeprime.commons.rpc.RPC;
import cn.chloeprime.commons.rpc.RPCFlow;
import cn.chloeprime.commons.rpc.RPCTarget;
import cn.chloeprime.commons.rpc.RemoteCallable;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.tacz.guns.api.GunProperties;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.resource.CommonAssetsManager;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.common.compat.CapabilityBasedModCompat;
import mod.chloeprime.gunsmithlib.common.internal.EnhancedKineticBullet;
import mod.chloeprime.gunsmithlib.mixin.ItemCooldownsAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.items.IItemHandler;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.joml.Matrix3f;
import org.joml.Vector3f;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

import static mod.chloeprime.gunsmithlib.common.compat.CapabilityBasedModCompat.MAX_DISPLAYED_AMMO_SCANNED;

public class GsHelper {
    public static float infDist(FloatSupplier nextGaussianFunc, float mean, float dev) {
        return infLerp(nextGaussianFunc.getAsFloat(), mean - dev, mean + dev);
    }

    public static double infDist(DoubleSupplier nextGaussianFunc, double mean, double dev) {
        return infLerp(nextGaussianFunc.getAsDouble(), mean - dev, mean + dev);
    }

    public static float infLerp(float delta, float start, float end) {
        var normalizedDelta = (float) Math.atan(delta) / Mth.PI + 0.5F;
        return Mth.lerp(normalizedDelta, start, end);
    }

    public static double infLerp(double delta, double start, double end) {
        var normalizedDelta = Math.atan(delta) / Math.PI + 0.5;
        return Mth.lerp(normalizedDelta, start, end);
    }

    /**
     * @return 增益倍率，永远不会为 0
     * @since 3.2.0
     */
    public static double getBuffCoefficient(ResourceLocation gunId, boolean isMelee) {
        if (!isMelee) {
            return 1;
        }
        return TimelessAPI.getCommonGunIndex(gunId)
                .map(index -> index.getGunData().getBulletData().getBulletAmount())
                .map(shrapnel -> shrapnel == 0 ? 1 : 1.0 / Math.abs(shrapnel))
                .orElse(1.0);
    }

    public static Optional<GunInfo> unpack(IGun gunItem, ItemStack gunStack) {
        var gunId = gunItem.getGunId(gunStack);
        return TimelessAPI.getCommonGunIndex(gunId).map(index -> new GunInfo(gunStack, gunItem, gunId, index));
    }

    public static double getAttributeValueWithBase(LivingEntity holder, Attribute attribute, double base) {
        var instance = holder.getAttribute(attribute);
        if (instance == null) {
            return base;
        }

        var oldBase = instance.getBaseValue();
        try {
            instance.setBaseValue(base);
            return instance.getValue();
        } finally {
            instance.setBaseValue(oldBase);
        }
    }

    public static int[] getModVersion() {
        if (version != null) {
            return version;
        }
        version = ModList.get().getModContainerById(GunsmithLib.MOD_ID)
                .map(container -> extractModVersion(container.getModInfo().getVersion()))
                .map(version -> new int[]{version.getMajorVersion(), version.getMinorVersion(), version.getIncrementalVersion()})
                .orElse(new int[]{0, 0, 0});
        return version;
    }

    public static int[] getGameVersion() {
        if (mcVersion != null) {
            return mcVersion;
        }

        mcVersion = Optional.ofNullable(FMLLoader.versionInfo().mcVersion())
                .map(DefaultArtifactVersion::new)
                .map(version -> new int[]{version.getMajorVersion(), version.getMinorVersion(), version.getIncrementalVersion()})
                .orElse(new int[]{-1, -1, -1});
        return mcVersion;
    }


    private static ArtifactVersion extractModVersion(ArtifactVersion fullVersion) {
        if (fullVersion.getMajorVersion() > 0) {
            return fullVersion;
        }
        return Arrays.stream(fullVersion.getQualifier().split("\\+")).findFirst()
                .<ArtifactVersion>map(DefaultArtifactVersion::new)
                .orElse(fullVersion);
    }

    public static Map<String, Object> parseStaticFields(Class<?> clazz) {
        Map<String, Object> constantMap = new LinkedHashMap<>();
        // 获取 GunAnimationConstant 的所有 public 字段
        Field[] fields = clazz.getFields();
        // 将 static final 的常量字段提取到 constantMap
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers())) {
                try {
                    // 获取变量名和值
                    String name = field.getName();
                    Object value = field.get(null);
                    constantMap.put(name, value);
                } catch (IllegalAccessException ex) {
                    GunsmithLib.LOGGER.warn("Failed to parse static fields for class {}", clazz.getCanonicalName(), ex);
                }
            }
        }
        return constantMap;
    }

    private static int[] version = null;
    private static int[] mcVersion = null;

    private GsHelper() {
    }

    public static double getEstimatedMaxRange(@Nullable Entity entity, @Nonnull ItemStack gunStack) {
        IGun gunItem = IGun.getIGunOrNull(gunStack);
        if (gunItem == null) {
            return 0;
        }
        return getEstimatedMaxRange(entity, gunStack, gunItem);
    }

    @SuppressWarnings("UnstableApiUsage")
    public static double getEstimatedMaxRange(@Nullable Entity entity, @Nonnull ItemStack gunStack, @Nonnull IGun gunItem) {
        if (!(entity instanceof LivingEntity shooter)) {
            return 0;
        }
        var gi = unpack(gunItem, gunStack).orElse(null);
        if (gi == null) {
            return 0;
        }
        return Optional.ofNullable(IGunOperator.fromLivingEntity(shooter).getCacheProperty())
                .map(cache -> {
                    float speed = cache.getCache(GunProperties.AMMO_SPEED);
                    float life = gi.index().getBulletData().getLifeSecond();
                    return speed * life;
                })
                .orElse(0F);
    }

    public static float getCooldownDuration(ItemCooldowns cooldowns, Item item) {
        var instance = ((ItemCooldownsAccessor) cooldowns).getCooldowns().get(item);
        if (instance == null) {
            return 0;
        }
        return instance.endTime - instance.startTime;
    }

    public record AttributeEvaluatorBuffer(
            List<AttributeModifier> addition,
            List<AttributeModifier> mulBase,
            List<AttributeModifier> mulTotal
    ) {
        public AttributeEvaluatorBuffer() {
            this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        }
    }

    private static final ThreadLocal<AttributeEvaluatorBuffer> BUFFER_BY_THREAD = ThreadLocal.withInitial(AttributeEvaluatorBuffer::new);

    public static double evaluateItemAttribute(
            ItemStack item, Supplier<Attribute> attributeHolder, double baseValue
    ) {
        var attribute = attributeHolder.get();
        var modifiers = item.getAttributeModifiers(EquipmentSlot.MAINHAND).get(attribute);
        var buffer = BUFFER_BY_THREAD.get();

        try {
            for (var modifier : modifiers) {
                if (modifier == null) {
                    continue;
                }
                switch (modifier.getOperation()) {
                    case ADDITION -> buffer.addition().add(modifier);
                    case MULTIPLY_BASE -> buffer.mulBase().add(modifier);
                    case MULTIPLY_TOTAL -> buffer.mulTotal().add(modifier);
                }
            }

            double afterAddition = baseValue;
            for(var modifier : buffer.addition()) {
                afterAddition += modifier.getAmount();
            }

            double finalValue = afterAddition;
            for(var modifier : buffer.mulBase()) {
                finalValue += afterAddition * modifier.getAmount();
            }

            for(var modifier : buffer.mulTotal()) {
                finalValue *= 1.0D + modifier.getAmount();
            }

            return attribute.sanitizeValue(finalValue);
        } finally {
            buffer.addition().clear();
            buffer.mulBase().clear();
            buffer.mulTotal().clear();
        }
    }

    public static <E extends Enum<E>> Codec<E> enumCodec(Class<E> type) {
        return Codec.STRING.xmap(
                str -> Enum.valueOf(type, str.toUpperCase(Locale.ROOT)),
                value -> value.name().toLowerCase(Locale.ROOT));
    }

    public static <T> Codec<List<T>> selfOrList(Codec<T> elementCodec) {
        return Codec
                .either(elementCodec, Codec.list(elementCodec))
                .xmap(either -> {
                    if (either.right().isPresent()) {
                        return either.right().get();
                    }
                    if (either.left().isPresent()) {
                        return List.of(either.left().get());
                    }
                    throw new IllegalStateException();
                }, list -> {
                    if (list.size() == 1) {
                        return Either.left(list.get(0));
                    } else {
                        return Either.right(list);
                    }
                });
    }

    public static void syncBulletExplodePos(Projectile bullet, Vec3 pos) {
        if (bullet.level().isClientSide) {
            syncBulletExplodePos0(bullet, pos);
        } else {
            RPC.call(RPCTarget.near(bullet), GsHelper::syncBulletExplodePos0, bullet, pos);
        }
    }

    @RemoteCallable(flow = RPCFlow.SERVER_TO_CLIENT, callLocally = true)
    private static void syncBulletExplodePos0(Projectile bullet, Vec3 pos) {
        if (bullet != null) {
            if (bullet instanceof EnhancedKineticBullet accessor) {
                accessor.gunsmith$onMovedToPos(pos);
            }
            bullet.setPos(pos);
            bullet.setDeltaMovement(Vec3.ZERO);
        }
    }

    /**
     * @return 无限弹药时返回 {@link OptionalInt#empty()}
     */
    public static OptionalInt scanAmmo(Player user, GunInfo gun) {
        var inv = scanInventoryAmmo(user, gun);
        if (inv.isEmpty()) {
            return OptionalInt.empty();
        }
        var backpack = scanBackpackAmmo(user, gun);
        if (backpack.isEmpty()) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(inv.getAsInt() + backpack.getAsInt());
    }

    /**
     * 不包含精妙背包内的子弹
     *
     * @return 无限弹药时返回 {@link OptionalInt#empty()}
     */
    public static OptionalInt scanInventoryAmmo(Player user, GunInfo gun) {
        Objects.requireNonNull(user);
        Objects.requireNonNull(gun);

        if (!IGunOperator.fromLivingEntity(user).needCheckAmmo()) {
            return OptionalInt.empty();
        }
        if (gun.gunItem().useDummyAmmo(gun.gunStack())) {
            return OptionalInt.of(gun.gunItem().getDummyAmmoAmount(gun.gunStack()));
        }
        var inventory = user.getCapability(ForgeCapabilities.ITEM_HANDLER, null).resolve().orElse(null);
        return getInventoryAmmo(gun.gunStack(), inventory);
    }

    /**
     * 精妙背包内的子弹
     *
     * @return 无限弹药时返回 {@link OptionalInt#empty()}
     */
    public static OptionalInt scanBackpackAmmo(Player user, GunInfo gun) {
        var count = CapabilityBasedModCompat.consumeAmmoFromPlayer(user, gun.gunStack(), MAX_DISPLAYED_AMMO_SCANNED, true);
        return count == MAX_DISPLAYED_AMMO_SCANNED ? OptionalInt.empty() : OptionalInt.of(count);
    }

    private static OptionalInt getInventoryAmmo(ItemStack stack, @Nullable IItemHandler inventory) {
        if (inventory == null) {
            return OptionalInt.of(0);
        }
        int result = 0;
        int slots = inventory.getSlots();
        for (int i = 0; i < slots; i++) {
            var inventoryItem = inventory.getStackInSlot(i);
            if (inventoryItem.getItem() instanceof IAmmo iAmmo && iAmmo.isAmmoOfGun(stack, inventoryItem)) {
                result += inventoryItem.getCount();
            }
            if (inventoryItem.getItem() instanceof IAmmoBox iAmmoBox && iAmmoBox.isAmmoBoxOfGun(stack, inventoryItem)) {
                if (iAmmoBox.isAllTypeCreative(inventoryItem) || iAmmoBox.isCreative(inventoryItem)) {
                    return OptionalInt.empty();
                }
                result += iAmmoBox.getAmmoCount(inventoryItem);
            }
        }
        return OptionalInt.of(result);
    }

    public static LuaFunction checkFunction(LuaValue luaValue) {
        if (luaValue.isfunction()) {
            return (LuaFunction) luaValue;
        } else if (luaValue.isnil()) {
            return null;
        } else {
            throw new LuaError("bad argument: function or nil expected, got " + luaValue.typename());
        }
    }

    /**
     * Deserialize a Lua table as if it is a JSON object,
     * Using TaCZ's GSON instance.
     */
    public static <T> T lua2obj(LuaValue value, Class<T> clazz) {
        return CommonAssetsManager.GSON.fromJson(LuaUtil.lua2json(value), clazz);
    }

    public static Matrix3f getModelMatrix(float yRot, Vec3 front, Matrix3f dst) {
        var buffer = COMPUTE_MODEL_MATRIX_BUFFER.get();
        var mat = buffer.matBuffer();
        var localX = buffer.xAxis().rotateY((float) -Math.toRadians(180 + yRot), buffer.x());
        mat.setColumn(0, localX);
        var localZ = buffer.z().set(LinearAlgebraTypes.moj2joml(front));
        mat.setColumn(2, localZ);
        var localY = localX.cross(localZ, buffer.y());
        mat.setColumn(1, localY);
        dst.set(mat);
        return dst;
    }

    public record ComputeModelMatrixBuffer(
            Vector3f xAxis,
            Vector3f x,
            Vector3f y,
            Vector3f z,
            Matrix3f matBuffer
    ) {
        public ComputeModelMatrixBuffer() {
            this(new Vector3f(1, 0, 0), new Vector3f(), new Vector3f(), new Vector3f(), new Matrix3f());
        }
    }

    /**
     * @since 6.2
     */
    public static <T> T modifyProperty(GunInfo gun, LivingEntity shooter, String id, Class<T> type, T original) {
        var operator = IGunOperator.fromLivingEntity(shooter);
        return gun.gunItem().modifyProperty(operator.getDataHolder(), gun.gunStack(), shooter, id, type, original);
    }

    private static final ThreadLocal<ComputeModelMatrixBuffer> COMPUTE_MODEL_MATRIX_BUFFER = ThreadLocal.withInitial(ComputeModelMatrixBuffer::new);
}
